package ru.goldfinch.deathchest.chest;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import lombok.Getter;
import lombok.Setter;
import org.bson.Document;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import ru.goldfinch.deathchest.DeathChest;
import ru.goldfinch.deathchest.utils.ReflectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ChestObject {

    @Getter private final UUID owner;
    @Getter private final UUID uniqueId;
    @Getter private final String playerNickname;
    @Getter private final Location location;
    @Getter@Setter
    private int remainTime;
    @Getter private final List<ItemStack> items;

    @Getter private static final MongoCollection<Document> chestsCollection =
            DeathChest.getInstance().getMongo().getDatabase().getCollection("chests");

    public ChestObject(UUID owner, UUID uniqueId, String playerNickname, Location location, int remainTime, List<ItemStack> items) {
        this.owner = owner;
        this.uniqueId = uniqueId;
        this.playerNickname = playerNickname;
        this.location = location;
        this.remainTime = remainTime;
        this.items = items;
    }

    public ChestObject(UUID owner, String playerNickname, Location location, List<ItemStack> items) {
        this.owner = owner;
        this.uniqueId = UUID.randomUUID();
        this.playerNickname = playerNickname;
        this.location = location;
        this.remainTime = ChestsManager.DESPAWN_TIME;
        this.items = items;
    }

    public void save() {
        Document chestDocument = new Document();
        List<String> items = new ArrayList<>();

        this.items.forEach(itemStack -> {
            if (itemStack != null)
                if (!itemStack.getType().isAir())
                    items.add(ReflectionUtils.itemToStringBlob(itemStack));
        });

        chestDocument.put("_id", this.uniqueId.toString());
        chestDocument.put("owner", this.owner.toString());
        chestDocument.put("nickname", this.playerNickname);
        chestDocument.put("remainTime", this.remainTime);
        chestDocument.put("location", locationToDocument(location));
        chestDocument.put("items", items);
        chestsCollection.insertOne(chestDocument);
        ChestsManager.getPlayersChests().put(location, this);
    }

    public void update() {
        BasicDBObject query = new BasicDBObject();
        query.put("_id", uniqueId.toString());

        if (chestsCollection.find(query).first() == null)
            return;

        chestsCollection.findOneAndDelete(query);
        save();
    }

    public void remove() {
        BasicDBObject query = new BasicDBObject();
        query.put("_id", uniqueId.toString());

        if (chestsCollection.find(query).first() == null)
            return;

        chestsCollection.findOneAndDelete(query);
        ChestsManager.getOpenedChests().remove(owner);
        ChestsManager.getPlayersChests().remove(location);
    }

    public static Document locationToDocument(Location location) {
        Document document = new Document();
        document.put("world", location.getWorld().getName());
        document.put("x", location.getX());
        document.put("y", location.getY());
        document.put("z", location.getZ());

        return document;
    }



}