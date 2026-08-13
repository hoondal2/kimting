package com.kimting.kimting.user.port.in;

import com.kimting.kimting.user.presentation.dto.AuthResponse;
import com.kimting.kimting.user.presentation.dto.LoginRequest;
import com.kimting.kimting.user.presentation.dto.RegisterRequest;

public interface AuthUseCase {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);
}
