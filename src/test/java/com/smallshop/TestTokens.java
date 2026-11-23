package com.smallshop;

import com.smallshop.shared.domain.UserId;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

public final class TestTokens {

    private TestTokens() {
    }

    public static JwtRequestPostProcessor asAdmin() {
        return jwt().jwt(token -> token.subject(UserId.newId().toString()))
                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    public static JwtRequestPostProcessor asCustomer(UserId customerId) {
        return jwt().jwt(token -> token.subject(customerId.toString()))
                .authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER"));
    }

    public static JwtRequestPostProcessor asCustomer() {
        return asCustomer(UserId.newId());
    }
}
