package com.hiword9.rprenames.mod.impl.renames_manager.updatable.parser.item_model.properties;

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

    public static void bootstrap() {}
}
