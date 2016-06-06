package chihz.restgaga.test.mock;

public class User {

    private final int id;

    private final String username;

    private final String birthday;

    public User(int id, String username, String birthday) {
        this.id = id;
        this.username = username;
        this.birthday = birthday;
    }

    public int getId() {
        return this.id;
    }

    public String getUsername() {
        return this.username;
    }

    public String getBirthday() {
        return this.birthday;
    }
}
