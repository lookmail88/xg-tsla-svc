package xuyang.dev.xgtslasvc.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.List;

@Configuration
public class OpenApiConfig {

//    @Value("${openapi.servers.local}")
//    private String localUrl;
//
//    @Value("${openapi.servers.prod}")
//    private String prodUrl;
//
//    @Bean
//    public OpenAPI customOpenAPI() {
//        return new OpenAPI()
//                .servers(List.of(
//                        new Server().url(localUrl).description("local"),
//                        new Server().url(prodUrl).description("production gateway")
//                ));
//    }

}

