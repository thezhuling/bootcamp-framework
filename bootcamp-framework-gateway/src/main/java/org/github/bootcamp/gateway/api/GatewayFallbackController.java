package org.github.bootcamp.gateway.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Circuit breaker fallback endpoint for downstream service failures.
 *
 * @author zhuling
 */
@RestController
public class GatewayFallbackController {

    @RequestMapping("/fallback")
    public ResponseEntity<String> fallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body("Service temporarily unavailable. Please try again later.");
    }
}
