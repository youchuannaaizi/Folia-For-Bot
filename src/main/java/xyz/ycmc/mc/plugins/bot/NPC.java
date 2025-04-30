package xyz.ycmc.mc.plugins.bot;

import com.destroystokyo.paper.profile.PlayerProfile;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import java.util.UUID;

public class NPC {
    private ArmorStand entity;
    private PlayerProfile playerProfile;
    private boolean isProtected;
    private Location spawnLocation;

    public void setProtected(boolean b) {
        this.isProtected = b;
        if (entity != null) {
            entity.setInvulnerable(b);
            entity.setSilent(true);
            entity.setCollidable(false);
        }
    }

    public void setPlayerProfile(PlayerProfile profile) {
        this.playerProfile = profile;
        if (entity != null && profile != null) {
            entity.setCustomName(profile.getName());
            entity.setCustomNameVisible(true);
        }
    }

    public void setSpawnLocation(Location spawnLoc) {
        this.spawnLocation = spawnLoc.clone();
    }

    public void spawn(Location location) {
        if (location.getWorld() == null) return;

        this.entity = location.getWorld().spawn(location, ArmorStand.class);
        entity.setVisible(false);
        entity.setGravity(false);
        entity.setBasePlate(false);
        entity.setArms(false);
        entity.setCanPickupItems(false);
        entity.setVelocity(new Vector(0, 0, 0));

        setProtected(this.isProtected);
        setPlayerProfile(this.playerProfile);
    }

    public LivingEntity getEntity() {
        return entity;
    }

    public UUID getUniqueId() {
        return entity != null ? entity.getUniqueId() : null;
    }

    public void destroy() {
        if (entity != null && !entity.isDead()) {
            entity.remove();
        }
    }
}