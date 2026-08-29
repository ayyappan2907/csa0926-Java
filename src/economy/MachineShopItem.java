package economy;

import model.MachineType;

/**
 * Immutable descriptor of one purchasable machine in the Machine Shop.
 * Price scaling: basePrice × (1 + priceGrowthRate)^ownedCount
 */
public record MachineShopItem(
    MachineType type,
    long        basePrice,
    double      priceGrowthRate,   // e.g. 0.08 = 8% per unit already owned
    int         unlockLevel        // minimum player level required
) {}
