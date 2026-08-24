package com.docusync.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI Configuration
 * 
 * Configures Swagger/OpenAPI documentation
 */
@Configuration
public class OpenApiConfig {
    
    /**
     * OpenAPI configuration
     */
    @Bean
    public OpenAPI docusyncOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("DocuSync Enterprise Engine API")
                        .description("""
                                REST API for DocuSync - Distributed Real-Time 
                                Collaborative Workspace Engine with CRDT Conflict 
                                Resolution & RAG AI
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("DocuSync Team")
                                .email("support@docusync.io")
                                .url("https://docusync.io"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080/api/v1")
                                .description("Local Development"),
                        new Server()
                                .url("https://api.docusync.io/api/v1")
                                .description("Production")
                ));
    }
}