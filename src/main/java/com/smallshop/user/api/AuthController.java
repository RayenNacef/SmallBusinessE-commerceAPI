package com.smallshop.user.api;

import com.smallshop.user.application.AccessToken;
import com.smallshop.user.application.LogIn;
import com.smallshop.user.application.RegisterCustomer;
import com.smallshop.user.domain.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
class AuthController {

    private final RegisterCustomer registerCustomer;
    private final LogIn logIn;

    AuthController(RegisterCustomer registerCustomer, LogIn logIn) {
        this.registerCustomer = registerCustomer;
        this.logIn = logIn;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    UserResponse register(@Valid @RequestBody RegisterRequest request) {
        User user = registerCustomer.register(request.email(), request.password());
        return UserResponse.from(user);
    }

    @PostMapping("/login")
    TokenResponse logIn(@Valid @RequestBody LoginRequest request) {
        AccessToken token = logIn.logIn(request.email(), request.password());
        return TokenResponse.from(token);
    }
}
