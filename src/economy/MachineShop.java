package economy;

import model.MachineType;
import player.PlayerProfile;
import java.util.*;

public class MachineShop {

    private final Map<MachineType, MachineShopItem> catalog = new EnumMap<>(MachineType.class);
    private final Map<MachineType, Integer> ownedCounts = new EnumMap<>(MachineType.class);

    public MachineShop() {
        catalog.put(MachineType.BELT, new MachineShopItem(MachineType.BELT, 10, 0.04, 1));
        catalog.put(MachineType.EXTRACTOR, new MachineShopItem(MachineType.EXTRACTOR, 50, 0.08, 1));
        catalog.put(MachineType.PROCESSOR, new MachineShopItem(MachineType.PROCESSOR, 200, 0.10, 3));
        catalog.put(MachineType.COMBINER, new MachineShopItem(MachineType.COMBINER, 750, 0.14, 5));
        catalog.put(MachineType.ADVANCED_COMBINER, new MachineShopItem(MachineType.ADVANCED_COMBINER, 2500, 0.18, 6));
        catalog.put(MachineType.CONVEYOR, new MachineShopItem(MachineType.CONVEYOR, 40, 0.06, 4));
        catalog.put(MachineType.STORAGE_SILO, new MachineShopItem(MachineType.STORAGE_SILO, 300, 0.05, 2));
        catalog.put(MachineType.EXPORT_TERMINAL, new MachineShopItem(MachineType.EXPORT_TERMINAL, 500, 0.06, 3));

        for (MachineType type : catalog.keySet()) {
            ownedCounts.put(type, 0);
        }
    }

    public long currentPrice(MachineType type) {
        MachineShopItem item = catalog.get(type);
        if (item == null) return 0;
        int count = ownedCounts.getOrDefault(type, 0);
        return Math.round(item.basePrice() * Math.pow(1 + item.priceGrowthRate(), count));
    }

    public boolean purchase(PlayerProfile profile, MachineType type) {
        MachineShopItem item = catalog.get(type);
        if (item == null) return false;
        if (profile.getCurrentLevel() < item.unlockLevel()) return false;
        long price = currentPrice(type);
        if (profile.getWallet().spend(price)) {
            ownedCounts.put(type, ownedCounts.getOrDefault(type, 0) + 1);
            return true;
        }
        return false;
    }

    public int getOwnedCount(MachineType type) {
        return ownedCounts.getOrDefault(type, 0);
    }

    public Map<MachineType, MachineShopItem> getCatalog() {
        return Collections.unmodifiableMap(catalog);
    }
}
