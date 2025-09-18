package model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class SessionTest {

    @Test
    public void test01AcceptCorrectToken() {
        Session session = newSession();
        assertDoesNotThrow(() -> session.ensureValid(session.getToken()));
    }

    @Test
    public void test02RejectsWrongToken() {
        assertThrowsLike(() -> newSession().ensureValid("wrongToken"),
                Session.incorrectToken);
    }

    @Test
    public void test03TokenExpiresAfterFiveMinutes() {
        Session session = newSessionExpired(7);
        assertTrue(session.isExpired());
        assertThrowsLike(() -> session.ensureValid(session.getToken()),
                Session.tokenExpired);
    }

    @Test
    public void test04StillValidAtExactlyFiveMinutes() {
        Session session = newSessionExpired(5);
        assertFalse(session.isExpired());
        assertDoesNotThrow(() -> session.ensureValid(session.getToken()));
    }


    private Session newSession() {
        Clock clock = new Clock();
        return new Session(clock);
    }

    private Session newSessionExpired(Integer timeToAdd) {
        Clock clock = new Clock(LocalDateTime.now());
        Session session = new Session(clock);
        assertFalse(session.isExpired());

        clock.advanceMinutes(timeToAdd);
        return session;
    }

    private void assertThrowsLike(Executable runnable, String expectedMessage) {
        assertEquals(
                expectedMessage,
                assertThrows(RuntimeException.class, runnable).getMessage()
        );
    }


}
