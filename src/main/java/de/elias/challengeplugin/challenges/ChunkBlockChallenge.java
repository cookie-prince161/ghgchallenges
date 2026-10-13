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
    private final List<Material> allBlockMaterials = new ArrayList<>();

    public ChunkBlockChallenge() {
        // Lädt dynamisch JEDEN platzierbaren Block aus ganz Minecraft (Zäune, Falltüren, Prismarin, etc.)
        for (Material material : Material.values()) {
            if (material.isBlock() && !material.isAir() && material != Material.BEDROCK && material != Material.BARRIER) {
                allBlockMaterials.add(material);
            }
        }
    }

    @Override
    public String getName() {
        return "Chunk Block Challenge";
    }

    @Override
    public String getDescription() {
        return "Jeder neue Chunk wird mit einem komplett zufälligen Minecraft-Block gefüllt!";
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
    public boolean isEnabled() { return enabled; }

    @Override
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

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
        player.sendMessage(Component.text("⚙️ Die Chunk Block Challenge hat derzeit keine Untereinstellungen.", NamedTextColor.YELLOW));
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!enabled) return;

        Chunk fromChunk = event.getFrom().getChunk();
        Chunk toChunk = event.getTo().getChunk();

        if (fromChunk.getX() != toChunk.getX() || fromChunk.getZ() != toChunk.getZ()) {
            long chunkKey = getChunkKey(toChunk.getX(), toChunk.getZ());

            if (!modifiedChunks.contains(chunkKey)) {
                modifiedChunks.add(chunkKey);
                transformChunk(toChunk);
                // Keine nervige Actionbar-Meldung mehr!
            }
        }
    }

    private void transformChunk(Chunk chunk) {
        if (allBlockMaterials.isEmpty()) return;
        Material randomMaterial = allBlockMaterials.get(random.nextInt(allBlockMaterials.size()));

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
                        block.setType(randomMaterial, false);
                    }
                }
            }
        }
    }

    private long getChunkKey(int x, int z) {
        return ((long) x << 32) | (z & 0xFFFFFFFFL);
    }
}
