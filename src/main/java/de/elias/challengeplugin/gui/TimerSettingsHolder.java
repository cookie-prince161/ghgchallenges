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

        // Slot 9: Modus-Wahl (Slot 9 links)
        // Normal = Roter Kompass (RECOVERY_COMPASS), Countdown = Blauer Kompass (COMPASS)
        ItemStack modeItem = new ItemStack(timer.isCountdown() ? Material.COMPASS : Material.RECOVERY_COMPASS);
        ItemMeta modeMeta = modeItem.getItemMeta();
        if (timer.isCountdown()) {
            modeMeta.displayName(Component.text("⏳ Modus: Countdown", NamedTextColor.AQUA));
            List<String> lore = new ArrayList<>();
            lore.add("§7Der Timer zählt rückwärts von: §e" + timer.formatTime(timer.getCountdownTime()));
            lore.add("§eKlicken, um zu Normal (Stoppuhr) zu wechseln.");
            modeMeta.setLore(lore);
        } else {
            modeMeta.displayName(Component.text("⏱ Modus: Normal (Stoppuhr)", NamedTextColor.RED));
            List<String> lore = new ArrayList<>();
            lore.add("§7Der Timer zählt vorwärts.");
            lore.add("§eKlicken, um zu Countdown zu wechseln.");
            modeMeta.setLore(lore);
        }
        modeItem.setItemMeta(modeMeta);
        inv.setItem(9, modeItem);

        // Slot 11: Start / Pause
        ItemStack toggleRun = new ItemStack(timer.isRunning() ? Material.RED_WOOL : Material.GREEN_WOOL);
        ItemMeta runMeta = toggleRun.getItemMeta();
        runMeta.displayName(timer.isRunning() ? Component.text("⏸ Timer pausieren", NamedTextColor.RED) : Component.text("▶ Timer starten", NamedTextColor.GREEN));
        toggleRun.setItemMeta(runMeta);
        inv.setItem(11, toggleRun);

        // Slot 13 & 14: Countdown-Zeit anpassen (wenn Countdown aktiv ist)
        if (timer.isCountdown()) {
            ItemStack addTime = new ItemStack(Material.CLOCK);
            ItemMeta addMeta = addTime.getItemMeta();
            addMeta.displayName(Component.text("➕ Zeit hinzufügen (+5 Min)", NamedTextColor.GREEN));
            addTime.setItemMeta(addMeta);
            inv.setItem(13, addTime);

            ItemStack subTime = new ItemStack(Material.FEATHER);
            ItemMeta subMeta = subTime.getItemMeta();
            subMeta.displayName(Component.text("➖ Zeit verringern (-5 Min)", NamedTextColor.RED));
            subTime.setItemMeta(subMeta);
            inv.setItem(14, subTime);
        }

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

        if (slot == 9) {
            timer.setCountdown(!timer.isCountdown());
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.2f);
            holder.openMenu(player);
        } else if (slot == 11) {
            if (timer.isRunning()) {
                timer.pause();
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.8f);
            } else {
                timer.resume();
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.2f);
            }
            holder.openMenu(player);
        } else if (slot == 13 && timer.isCountdown()) {
            timer.setCountdownTime(timer.getCountdownTime() + 300); // +5 Minuten
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BIT, 1.0f, 1.5f);
            holder.openMenu(player);
        } else if (slot == 14 && timer.isCountdown()) {
            timer.setCountdownTime(timer.getCountdownTime() - 300); // -5 Minuten
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BIT, 1.0f, 0.5f);
            holder.openMenu(player);
        } else if (slot == 15) {
            timer.reset();
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
            holder.openMenu(player);
        } else if (slot == 22) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 0.8f);
            MainMenuHolder mainMenu = new MainMenuHolder(challengeManager, timer);
            mainMenu.openMenu(player);
        }
    }

    @Override
    public @NotNull Inventory getInventory() { return null; }
}
