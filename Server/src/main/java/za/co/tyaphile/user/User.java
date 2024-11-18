package za.co.tyaphile.user;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

public class User {
    private final String id, name, email;
    private final Collection<String> orderedProducts = new ArrayList<>();

    public User(final String name, final String email) {
        this.name = name;
        this.email = email;
        this.id = UUID.randomUUID().toString();
    }

    public User(final String id, final String name, final String email) {
        this.name = name;
        this.email = email;
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }
}
