package com.tndavidson.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.bus.jackson.RemoteApplicationEventScan;

/**
 * Main entry point to our application
 *
 * @author Tim Davidson
 */
@SpringBootApplication
@RemoteApplicationEventScan // tells spring cloud bus to scan for custom events in the current package and subpackages
public class Application {
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}
