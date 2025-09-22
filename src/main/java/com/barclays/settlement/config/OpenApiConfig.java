package com.barclays.settlement.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI settlementOpenApi() {
    return new OpenAPI()
        .info(
            new Info()
                .title("Settlement Reconciliation API")
                .description(
                    "Operations endpoints for managing settlement reconciliation mismatches")
                .version("v1"))
        .externalDocs(
            new ExternalDocumentation()
                .description("Barclays Engineering Standards")
                .url("https://barclays.example.com/standards"));
  }
}
