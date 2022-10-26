package com.fwd.is;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@ComponentScan({"com.fwd"})
public class InfoLookupStarter {

	public static void main(String[] args) {
		SpringApplication.run(InfoLookupStarter.class, args);
	}

}
