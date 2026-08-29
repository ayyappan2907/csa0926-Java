package auth;

/**
 * Immutable session record for a logged-in Google user.
 * Created by GoogleAuthService after successful token exchange.
 */
public final class UserSession {

    private final String email;
    private final String name;
    private final String givenName;
    private final String pictureUrl;
    private final String accessToken;

    public UserSession(String email, String name, String givenName,
                       String pictureUrl, String accessToken) {
        this.email       = email;
        this.name        = name;
        this.givenName   = givenName;
        this.pictureUrl  = pictureUrl;
        this.accessToken = accessToken;
    }

    public String getEmail()       { return email; }
    public String getName()        { return name; }
    public String getGivenName()   { return givenName; }
    public String getPictureUrl()  { return pictureUrl; }
    public String getAccessToken() { return accessToken; }

    /** Returns the first letter of the user's name (for avatar circle). */
    public char getAvatarInitial() {
        String n = (givenName != null && !givenName.isEmpty()) ? givenName : name;
        return (n != null && !n.isEmpty()) ? Character.toUpperCase(n.charAt(0)) : '?';
    }

    @Override
    public String toString() {
        return "UserSession{email='" + email + "', name='" + name + "'}";
    }
}
