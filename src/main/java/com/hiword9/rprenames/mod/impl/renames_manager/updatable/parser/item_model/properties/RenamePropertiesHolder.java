package com.hiword9.rprenames.mod.impl.renames_manager.updatable.parser.item_model.properties;

import net.minecraft.client.renderer.item.SelectItemModel;

public interface RenamePropertiesHolder {
    RenameProperties rprenames$getProperties();

    void rprenames$setProperties(RenameProperties properties);

    static RenamePropertiesHolder of(SelectItemModel.SwitchCase<?> switchCase) {
        return (RenamePropertiesHolder) (Object) switchCase;
    }
}
