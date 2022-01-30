package ru.goldfinch.deathchest.data;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import ru.goldfinch.deathchest.DeathChest;

public class Mongo {

    @Getter private MongoClient mongoClient;
    @Getter private MongoDatabase database;

    public Mongo(JavaPlugin plugin) {
        String host = plugin.getConfig().getString("database.host");
        String login = plugin.getConfig().getString("database.login");
        String password = plugin.getConfig().getString("database.password");
        String databaseName = plugin.getConfig().getString("database.databaseName");
        int port = plugin.getConfig().getInt("database.port");

        String mongoClientURI;

        if (host == null || host.equalsIgnoreCase("localhost")) {
            mongoClientURI = "mongodb://localhost:27017";
        } else {
            mongoClientURI = "mongodb://" + login + ":" + password + "@" + host + ":" + port;
        }

        try {
            this.mongoClient = MongoClients.create(mongoClientURI);
        } catch (Exception e) {
            DeathChest.getInstance().getLogger().warning("Unsuccessful connection to MongoDB!");
            DeathChest.getInstance().getLogger().warning("Check credentials in plugin's config.yml");
            DeathChest.getInstance().getLogger().warning("Plugin disabled.");
            DeathChest.getInstance().getPluginLoader().disablePlugin(DeathChest.getInstance());
            return;
        }

        if (databaseName == null) {
            this.database = this.mongoClient.getDatabase("death_chests");
        } else {
            this.database = this.mongoClient.getDatabase(databaseName);
        }

    }

}
