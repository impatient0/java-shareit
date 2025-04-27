package ru.practicum.shareit.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class ShareitServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ShareitServerApplication.class, args);
	}

}
