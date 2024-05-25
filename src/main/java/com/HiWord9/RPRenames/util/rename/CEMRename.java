package com.HiWord9.RPRenames.util.rename;

import com.HiWord9.RPRenames.util.config.generation.CEMList;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.nbt.NbtCompound;
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
        this(name, packName, null, mob);
    }

    public CEMRename(String name,
                     String packName,
                     String path,
                     Mob mob) {
        super(name, packName, path, mob == null ? null : mob.properties(), DEFAULT_MOB_ITEM);
        this.mob = mob;
    }

    public Mob getMob() {
        return mob;
    }

    public void setMob(Mob mob) {
        this.mob = mob;
    }

    @Override
    public Properties getProperties() {
        return mob == null ? null : mob.properties();
    }

    @Override
    public String getNamePattern() {
        Mob mob = getMob();
        if (mob == null) return null;
        return mob.getPropName();
    }

    public ItemStack toSpawnEgg() {
        Item item = SpawnEggItem.forEntity(CEMList.EntityFromName(this.getMob().entity()));
        ItemStack spawnEgg = new ItemStack(item == null ? Items.ALLAY_SPAWN_EGG : item);
        spawnEgg.setCustomName(Text.of(this.getName()));
        NbtCompound nbt = spawnEgg.getOrCreateNbt();
        NbtCompound nbtName = new NbtCompound();
        nbtName.putString("CustomName", this.getName());
        nbt.put("EntityTag", nbtName);
        if (item == null) {
            NbtCompound nbt2 = nbt.getCompound("EntityTag");
            nbt2.putString("id", this.getMob().entity());
        }
        return spawnEgg;
    }

    public record Mob(String entity, Item icon, Properties properties, String path) {

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
}
