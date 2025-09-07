package com.HiWord9.RPRenames.util.config.favorite;

import com.HiWord9.RPRenames.util.config.generation.ParserHelper;
import com.HiWord9.RPRenames.util.rename.type.Rename;
import com.google.gson.*;
import net.minecraft.item.Item;

import java.lang.reflect.Type;

public class FavoriteRenameSerializer implements JsonSerializer<Rename>, JsonDeserializer<Rename> {

    @Override
    public JsonElement serialize(Rename rename, Type type, JsonSerializationContext context) {
        JsonObject result = new JsonObject();

        result.addProperty("name", rename.getName());

        return result;
    }

    @Override
    public Rename deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = jsonElement.getAsJsonObject();

        Item item = null;
        String jsonItem = context.deserialize(jsonObject.get("item"), String.class);
        if (jsonItem != null) item = ParserHelper.itemFromName(jsonItem);

        return new Rename(
                context.deserialize(jsonObject.get("name"), String.class),
                item
        );
    }
}
