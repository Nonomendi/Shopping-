package za.co.tyaphile.user;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class UserTest {
    @Test
    void testUserGeneration() {
        String name = "John";
        String email = "john@example.com";

        User johnUser = new User(name, email);
        assertNotNull(johnUser.getId());
        assertEquals(name,  johnUser.getName());
        assertEquals(email,  johnUser.getEmail());
    }
}
