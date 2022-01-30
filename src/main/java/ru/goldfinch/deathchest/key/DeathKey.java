package ru.goldfinch.deathchest.key;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import ru.goldfinch.deathchest.DeathChest;
import ru.goldfinch.deathchest.utils.Colors;
import ru.goldfinch.deathchest.utils.ItemBuilder;

public class DeathKey {

    public static void register() {
        ShapelessRecipe shapelessRecipe = new ShapelessRecipe(new NamespacedKey(DeathChest.getInstance(), "death_key"), getAsItem());

        shapelessRecipe.addIngredient(Material.TOTEM_OF_UNDYING);
        shapelessRecipe.addIngredient(Material.EMERALD);

        Bukkit.addRecipe(shapelessRecipe);
    }

    public static ItemStack getAsItem() {
        return new ItemBuilder(Material.BONE)
                .setDisplayName(Colors.RED + "Death Key")
                .setGlow(true)
                .setCustomModelData(666)
                .setLoreLines(
                        Colors.GRAY + "Allows you to open",
                        Colors.GRAY + "others' Death Chests"
                )
                .build();
    }

    public static boolean isDeathKey(ItemStack itemStack) {
        if (itemStack == null)
            return false;

        if (itemStack.getType().isAir())
            return false;

        if (!itemStack.getType().equals(Material.BONE))
            return false;

        if (!itemStack.hasItemMeta())
            return false;

        if (!itemStack.getItemMeta().hasCustomModelData())
            return false;

        return itemStack.getItemMeta().getCustomModelData() == 666;
    }
}
