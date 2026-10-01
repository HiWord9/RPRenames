package com.hiword9.rprenames.mod.impl.rename;

import com.hiword9.rprenames.api.core.rename.Rename;
import com.hiword9.rprenames.api.core.rename.renderer.RenameRenderer;
import com.hiword9.rprenames.api.ext.rename.Informative;
import com.hiword9.rprenames.mod.gui.widget.GhostCraft;
import com.hiword9.rprenames.mod.impl.rename.renderer.ItemModelRenameRenderer;
import com.hiword9.rprenames.mod.impl.renames_manager.updatable.parser.item_model.condition.ItemModelCondition;
import com.hiword9.rprenames.mod.impl.renames_manager.updatable.parser.item_model.properties.RenameProperties;
import com.hiword9.rprenames.mod.item_group.ItemGroupComponent;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.hiword9.rprenames.util.Util.config;

public class ItemModelRename
        extends Rename
        implements ItemGroupComponent, GhostCraft.Loader, Informative
{
    protected final List<ItemModelCondition.Applicable> conditions = new ArrayList<>();
    protected final List<List<ItemModelCondition>> contexts = new ArrayList<>();
    protected final RenameProperties properties;

    public ItemModelRename(
            List<ItemModelCondition.Applicable> conditions,
            List<List<ItemModelCondition>> contexts,
            List<Component> names,
            RenameProperties properties,
            Item... items
    ) {
        super(names, items);
        if (conditions != null) this.conditions.addAll(conditions);
        if (contexts != null) this.contexts.addAll(contexts);
        this.properties = properties;
    }

    @Override
    public ItemStack toStack(int itemIndex, int nameIndex) {
        ItemStack stack = new ItemStack(items.get(itemIndex));
        for (ItemModelCondition.Applicable condition : conditions) {
            condition.apply(stack);
        }
        stack.set(DataComponents.CUSTOM_NAME, names.get(nameIndex));
        return stack;
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj)
                && obj instanceof ItemModelRename i
                && Objects.deepEquals(conditions, i.conditions)
                && Objects.equals(properties, i.properties);
    }

    @Override
    public RenameRenderer.Builder<?> getNewRendererBuilder(RenameRenderer.RenderArea renderArea) {
        return new ItemModelRenameRenderer.Builder(this, renderArea);
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
    public List<ItemStack> getItemGroupStacks() {
        if (config().compareItemGroupRenames) return List.of(toStack());
        return toStackAll();
    }

    public RenameProperties getProperties() {
        return properties;
    }

    @Override
    public List<Component> getInfo() {
        var info = new ArrayList<Component>();
        info.add(Component.translatable("rprenames.command.info.requiredConditions", conditions.size()).withStyle(ChatFormatting.AQUA));
        info.add(Component.translatable("rprenames.command.info.availableContexts", contexts.size()).withStyle(ChatFormatting.AQUA));
        return info;
    }
}
