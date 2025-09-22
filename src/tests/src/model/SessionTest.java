package model;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;

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
        Session session = newSessionExpired();
        assertTrue(session.isExpired());
    }

    @Test
    public void test04SessionExpiresAfterFiveMinutes() {
        Session session = newSessionExpired();
        assertTrue(session.isExpired());
    }

    private Session newSession() {
        Clock clock = new Clock();
        return new Session(clock);
    }

    private Session newSessionExpired() {
        Clock myClock =  new Clock() {
            Iterator<LocalDateTime> it = List.of( LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now().plusMinutes(6) ).iterator();
            public LocalDateTime now() {
                return it.next();
            }
        };

        Session session = new Session(myClock);

        myClock.now();
        return session;
    }
}
