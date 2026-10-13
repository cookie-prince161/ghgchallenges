package de.elias.challengeplugin.challenges;

import de.elias.challengeplugin.api.Challenge;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class ChunkBlockChallenge implements Challenge {

    private boolean enabled = false;
    private final Set<Long> modifiedChunks = new HashSet<>();
    private final Random random = new Random();

    // Liste von möglichen Blöcken, die zufällig gewählt werden können
    private final List<Material> allowedBlocks = List.of(
            Material.STONE, Material.DIRT, Material.OAK_LOG, Material.DIAMOND_BLOCK,
            Material.TNT, Material.WATER, Material.LAVA, Material.SAND,
            Material.GLASS, Material.OBSIDIAN, Material.BRICKS, Material.MELON
    );

    @Override
    public String getName() {
        return "Chunk Block Challenge";
    }

    @Override
    public String getDescription() {
        return "Jeder neue Chunk wird mit einem zufälligen Block gefüllt!";
    }

    @Override
    public ItemStack getGuiIcon() {
        ItemStack icon = new ItemStack(Material.GRASS_BLOCK);
        ItemMeta meta = icon.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("🌱 Chunk Block Challenge", NamedTextColor.GREEN));
        }
        icon.setItemMeta(meta);
        return icon;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public void onStart() {
        modifiedChunks.clear();
        Bukkit.broadcast(Component.text("⚡ Chunk Block Challenge gestartet!", NamedTextColor.GOLD));
    }

    @Override
    public void onStop() {
        modifiedChunks.clear();
        Bukkit.broadcast(Component.text("🛑 Chunk Block Challenge beendet!", NamedTextColor.RED));
    }

    @Override
    public void openSettingsMenu(Player player) {
        player.sendMessage(Component.text("⚙️ Die Chunk Block Challenge hat derzeit keine Unterschriften-Einstellungen.", NamedTextColor.YELLOW));
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!enabled) return;

        Chunk fromChunk = event.getFrom().getChunk();
        Chunk toChunk = event.getTo().getChunk();

        // Prüfen, ob der Spieler einen neuen Chunk betreten hat
        if (fromChunk.getX() != toChunk.getX() || fromChunk.getZ() != toChunk.getZ()) {
            long chunkKey = getChunkKey(toChunk.getX(), toChunk.getZ());

            // Wenn dieser Chunk noch nicht modifiziert wurde
            if (!modifiedChunks.contains(chunkKey)) {
                modifiedChunks.add(chunkKey);
                transformChunk(toChunk);
                
                Player player = event.getPlayer();
                player.sendActionBar(Component.text("✨ Neuer Chunk transformiert!", NamedTextColor.LIGHT_PURPLE));
                player.playSound(player.getLocation(), Sound.ENTITY_ENDER_EYE_DEATH, 0.5f, 1.0f);
            }
        }
    }

    private void transformChunk(Chunk chunk) {
        Material randomMaterial = allowedBlocks.get(random.nextInt(allowedBlocks.size()));

        // Ersetzt Blöcke im Chunk (zur Performance-Schonung in einer bestimmten Y-Höhe, z.B. oberhalb von y=0 bis y=100)
        int minX = chunk.getX() * 16;
        int minZ = chunk.getZ() * 16;
        int maxX = minX + 16;
        int maxZ = minZ + 16;

        org.bukkit.World world = chunk.getWorld();
        for (int x = minX; x < maxX; x++) {
            for (int z = minZ; z < maxZ; z++) {
                for (int y = world.getMinHeight(); y < 100; y++) {
                    Block block = world.getBlockAt(x, y, z);
                    if (block.getType() != Material.AIR && block.getType() != Material.BEDROCK) {
                        block.setType(randomMaterial, false); // false = kein sofortiges Block-Physics-Update für bessere Performance
                    }
                }
            }
        }
    }

    private long getChunkKey(int x, int z) {
        return ((long) x << 32) | (z & 0xFFFFFFFFL);
    }
}
