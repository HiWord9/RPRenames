package com.hiword9.rprenames.mod.impl.rename;

import com.hiword9.rprenames.api.core.rename.Rename;
import com.hiword9.rprenames.api.core.rename.renderer.RenameRenderer;
import com.hiword9.rprenames.api.ext.rename.HasResourcePack;
import com.hiword9.rprenames.api.ext.rename.Informative;
import com.hiword9.rprenames.mod.RPRenames;
import com.hiword9.rprenames.mod.gui.widget.GhostCraft;
import com.hiword9.rprenames.mod.impl.rename.renderer.CEMRenameRenderer;
import com.hiword9.rprenames.mod.item_group.ItemGroupComponent;
import com.hiword9.rprenames.mod.util.PropertiesHelper;
import com.hiword9.rprenames.mod.util.RenameInfoHelper;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.TypedEntityData;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.storage.NbtReadView;
import net.minecraft.text.Text;
import net.minecraft.util.ErrorReporter;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.regex.Pattern;

import static com.hiword9.rprenames.mod.util.Util.*;

public class CEMRename
        extends Rename
        implements HasResourcePack, ItemGroupComponent, GhostCraft.Loader, Informative
{
    public static final Item DEFAULT_MOB_ITEM = Items.NAME_TAG;

    private final String packName;
    private final String path;
    private final EntityType<?> entity;
    private final Properties properties;
    private final String namePattern;
    private final Rename itemRename;

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
            Rename itemRename
    ) {
        super(Text.of(name), DEFAULT_MOB_ITEM);
        this.packName = packName;
        this.path = path;
        this.entity = entity;
        this.properties = properties;
        this.namePattern = namePattern;
        this.itemRename = itemRename;
    }

    public Rename getItemRename() {
        return itemRename;
    }

    public String getOriginalNamePattern() {
        return namePattern;
    }

    public Pattern getNamePattern() {
        return PropertiesHelper.getPropPattern(namePattern);
    }

    public EntityType<?> getEntity() {
        return entity;
    }

    public ItemStack toSpawnEgg() {
        Item spawnEggItem = SpawnEggItem.forEntity(this.getEntity());
        ItemStack stack = new ItemStack(spawnEggItem == null ? Items.ALLAY_SPAWN_EGG : spawnEggItem);

        stack.set(DataComponentTypes.CUSTOM_NAME, this.getName());

        if (spawnEggItem == null) {
            NbtCompound nbtName = new NbtCompound();
            nbtName.putString("id", Registries.ENTITY_TYPE.getId(this.getEntity()).toString());
            stack.set(DataComponentTypes.ENTITY_DATA, TypedEntityData.create(this.getEntity(), nbtName));
        }
        return stack;
    }

    @Override
    public boolean matchesStack(ItemStack stack) {
        if (itemRename != null && itemRename.matchesStack(stack)) return true;
        if (stack.getItem() instanceof SpawnEggItem spawnEggItem && client().world != null) {
            var registries = client().world.getRegistryManager();

            var entityType = spawnEggItem.getEntityType(stack);
            var name = stack.getCustomName();

            var entityData = stack.get(DataComponentTypes.ENTITY_DATA);
            if (entityData != null) {
                var nbt = entityData.copyNbtWithoutId();

                try (var logging = new ErrorReporter.Logging(ErrorReporter.Logging.CONTEXT, RPRenames.LOGGER)) {
                    var readView = NbtReadView.create(logging, registries, nbt);
                    var entityCustomName = BlockEntity.tryParseCustomName(readView, "CustomName");
                    if (entityCustomName != null) name = entityCustomName;
                }
            }

            if (entityType == getEntity() && name != null) {
                var namePattern = getNamePattern();

                if (namePattern == null
                        ? name.equals(this.getName())
                        : namePattern.matcher(name.getString()).matches()
                ) return true;
            }
        }
        return super.matchesStack(stack);
    }

    public RenameRenderer.Builder<CEMRename> getNewRendererBuilder(RenameRenderer.RenderArea renderArea) {
        return new CEMRenameRenderer.Builder(this, renderArea);
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj)
                && obj instanceof CEMRename cemRename
                && Objects.equals(entity, cemRename.entity);
    }

    @Override
    public String getPackName() {
        return packName;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public void loadGhostCraft(GhostCraft ghostCraft, ItemStack itemStack) {
        if (!itemStack.isEmpty()) return;

        var source = new ItemStack(getItem());
        var result = toStack();

        var stacks = new ItemStack[ghostCraft.length];
        if (ghostCraft.length >= 1) {
            stacks[0] = source;
            stacks[ghostCraft.length - 1] = result;
        }

        ghostCraft.setStacks(stacks);
        ghostCraft.setRender(true);
    }

    @Override
    public List<Text> getInfo() {
        var info = new ArrayList<Text>();
        if (itemRename != null && itemRename instanceof Informative informative) {
            info.addAll(informative.getInfo());
        }
        info.add(
                Text.translatable("rprenames.command.info.cemProperties")
                        .formatted(Formatting.LIGHT_PURPLE)
        );
        info.addAll(RenameInfoHelper.getProperties(properties));
        info.addAll(RenameInfoHelper.getRPPath(packName, path));
        return info;
    }

    @Override
    public List<ItemStack> getItemGroupStacks() {
        if (config().generateSpawnEggsInItemGroup) return List.of(toSpawnEgg());
        return toStackAll();
    }
}
