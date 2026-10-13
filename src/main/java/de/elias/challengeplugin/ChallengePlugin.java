package de.elias.challengeplugin;

import de.elias.challengeplugin.api.ChallengeManager;
import de.elias.challengeplugin.challenges.ChunkBlockChallenge;
import de.elias.challengeplugin.commands.ChallengesCommand;
import de.elias.challengeplugin.gui.MainMenuHolder;
import de.elias.challengeplugin.gui.TimerSettingsHolder;
import de.elias.challengeplugin.timer.ChallengeTimer;
import de.elias.challengeplugin.timer.TimerCommand;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class ChallengePlugin extends JavaPlugin {

    private ChallengeManager challengeManager;
    private ChallengeTimer challengeTimer;

    @Override
    public void onEnable() {
        // Initialisiere Manager & Timer
        this.challengeManager = new ChallengeManager(this);
        this.challengeTimer = new ChallengeTimer(this);

        // Registriere Commands
        if (getCommand("challenges") != null) {
            getCommand("challenges").setExecutor(new ChallengesCommand(challengeManager, challengeTimer));
        }
        if (getCommand("timer") != null) {
            getCommand("timer").setExecutor(new TimerCommand(challengeTimer));
        }

        // Registriere die erste Challenge (Chunk Block Challenge)
        ChunkBlockChallenge chunkBlockChallenge = new ChunkBlockChallenge();
        challengeManager.registerChallenge(chunkBlockChallenge);

        // Registriere Event-Listener für Hauptmenü und Timer-Einstellungen
        Bukkit.getPluginManager().registerEvents(new MainMenuHolder(challengeManager, challengeTimer), this);
        Bukkit.getPluginManager().registerEvents(new TimerSettingsHolder(challengeManager, challengeTimer), this);

        getLogger().info("ChallengePlugin wurde erfolgreich aktiviert, Elias!");
    }

    @Override
    public void onDisable() {
        if (challengeTimer != null) {
            challengeTimer.pause();
        }
        if (challengeManager != null) {
            challengeManager.stopAllChallenges();
        }
        getLogger().info("ChallengePlugin wurde deaktiviert.");
    }
}
