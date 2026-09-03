package evcharging.exception;

/**
 * Thrown when the selected charging slot is already booked
 * or unavailable.
 */
public class SlotUnavailableException extends Exception {
    public SlotUnavailableException(String message) {
        super(message);
    }
}