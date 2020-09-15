package org.tts;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.tts.config.DataSourceConfig;

import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@EnableSwagger2
@ComponentScan(basePackages = { "org.tts", "org.tts.api",
		 "io.swagger.configuration"})
public class Sbml4jApplication {
	@Autowired
	DataSourceConfig dataSourceConfig;

	public static void main(String[] args) {
		SpringApplication.run(Sbml4jApplication.class, args);
	}
	
}
