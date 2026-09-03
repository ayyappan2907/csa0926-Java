package evcharging.exception;

/**
 * Thrown for invalid bookings such as invalid slot,
 * invalid date, or incorrect booking details.
 */
public class InvalidBookingException extends Exception {
    public InvalidBookingException(String message) {
        super(message);
    }
}