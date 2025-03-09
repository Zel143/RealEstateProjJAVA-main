package realestate.login;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class UserManager {
    private static final String USER_FILE = "users.dat";
    private Map<String, String> userCredentials;

    public UserManager() {
        userCredentials = new HashMap<>();
        loadUsersFromFile();
    }

    private void loadUsersFromFile() {
        File file = new File(USER_FILE);
        if (!file.exists()) return;

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            userCredentials = (Map<String, String>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading users: " + e.getMessage());
        }
    }

    private void saveUsersToFile() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(USER_FILE))) {
            oos.writeObject(userCredentials);
        } catch (IOException e) {
            System.err.println("Error saving users: " + e.getMessage());
        }
    }

    public boolean registerUser(String username, String password) {
        if (userCredentials.containsKey(username)) {
            return false;
        }
        userCredentials.put(username, password);
        saveUsersToFile();
        return true;
    }

    public boolean authenticateUser(String username, String password) {
        return userCredentials.containsKey(username) && userCredentials.get(username).equals(password);
    }

    public boolean resetPassword(String username, String newPassword) {
        if (!userCredentials.containsKey(username)) {
            return false;
        }
        userCredentials.put(username, newPassword);
        saveUsersToFile();
        return true;
    }
}
