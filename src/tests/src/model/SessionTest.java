package model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class SessionTest implements AssertThrowsLike {

    @Test
    public void test01AcceptCorrectToken() {
        Session session = newSession();
        assertDoesNotThrow(() -> session.validateToken(session.getToken()));
    }

    @Test
    public void test02RejectsWrongToken() {
        assertThrowsLike(() -> newSession().validateToken("wrongToken"),
                Session.incorrectToken);
    }

    @Test
    public void test03SessionExpiresAtExactlyFiveMinutes() {
        Session session = newSessionExpired(5);
        assertTrue(session.isExpired());
    }

    @Test
    public void test04SessionExpiresAfterFiveMinutes() {
        Session session = newSessionExpired(7);
        assertTrue(session.isExpired());
    }

    private Session newSession() {
        Clock clock = new Clock();
        return new Session(clock);
    }

    private Session newSessionExpired(Integer timeToAdd) {
        Clock myClock = new Clock() {
            private LocalDateTime current = LocalDateTime.now();

            @Override
            public LocalDateTime now() {
                LocalDateTime toReturn = current;
                current = current.plusMinutes(1);
                return toReturn;
            }

            public void advanceMinutes(Integer minutes) {
                current = current.plusMinutes(minutes);
            }
        };

        Session session = new Session(myClock);

        myClock.advanceMinutes(timeToAdd);
        return session;
    }
}
