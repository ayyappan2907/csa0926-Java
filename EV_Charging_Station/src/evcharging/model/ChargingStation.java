package evcharging.model;

/** Represents an EV charging station. */
public class ChargingStation {
    private int stationId;
    private String stationName;
    private String location;
    private String chargerType;
    private double ratePerKwh;

    public ChargingStation(int stationId, String stationName,
                           String location, String chargerType,
                           double ratePerKwh) {
        this.stationId = stationId;
        this.stationName = stationName;
        this.location = location;
        this.chargerType = chargerType;
        this.ratePerKwh = ratePerKwh;
    }

    public int getStationId() { return stationId; }
    public String getStationName() { return stationName; }
    public String getLocation() { return location; }
    public String getChargerType() { return chargerType; }
    public double getRatePerKwh() { return ratePerKwh; }

    public void setRatePerKwh(double ratePerKwh) {
        this.ratePerKwh = ratePerKwh;
    }

    @Override
    public String toString() {
        return String.format(
            "%-3d %-20s %-15s %-15s Rs.%.2f/kWh",
            stationId, stationName, location,
            chargerType, ratePerKwh
        );
    }
}