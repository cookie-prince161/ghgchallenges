package de.elias.challengeplugin.timer;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class TimerCommand implements CommandExecutor {

    private final ChallengeTimer timer;

    public TimerCommand(ChallengeTimer timer) {
        this.timer = timer;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("challengeplugin.admin")) {
            sender.sendMessage(Component.text("Dazu hast du keine Berechtigung!", NamedTextColor.RED));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(Component.text("Verwendung: /timer <start|pause|resume|reset|settings>", NamedTextColor.YELLOW));
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "start":
            case "resume":
                timer.resume();
                sender.sendMessage(Component.text("⏱ Timer fortgesetzt!", NamedTextColor.GREEN));
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
            case "settings":
                if (sender instanceof Player player) {
                    // Hier öffnen wir später das Timer-Settings-Menü
                    player.sendMessage(Component.text("⚙️ Timer-Einstellungen öffnen sich im Menü...", NamedTextColor.GOLD));
                } else {
                    sender.sendMessage(Component.text("Dieser Befehl kann nur von Spielern ausgeführt werden.", NamedTextColor.RED));
                }
                break;
            default:
                sender.sendMessage(Component.text("Unbekannter Timer-Befehl. Nutze start, pause, resume, reset oder settings.", NamedTextColor.RED));
                break;
        }
        return true;
    }
}
