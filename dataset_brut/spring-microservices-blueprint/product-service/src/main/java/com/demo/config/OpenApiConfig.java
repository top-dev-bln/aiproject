package com.demo.config;

/**
 * Author: Vito Nguyen (<a href="https://github.com/cuongnh28">...</a>)
 */
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
    public OpenAPI productServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Product Service API")
                        .description("Product management APIs with Kafka integration and Resilience4j Circuit Breaker.\n\n"
                                + "This service allows creating, updating, listing and searching products, \n"
                                + "emitting domain events to Kafka and calling remote services with fault tolerance.")
                        .version("v1.0.0")
                        .termsOfService("https://example.com/terms")
                        .contact(new Contact()
                                .name("Vito Nguyen (cuongnh28)")
                                .url("https://github.com/cuongnh28"))
                        .license(new License().name("Apache 2.0").url("https://www.apache.org/licenses/LICENSE-2.0")))
                .externalDocs(new ExternalDocumentation()
                        .description("Project README")
                        .url("https://example.com/docs"))
                .addTagsItem(new Tag().name("product-controller").description("CRUD and query endpoints for products"))
                .addTagsItem(new Tag().name("async-test-controller").description("Async endpoints demonstrating threading and error handling"))
                .addTagsItem(new Tag().name("user-controller").description("Utility endpoint to generate users for testing"));
    }
}
