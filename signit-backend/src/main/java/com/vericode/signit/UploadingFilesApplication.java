package com.vericode.signit;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.vericode.signit.storage.StorageProperties;
import com.vericode.signit.storage.S3.S3Service;

@SpringBootApplication()
@EnableConfigurationProperties(StorageProperties.class)
@EnableJpaRepositories(basePackages = {"com.example.data.File", "com.example.data.User"})
@EntityScan(basePackages = {"com.example.data.File", "com.example.data.User"})

public class UploadingFilesApplication {

	public static void main(String[] args) {
		SpringApplication.run(UploadingFilesApplication.class, args);
	}

	@Bean
	CommandLineRunner init(S3Service storageService) {
		return (args) -> {
			storageService.init();
		};
	}
}