package com.smallshop.user.api;

import com.smallshop.user.application.AccessToken;
import com.smallshop.user.application.InvalidCredentialsException;
import com.smallshop.user.application.LogIn;
import com.smallshop.user.application.RegisterCustomer;
import com.smallshop.user.domain.DuplicateEmailException;
import com.smallshop.user.domain.User;
import com.smallshop.user.infrastructure.security.SecurityConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(SecurityConfiguration.class)
@MockitoBean(types = JwtDecoder.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RegisterCustomer registerCustomer;

    @MockitoBean
    private LogIn logIn;

    @Test
    void registeringCreatesACustomerWithoutReturningThePassword() throws Exception {
        when(registerCustomer.register("alice@example.com", "secret-password"))
                .thenReturn(User.registerCustomer("alice@example.com", "hashed"));

        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"alice@example.com\", \"password\": \"secret-password\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("alice@example.com"))
                .andExpect(jsonPath("$.role").value("CUSTOMER"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void registeringATakenEmailIsAConflict() throws Exception {
        when(registerCustomer.register("alice@example.com", "secret-password"))
                .thenThrow(new DuplicateEmailException("alice@example.com"));

        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"alice@example.com\", \"password\": \"secret-password\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void invalidEmailAndShortPasswordAreRejected() throws Exception {
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"not-an-email\", \"password\": \"short\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.password").exists());
    }

    @Test
    void loginReturnsABearerToken() throws Exception {
        Instant expiresAt = Instant.parse("2030-01-01T00:00:00Z");
        when(logIn.logIn("alice@example.com", "secret-password")).thenReturn(new AccessToken("signed-jwt", expiresAt));

        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"alice@example.com\", \"password\": \"secret-password\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("signed-jwt"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    void wrongCredentialsAreUnauthorized() throws Exception {
        when(logIn.logIn("alice@example.com", "wrong-password")).thenThrow(new InvalidCredentialsException());

        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"alice@example.com\", \"password\": \"wrong-password\"}"))
                .andExpect(status().isUnauthorized());
    }
}
