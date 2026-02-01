# Bucket4j - Best Practices Context (Latest API)

## Core Principles

Bucket4j is a **rate-limiting library** for Java applications. It implements the **token bucket algorithm**.

## Latest Dependency (No Deprecated APIs)
```xml
<dependency>
    <groupId>com.bucket4j</groupId>
    <artifactId>bucket4j-core</artifactId>
    <version>8.10.1</version>
</dependency>
```

## CORRECT Usage (Non-Deprecated)

### 1. Creating Bandwidth with Builder
```java
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import java.time.Duration;

// ✅ CORRECT - Using builder pattern
Bandwidth limit = Bandwidth.builder()
    .capacity(100)
    .refillGreedy(100, Duration.ofHours(1))
    .build();

Bucket bucket = Bucket.builder()
    .addLimit(limit)
    .build();
```

### 2. Different Refill Strategies

#### Greedy Refill (Default)
```java
// Refills all tokens at once after duration
Bandwidth limit = Bandwidth.builder()
    .capacity(100)
    .refillGreedy(100, Duration.ofMinutes(1))
    .build();
```

#### Intervally Refill
```java
// Refills tokens at fixed intervals
Bandwidth limit = Bandwidth.builder()
    .capacity(100)
    .refillIntervally(10, Duration.ofSeconds(1)) // 10 tokens every second
    .build();
```

#### Intervally-Aligned Refill
```java
// Refills at aligned time points (e.g., start of each minute)
import java.time.Instant;

Bandwidth limit = Bandwidth.builder()
    .capacity(100)
    .refillIntervallyAligned(100, Duration.ofMinutes(1), Instant.now())
    .build();
```

### 3. Multiple Limits
```java
// Per-second and per-minute limits
Bandwidth perSecond = Bandwidth.builder()
    .capacity(10)
    .refillGreedy(10, Duration.ofSeconds(1))
    .build();

Bandwidth perMinute = Bandwidth.builder()
    .capacity(100)
    .refillGreedy(100, Duration.ofMinutes(1))
    .build();

Bucket bucket = Bucket.builder()
    .addLimit(perSecond)
    .addLimit(perMinute)
    .build();
```

## Consuming Tokens

### 1. Try Consume (Blocking)
```java
// Try to consume 1 token
if (bucket.tryConsume(1)) {
    // Request allowed
    return processRequest();
} else {
    // Rate limit exceeded
    throw new RateLimitExceededException();
}
```

### 2. Try Consume with Timeout
```java
import java.time.Duration;

// Wait up to 100ms for token
boolean consumed = bucket.asBlocking()
    .tryConsume(1, Duration.ofMillis(100));

if (consumed) {
    return processRequest();
} else {
    throw new RateLimitExceededException();
}
```

### 3. Consume (Blocking)
```java
// Block until token is available
bucket.asBlocking().consume(1);
return processRequest();
```

### 4. Probe and Get State
```java
import io.github.bucket4j.ConsumptionProbe;

ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

if (probe.isConsumed()) {
    // Success
    long remaining = probe.getRemainingTokens();
    return ResponseEntity.ok()
        .header("X-Rate-Limit-Remaining", String.valueOf(remaining))
        .body(result);
} else {
    // Rate limited
    long waitTime = probe.getNanosToWaitForRefill();
    return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
        .header("X-Rate-Limit-Retry-After-Seconds", 
                String.valueOf(Duration.ofNanos(waitTime).getSeconds()))
        .build();
}
```

## Integration with Spring Boot

### 1. In-Memory Rate Limiter (Simple)
```java
@Component
public class RateLimiterService {
    
    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();
    
    public Bucket resolveBucket(String key) {
        return cache.computeIfAbsent(key, k -> createBucket());
    }
    
    private Bucket createBucket() {
        Bandwidth limit = Bandwidth.builder()
            .capacity(100)
            .refillGreedy(100, Duration.ofHours(1))
            .build();
        
        return Bucket.builder()
            .addLimit(limit)
            .build();
    }
}
```

### 2. With Spring Cache (Distributed)
```java
@Service
public class RateLimiterService {
    
    private final CacheManager cacheManager;
    
    public Bucket resolveBucket(String key) {
        Cache cache = cacheManager.getCache("rateLimiterBuckets");
        Bucket bucket = cache.get(key, Bucket.class);
        
        if (bucket == null) {
            bucket = createBucket();
            cache.put(key, bucket);
        }
        
        return bucket;
    }
    
    private Bucket createBucket() {
        Bandwidth limit = Bandwidth.builder()
            .capacity(100)
            .refillGreedy(100, Duration.ofHours(1))
            .build();
        
        return Bucket.builder()
            .addLimit(limit)
            .build();
    }
}
```

### 3. Interceptor for REST APIs
```java
@Component
public class RateLimitInterceptor implements HandlerInterceptor {
    
    private final RateLimiterService rateLimiterService;
    
    @Override
    public boolean preHandle(HttpServletRequest request, 
                            HttpServletResponse response, 
                            Object handler) throws Exception {
        
        String key = getClientKey(request);
        Bucket bucket = rateLimiterService.resolveBucket(key);
        
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        
        if (probe.isConsumed()) {
            response.addHeader("X-Rate-Limit-Remaining", 
                             String.valueOf(probe.getRemainingTokens()));
            return true;
        } else {
            long waitForRefill = probe.getNanosToWaitForRefill();
            response.addHeader("X-Rate-Limit-Retry-After-Seconds",
                             String.valueOf(Duration.ofNanos(waitForRefill).getSeconds()));
            response.sendError(HttpStatus.TOO_MANY_REQUESTS.value(),
                             "Rate limit exceeded");
            return false;
        }
    }
    
    private String getClientKey(HttpServletRequest request) {
        String userEmail = (String) request.getAttribute("userEmail");
        return userEmail != null ? userEmail : request.getRemoteAddr();
    }
}
```

