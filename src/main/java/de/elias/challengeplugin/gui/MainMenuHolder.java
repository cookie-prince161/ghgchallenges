package de.elias.challengeplugin.gui;

import de.elias.challengeplugin.api.Challenge;
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

public class MainMenuHolder implements InventoryHolder, Listener {

    private final ChallengeManager challengeManager;
    private final ChallengeTimer timer;
    private boolean isMultiplayer = false;

    public MainMenuHolder(ChallengeManager challengeManager, ChallengeTimer timer) {
        this.challengeManager = challengeManager;
        this.timer = timer;
    }

    public void openMenu(Player player) {
        Inventory inv = Bukkit.createInventory(this, 27, Component.text("⚡ Challenge Hauptmenü", NamedTextColor.DARK_PURPLE));

        // Slot 9: Singleplayer / Multiplayer Toggle
        updateToggleItem(inv);

        // Slot 8: Timer-Einstellungen (Uhr)
        ItemStack clock = new ItemStack(Material.CLOCK);
        ItemMeta clockMeta = clock.getItemMeta();
        clockMeta.displayName(Component.text("⏱ Timer-Einstellungen", NamedTextColor.GOLD));
        List<String> clockLore = new ArrayList<>();
        clockLore.add("§7Aktuelle Zeit: §e" + timer.formatTime(timer.getSeconds()));
        clockLore.add("§7Status: " + (timer.isRunning() ? "§aLäuft" : "§cPausiert"));
        clockLore.add("");
        clockLore.add("§eKlicken zum Umschalten (Start/Pause)!");
        clockMeta.setLore(clockLore);
        clock.setItemMeta(clockMeta);
        inv.setItem(8, clock);

        // Challenges ab Slot 11 aufreihen
        int slot = 11;
        for (Challenge challenge : challengeManager.getChallenges()) {
            if (slot > 17 && slot < 20) slot = 20;
            if (slot > 25) break;

            ItemStack icon = challenge.getGuiIcon().clone();
            ItemMeta meta = icon.getItemMeta();
            if (meta != null) {
                List<Component> lore = new ArrayList<>();
                lore.add(Component.text(challenge.getDescription(), NamedTextColor.GRAY));
                lore.add(Component.text(""));
                lore.add(challenge.isEnabled() ? Component.text("● Status: Aktiviert", NamedTextColor.GREEN) : Component.text("○ Status: Deaktiviert", NamedTextColor.RED));
                lore.add(Component.text("Klicken zum Umschalten (An/Aus)", NamedTextColor.YELLOW));
                meta.lore(lore);
                icon.setItemMeta(meta);
            }
            inv.setItem(slot, icon);
            slot++;
            if (slot % 9 == 0) slot++;
        }

        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 0.5f, 1.0f);
    }

    private void updateToggleItem(Inventory inv) {
        ItemStack toggleItem;
        ItemMeta meta;

        if (isMultiplayer) {
            toggleItem = new ItemStack(Material.NETHER_STAR);
            meta = toggleItem.getItemMeta();
            meta.displayName(Component.text("👥 Modus: Multiplayer", NamedTextColor.GREEN));
            List<String> lore = new ArrayList<>();
            lore.add("§7Alle Spieler müssen mitmachen.");
            lore.add("§eKlicken zum Wechseln zu Singleplayer.");
            meta.setLore(lore);
        } else {
            toggleItem = new ItemStack(Material.PLAYER_HEAD);
            meta = toggleItem.getItemMeta();
            meta.displayName(Component.text("👤 Modus: Singleplayer", NamedTextColor.AQUA));
            List<String> lore = new ArrayList<>();
            lore.add("§7Nur der Spieler wird gewertet.");
            lore.add("§eKlicken zum Wechseln zu Multiplayer.");
            meta.setLore(lore);
        }
        toggleItem.setItemMeta(meta);
        inv.setItem(9, toggleItem);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof MainMenuHolder holder)) return;
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) return;
        int slot = event.getRawSlot();

        // Slot 9: Toggle Singleplayer/Multiplayer
        if (slot == 9) {
            holder.isMultiplayer = !holder.isMultiplayer;
            holder.updateToggleItem(event.getInventory());
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.2f);
            return;
        }

        // Slot 8: Timer Schnellstart / Pause per Klick
        if (slot == 8) {
            if (timer.isRunning()) {
                timer.pause();
                player.sendMessage(Component.text("⏸ Timer pausiert!", NamedTextColor.YELLOW));
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.8f);
            } else {
                timer.resume();
                player.sendMessage(Component.text("⏱ Timer gestartet!", NamedTextColor.GREEN));
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.2f);
            }
            holder.openMenu(player); // Menü aktualisieren
            return;
        }

        // Klick auf Challenge-Items (Slots ab 11)
        ItemStack clicked = event.getCurrentItem();
        if (clicked != null && clicked.getType() != Material.AIR) {
            // Finde heraus, welche Challenge angeklickt wurde anhand des Item-Namens
            if (clicked.hasItemMeta() && clicked.getItemMeta().hasDisplayName()) {
                String displayName = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(clicked.getItemMeta().displayName());
                
                for (Challenge challenge : challengeManager.getChallenges()) {
                    if (displayName.contains(challenge.getName())) {
                        boolean newState = !challenge.isEnabled();
                        challenge.setEnabled(newState);
                        if (newState) {
                            challenge.onStart();
                            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                        } else {
                            challenge.onStop();
                            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.5f, 0.8f);
                        }
                        holder.openMenu(player); // Menü neu laden, damit der Status (Grün/Rot) aktualisiert wird!
                        break;
                    }
                }
            }
        }
    }

    @Override
    public @NotNull Inventory getInventory() {
        return null;
    }
}
