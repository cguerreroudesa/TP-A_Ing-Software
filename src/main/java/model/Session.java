package model;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class Session {
    String token;
    Clock clock;
    LocalDateTime tokenCreationTime;
    public static String incorrectToken = "Token incorrecto";
    public static String tokenExpired = "Token expired";

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

    public LocalDateTime getTokenCreationTime() {return this.tokenCreationTime;}

    public void ensureValid(String token) {
        validateToken(token);
        validateExpiration();
    }

    private void validateExpiration() {
        if (isExpired()){
            throw new RuntimeException(tokenExpired);
        }
    }

    private void validateToken(String token) {
        if (!Objects.equals(this.token, token)) {
            throw new RuntimeException(incorrectToken);
        }
    }

    public boolean isExpired() {
        return this.tokenCreationTime.plusMinutes(5)
                .isBefore(clock.now());
    }
}
