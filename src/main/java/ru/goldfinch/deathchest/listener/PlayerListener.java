package ru.goldfinch.deathchest.listener;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import ru.goldfinch.deathchest.chest.ChestObject;
import ru.goldfinch.deathchest.chest.ChestsManager;
import ru.goldfinch.deathchest.key.DeathKey;
import ru.goldfinch.deathchest.utils.Colors;

import java.util.ArrayList;
import java.util.List;

public class PlayerListener implements Listener {

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Player player = e.getEntity();
        Location location = player.getLocation();

        if (player.getInventory().isEmpty())
            return;

        location.getBlock().setType(Material.CHEST);

        List<ItemStack> items = new ArrayList<>();
        for (ItemStack itemStack : player.getInventory()) {
            if (itemStack == null)
                continue;

            if (itemStack.getType().isAir())
                continue;

            items.add(itemStack);
        }

        ChestObject chestObject = new ChestObject(player.getUniqueId(), player.getName(), location.getBlock().getLocation(), items);
        chestObject.save();
        e.getDrops().clear();

        Block blockSign = location.getBlock().getRelative(BlockFace.UP);
        blockSign.setType(Material.OAK_SIGN);

        Sign sign = (Sign) blockSign.getState();
        sign.setLine(1, chestObject.getPlayerNickname() + "'s");
        sign.setLine(2, "Death Chest");
        sign.update();

        player.sendMessage("");
        player.sendMessage("");
        player.sendMessage(Colors.RED + "Death Chest, with you death loot is waiting for you:");
        player.sendMessage(Colors.GRAY + "World: " + Colors.WHITE + chestObject.getLocation().getWorld().getName());
        player.sendMessage(Colors.GRAY + "X: " + Colors.WHITE + Math.round(chestObject.getLocation().getX()));
        player.sendMessage(Colors.GRAY + "Y: " + Colors.WHITE + Math.round(chestObject.getLocation().getY()));
        player.sendMessage(Colors.GRAY + "Z: " + Colors.WHITE + Math.round(chestObject.getLocation().getZ()));
        player.sendMessage(Colors.RED + "It will be dropped after " + Colors.WHITE + Math.round(ChestsManager.DESPAWN_TIME / 60F) + Colors.RED + " minutes!");
        player.sendMessage(Colors.RED + "Other players will be notified about your loot after " + Colors.WHITE + Math.round(ChestsManager.ALL_PLAYERS_ACCESS_TIME / 60F) + Colors.RED + " minutes!");
        player.sendMessage("");
        player.sendMessage("");

    }

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        Block block = e.getBlock();

        ChestObject chestObject = ChestsManager.getPlayersChests().get(block.getLocation());

        if (chestObject == null)
            return;

        e.setCancelled(true);
    }

    @EventHandler
    public void onDamage(EntityExplodeEvent e) {
        e.blockList().removeIf(block -> ChestsManager.getPlayersChests().get(block.getLocation()) != null || (block.getType() == Material.OAK_SIGN && ((Sign)block.getState()).getLine(2).equals("Death Chest")));
    }

    @EventHandler
    public void onChestOpen(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        Block block = e.getClickedBlock();

        if (block == null)
            return;

        if (!block.getType().equals(Material.CHEST))
            return;

        Location location = block.getLocation();

        ChestObject chestObject = ChestsManager.getPlayersChests().get(location);

        if (chestObject == null)
            return;

        e.setCancelled(true);

        if (chestObject.getOwner() != player.getUniqueId()) {
            ItemStack itemStack = e.getItem();

            if (!DeathKey.isDeathKey(itemStack)) {
                player.sendMessage("");
                player.sendMessage("");
                player.sendMessage(Colors.RED + "This is not your Death Chest!");
                player.sendMessage(Colors.GRAY + "[?] You can open others' Death Chests, by using " + Colors.WHITE + "Death Key");
                player.sendMessage(Colors.GRAY + "You can craft it with " + Colors.WHITE + "an emerald " + Colors.GRAY + "&" + Colors.WHITE + " a Totem of Undying");
                player.sendMessage("");
                player.sendMessage("");
                return;
            } else {
                player.getItemInHand().subtract();
            }

            Player owner = Bukkit.getPlayer(chestObject.getOwner());

            if (owner != null) {
                owner.sendMessage("");
                owner.sendMessage("");
                owner.sendMessage(Colors.RED + "Player " + player.getName() + " has opened your Death Chest, on this coords:");
                owner.sendMessage(Colors.GRAY + "World: " + Colors.WHITE + chestObject.getLocation().getWorld().getName());
                owner.sendMessage(Colors.GRAY + "X: " + Colors.WHITE + Math.round(chestObject.getLocation().getX()));
                owner.sendMessage(Colors.GRAY + "Y: " + Colors.WHITE + Math.round(chestObject.getLocation().getY()));
                owner.sendMessage(Colors.GRAY + "Z: " + Colors.WHITE + Math.round(chestObject.getLocation().getZ()));
                owner.sendMessage("");
                owner.sendMessage("");
            }

        }

        location.getWorld().playSound(location, Sound.BLOCK_CHEST_OPEN, 1.0F, 1.0F);

        Inventory inventory = Bukkit.createInventory(null, 36, "Death Chest of " + player.getName());

        for (ItemStack item : chestObject.getItems())
            inventory.addItem(item);

        player.openInventory(inventory);
        ChestsManager.getPlayersChests().remove(location);
        ChestsManager.getOpenedChests().put(player.getUniqueId(), chestObject);
    }

    @EventHandler
    public void onChestClose(InventoryCloseEvent e) {
        Player player = (Player) e.getPlayer();
        Inventory inventory = e.getInventory();

        if (!e.getView().getTitle().startsWith("Death Chest of"))
            return;

        if (ChestsManager.getOpenedChests().containsKey(player.getUniqueId())) {
            ChestObject chestObject = ChestsManager.getOpenedChests().get(player.getUniqueId());
            Location location = chestObject.getLocation();

            location.getBlock().getRelative(BlockFace.UP).setType(Material.AIR);
            location.getBlock().setType(Material.AIR);

            if (!inventory.isEmpty()) {
                inventory.forEach(itemStack -> {
                    if (itemStack != null)
                        if (!itemStack.getType().isAir())
                            location.getWorld().dropItemNaturally(location, itemStack);
                });
            }

            location.getWorld().playSound(location, Sound.BLOCK_WOOD_BREAK, 1.0F, 1.0F);

            chestObject.remove();
        }
    }
}
