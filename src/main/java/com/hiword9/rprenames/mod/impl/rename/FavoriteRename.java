package com.hiword9.rprenames.mod.impl.rename;

import com.hiword9.rprenames.api.core.rename.Rename;
import com.hiword9.rprenames.mod.item_group.ItemGroupComponent;
import com.hiword9.rprenames.mod.util.Util;
import com.google.gson.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import java.lang.reflect.Type;
import java.util.List;

public class FavoriteRename extends Rename implements ItemGroupComponent {
    public FavoriteRename(String name, Item item) {
        super(Component.nullToEmpty(name), item);
    }

    public void setItem(Item item) {
        if (!items.isEmpty()) items.removeFirst();
        items.addFirst(item);
    }

    @Override
    public List<ItemStack> getItemGroupStacks() {
        return toStackAll();
    }

    public static class Serializer implements JsonSerializer<FavoriteRename>, JsonDeserializer<FavoriteRename> {
        @Override
        public JsonElement serialize(FavoriteRename rename, Type type, JsonSerializationContext context) {
            JsonObject result = new JsonObject();

            result.addProperty("name", rename.getName().getString());

            return result;
        }

        @Override
        public FavoriteRename deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = jsonElement.getAsJsonObject();

            Item item = null;
            String jsonItem = context.deserialize(jsonObject.get("item"), String.class);
            if (jsonItem != null) item = Util.itemFromId(jsonItem);

            return new FavoriteRename(
                    context.deserialize(jsonObject.get("name"), String.class),
                    item
            );
        }
    }
}
