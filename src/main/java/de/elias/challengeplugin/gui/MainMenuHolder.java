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
    private boolean isMultiplayer = false; // Standard Singleplayer

    public MainMenuHolder(ChallengeManager challengeManager, ChallengeTimer timer) {
        this.challengeManager = challengeManager;
        this.timer = timer;
    }

    public void openMenu(Player player) {
        Inventory inv = Bukkit.createInventory(this, 27, Component.text("⚡ Challenge Hauptmenü", NamedTextColor.DARK_PURPLE));

        // Slot 9: Singleplayer / Multiplayer Toggle (Mitte links)
        updateToggleItem(inv);

        // Uhr in den Settings / Schnellzugriff (z.B. Slot 8 oder oben rechts)
        ItemStack clock = new ItemStack(Material.CLOCK);
        ItemMeta clockMeta = clock.getItemMeta();
        clockMeta.displayName(Component.text("⏱ Timer-Einstellungen", NamedTextColor.GOLD));
        List<String> clockLore = new ArrayList<>();
        clockLore.add("§7Aktuelle Zeit: §e" + timer.formatTime(timer.getSeconds()));
        clockLore.add("§7Status: " + (timer.isRunning() ? "§aLäuft" : "§cPausiert"));
        clockMeta.setLore(clockLore);
        clock.setItemMeta(clockMeta);
        inv.setItem(8, clock);

        // Challenges ab Slot 11 aufreihen
        int slot = 11;
        for (Challenge challenge : challengeManager.getChallenges()) {
            if (slot > 17 && slot < 20) slot = 20; // Überspringe Slot 18 (erste Spalte unten)
            if (slot > 25) break; // Inventar voll

            ItemStack icon = challenge.getGuiIcon().clone();
            ItemMeta meta = icon.getItemMeta();
            if (meta != null) {
                List<Component> lore = new ArrayList<>();
                lore.add(Component.text(challenge.getDescription(), NamedTextColor.GRAY));
                lore.add(Component.text(""));
                lore.add(challenge.isEnabled() ? Component.text("● Status: Aktiviert", NamedTextColor.GREEN) : Component.text("○ Status: Deaktiviert", NamedTextColor.RED));
                lore.add(Component.text("Klicken zum Öffnen der Einstellungen", NamedTextColor.YELLOW));
                meta.lore(lore);
                icon.setItemMeta(meta);
            }
            inv.setItem(slot, icon);
            slot++;
            // Überspringe Spalte 0 (bleibt leer bei Slot 0, 9, 18)
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
        inv.setItem(9, toggleItem); // Slot 9: Exakt mittig links (Spalte 0 bleibt auf 0 und 18 frei)
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof MainMenuHolder holder)) return;
        event.setCancelled(true); // Verhindert das Herausnehmen von Items

        if (!(event.getWhoClicked() instanceof Player player)) return;
        int slot = event.getRawSlot();

        // Klick auf Slot 9 (Toggle)
        if (slot == 9) {
            holder.isMultiplayer = !holder.isMultiplayer;
            holder.updateToggleItem(event.getInventory());
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.2f);
            return;
        }

        // Klick auf Timer (Slot 8)
        if (slot == 8) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
            player.sendMessage(Component.text("⏱ Timer-Status: " + timer.formatTime(timer.getSeconds()), NamedTextColor.GOLD));
            return;
        }

        // Klick auf eine Challenge (vereinfacht für das Hauptmenü)
        ItemStack clicked = event.getCurrentItem();
        if (clicked != null && clicked.getType() != Material.AIR) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            // Hier öffnen wir im nächsten Schritt das individuelle Challenge-Settings-Menü
        }
    }

    @Override
    public @NotNull Inventory getInventory() {
        return null;
    }
}
