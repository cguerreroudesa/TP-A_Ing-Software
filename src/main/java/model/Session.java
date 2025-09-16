package model;

import java.time.LocalDateTime;
import java.util.UUID;

public class Session {
    String token;
    Clock clock;
    LocalDateTime tokenCreationTime;
    public Session(Clock clock) {
        this.tokenCreationTime = clock.now();
        this.clock = clock;
        this.token = generateToken();
    }

    private String generateToken() {
        return UUID.randomUUID().toString();
    }

    public String getToken() {return this.token;}

    public Clock getClock() {return this.clock;}

    public LocalDateTime getCreationTime() {return this.tokenCreationTime;}
}
