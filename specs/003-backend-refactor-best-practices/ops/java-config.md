# Java Runtime Config (Cloud-Agnostic)

Apply via `JAVA_TOOL_OPTIONS` in systemd, container, or CI/CD environment.

## Recommended Flags (low-memory)
```
-XX:+UseG1GC
-XX:MaxRAMPercentage=70
-XX:InitialRAMPercentage=50
-XX:MinRAMPercentage=20
-XX:+UseStringDeduplication
-Dfile.encoding=UTF-8
-Djava.security.egd=file:/dev/urandom
```

## Notes
- Tune percentages based on instance size.
- Keep flags minimal on free tier to avoid GC overhead.
