package net.exnet.runnables;

import net.exnet.CircleNPCPlugin;
import net.exnet.npcs.CircleNPC;
import org.bukkit.scheduler.BukkitRunnable;

public class UpdateForNPCRunnable extends BukkitRunnable {

    private final CircleNPCPlugin plugin;

    public UpdateForNPCRunnable(CircleNPCPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        plugin.circleNPCList.forEach(CircleNPC::update);
    }

}
