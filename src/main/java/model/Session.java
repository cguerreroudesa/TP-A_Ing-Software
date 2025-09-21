package model;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class Session {
    private String token;
    private Clock clock;
    private LocalDateTime tokenCreationTime;
    public static String incorrectToken = "Incorrect Token";
    public static String sessionExpired = "Your session is expired please login again";


    public Session(Clock clock) {
        this.tokenCreationTime = clock.now();
        this.clock = clock;
        this.token = generateToken();
    }

    private String generateToken() {
        return UUID.randomUUID().toString();
    }

    public String getToken() {return this.token;}

    public LocalDateTime getTokenCreationTime() {return this.tokenCreationTime;}


    public void validateExpiration() {
        if (isExpired()){
            throw new RuntimeException(sessionExpired);
        }
    }

    public void validateToken(String token) {
        if (!Objects.equals(this.token, token)) {
            throw new RuntimeException(incorrectToken);
        }
    }

    public boolean isExpired() {
        return !clock.now().isBefore(tokenCreationTime.plusMinutes(5));
    }
}
