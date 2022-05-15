package net.exnet;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.MemoryNPCDataStore;
import net.exnet.npcs.CircleNPC;
import net.exnet.runnables.UpdateForNPCRunnable;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class CircleNPCPlugin extends JavaPlugin {

    public final List<CircleNPC> circleNPCList = new LinkedList<>();

    @Override
    public void onEnable() {
        registerCitizensAPI();

        new UpdateForNPCRunnable(this).runTaskTimer(this, 0L, 0L);
    }

    @Override
    public void onDisable() {
        removeAll();
        unregisterCitizensAPI();
    }

    public void registerCitizensAPI() {
        if (CitizensAPI.getNamedNPCRegistry("circle") == null)
            CitizensAPI.createNamedNPCRegistry("circle", new MemoryNPCDataStore());
    }

    public void unregisterCitizensAPI() {
        if (CitizensAPI.getNamedNPCRegistry("circle") != null)
            CitizensAPI.removeNamedNPCRegistry("circle");
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 1) {
            sender.sendMessage("/" + label + " spawn <player> - 플레이어를 소환합니다.");
            sender.sendMessage("/" + label + " removeAll - NPC를 모두 지웁니다.");
            return true;
        }

        switch (args[0].toLowerCase(Locale.ENGLISH)) {
            case "spawn" -> {
                if (args.length < 2) {
                    sender.sendMessage(Component.text("대상 닉네임을 입력해주세요.").color(TextColor.color(255, 0, 0)));
                    break;
                }

                for (int i = 1; i < args.length; i++) {
                    String playerName = args[i];
                    Player targetPlayer = getServer().getPlayer(playerName);

                    if (targetPlayer == null) {
                        sender.sendMessage(Component.text("'" + playerName + "'라는 플레이어를 찾을 수 없습니다").color(TextColor.color(255, 0, 0)));
                        continue;
                    } else if (!isRunnable(targetPlayer)) {
                        sender.sendMessage(Component.text(targetPlayer.getName() + "(은)는 이미 실행중입니다.").color(TextColor.color(255, 0, 0)));
                        continue;
                    }

                    CircleNPC npc = new CircleNPC(this, targetPlayer);
                    npc.start();

                    circleNPCList.add(npc);
                    sender.sendMessage(Component.text("'" + playerName + "'에게 NPC를 소환하였습니다.").color(TextColor.color(0, 255, 0)));
                }
            }
            case "removeall" -> {
                removeAll();
                sender.sendMessage(Component.text("모든 NPC를 제거하였습니다.").color(TextColor.color(0, 255, 0)));
            }
            default -> sender.sendMessage(Component.text("잘못된 사용법입니다.").color(TextColor.color(255, 0, 0)));
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return List.of("spawn", "removeAll");
        }
        return getServer().getOnlinePlayers().stream().filter(this::isRunnable).map(Player::getName).collect(Collectors.toList());
    }

    public void removeAll() {
        circleNPCList.forEach(CircleNPC::end);
        circleNPCList.clear();
    }

    public boolean isRunnable(Player player) {
        return circleNPCList.stream().noneMatch(npc -> npc.equalsTarget(player.getName()));
    }

}
