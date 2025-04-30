package xyz.ycmc.mc.plugins.bot;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class BotManager {
    private final JavaPlugin plugin;
    private final Map<String, BotData> bots = new ConcurrentHashMap<>();
    private final Map<UUID, BotData> npcUuidToBotData = new ConcurrentHashMap<>();
    private static final String STEVE_SKIN = "ewogICJ0aW1lc3RhbXAiIDogMTY5MjA0OTM0OTQxOCwKICAicHJvZmlsZUlkIiA6ICI5Y2VkYjQwNzRhYzQ0M2M5YjQ4N2Y3N2JmYjQ1YjM3YiIsCiAgInByb2ZpbGVOYW1lIiA6ICJTVEVWRV9TS0lOX1RFWFQiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzhkMWU3MWVkM2Q4ZGU0Y2JhYjY2OTI4YjE3Y2Y0YzA3YjlmY2ViY2Q5YjUzYmUyMjFjZDEzZTA1MTQwYjQyIgogICAgfQogIH0KfQ==";

    public BotManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public CompletableFuture<Boolean> spawnBot(Player owner, String botName) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    String compositeKey = owner.getUniqueId() + ":" + botName;
                    if (bots.containsKey(compositeKey)) {
                        throw new IllegalArgumentException("Bot name '" + botName + "' already exists");
                    }

                    Location spawnLoc = owner.getLocation().clone();
                    Chunk chunk = spawnLoc.getChunk();
                    if (!chunk.isLoaded()) chunk.load();

                    NPC npc = new NPC();
                    npc.setPlayerProfile(createSteveProfile(UUID.randomUUID(), botName));
                    npc.setProtected(true);
                    npc.setSpawnLocation(spawnLoc);
                    npc.spawn(spawnLoc);

                    BotData data = new BotData(npc, owner.getUniqueId(), botName, chunk);
                    bots.put(compositeKey, data);
                    npcUuidToBotData.put(npc.getUniqueId(), data);
                    future.complete(true);
                } catch (Exception e) {
                    plugin.getLogger().severe("Spawn bot failed: " + e.getMessage());
                    e.printStackTrace();
                    future.complete(false);
                }
            }
        }.runTask(plugin);
        return future;
    }

    public boolean removeBot(Player owner, String botName) {
        String key = owner.getUniqueId() + ":" + botName;
        BotData data = bots.remove(key);
        if (data != null) {
            npcUuidToBotData.remove(data.npc.getUniqueId());
            data.npc.destroy();
            return true;
        }
        return false;
    }

    public boolean removeBotByUuid(UUID npcUuid) {
        BotData data = npcUuidToBotData.remove(npcUuid);
        if (data != null) {
            bots.remove(data.owner + ":" + data.botName);
            data.npc.destroy();
            return true;
        }
        return false;
    }

    public void removeAllBots() {
        bots.values().forEach(data -> data.npc.destroy());
        bots.clear();
        npcUuidToBotData.clear();
    }

    public void removeBotsInChunk(Chunk chunk) {
        List<String> toRemove = new ArrayList<>();
        bots.forEach((key, data) -> {
            if (data.chunk.equals(chunk)) {
                data.npc.destroy();
                npcUuidToBotData.remove(data.npc.getUniqueId());
                toRemove.add(key);
            }
        });
        toRemove.forEach(bots::remove);
    }

    public boolean isBot(UUID uuid) {
        return npcUuidToBotData.containsKey(uuid);
    }

    public Collection<String> getBotNames(UUID ownerId) {
        List<String> names = new ArrayList<>();
        bots.forEach((key, data) -> {
            if (key.startsWith(ownerId.toString())) {
                names.add(data.botName);
            }
        });
        return names;
    }

    private PlayerProfile createSteveProfile(UUID uuid, String name) {
        PlayerProfile profile = Bukkit.createProfile(uuid, name);
        profile.setProperty(new ProfileProperty("textures", STEVE_SKIN));
        return profile;
    }

    private static class BotData {
        private final NPC npc;
        private final UUID owner;
        private final String botName;
        private final Chunk chunk;

        public BotData(NPC npc, UUID owner, String botName, Chunk chunk) {
            this.npc = npc;
            this.owner = owner;
            this.botName = botName;
            this.chunk = chunk;
        }
    }
}