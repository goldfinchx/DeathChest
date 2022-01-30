package ru.goldfinch.deathchest.chest;

import lombok.Getter;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.goldfinch.deathchest.DeathChest;
import ru.goldfinch.deathchest.utils.Colors;
import ru.goldfinch.deathchest.utils.ReflectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class ChestsManager {
    public final static int DESPAWN_TIME = DeathChest.getInstance().getConfig().getInt("despawn-cooldown");
    public final static int ALL_PLAYERS_ACCESS_TIME = DeathChest.getInstance().getConfig().getInt("all-players-access");
    @Getter
    private static final HashMap<Location, ChestObject> playersChests = new HashMap<>();
    @Getter private static final HashMap<UUID, ChestObject> openedChests = new HashMap<>();

    public static void runController() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(DeathChest.getInstance(), () -> playersChests.forEach((location, chestObject) -> {
            Player player = Bukkit.getPlayer(chestObject.getOwner());
            chestObject.setRemainTime(chestObject.getRemainTime()-1);

            if (chestObject.getRemainTime() == 0) {
                location.getBlock().setType(Material.AIR);
                location.getWorld().playSound(location, Sound.BLOCK_WOOD_BREAK, 1.0F, 1.0F);

                if (!chestObject.getItems().isEmpty()) {
                    chestObject.getItems().forEach(itemStack -> {
                        if (itemStack != null)
                            if (!itemStack.getType().isAir())
                                location.getWorld().dropItemNaturally(location, itemStack);
                    });
                }

                chestObject.remove();
                return;
            }

            if (chestObject.getRemainTime() == ALL_PLAYERS_ACCESS_TIME) {
                Bukkit.getOnlinePlayers().stream().filter(onlinePlayer -> !onlinePlayer.getUniqueId().equals(chestObject.getOwner())).forEach(onlinePlayer -> {
                    onlinePlayer.sendMessage("");
                    onlinePlayer.sendMessage("");
                    onlinePlayer.sendMessage(Colors.RED + chestObject.getPlayerNickname() + "' Death Chest is waiting for you:");
                    onlinePlayer.sendMessage(Colors.GRAY + "World: " + Colors.WHITE + chestObject.getLocation().getWorld().getName());
                    onlinePlayer.sendMessage(Colors.GRAY + "X: " + Colors.WHITE + Math.round(chestObject.getLocation().getBlockX()));
                    onlinePlayer.sendMessage(Colors.GRAY + "Y: " + Colors.WHITE + Math.round(chestObject.getLocation().getBlockY()));
                    onlinePlayer.sendMessage(Colors.GRAY + "Z: " + Colors.WHITE + Math.round(chestObject.getLocation().getBlockZ()));
                    onlinePlayer.sendMessage(Colors.GRAY + "[?] You can open it with " + Colors.WHITE + "Death Key");
                    onlinePlayer.sendMessage(Colors.GRAY + "You can craft it with " + Colors.WHITE + "an emerald " + Colors.GRAY + "&" + Colors.WHITE + " a Totem of Undying");
                    onlinePlayer.sendMessage("");
                    onlinePlayer.sendMessage("");

                    onlinePlayer.playSound(onlinePlayer.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.2F, 0.2F);
                });

                if (player != null) {
                    player.sendMessage("");
                    player.sendMessage("");
                    player.sendMessage(Colors.RED + "Other players have been notified about your Death Chest!");
                    player.sendMessage("");
                    player.sendMessage("");

                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.2F, 0.2F);
                }
            }

            if (chestObject.getRemainTime() % 60 == 0) {
                if (player == null)
                    return;

                player.sendMessage("");
                player.sendMessage("");
                player.sendMessage(Colors.RED + "Death Chest, with you death loot is waiting for you:");
                player.sendMessage(Colors.GRAY + "World: " + Colors.WHITE + chestObject.getLocation().getWorld().getName());
                player.sendMessage(Colors.GRAY + "X: " + Colors.WHITE + Math.round(chestObject.getLocation().getX()));
                player.sendMessage(Colors.GRAY + "Y: " + Colors.WHITE + Math.round(chestObject.getLocation().getY()));
                player.sendMessage(Colors.GRAY + "Z: " + Colors.WHITE + Math.round(chestObject.getLocation().getZ()));
                player.sendMessage(Colors.RED + "It will be dropped after " + Colors.WHITE + (chestObject.getRemainTime() / 60) + Colors.RED + " minutes!");
                if (chestObject.getRemainTime() > ALL_PLAYERS_ACCESS_TIME)
                    player.sendMessage(Colors.RED + "Other players will be notified about your loot after " + Colors.WHITE + Math.round((chestObject.getRemainTime()- ALL_PLAYERS_ACCESS_TIME) / 60F) + Colors.RED + " minutes!");
                player.sendMessage("");
                player.sendMessage("");

                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.2F, 0.2F);
            }

        }), 20L, 20L);

    }

    public static void loadUpChests() {
        for (Document document : ChestObject.getChestsCollection().find()) {
            Document locationDocument = document.get("location", Document.class);

            UUID owner = UUID.fromString(document.getString("owner"));
            UUID uuid = UUID.fromString(document.getString("_id"));
            String nickname = document.getString("nickname");
            int remainTime = document.getInteger("remainTime");

            Location location = new Location(
                    Bukkit.getWorld(locationDocument.getString("world")),
                    locationDocument.getDouble("x"),
                    locationDocument.getDouble("y"),
                    locationDocument.getDouble("z"));

            List<ItemStack> items = new ArrayList<>();
            document.getList("items", String.class).forEach(string -> items.add(ReflectionUtils.stringBlobToItem(string)));

            ChestObject chestObject = new ChestObject(owner, uuid, nickname, location, remainTime, items);

            ChestsManager.getPlayersChests().put(location, chestObject);
        }
    }

}
