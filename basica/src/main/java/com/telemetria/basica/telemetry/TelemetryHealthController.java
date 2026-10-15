package com.telemetria.basica.telemetry;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TelemetryHealthController {

	@GetMapping("/health")
	public String health() {
		return "UP";
	}
}
