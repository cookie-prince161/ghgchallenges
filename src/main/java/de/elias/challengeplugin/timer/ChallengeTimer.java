package de.elias.challengeplugin.timer;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ChallengeTimer {

    private final JavaPlugin plugin;
    private long seconds = 0;
    private boolean running = false;
    private boolean started = false;
    private boolean countdown = false;
    private long countdownTime = 3600; // 1 Stunde Standard
    private final Set<UUID> hiddenPlayers = new HashSet<>();
    private BukkitTask task;
    private int rainbowIndex = 0;

    private final TextColor[] rainbowColors = new TextColor[]{
            TextColor.color(0xFF0000), TextColor.color(0xFF7F00), TextColor.color(0xFFFF00),
            TextColor.color(0x00FF00), TextColor.color(0x0000FF), TextColor.color(0x4B0082), TextColor.color(0x8B00FF)
    };

    public ChallengeTimer(JavaPlugin plugin) {
        this.plugin = plugin;
        startRunnable();
    }

    private void startRunnable() {
        task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (running) {
                if (countdown) {
                    if (seconds > 0) {
                        seconds--;
                    } else {
                        Bukkit.broadcast(Component.text("Der Countdown ist abgelaufen!", NamedTextColor.RED));
                        running = false;
                    }
                } else {
                    seconds++;
                }
            }

            // Rainbow Animation Index fortschreiten
            rainbowIndex = (rainbowIndex + 1) % rainbowColors.length;

            // Actionbar Component zusammenbauen
            Component barComponent;
            if (!started) {
                // Rainbow Effekt für "Timer: Not started yet"
                barComponent = Component.text("⏱ Timer: ", NamedTextColor.GRAY)
                        .append(Component.text("Not started yet", rainbowColors[rainbowIndex]));
            } else if (!running) {
                barComponent = Component.text("⏱ Timer: ", NamedTextColor.GOLD)
                        .append(Component.text("Paused (" + formatTime(seconds) + ")", NamedTextColor.YELLOW));
            } else {
                barComponent = Component.text("⏱ Timer: " + formatTime(seconds), NamedTextColor.GOLD);
            }

            // Actionbar an alle Online-Spieler senden, die ihn nicht versteckt haben
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!hiddenPlayers.contains(player.getUniqueId())) {
                    player.sendActionBar(barComponent);
                }
            }
        }, 0L, 20L);
    }

    public void start() {
        this.running = true;
        this.started = true;
    }

    public void pause() {
        this.running = false;
    }

    public void resume() {
        if (!started) started = true;
        this.running = true;
    }

    public void reset() {
        this.running = false;
        this.started = false;
        this.seconds = countdown ? countdownTime : 0;
    }

    public void toggleHide(Player player) {
        if (hiddenPlayers.contains(player.getUniqueId())) {
            hiddenPlayers.remove(player.getUniqueId());
            player.sendMessage(Component.text("👁 Timer wird wieder angezeigt.", NamedTextColor.GREEN));
        } else {
            hiddenPlayers.add(player.getUniqueId());
            player.sendMessage(Component.text("🙈 Timer wurde ausgeblendet.", NamedTextColor.YELLOW));
        }
    }

    public boolean isRunning() { return running; }
    public boolean isStarted() { return started; }
    public long getSeconds() { return seconds; }
    public void setSeconds(long seconds) { this.seconds = seconds; }
    public boolean isCountdown() { return countdown; }
    public void setCountdown(boolean countdown) {
        this.countdown = countdown;
        this.seconds = countdown ? countdownTime : 0;
    }
    public long getCountdownTime() { return countdownTime; }
    public void setCountdownTime(long countdownTime) {
        this.countdownTime = Math.max(60, countdownTime);
        if (countdown) this.seconds = this.countdownTime;
    }

    public String formatTime(long totalSeconds) {
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long secs = totalSeconds % 60;

        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, secs);
        } else {
            return String.format("%02d:%02d", minutes, secs);
        }
    }
}
