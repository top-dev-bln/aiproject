package murraco.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

  private static final String ADMIN_USER = "admin";
  private static final String ADMIN_PASSWORD = "admin123456";

  @Autowired
  private MockMvc mockMvc;

  @Test
  void signin_withValidCredentials_returnsToken() throws Exception {
    mockMvc.perform(post("/users/signin")
            .param("username", ADMIN_USER)
            .param("password", ADMIN_PASSWORD))
        .andExpect(status().isOk())
        .andExpect(result -> {
          String body = result.getResponse().getContentAsString();
          assert body != null && !body.isBlank() : "Expected non-empty JWT token";
        });
  }

  @Test
  void me_withoutToken_returns403() throws Exception {
    mockMvc.perform(get("/users/me"))
        .andExpect(status().isForbidden());
  }

  @Test
  void me_withValidToken_returnsUserData() throws Exception {
    String token = mockMvc.perform(post("/users/signin")
            .param("username", ADMIN_USER)
            .param("password", ADMIN_PASSWORD))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();

    mockMvc.perform(get("/users/me")
            .header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.username").value(ADMIN_USER))
        .andExpect(jsonPath("$.email").value("admin@email.com"))
        .andExpect(jsonPath("$.appUserRoles").isArray());
  }

  @Test
  void refresh_withValidToken_returnsNewJwt() throws Exception {
    String token = mockMvc.perform(post("/users/signin")
            .param("username", ADMIN_USER)
            .param("password", ADMIN_PASSWORD))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();

    mockMvc.perform(get("/users/refresh")
            .header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(result -> {
          String body = result.getResponse().getContentAsString();
          assert body != null && body.length() > 20 : "Expected non-empty JWT";
        });
  }

  @Test
  void signup_duplicateUsername_returns422() throws Exception {
    String body = """
        {"username":"admin","email":"other@example.com","password":"password12","appUserRoles":["ROLE_CLIENT"]}
        """;
    mockMvc.perform(post("/users/signup")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
        .andExpect(status().isUnprocessableEntity());
  }

  @Test
  void me_withMalformedToken_returns401() throws Exception {
    mockMvc.perform(get("/users/me")
            .header("Authorization", "Bearer not-a-valid-jwt"))
        .andExpect(status().isUnauthorized());
  }
}
