package za.co.tyaphile.user;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDB {
    private final ArrayList<User> users = new ArrayList<>();

    public List<User> getUsers() {
        return users.stream().toList();
    }

    public void addUser(User user) {
        users.add(user);
    }

    public void removeUser(String id) {
        Optional<User> user = users.stream().filter(x -> x.getId().equals(id)).findAny();
        user.ifPresent(users::remove);
    }
}
