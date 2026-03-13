package com.technicaltest.messageworker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableReactiveMongoRepositories(basePackages = "com.technicaltest.messageworker.repository")
@EnableRetry
public class MessageworkerApplication {

	public static void main(String[] args) {
		SpringApplication.run(MessageworkerApplication.class, args);
	}

}
