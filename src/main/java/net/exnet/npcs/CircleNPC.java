package net.exnet.npcs;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.DespawnReason;
import net.citizensnpcs.api.event.SpawnReason;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.trait.Equipment;
import net.citizensnpcs.trait.Gravity;
import net.citizensnpcs.trait.SkinTrait;
import net.exnet.CircleNPCPlugin;
import net.kyori.adventure.text.format.NamedTextColor;
import org.apache.commons.lang.RandomStringUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

import java.util.function.Function;

public class CircleNPC {

    public static final String SKIN_VALUE = "ewogICJ0aW1lc3RhbXAiIDogMTYwNTkxMDI2NjYwMiwKICAicHJvZmlsZUlkIiA6ICI5MWYwNGZlOTBmMzY0M2I1OGYyMGUzMzc1Zjg2ZDM5ZSIsCiAgInByb2ZpbGVOYW1lIiA6ICJTdG9ybVN0b3JteSIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS83NjQxZWM1MzQ2ODQ3MTZjYmRhZjdjNWYwOGVhZTYyODRkOWFmMThlNWU2MjNiZGNlNjg0NzQyYmQ0ZjQxYTZmIgogICAgfQogIH0KfQ==";
    public static final String SKIN_SIGNATURE = "toGqf3In+U6dUyQyxT0P6Eooj3hA1othpQiEGMZxN8fmYuftsA++BCC51DttFuw+RUQJ/in4YCBY66mILyp7Wyk+hNX4RETaWQSeJbvnaai80bzAouip7SNsB37cfFWwZXFv7sKr3XHd7MRDp5HjCG4FPEOpL/fXMWt3MqozS14TiwTj3APdBSjvw4xa1CprZIHNURNmPS5TuxzCqA0jnt6aQGqnNdx8v4+Brp7MQQV8EQMzPsB5CKA1ki5814PL7oRoAYlg9VpF1oSXO5Bs1zUuNt2xr1WF+p+D8RhjSCba+g0Xu6+MPVTaqUELU83NeQmhia9CHqmVUi93Snjm1puFKDB50TMScoTVs+KE8Dnpgzrl+Igmbu6N4hoatjvusjMX2QKk1Fhfj3C/0FO429cjBUX2h5cdtymMD0pragH7GfjVujRSYea4EXUykjGqo9cCE6+7DLZJnfB0e+wqT+7+//WNtXT0ZNXtM+2biqU8KiFIt06pQYa9yp3/Ta6h60jWyKiRZ0UAyk/kQ3oKA3KoHSgl1nexc6523t1ubrGuUsSZyaEhCRUMfS485/eqEwL7VMCPsjmWuKa2SYW3re1xtNsxQ1vVI9mXwK0Q0DQmaD9ptsk2COV/xdEqSLey/CdAX0RE+teeqVQXuh54UhF6IshG6I4iOntIePK4/wY=";

    public static final long TARGET_RUNNING_TIME = 1000L * 15L;
    public static final int SPEED_SCALE = 15;
    public static final double RADIUS = 2.75D;

    public CircleNPCPlugin plugin;
    public Player targetPlayer;

    public String citizensName;
    public NPC citizensNPC;

    public boolean running;
    public long startTime;
    public int stage;

    public CircleNPC(CircleNPCPlugin plugin, Player targetPlayer) {
        this.plugin = plugin;
        this.targetPlayer = targetPlayer;
    }

    public void start() {
        startTime = System.currentTimeMillis();
        running = true;
        spawnNPC();

        Scoreboard scoreboard = targetPlayer.getScoreboard();
        Team team = scoreboard.getTeam("circle");

        if (team == null) {
            team = scoreboard.registerNewTeam("circle");
            team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
            team.setAllowFriendlyFire(false);
            team.setCanSeeFriendlyInvisibles(false);
            team.color(NamedTextColor.GRAY);
        }

        team.addEntry(citizensName);
    }

    public void update() {
        if ((System.currentTimeMillis() - startTime) > TARGET_RUNNING_TIME || !isAlive() || !isSpawned()) {
            end();
            return;
        } else if (!isSpawned()) {
            return;
        }

        ++stage;

        Location teleportLocation = getNPCLocation();
        citizensNPC.teleport(teleportLocation, PlayerTeleportEvent.TeleportCause.PLUGIN);
        citizensNPC.faceLocation(targetPlayer.getLocation());
    }

    public void end() {
        running = false;
        destroyNPC();
    }

    public void spawnNPC() {
        if (isSpawned()) {
            return;
        }

        NPCRegistry registry = CitizensAPI.getNamedNPCRegistry("circle");
        citizensName = RandomStringUtils.randomAlphanumeric(16);
        citizensNPC = registry.createNPC(EntityType.PLAYER, citizensName);
        citizensNPC.setProtected(true);
        citizensNPC.setFlyable(true);
        citizensNPC.setAlwaysUseNameHologram(false);
        citizensNPC.setUseMinecraftAI(false);
        citizensNPC.addTrait(applyTraitBefore(new Gravity(), trait -> {
            trait.gravitate(false);
            return trait;
        }));
        applyTraitAfter(citizensNPC, new SkinTrait(), trait -> {
            trait.setFetchDefaultSkin(false);
            trait.setShouldUpdateSkins(true);
            trait.setSkinPersistent(citizensName, SKIN_SIGNATURE, SKIN_VALUE);
        });
        applyTraitAfter(citizensNPC, new Equipment(), trait -> {
            trait.set(Equipment.EquipmentSlot.HELMET, new ItemStack(Material.GOLDEN_HELMET));
            trait.set(Equipment.EquipmentSlot.BOOTS, new ItemStack(Material.LEATHER_BOOTS));
        });
        citizensNPC.spawn(targetPlayer.getLocation().clone().add(0, 16, 0), SpawnReason.PLUGIN);
    }

    public void destroyNPC() {
        if (!isSpawned()) {
            return;
        }

        citizensNPC.despawn(DespawnReason.PLUGIN);
        citizensNPC = null;
        citizensName = null;
        stage = 0;

        new BukkitRunnable() {
            @Override
            public void run() {
                plugin.circleNPCList.remove(CircleNPC.this);
            }
        }.runTask(plugin);
    }

    public Location getNPCLocation() {
        return calculationLocation(targetPlayer.getLocation().clone(), RADIUS, Math.toRadians((stage * SPEED_SCALE) % 360));
    }

    public Location calculationLocation(Location playerLocation, double radius, double angle) {
        Location location = new Location(playerLocation.getWorld(), playerLocation.getX(), playerLocation.getY(), playerLocation.getZ(), 0.0F, 0.0F);

        Vector directionVector = location.getDirection();
        Vector angleVector = directionVector.normalize().rotateAroundY(angle);
        Vector calcVector = angleVector.multiply(radius);

        return location.add(calcVector);
    }

    public <T extends Trait> T applyTraitBefore(T instance, Function<T, T> function) {
        return function.apply(instance);
    }

    public <T extends Trait> void applyTraitAfter(NPC npc, T instance, CallBack<T> callBack) {
        npc.addTrait(instance);
        callBack.callback(instance);
    }

    public boolean isSpawned() {
        return citizensNPC != null && citizensNPC.isSpawned();
    }

    public boolean isAlive() {
        return targetPlayer.isOnline();
    }

    public boolean isRunning() {
        return isAlive() && running;
    }

    public boolean equalsTarget(String playerName) {
        return targetPlayer.getName().equalsIgnoreCase(playerName);
    }

    private interface CallBack<T> {
        void callback(T t);
    }

}
