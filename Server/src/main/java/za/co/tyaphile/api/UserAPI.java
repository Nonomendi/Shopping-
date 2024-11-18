package za.co.tyaphile.api;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import za.co.tyaphile.database.DatabaseManager;
import za.co.tyaphile.user.User;

import java.util.Map;

import static za.co.tyaphile.ECommerceServer.getErrorMessage;

public class UserAPI {

    public void deleteUser(Context context) {
        String id = context.pathParam("id");
        DatabaseManager.removeUser(id);
    }

    public void addUser(Context context) {
        Map<?, ?> request = (Map<?, ?>) new Gson().fromJson(context.body(), Map.class);

        User user = DatabaseManager.getUser(request.get("name").toString(), request.get("email").toString());
        if (user == null) {
            boolean isAdded = DatabaseManager.addUser(request.get("name").toString(), request.get("email").toString());

            if (isAdded) {
                user = DatabaseManager.getUser(request.get("name").toString(), request.get("email").toString());

                assert user != null;
                context.json(getUserJson(user));
            } else {
                context.status(HttpStatus.BAD_REQUEST);
                context.json(getErrorMessage(HttpStatus.BAD_REQUEST, "Failed to add user, email address already in use"));
            }
        } else {
            context.json(getUserJson(user));
        }
    }

    private String getUserJson(User user) {
        JsonObject json = new JsonObject();
        json.addProperty("id", user.getId());
        json.addProperty("name", user.getName());
        json.addProperty("email", user.getEmail());
        return json.toString();
    }
}
