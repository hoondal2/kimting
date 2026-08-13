package com.kimting.kimting.user.presentation;

import com.kimting.kimting.user.port.in.AuthUseCase;
import com.kimting.kimting.user.presentation.dto.AuthResponse;
import com.kimting.kimting.user.presentation.dto.LoginRequest;
import com.kimting.kimting.user.presentation.dto.RegisterRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthUseCase authUseCase;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest req) {
        return ResponseEntity.ok(authUseCase.register(req));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest req) {
        return ResponseEntity.ok(authUseCase.login(req));
    }
}
