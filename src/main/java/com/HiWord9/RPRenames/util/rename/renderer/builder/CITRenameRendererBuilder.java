package com.HiWord9.RPRenames.util.rename.renderer.builder;

import com.HiWord9.RPRenames.util.gui.widget.RPRWidget;
import com.HiWord9.RPRenames.util.rename.renderer.CITRenameRenderer;
import com.HiWord9.RPRenames.util.rename.type.CITRename;

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
