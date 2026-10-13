package de.elias.challengeplugin.api;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ChallengeManager {

    private final JavaPlugin plugin;
    private final Map<String, Challenge> challenges = new HashMap<>();

    public ChallengeManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void registerChallenge(Challenge challenge) {
        challenges.put(challenge.getName().toLowerCase(), challenge);
        // Registriert die Challenge automatisch als Event-Listener in Bukkit
        Bukkit.getPluginManager().registerEvents(challenge, plugin);
    }

    public Challenge getChallenge(String name) {
        return challenges.get(name.toLowerCase());
    }

    public Collection<Challenge> getChallenges() {
        return challenges.values();
    }

    public void stopAllChallenges() {
        for (Challenge challenge : challenges.values()) {
            if (challenge.isEnabled()) {
                challenge.setEnabled(false);
                challenge.onStop();
            }
        }
    }
}
