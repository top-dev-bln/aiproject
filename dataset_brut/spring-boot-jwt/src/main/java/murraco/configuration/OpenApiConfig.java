package murraco.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI api() {
    return new OpenAPI()
        .info(new Info()
            .title("JSON Web Token Authentication API")
            .description(
                "Sample JWT authentication service. Demo users: `admin` / `admin123456` and `client` / `client123456`. "
                    + "After sign-in, use **Authorize** and enter `Bearer <token>`.")
            .version("1.0.0")
            .license(new License().name("MIT License").url("http://opensource.org/licenses/MIT"))
            .contact(new Contact().email("mauriurraco@gmail.com")))
        .components(new Components()
            .addSecuritySchemes("bearerAuth",
                new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")));
  }

}
