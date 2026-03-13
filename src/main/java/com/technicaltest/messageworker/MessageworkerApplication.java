package com.technicaltest.messageworker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

@SpringBootApplication
@EnableReactiveMongoRepositories(basePackages = "com.technicaltest.messageworker.repository")
public class MessageworkerApplication {

	public static void main(String[] args) {
		SpringApplication.run(MessageworkerApplication.class, args);
	}

}
