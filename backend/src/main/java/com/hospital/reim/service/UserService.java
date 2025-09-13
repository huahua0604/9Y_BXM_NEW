package com.hospital.reim.service;

import com.hospital.reim.config.JwtService;
import com.hospital.reim.dto.LoginRequest;
import com.hospital.reim.dto.LoginResponse;
import com.hospital.reim.entity.Role;
import com.hospital.reim.entity.User;
import com.hospital.reim.exception.BadRequestException;
import com.hospital.reim.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepo;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService; // 来自 SecurityConfig

    public LoginResponse login(LoginRequest req) {
        try {
            var auth = new UsernamePasswordAuthenticationToken(req.getEmployeeId(), req.getPassword());
            authenticationManager.authenticate(auth);
        } catch (BadCredentialsException e) {
            throw new BadRequestException("工号或密码错误");
        }

        User u = userRepo.findByEmployeeId(req.getEmployeeId()).orElseThrow();
        UserDetails ud = userDetailsService.loadUserByUsername(u.getEmployeeId());
        String token = jwtService.generateToken(ud);
        return new LoginResponse(token, u.getName(), u.getRoles(), u.isMustChangePassword());
    }

    public void changePassword(String employeeId, String oldPassword, String newPassword) {
        User u = userRepo.findByEmployeeId(employeeId).orElseThrow();
        if (!encoder.matches(oldPassword, u.getPasswordHash())) {
            throw new BadRequestException("原密码不正确");
        }
        u.setPasswordHash(encoder.encode(newPassword));
        u.setMustChangePassword(false);
        userRepo.save(u);
    }

    public boolean hasReviewer(String employeeId) {
        User u = userRepo.findByEmployeeId(employeeId).orElseThrow();
        return u.getRoles().contains(Role.REVIEWER) || u.getRoles().contains(Role.ADMIN);
    }
}
