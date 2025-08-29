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

    private final Mob mob;
    private final String namePattern;
    private final AbstractRename itemRename;

    public CEMRename(String name) {
        this(name, (Mob) null);
    }

    public CEMRename(String name, EntityType<?> entity) {
        this(name, new Mob(entity));
    }

    public CEMRename(String name, Mob mob) {
        this(name, null, null, null, mob, null);
    }

    public CEMRename(
            String name,
            String packName,
            String path,
            String namePattern,
            Mob mob,
            AbstractRename itemRename
    ) {
        super(name, packName, path, DEFAULT_MOB_ITEM);
        this.mob = mob;
        this.namePattern = namePattern;
        this.itemRename = itemRename;
    }

    public Mob getMob() {
        return mob;
    }

    public AbstractRename getItemRename() {
        return itemRename;
    }

    @Override
    public String getNamePattern() {
        return namePattern;
    }

    public ItemStack toSpawnEgg() {
        Item item = SpawnEggItem.forEntity(this.getMob().getEntity());
        ItemStack spawnEgg = new ItemStack(item == null ? Items.ALLAY_SPAWN_EGG : item);
        spawnEgg.set(DataComponentTypes.CUSTOM_NAME, Text.of(this.getName()));
        NbtCompound nbtName = new NbtCompound();
        nbtName.putString("CustomName", this.getName());
        if (item == null) {
            nbtName.putString("id", Registries.ENTITY_TYPE.getId(this.getMob().getEntity()).toString());
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
                && paramsEquals(this.namePattern, obj.namePattern, ignoreNull)
                && paramsEquals(this.itemRename, obj.itemRename, ignoreNull)
                && (this.getMob() == null ? obj.getMob() == null || ignoreNull :
                        this.getMob().equalsNoSame(obj.getMob(), ignoreNull));
    }

    @Override
    public boolean same(AbstractRename obj, boolean ignoreNull) {
        if (obj instanceof CEMRename cemRename) {
            return same(cemRename, ignoreNull);
        }
        return false;
    }

    public boolean same(CEMRename obj, boolean ignoreNull) {
        return super.same(obj, ignoreNull) &&
                (this.getMob() == null ? obj.getMob() == null || ignoreNull :
                        this.getMob().same(obj.getMob(), ignoreNull));
    }

    public static final class Mob {
        private final EntityType<?> entity;
        private final Properties properties;

        public Mob(EntityType<?> entity) {
            this(entity, null);
        }

        public Mob(EntityType<?> entity, Properties properties) {
            this.entity = entity;
            this.properties = properties;
        }

        public EntityType<?> getEntity() {
            return entity;
        }

        public Properties getProperties() {
            return properties;
        }

        public boolean equals(Object obj) {
            if (obj instanceof Mob mob) {
                return this.equals(mob);
            }
            return false;
        }

        public boolean equals(Mob obj) {
            return equals(obj, false);
        }

        public boolean equals(Mob obj, boolean ignoreNull) {
            return this.same(obj, ignoreNull) && this.equalsNoSame(obj, ignoreNull);
        }

        public boolean equalsNoSame(Mob obj, boolean ignoreNull) {
            if (obj == null) return ignoreNull;
            return paramsEquals(this.properties, obj.properties, ignoreNull);
        }

        public boolean same(Mob obj, boolean ignoreNull) {
            if (obj == null) return ignoreNull;
            return paramsEquals(this.entity, obj.entity, ignoreNull);
        }
    }

    public RenameRenderer getNewRenderer(RPRWidget rprWidget, boolean favorite) {
        return new CEMRenameRenderer(this, rprWidget, favorite);
    }
}
