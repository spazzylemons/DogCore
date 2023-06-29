package net.dumbdogdiner.dogcore.listener;

import net.dumbdogdiner.dogcore.Permissions;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("deprecation")
public final class HatListener implements Listener {
    /** The slot that helmet is on. */
    private static final int HELMET_SLOT = 39;

    @EventHandler
    public void onClick(final @NotNull InventoryClickEvent event) {
        if (!event.getWhoClicked().hasPermission(Permissions.HAT)) {
            return;
        }

        var inventory = event.getClickedInventory();
        if (inventory instanceof PlayerInventory playerInventory) {
            var cursor = event.getCursor();
            if (event.getClick() == ClickType.LEFT && event.getSlot() == HELMET_SLOT && cursor != null) {
                // check if curse of binding
                var currentHat = playerInventory.getHelmet();
                if (currentHat != null && currentHat.containsEnchantment(Enchantment.BINDING_CURSE)) {
                    // don't allow bypassing binding with this
                    return;
                }
                event.setCancelled(true);
                playerInventory.setHelmet(event.getCursor());
                event.setCursor(currentHat);
            }
        }
    }
}
