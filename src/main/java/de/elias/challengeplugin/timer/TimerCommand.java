package de.elias.challengeplugin.timer;

import de.elias.challengeplugin.api.ChallengeManager;
import de.elias.challengeplugin.gui.TimerSettingsHolder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class TimerCommand implements CommandExecutor {

    private final ChallengeTimer timer;
    private final ChallengeManager challengeManager;

    public TimerCommand(ChallengeTimer timer, ChallengeManager challengeManager) {
        this.timer = timer;
        this.challengeManager = challengeManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("challengeplugin.admin")) {
            sender.sendMessage(Component.text("Dazu hast du keine Berechtigung!", NamedTextColor.RED));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(Component.text("Verwendung: /timer <start|pause|resume|reset|settings|hide|show>", NamedTextColor.YELLOW));
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "start":
            case "resume":
                timer.resume();
                sender.sendMessage(Component.text("⏱ Timer gestartet/fortgesetzt!", NamedTextColor.GREEN));
                break;
            case "pause":
            case "stop":
                timer.pause();
                sender.sendMessage(Component.text("⏸ Timer pausiert!", NamedTextColor.YELLOW));
                break;
            case "reset":
                timer.reset();
                sender.sendMessage(Component.text("🔄 Timer zurückgesetzt!", NamedTextColor.AQUA));
                break;
            case "hide":
            case "show":
                if (sender instanceof Player player) {
                    timer.toggleHide(player);
                }
                break;
            case "settings":
                if (sender instanceof Player player) {
                    TimerSettingsHolder settingsHolder = new TimerSettingsHolder(challengeManager, timer);
                    settingsHolder.openMenu(player);
                }
                break;
            default:
                sender.sendMessage(Component.text("Unbekannter Befehl. Nutze start, pause, resume, reset, settings, hide oder show.", NamedTextColor.RED));
                break;
        }
        return true;
    }
}
