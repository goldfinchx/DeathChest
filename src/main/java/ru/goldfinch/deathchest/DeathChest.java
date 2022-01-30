package ru.goldfinch.deathchest;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import ru.goldfinch.deathchest.chest.ChestsManager;
import ru.goldfinch.deathchest.data.Mongo;
import ru.goldfinch.deathchest.key.DeathKey;
import ru.goldfinch.deathchest.listener.PlayerListener;

public final class DeathChest extends JavaPlugin {

    @Getter
    private static DeathChest instance;
    @Getter private Mongo mongo;

    @Override
    public void onEnable() {
        instance = this;

        this.saveDefaultConfig();
        this.saveConfig();

        this.mongo = new Mongo(this);

        if (this.mongo.getMongoClient() == null)
            return;

        Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);

        ChestsManager.loadUpChests();
        ChestsManager.runController();

        DeathKey.register();

        this.getLogger().info("Plugin Enabled!");
    }

    @Override
    public void onDisable() {
        ChestsManager.getPlayersChests().forEach((location, chestObject) -> chestObject.update());

        if (this.mongo != null && this.mongo.getMongoClient() != null)
            this.mongo.getMongoClient().close();

        Bukkit.getScheduler().cancelTasks(this);

        this.getLogger().info("Plugin Disabled!");
    }

}
