package com.stockbase.controller;

import com.stockbase.exception.BadRequestException;
import com.stockbase.exception.DuplicateResourceException;
import com.stockbase.model.User;
import com.stockbase.repository.UserRepository;
import com.stockbase.security.JwtUtil;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authManager;
    private final UserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public Map<String, Object> login(@Valid @RequestBody LoginRequest req) {
        try {
            authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.email, req.password));
        } catch (BadCredentialsException e) {
            throw new BadRequestException("Invalid email or password");
        }
        User user = userRepository.findByEmail(req.email).orElseThrow();
        UserDetails ud = userDetailsService.loadUserByUsername(req.email);
        String token = jwtUtil.generateToken(ud);
        return buildResponse(user, token);
    }

    @PostMapping("/register")
    public Map<String, Object> register(@Valid @RequestBody RegisterRequest req) {
        if (userRepository.existsByEmail(req.email)) {
            throw new DuplicateResourceException("Email already registered: " + req.email);
        }
        User user = User.builder()
                .fullName(req.fullName)
                .email(req.email)
                .password(passwordEncoder.encode(req.password))
                .role(User.Role.USER)
                .build();
        userRepository.save(user);
        UserDetails ud = userDetailsService.loadUserByUsername(req.email);
        String token = jwtUtil.generateToken(ud);
        return buildResponse(user, token);
    }

    @GetMapping("/me")
    public Map<String, Object> me(@AuthenticationPrincipal UserDetails ud) {
        User user = userRepository.findByEmail(ud.getUsername()).orElseThrow();
        return Map.of("id", user.getId(), "fullName", user.getFullName(),
                "email", user.getEmail(), "role", user.getRole());
    }

    private Map<String, Object> buildResponse(User user, String token) {
        return Map.of(
                "token", token,
                "id", user.getId(),
                "fullName", user.getFullName(),
                "email", user.getEmail(),
                "role", user.getRole()
        );
    }

    @Data static class LoginRequest {
        @NotBlank @Email String email;
        @NotBlank String password;
    }

    @Data static class RegisterRequest {
        @NotBlank String fullName;
        @NotBlank @Email String email;
        @NotBlank @Size(min = 6) String password;
    }
}
