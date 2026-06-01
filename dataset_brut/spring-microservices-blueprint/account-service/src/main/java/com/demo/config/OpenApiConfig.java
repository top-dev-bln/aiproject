package com.demo.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Vito Nguyen (<a href="https://github.com/cuongnh28">...</a>)
 */

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI accountServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Account Service API")
                        .description("User account management and authentication APIs.\n\n"
                                + "This service provides endpoints for user registration, login, role-based \n"
                                + "authorization, and profile retrieval. It also exposes test endpoints for \n"
                                + "verifying security and infrastructure behaviors.")
                        .version("v1.0.0")
                        .termsOfService("https://example.com/terms")
                        .contact(new Contact()
                                .name("Vito Nguyen (cuongnh28)")
                                .url("https://github.com/cuongnh28"))
                        .license(new License().name("Apache 2.0").url("https://www.apache.org/licenses/LICENSE-2.0")))
                .externalDocs(new ExternalDocumentation()
                        .description("Project README")
                        .url("https://example.com/docs"))
                .addTagsItem(new Tag().name("auth-controller").description("Authentication endpoints: sign in, sign up"))
                .addTagsItem(new Tag().name("user-controller").description("User profile and account information"))
                .addTagsItem(new Tag().name("test-controller").description("Public/admin/user access test endpoints"))
                .addTagsItem(new Tag().name("product-event-controller").description("Kafka product event test endpoints"));
    }
}
