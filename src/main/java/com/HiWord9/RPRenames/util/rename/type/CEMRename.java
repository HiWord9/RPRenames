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
        this(name, null, null);
    }

    public CEMRename(String name,
                     String packName,
                     Mob mob) {
        this(name, packName, null, null, mob);
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
        return mob.getPropName();
    }

    public ItemStack toSpawnEgg() {
        Item item = SpawnEggItem.forEntity(this.getMob().entity());
        ItemStack spawnEgg = new ItemStack(item == null ? Items.ALLAY_SPAWN_EGG : item);
        spawnEgg.set(DataComponentTypes.CUSTOM_NAME, Text.of(this.getName()));
        NbtCompound nbtName = new NbtCompound();
        nbtName.putString("CustomName", this.getName());
        if (item == null) {
            nbtName.putString("id", Registries.ENTITY_TYPE.getId(this.getMob().entity()).toString());
            NbtComponent.set(DataComponentTypes.ENTITY_DATA, spawnEgg, nbtName);
        }
        return spawnEgg;
    }

    public record Mob(EntityType<?> entity, Properties properties, String path, String packName) {
        public String getPropName() {
            if (properties == null) return null;
            Set<String> propertyNames = properties.stringPropertyNames();
            for (String s : propertyNames) {
                if (s.startsWith("name.")) {
                    if (propertyNames.contains("skins." + s.substring(5))) {
                        return properties.getProperty(s);
                    }
                }
            }
            return null;
        }
    }

    public RenameRenderer getNewRenderer(RPRWidget rprWidget, boolean favorite) {
        return new CEMRenameRenderer(this, rprWidget, favorite);
    }
}
