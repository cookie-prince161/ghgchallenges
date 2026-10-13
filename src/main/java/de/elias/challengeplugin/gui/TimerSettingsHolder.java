package de.elias.challengeplugin.gui;

import de.elias.challengeplugin.api.ChallengeManager;
import de.elias.challengeplugin.timer.ChallengeTimer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class TimerSettingsHolder implements InventoryHolder, Listener {

    private final ChallengeManager challengeManager;
    private final ChallengeTimer timer;

    public TimerSettingsHolder(ChallengeManager challengeManager, ChallengeTimer timer) {
        this.challengeManager = challengeManager;
        this.timer = timer;
    }

    public void openMenu(Player player) {
        Inventory inv = Bukkit.createInventory(this, 27, Component.text("⏱ Timer-Einstellungen", NamedTextColor.GOLD));

        // Slot 11: Start / Pause Schalter
        ItemStack toggleRun = new ItemStack(timer.isRunning() ? Material.RED_WOOL : Material.GREEN_WOOL);
        ItemMeta runMeta = toggleRun.getItemMeta();
        runMeta.displayName(timer.isRunning() ? Component.text("⏸ Timer pausieren", NamedTextColor.RED) : Component.text("▶ Timer starten", NamedTextColor.GREEN));
        toggleRun.setItemMeta(runMeta);
        inv.setItem(11, toggleRun);

        // Slot 13: Timer Modus (Normal vs Countdown)
        ItemStack modeItem = new ItemStack(Material.COMPASS);
        ItemMeta modeMeta = modeItem.getItemMeta();
        if (timer.isCountdown()) {
            modeMeta.displayName(Component.text("⏳ Modus: Countdown (" + timer.formatTime(timer.getCountdownTime()) + ")", NamedTextColor.YELLOW));
            List<String> lore = new ArrayList<>();
            lore.add("§7Der Timer zählt rückwärts.");
            lore.add("§eKlicken, um zu normaler Zeit zu wechseln.");
            modeMeta.setLore(lore);
        } else {
            modeMeta.displayName(Component.text("⏱ Modus: Normal (Stoppuhr)", NamedTextColor.AQUA));
            List<String> lore = new ArrayList<>();
            lore.add("§7Der Timer zählt vorwärts.");
            lore.add("§eKlicken, um zu Countdown zu wechseln.");
            modeMeta.setLore(lore);
        }
        modeItem.setItemMeta(modeMeta);
        inv.setItem(13, modeItem);

        // Slot 15: Zurücksetzen
        ItemStack resetItem = new ItemStack(Material.BARRIER);
        ItemMeta resetMeta = resetItem.getItemMeta();
        resetMeta.displayName(Component.text("🔄 Timer zurücksetzen", NamedTextColor.LIGHT_PURPLE));
        resetItem.setItemMeta(resetMeta);
        inv.setItem(15, resetItem);

        // Slot 22: Zurück zum Hauptmenü
        ItemStack backItem = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backItem.getItemMeta();
        backMeta.displayName(Component.text("⬅ Zurück zum Hauptmenü", NamedTextColor.WHITE));
        backItem.setItemMeta(backMeta);
        inv.setItem(22, backItem);

        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 0.5f, 1.2f);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof TimerSettingsHolder holder)) return;
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) return;
        int slot = event.getRawSlot();

        if (slot == 11) {
            // Start / Pause
            if (timer.isRunning()) {
                timer.pause();
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.8f);
            } else {
                timer.resume();
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.2f);
            }
            holder.openMenu(player); // Menü aktualisieren
        } else if (slot == 13) {
            // Modus wechseln
            timer.setCountdown(!timer.isCountdown());
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            holder.openMenu(player);
        } else if (slot == 15) {
            // Reset
            timer.reset();
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
            holder.openMenu(player);
        } else if (slot == 22) {
            // Zurück zum Hauptmenü
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 0.8f);
            MainMenuHolder mainMenu = new MainMenuHolder(challengeManager, timer);
            mainMenu.openMenu(player);
        }
    }

    @Override
    public @NotNull Inventory getInventory() {
        return null;
    }
}
