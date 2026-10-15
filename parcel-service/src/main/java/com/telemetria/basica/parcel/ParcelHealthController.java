package com.telemetria.basica.parcel;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ParcelHealthController {

	@GetMapping("/health")
	public String health() {
		return "UP";
	}
}
