package com.HiWord9.RPRenames.util.rename;

import com.HiWord9.RPRenames.util.config.generation.ParserHelper;
import com.HiWord9.RPRenames.util.rename.type.AbstractRename;
import com.google.gson.*;
import net.minecraft.item.Item;

import java.lang.reflect.Type;

public class RenameSerializer implements JsonSerializer<AbstractRename>, JsonDeserializer<AbstractRename> {

    @Override
    public JsonElement serialize(AbstractRename rename, Type type, JsonSerializationContext context) {
        JsonObject result = new JsonObject();

        result.addProperty("name", rename.getName());
//        result.addProperty("item", ParserHelper.idFromItem(rename.getItem()));
//        result.addProperty("packName", rename.packName);

        return result;
    }

    @Override
    public AbstractRename deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = jsonElement.getAsJsonObject();

        Item item = null;
        String jsonItem = context.deserialize(jsonObject.get("item"), String.class);
        if (jsonItem != null) item = ParserHelper.itemFromName(jsonItem);

        return new AbstractRename(
                context.deserialize(jsonObject.get("name"), String.class),
                context.deserialize(jsonObject.get("packName"), String.class),
                null,
                null,
                item
        );
    }
}
