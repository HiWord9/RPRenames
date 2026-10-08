package com.hiword9.rprenames.mod.impl.renames_manager.updatable.parser.item_model.properties;

import com.hiword9.rprenames.api.ext.rename.Visibility;
import com.hiword9.rprenames.mod.RPRenames;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.util.ExtraCodecs;
import java.util.List;

public class BuiltinRenameProperties {
    public static final RenameProperty<List<Component>> DESCRIPTION = RenameProperty.register(
            RPRenames.asId("description"),
            ExtraCodecs.compactListCodec(ComponentSerialization.CODEC),
            List.of()
    );

    public static final RenameProperty<Visibility> VISIBILITY = RenameProperty.register(
            RPRenames.asId("visibility"),
            Visibility.CODEC,
            Visibility.LISTED
    );

    public static void bootstrap() {}
}
