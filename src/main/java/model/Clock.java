package model;

import java.time.LocalDateTime;

public class Clock {
    private LocalDateTime current = LocalDateTime.now();

    public LocalDateTime now() {
        return current;
    }
    public Clock advanceMinutes(Integer timeToAdd) {
        current = current.plusMinutes(timeToAdd);
        return this;
    }
}
