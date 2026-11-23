package com.smallshop.user.infrastructure;

import com.smallshop.user.application.CreateInitialAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
class InitialAdminCreator implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(InitialAdminCreator.class);

    private final CreateInitialAdmin createInitialAdmin;
    private final String email;
    private final String password;

    InitialAdminCreator(
            CreateInitialAdmin createInitialAdmin,
            @Value("${app.admin.email:}") String email,
            @Value("${app.admin.password:}") String password) {
        this.createInitialAdmin = createInitialAdmin;
        this.email = email;
        this.password = password;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (email.isBlank() || password.isBlank()) {
            return;
        }
        if (createInitialAdmin.createIfNoAdminExists(email, password)) {
            log.info("Created initial admin account {}", email);
        }
    }
}