### 4. Configuration
```java
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    
    private final RateLimitInterceptor rateLimitInterceptor;
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/public/**");
    }
}
```

## Advanced Patterns

### 1. Per-User Rate Limiting
```java
@Service
public class UserRateLimiterService {
    
    private final Map<String, Bucket> userBuckets = new ConcurrentHashMap<>();
    
    public Bucket resolveBucket(String userEmail) {
        return userBuckets.computeIfAbsent(userEmail, this::createUserBucket);
    }
    
    private Bucket createUserBucket(String userEmail) {
        // Different limits based on user type
        if (isPremiumUser(userEmail)) {
            return createPremiumBucket();
        } else {
            return createFreeBucket();
        }
    }
    
    private Bucket createFreeBucket() {
        Bandwidth limit = Bandwidth.builder()
            .capacity(100)
            .refillGreedy(100, Duration.ofHours(1))
            .build();
        
        return Bucket.builder().addLimit(limit).build();
    }
    
    private Bucket createPremiumBucket() {
        Bandwidth limit = Bandwidth.builder()
            .capacity(1000)
            .refillGreedy(1000, Duration.ofHours(1))
            .build();
        
        return Bucket.builder().addLimit(limit).build();
    }
}
```

### 2. Annotation-Based
```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {
    int capacity() default 100;
    int refillTokens() default 100;
    long refillDuration() default 1;
    TimeUnit refillUnit() default TimeUnit.HOURS;
}
```

```java
@Aspect
@Component
public class RateLimitAspect {
    
    private final RateLimiterService rateLimiterService;
    
    @Around("@annotation(rateLimit)")
    public Object rateLimit(ProceedingJoinPoint joinPoint, RateLimit rateLimit) 
            throws Throwable {
        
        String key = getCurrentUserKey();
        Bucket bucket = rateLimiterService.resolveBucket(key);
        
        if (bucket.tryConsume(1)) {
            return joinPoint.proceed();
        } else {
            throw new RateLimitExceededException();
        }
    }
}
```

### 3. With Redis (Distributed)
```xml
<dependency>
    <groupId>com.bucket4j</groupId>
    <artifactId>bucket4j-redis</artifactId>
    <version>8.10.1</version>
</dependency>
```

```java
@Service
public class RedisRateLimiterService {
    
    private final RedissonClient redissonClient;
    
    public Bucket resolveBucket(String key) {
        RBucket<byte[]> redisBucket = redissonClient.getBucket(key);
        
        BucketConfiguration configuration = BucketConfiguration.builder()
            .addLimit(Bandwidth.builder()
                .capacity(100)
                .refillGreedy(100, Duration.ofHours(1))
                .build())
            .build();
        
        return redisBucket.getAndSet(
            Bucket.builder()
                .addLimit(configuration.getBandwidths()[0])
                .build()
                .getAvailableTokens()
        );
    }
}
```

## Best Practices

### ✅ DO

1. **Use builder pattern** - `Bandwidth.builder()`
2. **Cache buckets** - Don't create new bucket for each request
3. **Use tryConsumeAndReturnRemaining** - Get info for headers
4. **Set appropriate capacity** - Based on expected load
5. **Choose right refill strategy** - Greedy vs Intervally
6. **Add multiple limits** - Per-second + per-hour
7. **Include retry-after header** - Help clients back off
8. **Log rate limit violations** - Monitor abuse
9. **Use distributed cache** for multi-instance deployments
10. **Test rate limits** - Verify behavior under load

### ❌ DON'T

1. **DON'T use deprecated Refill.of()** - Use builder
2. **DON'T create bucket per request** - Cache them
3. **DON'T use too small capacity** - Causes frequent limits
4. **DON'T use too large capacity** - Allows burst abuse
5. **DON'T forget to handle exceptions** - Return proper error
6. **DON'T rate limit health checks** - Exclude monitoring endpoints
7. **DON'T use in-memory cache** for multiple instances

## Testing

### Unit Test
```java
@Test
void shouldRateLimitAfterCapacity() {
    Bandwidth limit = Bandwidth.builder()
        .capacity(2)
        .refillGreedy(2, Duration.ofHours(1))
        .build();
    
    Bucket bucket = Bucket.builder()
        .addLimit(limit)
        .build();
    
    // First 2 requests should succeed
    assertTrue(bucket.tryConsume(1));
    assertTrue(bucket.tryConsume(1));
    
    // Third should fail
    assertFalse(bucket.tryConsume(1));
}
```

### Integration Test
```java
@SpringBootTest
@AutoConfigureMockMvc
class RateLimitIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void shouldRateLimitRequests() throws Exception {
        // Make 100 requests (capacity)
        for (int i = 0; i < 100; i++) {
            mockMvc.perform(get("/api/test"))
                   .andExpect(status().isOk());
        }
        
        // 101st should be rate limited
        mockMvc.perform(get("/api/test"))
               .andExpect(status().isTooManyRequests());
    }
}
```

## Monitoring

### Metrics
```java
@Component
public class RateLimiterMetrics {
    
    private final MeterRegistry meterRegistry;
    
    public void recordRateLimitHit(String endpoint) {
        meterRegistry.counter("rate_limit.hit", "endpoint", endpoint).increment();
    }
    
    public void recordRateLimitMiss(String endpoint) {
        meterRegistry.counter("rate_limit.miss", "endpoint", endpoint).increment();
    }
}
```

## Resources

- Official Docs: https://bucket4j.com/
- GitHub: https://github.com/bucket4j/bucket4j
- Examples: https://github.com/bucket4j/bucket4j/tree/master/bucket4j-examples
