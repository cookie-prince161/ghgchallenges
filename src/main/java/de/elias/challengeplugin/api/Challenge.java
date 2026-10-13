package de.elias.challengeplugin.api;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public interface Challenge extends Listener {

    String getName();

    String getDescription();

    ItemStack getGuiIcon();

    boolean isEnabled();

    void setEnabled(boolean enabled);

    void onStart();

    void onStop();

    void openSettingsMenu(Player player);
}
