package com.appsdeveloperblog.app.ws;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.service.VendorExtension;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

@Configuration
@EnableSwagger2
public class SwaggerConfig {

    Contact contact = new Contact("Andrés Salazar Colina", "", "andressalazarcolina@gmail.com");

    List<VendorExtension> vendorExtensions = new ArrayList<>();

    ApiInfo apiInfo = new ApiInfo(
            "RESTful Web Service Documentation",
            "This page documents RESTful web service endpoints",
            "1.0",
            "",
            contact,
            "Apache 2.0",
            "http://apache.org/licenses/LICENSE-2.0",
            vendorExtensions);


    @Bean
    public Docket apiDocket(){
        return new Docket(DocumentationType.SWAGGER_2)
                .protocols(new HashSet<>(List.of("HTTP","HTTPs")))
                .apiInfo(apiInfo)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.appsdeveloperblog.app.ws"))
                .paths(PathSelectors.any())
                .build();
    }

}
