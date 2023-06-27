package net.dumbdogdiner.dogcore.commands;

import dev.jorel.commandapi.annotations.Command;
import dev.jorel.commandapi.annotations.Default;
import dev.jorel.commandapi.annotations.Permission;
import net.dumbdogdiner.dogcore.Permissions;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@Command("hat")
@Permission(Permissions.HAT)
public final class HatCommand {
    private HatCommand() { }

    @Default
    public static void hat(final @NotNull Player player) {
        var inventory = player.getInventory();
        var inHand = inventory.getItemInMainHand();
        var currentHat = inventory.getHelmet();

        if (currentHat != null && currentHat.containsEnchantment(Enchantment.BINDING_CURSE)) {
            // don't bypass curse of binding
            return;
        }

        inventory.setHelmet(inHand);
        inventory.setItemInMainHand(currentHat);
    }
}
