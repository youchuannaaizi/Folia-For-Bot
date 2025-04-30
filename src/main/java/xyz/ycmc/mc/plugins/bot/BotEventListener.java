package xyz.ycmc.mc.plugins.bot;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import java.util.UUID;

public class BotEventListener implements Listener {
    private final BotManager botManager;

    public BotEventListener(BotManager botManager) {
        this.botManager = botManager;
    }

    @EventHandler
    public void onBotDamage(EntityDamageEvent event) {
        if (botManager.isBot(event.getEntity().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBotDeath(EntityDeathEvent event) {
        UUID entityUuid = event.getEntity().getUniqueId();
        if (botManager.isBot(entityUuid)) {
            event.getDrops().clear();
            botManager.removeBotByUuid(entityUuid);
        }
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        botManager.removeBotsInChunk(event.getChunk());
    }
}