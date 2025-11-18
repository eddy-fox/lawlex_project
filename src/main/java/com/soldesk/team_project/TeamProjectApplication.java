package com.soldesk.team_project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class TeamProjectApplication {

	public static void main(String[] args) {
		SpringApplication.run(TeamProjectApplication.class, args);
	}

}
