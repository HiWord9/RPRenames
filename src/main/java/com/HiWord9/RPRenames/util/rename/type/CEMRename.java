package com.HiWord9.RPRenames.util.rename.type;

import com.HiWord9.RPRenames.util.gui.widget.RPRWidget;
import com.HiWord9.RPRenames.util.rename.renderer.CEMRenameRenderer;
import com.HiWord9.RPRenames.util.rename.renderer.RenameRenderer;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;

import java.util.Properties;

public class CEMRename extends AbstractRename implements HasNamePattern {
    public static final Item DEFAULT_MOB_ITEM = Items.NAME_TAG;

    private final EntityType<?> entity;
    private final Properties properties;
    private final String namePattern;
    private final AbstractRename itemRename;

    public CEMRename(String name, EntityType<?> entity) {
        this(name, entity, null);
    }

    public CEMRename(String name, EntityType<?> entity, Properties properties) {
        this(name, null, null, null, entity, properties, null);
    }

    public CEMRename(
            String name,
            String packName,
            String path,
            String namePattern,
            EntityType<?> entity,
            Properties properties,
            AbstractRename itemRename
    ) {
        super(name, packName, path, DEFAULT_MOB_ITEM);
        this.entity = entity;
        this.properties = properties;
        this.namePattern = namePattern;
        this.itemRename = itemRename;
    }

    public AbstractRename getItemRename() {
        return itemRename;
    }

    @Override
    public String getNamePattern() {
        return namePattern;
    }

    public Properties getProperties() {
        return properties;
    }

    public EntityType<?> getEntity() {
        return entity;
    }

    public ItemStack toSpawnEgg() {
        Item item = SpawnEggItem.forEntity(this.getEntity());
        ItemStack spawnEgg = new ItemStack(item == null ? Items.ALLAY_SPAWN_EGG : item);
        spawnEgg.set(DataComponentTypes.CUSTOM_NAME, Text.of(this.getName()));
        NbtCompound nbtName = new NbtCompound();
        nbtName.putString("CustomName", this.getName());
        if (item == null) {
            nbtName.putString("id", Registries.ENTITY_TYPE.getId(this.getEntity()).toString());
            NbtComponent.set(DataComponentTypes.ENTITY_DATA, spawnEgg, nbtName);
        }
        return spawnEgg;
    }

    @Override
    public boolean equals(AbstractRename obj, boolean ignoreNull) {
        if (obj instanceof CEMRename cemRename) {
            return equals(cemRename, ignoreNull);
        }
        return false;
    }

    public boolean equals(CEMRename obj, boolean ignoreNull) {
        return super.equals(obj, ignoreNull)
                && paramsEquals(this.entity, obj.entity, ignoreNull)
                && paramsEquals(this.properties, obj.properties, ignoreNull)
                && paramsEquals(this.namePattern, obj.namePattern, ignoreNull)
                && paramsEquals(this.itemRename, obj.itemRename, ignoreNull);
    }

    @Override
    public boolean same(AbstractRename obj, boolean ignoreNull) {
        if (obj instanceof CEMRename cemRename) {
            return same(cemRename, ignoreNull);
        }
        return false;
    }

    public boolean same(CEMRename obj, boolean ignoreNull) {
        return super.same(obj, ignoreNull)
                && paramsEquals(this.entity, obj.entity, ignoreNull)
                && paramsEquals(this.properties, obj.properties, ignoreNull);
    }

    public RenameRenderer getNewRenderer(RPRWidget rprWidget, boolean favorite) {
        return new CEMRenameRenderer(this, rprWidget, favorite);
    }
}
