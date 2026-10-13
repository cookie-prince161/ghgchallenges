package de.elias.challengeplugin.timer;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public class ChallengeTimer {

    private final JavaPlugin plugin;
    private long seconds = 0;
    private boolean running = false;
    private boolean countdown = false;
    private long countdownTime = 3600; // Standard 1 Stunde, wenn Countdown aktiv
    private BukkitTask task;

    public ChallengeTimer(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void start() {
        if (running) return;
        running = true;

        task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!running) return;

            if (countdown) {
                if (seconds > 0) {
                    seconds--;
                } else {
                    // Timer abgelaufen!
                    Bukkit.broadcast(Component.text("Der Countdown ist abgelaufen!", NamedTextColor.RED));
                    // Hier könnte man optional die Challenge stoppen
                }
            } else {
                seconds++;
            }

            // Zeige die Zeit in der Actionbar für alle Spieler an
            String timeFormatted = formatTime(seconds);
            Component barComponent = Component.text("⏱ Timer: " + timeFormatted, NamedTextColor.GOLD);
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.sendActionBar(barComponent);
            }
        }, 0L, 20L); // Jene Sekunde (20 Ticks)
    }

    public void pause() {
        running = false;
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    public void resume() {
        start();
    }

    public void reset() {
        pause();
        this.seconds = countdown ? countdownTime : 0;
    }

    public boolean isRunning() {
        return running;
    }

    public long getSeconds() {
        return seconds;
    }

    public void setSeconds(long seconds) {
        this.seconds = seconds;
    }

    public boolean isCountdown() {
        return countdown;
    }

    public void setCountdown(boolean countdown) {
        this.countdown = countdown;
    }

    public long getCountdownTime() {
        return countdownTime;
    }

    public void setCountdownTime(long countdownTime) {
        this.countdownTime = countdownTime;
        if (countdown) {
            this.seconds = countdownTime;
        }
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
