package net.shopec;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.wf.jwtp.configuration.EnableJwtPermission;


@EnableAutoConfiguration
@EnableCaching
@EnableJwtPermission
@SpringBootApplication
@MapperScan(basePackages = "net.shopec.dao")
@ComponentScan(basePackages = { "net.shopec.util", "net.shopec" })
public class ApplicationB2B2C extends SpringBootServletInitializer {

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(ApplicationB2B2C.class);
	}

	public static void main(String[] args) {
		SpringApplication.run(ApplicationB2B2C.class, args);
	}
	
}
