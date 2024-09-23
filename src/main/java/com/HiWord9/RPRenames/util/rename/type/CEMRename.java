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
import java.util.Set;

public class CEMRename extends AbstractRename {
    public static final Item DEFAULT_MOB_ITEM = Items.NAME_TAG;
    protected Mob mob;

    public CEMRename(String name) {
        this(name, (Mob) null);
    }

    public CEMRename(String name, EntityType<?> entity) {
        this(name, new Mob(entity));
    }

    public CEMRename(String name,
                     Mob mob) {
        this(name, null, null, null, mob);
    }

    public CEMRename(String name,
                     String packName,
                     String path,
                     Properties properties,
                     Mob mob) {
        super(name, packName, path, properties, DEFAULT_MOB_ITEM);
        this.mob = mob;
    }

    public Mob getMob() {
        return mob;
    }

    public void setMob(Mob mob) {
        this.mob = mob;
    }

    @Override
    public String getNamePattern() {
        Mob mob = getMob();
        if (mob == null) return null;
        return mob.getNamePattern();
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
        return super.equals(obj, ignoreNull) &&
                (this.getMob() == null ? obj.getMob() == null || ignoreNull :
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
        private final String path;
        private final String packName;
        private final String namePattern;
        private final String nameIndex;

        public Mob(EntityType<?> entity) {
            this(entity, null);
        }

        public Mob(EntityType<?> entity, String packName) {
            this(entity, null, null, packName, null);
        }

        public Mob(EntityType<?> entity, Properties properties, String path, String packName, String nameIndex) {
            this.entity = entity;
            this.properties = properties;
            this.path = path;
            this.packName = packName;
            this.nameIndex = nameIndex;
            this.namePattern = findPropName();
        }

        public EntityType<?> getEntity() {
            return entity;
        }

        public Properties getProperties() {
            return properties;
        }

        public String getPath() {
            return path;
        }

        public String getPackName() {
            return packName;
        }

        public String getNamePattern() {
            return namePattern;
        }

        public String findPropName() {
            return findPropName(properties, nameIndex);
        }

        public static String findPropName(Properties properties, String nameIndex) {
            if (properties == null || nameIndex == null) return null;
            Set<String> propertyNames = properties.stringPropertyNames();
            for (String s : propertyNames) {
                if (s.startsWith("name." + nameIndex)) {
                    return properties.getProperty(s);
                }
            }
            return null;
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
            return paramsEquals(this.packName, obj.packName, ignoreNull)
                    && paramsEquals(this.path, obj.path, ignoreNull)
                    && paramsEquals(this.properties, obj.properties, ignoreNull);
        }

        public boolean same(Mob obj, boolean ignoreNull) {
            if (obj == null) return ignoreNull;
            return paramsEquals(this.entity, obj.entity, ignoreNull)
                    && paramsEquals(this.namePattern, obj.namePattern, ignoreNull);
        }
    }

    public RenameRenderer getNewRenderer(RPRWidget rprWidget, boolean favorite) {
        return new CEMRenameRenderer(this, rprWidget, favorite);
    }
}
