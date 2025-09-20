package com.HiWord9.RPRenames.mod.impl.rename.renderer.builder;

import com.HiWord9.RPRenames.api.rename.renderer.builder.RenameRendererBuilder;
import com.HiWord9.RPRenames.mod.gui.widget.RPRWidget;
import com.HiWord9.RPRenames.mod.impl.rename.renderer.CITRenameRenderer;
import com.HiWord9.RPRenames.mod.impl.rename.CITRename;

import java.util.function.Supplier;

public class CITRenameRendererBuilder extends RenameRendererBuilder<CITRename> implements AcceptsRPRWidget, AcceptsFavoriteSupplier {
    private Supplier<Boolean> favoriteSupplier = () -> false;
    private RPRWidget rprWidget = null;

    public CITRenameRendererBuilder(CITRename rename) {
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
    public CITRenameRenderer build() {
        return new CITRenameRenderer(rename, rprWidget, favoriteSupplier);
    }
}
