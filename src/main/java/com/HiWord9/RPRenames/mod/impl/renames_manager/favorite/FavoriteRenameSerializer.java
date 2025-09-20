package com.HiWord9.RPRenames.mod.impl.renames_manager.favorite;

import com.HiWord9.RPRenames.mod.util.ParserHelper;
import com.google.gson.*;
import net.minecraft.item.Item;

import java.lang.reflect.Type;

public class FavoriteRenameSerializer implements JsonSerializer<FavoriteRename>, JsonDeserializer<FavoriteRename> {

    @Override
    public JsonElement serialize(FavoriteRename rename, Type type, JsonSerializationContext context) {
        JsonObject result = new JsonObject();

        result.addProperty("name", rename.getName());

        return result;
    }

    @Override
    public FavoriteRename deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = jsonElement.getAsJsonObject();

        Item item = null;
        String jsonItem = context.deserialize(jsonObject.get("item"), String.class);
        if (jsonItem != null) item = ParserHelper.itemFromId(jsonItem);

        return new FavoriteRename(
                context.deserialize(jsonObject.get("name"), String.class),
                item
        );
    }
}
