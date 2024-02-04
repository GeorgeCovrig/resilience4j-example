package product.service;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;


import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Controller
@RequestMapping("/product")
@Log4j2
public class ProductController {

    List<String> products = List.of("Beans", "Oat cereals", "Bread", "Milk");


    @GetMapping("/all")
    public ResponseEntity<List<String>> getProducts() {
        return ResponseEntity.ok(products);

    }

    @GetMapping("/test-retry")
    @Retry(name = "retry-test", fallbackMethod = "retryTestFallback")
    public ResponseEntity<String> retryTest() throws URISyntaxException {
        log.info("test retry call ");
        return new RestTemplate().getForEntity(new URI("https//:something-that"), String.class);
    }

    public ResponseEntity<String> retryTestFallback(Exception e) {
        return ResponseEntity.ok("Something went wrong in retry endpoint");
    }

    static int counter = 0;

    @GetMapping("/circuit-breaker")
    @CircuitBreaker(name = "circuit-breaker-test", fallbackMethod = "circuitBreakerTest")
    public ResponseEntity<String> circuitBreakerTest() throws URISyntaxException {
        log.info("circuit-breaker call " + counter++);
        return new RestTemplate().getForEntity(new URI("https//:something-that"), String.class);
    }

    public ResponseEntity<String> circuitBreakerTest(Exception e) {
        return ResponseEntity.ok("Something went wrong in circuit breaker endpoint");
    }


    @GetMapping("/rate-limiter")
    @RateLimiter(name = "rate-limiter")
    public ResponseEntity<String> rateLimiterTest() throws URISyntaxException {
        log.info("rate limiter test ");
        return ResponseEntity.ok("rate limiter example");
    }

    @GetMapping("/time-limiter")
    @TimeLimiter(name = "time-limiter")
    public CompletableFuture<String> timeLimiterTest() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                TimeUnit.SECONDS.sleep(5);
                return "Operation completed successfully!";
            } catch (InterruptedException e) {
                return "Operation interrupted!";
            }
        });
    }

    @GetMapping("/bulkhead")
    @Bulkhead(name = "bulkhead")
    public ResponseEntity<String> bulkheadTest() throws URISyntaxException {
        log.info("bulkhead test ");
        return ResponseEntity.ok("bulkhead example");
    }

}



