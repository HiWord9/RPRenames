package com.hiword9.rprenames.mod.integration;

import com.hiword9.merged_item_models.api.MergedItemModelsApi;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.server.packs.PackLocationInfo;
import org.jetbrains.annotations.Nullable;

public class MergedItemModelsIntegration {
    private static final boolean LOADED = isLoaded();

    public static @Nullable PackLocationInfo getSourcePack(ItemModel.Unbaked model) {
        if (!LOADED) return null;
        return MergedItemModelsApi.getSourcePackInfo(model).orElse(null);
    }

    private static boolean isLoaded() {
        try {
            Class.forName(
                    "com.hiword9.merged_item_models.api.MergedItemModelsApi",
                    false,
                    MergedItemModelsIntegration.class.getClassLoader()
            );
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
