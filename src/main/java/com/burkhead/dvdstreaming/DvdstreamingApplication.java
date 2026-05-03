package com.burkhead.dvdstreaming;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import static com.burkhead.dvdstreaming.utils.ConfigValues.getAllConfigValues;

@SpringBootApplication
public class DvdstreamingApplication {

	public static void main(String[] args) {
		getAllConfigValues();
		SpringApplication.run(DvdstreamingApplication.class, args);
	}

}
