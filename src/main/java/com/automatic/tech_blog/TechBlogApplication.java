package com.automatic.tech_blog;

import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories("com.automatic.tech_blog.repository")
public class TechBlogApplication {

	public static void main(String[] args) {
		try {
			String uuid = UUID.randomUUID().toString();
			MDC.put("uuid", uuid);
			SpringApplication.run(TechBlogApplication.class, args);
		} catch (Exception e) {
			System.out.println("Error while running TechBlogJob");
		} finally {
			MDC.clear();
		}
	}

}
