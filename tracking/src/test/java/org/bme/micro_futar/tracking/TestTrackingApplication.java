package org.bme.micro_futar.tracking;

import org.springframework.boot.SpringApplication;

public class TestTrackingApplication {

	public static void main(String[] args) {
		SpringApplication.from(TrackingApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
