package com.hospital.reim.service;

import com.hospital.reim.dto.CreateUserRequest;
import com.hospital.reim.dto.LoginRequest;
import com.hospital.reim.dto.LoginResponse;
import com.hospital.reim.entity.Role;
import com.hospital.reim.entity.User;
import com.hospital.reim.exception.BadRequestException;
import com.hospital.reim.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import com.hospital.reim.config.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepo;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService; // 来自 SecurityConfig

    // ===== 登录 =====
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

    // ===== 修改密码（保持你现在的规则：仅长度 8–64）=====
    public void changePassword(String employeeId, String oldPassword, String newPassword) {
        User u = userRepo.findByEmployeeId(employeeId).orElseThrow();

        if (!encoder.matches(oldPassword, u.getPasswordHash())) {
            throw new BadRequestException("原密码不正确");
        }
        if (newPassword == null || newPassword.length() < 8 || newPassword.length() > 64) {
            throw new BadRequestException("新密码长度需在 8-64 位之间");
        }

        u.setPasswordHash(encoder.encode(newPassword));
        u.setMustChangePassword(false);
        userRepo.save(u);
    }

    // ===== 角色判断 =====
    public boolean hasReviewer(String employeeId) {
        User u = userRepo.findByEmployeeId(employeeId).orElseThrow();
        return u.getRoles().contains(Role.REVIEWER) || u.getRoles().contains(Role.ADMIN);
    }

    // =====================================================================
    // ================  管理员：新增用户 / 重置密码  ==========================
    // =====================================================================

    private static final String DEFAULT_PASSWORD = "ChangeMe123!";

    /** 管理员创建用户（配合 AdminController 使用） */
    public CreatedUserResult createUser(CreateUserRequest req) {
        // 1) 工号唯一
        userRepo.findByEmployeeId(req.getEmployeeId()).ifPresent(u -> {
            throw new BadRequestException("工号已存在");
        });

        // 2) 密码：未传则使用默认密码；已传则仅做长度校验（与你当前规则一致）
        String rawPwd = (req.getPassword() == null || req.getPassword().isBlank())
                ? (req.getEmployeeId()+req.getEmployeeId())
                : req.getPassword();

        if (rawPwd.length() < 8 || rawPwd.length() > 64) {
            throw new BadRequestException("密码长度需在 8-64 位之间");
        }

        Set<Role> roles = (req.getRoles() == null || req.getRoles().isEmpty())
                ? Set.of(Role.USER)
                : req.getRoles().stream().map(String::toUpperCase).map(roleName -> {
                    try { return Role.valueOf(roleName); }
                    catch (IllegalArgumentException e) {
                        throw new BadRequestException("非法角色：" + roleName);
                    }
                }).collect(Collectors.toSet());

        // 4) mustChangePassword：若使用默认密码则强制 true；否则按传入值（默认 true）
        boolean usingDefault = (req.getPassword() == null || req.getPassword().isBlank());
        boolean mustChange = usingDefault ? true : (req.getMustChangePassword() == null ? true : req.getMustChangePassword());

        // 5) 保存
        User u = new User();
        u.setEmployeeId(req.getEmployeeId());
        u.setName(req.getName());
        u.setDepartment(req.getDepartment());
        u.setRoles(roles);
        u.setMustChangePassword(mustChange);
        u.setPasswordHash(encoder.encode(rawPwd));
        userRepo.save(u);

        return new CreatedUserResult(u.getId(), usingDefault ? (req.getEmployeeId()+req.getEmployeeId()) : null, usingDefault);
    }
    
    public String resetPasswordToDefault(Long userId) {
    User u = userRepo.findById(userId).orElseThrow(() -> new BadRequestException("用户不存在"));

    String emp = u.getEmployeeId();
    if (emp == null || emp.isBlank()) {
        throw new BadRequestException("该用户工号为空，无法生成默认密码");
    }

    String raw = emp + emp; // 默认密码 = 两遍工号

    // 保持你当前的长度规则
    if (raw.length() < 8 || raw.length() > 64) {
        throw new BadRequestException("默认密码长度不满足 8-64 位，请手动指定密码");
    }

    u.setPasswordHash(encoder.encode(raw));
    u.setMustChangePassword(true);
    userRepo.save(u);
    return raw; // 返回给管理员显示一次
    }
    public record CreatedUserResult(Long id, String plainPassword, boolean usingDefault) {}
}
