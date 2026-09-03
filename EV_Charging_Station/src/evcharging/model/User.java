package evcharging.model;

/**
 * Abstract base class for every EV charging system user.
 * Demonstrates encapsulation, abstraction and inheritance.
 */
public abstract class User {
    private String userId;
    private String name;
    private String email;
    private String password;

    protected User(String userId, String name, String email, String password) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public String getUserId() { return userId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }

    public void setName(String name) {
        this.name = name;
    }

    public abstract String getRole();

    @Override
    public String toString() {
        return getRole() + " [" + userId + " - " + name + "]";
    }
}