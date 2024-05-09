package com.HiWord9.RPRenames.util.rename;

import com.HiWord9.RPRenames.util.config.generation.ParserHelper;
import com.google.gson.*;
import net.minecraft.item.Item;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Properties;

public class RenameSerializer implements JsonSerializer<Rename>, JsonDeserializer<Rename> {

    @Override
    public JsonElement serialize(Rename rename, Type type, JsonSerializationContext context) {
        JsonObject result = new JsonObject();

        result.addProperty("name", rename.name);
        result.add("items", serializedItems(rename.items));
        result.addProperty("packName", rename.packName);
        result.addProperty("path", rename.path);
        result.addProperty("stackSize", rename.stackSize);
        result.add("damage", context.serialize(rename.damage));
        result.addProperty("enchantment", rename.enchantment);
        result.addProperty("enchantmentLevel", rename.enchantmentLevel);
        result.add("properties", context.serialize(rename.properties));
        result.addProperty("description", rename.description);
        result.add("mob", context.serialize(rename.mob));

        return result;
    }

    @Override
    public Rename deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = jsonElement.getAsJsonObject();

        return new Rename(
                context.deserialize(jsonObject.get("name"), String.class),
                deserializedItems(jsonObject.get("items").getAsJsonArray()),
                context.deserialize(jsonObject.get("packName"), String.class),
                context.deserialize(jsonObject.get("path"), String.class),
                context.deserialize(jsonObject.get("stackSize"), Integer.class),
                context.deserialize(jsonObject.get("damage"), Rename.Damage.class),
                context.deserialize(jsonObject.get("enchantment"), String.class),
                context.deserialize(jsonObject.get("enchantmentLevel"), Integer.class),
                context.deserialize(jsonObject.get("properties"), Properties.class),
                context.deserialize(jsonObject.get("description"), String.class),
                context.deserialize(jsonObject.get("mob"), Rename.Mob.class)
        );
    }

    private JsonArray serializedItems(ArrayList<Item> items) {
        JsonArray jsonArray = new JsonArray();
        for (Item item : items) {
            jsonArray.add(ParserHelper.idFromItem(item));
        }
        return jsonArray;
    }

    private ArrayList<Item> deserializedItems(JsonArray jsonArray) {
        ArrayList<Item> items = new ArrayList<>();
        for (JsonElement jsonElement : jsonArray.asList()) {
            items.add(ParserHelper.itemFromName(jsonElement.getAsString()));
        }
        return items;
    }

    public static class MobSerializer implements JsonSerializer<Rename.Mob>, JsonDeserializer<Rename.Mob> {

        @Override
        public JsonElement serialize(Rename.Mob mob, Type type, JsonSerializationContext context) {
            JsonObject result = new JsonObject();

            result.addProperty("entity", mob.entity());
            result.addProperty("icon", serializedIcon(mob.icon()));
            result.add("properties", context.serialize(mob.properties()));
            result.addProperty("path", mob.path());

            return result;
        }

        @Override
        public Rename.Mob deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            return new Rename.Mob(
                    context.deserialize(jsonObject.get("entity"), String.class),
                    deserializedIcon(jsonObject.get("icon").getAsString()),
                    context.deserialize(jsonObject.get("properties"), Properties.class),
                    context.deserialize(jsonObject.get("path"), String.class)
            );
        }

        private String serializedIcon(Item icon) {
            return ParserHelper.idFromItem(icon);
        }

        private Item deserializedIcon(String simplifiedIcon) {
            return ParserHelper.itemFromName(simplifiedIcon);
        }
    }
}
