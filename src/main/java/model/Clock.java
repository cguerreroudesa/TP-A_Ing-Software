package model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Clock {
    private LocalDateTime currentTime;

    public Clock() {
        this.currentTime = null;
    }

    public Clock(LocalDateTime fixedTime) {
        this.currentTime = fixedTime;
    }

    public LocalDate today() {
        return (currentTime != null) ? currentTime.toLocalDate() : LocalDate.now();
    }

    public LocalDateTime now() {
        return (currentTime != null) ? currentTime : LocalDateTime.now();
    }

    public void advanceMinutes(int minutes) {
        if (currentTime == null) {
            throw new UnsupportedOperationException("Este Clock no es controlable");
        }
        currentTime = currentTime.plusMinutes(minutes);
    }

}

