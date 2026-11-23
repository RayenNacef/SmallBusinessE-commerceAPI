package com.smallshop.catalog.api;

import com.smallshop.catalog.application.CreateCategory;
import com.smallshop.catalog.application.RenameCategory;
import com.smallshop.catalog.domain.Category;
import com.smallshop.catalog.domain.DuplicateCategoryNameException;
import com.smallshop.user.infrastructure.security.SecurityConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static com.smallshop.TestTokens.asAdmin;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminCategoryController.class)
@Import(SecurityConfiguration.class)
@MockitoBean(types = {JwtDecoder.class, RenameCategory.class})
class AdminCategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CreateCategory createCategory;

    @Test
    void adminCreatesACategory() throws Exception {
        when(createCategory.create("Coffee")).thenReturn(Category.create("Coffee"));

        mockMvc.perform(post("/api/admin/categories").with(asAdmin())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"name\": \"Coffee\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Coffee"));
    }

    @Test
    void duplicateCategoryNameIsAConflict() throws Exception {
        when(createCategory.create("Coffee")).thenThrow(new DuplicateCategoryNameException("Coffee"));

        mockMvc.perform(post("/api/admin/categories").with(asAdmin())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"name\": \"Coffee\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("A category named 'Coffee' already exists"));
    }

    @Test
    void blankCategoryNameIsRejected() throws Exception {
        mockMvc.perform(post("/api/admin/categories").with(asAdmin())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"name\": \"  \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists());
    }
}
