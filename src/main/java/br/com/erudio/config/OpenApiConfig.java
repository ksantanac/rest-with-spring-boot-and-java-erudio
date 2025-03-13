package br.com.erudio.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration // Inicializar a classe junto com o contexo
public class OpenApiConfig {

    @Bean // Objeto instanciado, montado e configurado pelo spring
    OpenAPI customOpenApi(){
        return new OpenAPI()
            .info(new Info()
                    .title("REST API's RESTful from 0 with Java, Spring Boot, Kubernets and Docker.")
                    .version("v1")
                    .description("REST API's RESTful from 0 with Java, Spring Boot, Kubernets and Docker.")
                    .termsOfService("http://www.linkedin.com/in/kaue-matheus-santana-9a24991b1")
                    .license(new License()
                        .name("Apache 2.0")
                        .url("http://www.linkedin.com/in/kaue-matheus-santana-9a24991b1")
                    )
            );
    }

}
