package player;

/**
 * Thread-safe cash wallet owned by PlayerProfile.
 * Cash is awarded ONLY when an item is sold (reaches Storage Silo / Export Terminal).
 */
public class Wallet {
    private long balance;
    private long lifetimeEarned;

    public Wallet(long startingBalance) {
        this.balance = startingBalance;
        this.lifetimeEarned = startingBalance;
    }

    public synchronized boolean spend(long amount) {
        if (amount > balance) return false;
        balance -= amount;
        return true;
    }

    public synchronized void credit(long amount) {
        balance += amount;
        lifetimeEarned += amount;
    }

    public synchronized long getBalance()        { return balance; }
    public synchronized long getLifetimeEarned() { return lifetimeEarned; }

    public synchronized String formatBalance() {
        return "$" + String.format("%,d", balance);
    }
}
