package evcharging.model;

/** Represents an electric vehicle owned by a customer. */
public class Vehicle {
    private int vehicleId;
    private String vehicleNumber;
    private String vehicleType;
    private Customer owner;

    public Vehicle(int vehicleId, String vehicleNumber,
                   String vehicleType, Customer owner) {
        this.vehicleId = vehicleId;
        this.vehicleNumber = vehicleNumber;
        this.vehicleType = vehicleType;
        this.owner = owner;
    }

    public int getVehicleId() { return vehicleId; }
    public String getVehicleNumber() { return vehicleNumber; }
    public String getVehicleType() { return vehicleType; }
    public Customer getOwner() { return owner; }

    @Override
    public String toString() {
        return vehicleNumber + " - " + vehicleType;
    }
}