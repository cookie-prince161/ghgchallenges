package de.elias.challengeplugin.commands;

import de.elias.challengeplugin.api.ChallengeManager;
import de.elias.challengeplugin.timer.ChallengeTimer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Random;

public class WorldResetCommand implements CommandExecutor {

    private final ChallengeTimer timer;
    private final ChallengeManager challengeManager;

    public WorldResetCommand(ChallengeTimer timer, ChallengeManager challengeManager) {
        this.timer = timer;
        this.challengeManager = challengeManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("challengeplugin.admin")) {
            sender.sendMessage(Component.text("Dazu hast du keine Berechtigung!", NamedTextColor.RED));
            return true;
        }

        // 1. Timer und Challenges stoppen
        timer.reset();
        challengeManager.stopAllChallenges();

        // 2. Chat-Ankündigung
        Bukkit.broadcast(Component.text("🌍 Eine neue Welt wird generiert... Bitte warten!", NamedTextColor.GOLD));

        // 3. Neue Welt asynchron erstellen / laden
        long newSeed = new Random().nextLong();
        String worldName = "world_reset_" + System.currentTimeMillis();

        WorldCreator creator = new WorldCreator(worldName);
        creator.seed(newSeed);
        World newWorld = creator.createWorld();

        if (newWorld != null) {
            // Spawn-Punkt finden
            org.bukkit.Location spawnLocation = newWorld.getSpawnLocation();

            // 4. Alle Spieler in die neue Welt teleportieren
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.teleport(spawnLocation);
                player.sendMessage(Component.text("✨ Du wurdest in die neue Welt teleportiert! Seed: " + newSeed, NamedTextColor.GREEN));
            }

            Bukkit.broadcast(Component.text("✅ Neue Welt erfolgreich geladen!", NamedTextColor.GREEN));
        } else {
            sender.sendMessage(Component.text("Fehler beim Erstellen der neuen Welt!", NamedTextColor.RED));
        }

        return true;
    }
}
