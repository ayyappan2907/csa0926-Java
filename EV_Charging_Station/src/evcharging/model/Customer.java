package evcharging.model;

/** Customer who books EV charging slots. */
public class Customer extends User {
    public Customer(String userId, String name,
                    String email, String password) {
        super(userId, name, email, password);
    }

    @Override
    public String getRole() {
        return "CUSTOMER";
    }
}