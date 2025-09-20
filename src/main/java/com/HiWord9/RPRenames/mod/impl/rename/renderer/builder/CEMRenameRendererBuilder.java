package com.HiWord9.RPRenames.mod.impl.rename.renderer.builder;

import com.HiWord9.RPRenames.api.rename.renderer.builder.RenameRendererBuilder;
import com.HiWord9.RPRenames.mod.gui.widget.RPRWidget;
import com.HiWord9.RPRenames.mod.impl.rename.renderer.CEMRenameRenderer;
import com.HiWord9.RPRenames.mod.impl.rename.CEMRename;

import java.util.function.Supplier;

public class CEMRenameRendererBuilder extends RenameRendererBuilder<CEMRename> implements AcceptsRPRWidget, AcceptsFavoriteSupplier {
    private Supplier<Boolean> favoriteSupplier = () -> false;
    private RPRWidget rprWidget = null;

    public CEMRenameRendererBuilder(CEMRename rename) {
        super(rename);
    }

    @Override
    public void setFavoriteSupplier(Supplier<Boolean> favoriteSupplier) {
        this.favoriteSupplier = favoriteSupplier;
    }

    @Override
    public void setRPRWidget(RPRWidget rprWidget) {
        this.rprWidget = rprWidget;
    }

    @Override
    public CEMRenameRenderer build() {
        return new CEMRenameRenderer(rename, rprWidget, favoriteSupplier);
    }
}
