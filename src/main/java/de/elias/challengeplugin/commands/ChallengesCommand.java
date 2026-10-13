package de.elias.challengeplugin.commands;

import de.elias.challengeplugin.api.ChallengeManager;
import de.elias.challengeplugin.gui.MainMenuHolder;
import de.elias.challengeplugin.timer.ChallengeTimer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ChallengesCommand implements CommandExecutor {

    private final ChallengeManager challengeManager;
    private final ChallengeTimer timer;

    public ChallengesCommand(ChallengeManager challengeManager, ChallengeTimer timer) {
        this.challengeManager = challengeManager;
        this.timer = timer;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Dieser Befehl kann nur von Spielern ausgeführt werden.");
            return true;
        }

        if (args.length > 0) {
            String sub = args[0].toLowerCase();
            switch (sub) {
                case "pause":
                case "stop":
                    timer.pause();
                    challengeManager.stopAllChallenges();
                    player.sendMessage(Component.text("⏸ Timer und alle Challenges gestoppt/pausiert!", NamedTextColor.YELLOW));
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.8f);
                    return true;
                case "resume":
                case "start":
                    timer.resume();
                    player.sendMessage(Component.text("⏱ Timer und Challenges gestartet/fortgesetzt!", NamedTextColor.GREEN));
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.2f);
                    return true;
                case "reset":
                    timer.reset();
                    challengeManager.stopAllChallenges();
                    player.sendMessage(Component.text("🔄 Timer zurückgesetzt und Challenges gestoppt.", NamedTextColor.AQUA));
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
                    return true;
            }
        }

        // Öffne das Hauptmenü
        MainMenuHolder menu = new MainMenuHolder(challengeManager, timer);
        menu.openMenu(player);
        return true;
    }
}
