package com.HiWord9.RPRenames.mod.impl.rename;

import com.HiWord9.RPRenames.api.core.rename.Rename;
import com.HiWord9.RPRenames.api.core.rename.renderer.RenameRenderer;
import com.HiWord9.RPRenames.mod.gui.widget.GhostCraft;
import com.HiWord9.RPRenames.mod.impl.rename.renderer.ItemModelRenameRenderer;
import com.HiWord9.RPRenames.mod.impl.renames_manager.updatable.parser.item_model.condition.ItemModelCondition;
import com.HiWord9.RPRenames.mod.item_group.ItemGroupComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.HiWord9.RPRenames.mod.util.Util.config;

public class ItemModelRename
        extends Rename
        implements ItemGroupComponent, GhostCraft.Loader, Informative
{
    protected final List<ItemModelCondition.Applicable> conditions = new ArrayList<>();
    protected final List<List<ItemModelCondition>> contexts = new ArrayList<>();

    public ItemModelRename(
            List<ItemModelCondition.Applicable> conditions,
            List<List<ItemModelCondition>> contexts,
            Text name,
            Item... items
    ) {
        super(name, items);
        if (conditions != null) this.conditions.addAll(conditions);
        if (contexts != null) this.contexts.addAll(contexts);
    }

    @Override
    public ItemStack toStack(int index) {
        ItemStack stack = new ItemStack(items.get(index));
        for (ItemModelCondition.Applicable condition : conditions) {
            condition.apply(stack);
        }
        return stack;
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj)
                && obj instanceof ItemModelRename i
                && Objects.deepEquals(conditions, i.conditions);
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

    @Override
    public List<Text> getInfo() {
        var info = new ArrayList<Text>();
        info.add(Text.translatable("rprenames.command.info.requiredConditions", conditions.size()).formatted(Formatting.AQUA));
        info.add(Text.translatable("rprenames.command.info.availableContexts", contexts.size()).formatted(Formatting.AQUA));
        return info;
    }
}
