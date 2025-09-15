package model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class SessionTest {
    @Test
    public void test01checkSessionIsCreated() {

    }

    private void assertThrowsLike(Executable runnable, String expectedMessage) {
        assertEquals(
                expectedMessage,
                assertThrows(RuntimeException.class, runnable).getMessage()
        );
    }
}
