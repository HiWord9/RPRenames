package com.hiword9.rprenames.mod.impl.renames_manager.updatable.parser.item_model.properties;

import net.minecraft.client.renderer.item.SelectItemModel;

public interface RenamePropertiesHolder {
    RenamePropertiesDefinition rprenames$getProperties();

    void rprenames$setProperties(RenamePropertiesDefinition properties);

    static RenamePropertiesHolder of(SelectItemModel.SwitchCase<?> switchCase) {
        return (RenamePropertiesHolder) (Object) switchCase;
    }
}
