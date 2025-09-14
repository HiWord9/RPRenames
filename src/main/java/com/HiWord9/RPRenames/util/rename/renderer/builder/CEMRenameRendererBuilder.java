package com.HiWord9.RPRenames.util.rename.renderer.builder;

import com.HiWord9.RPRenames.util.gui.widget.RPRWidget;
import com.HiWord9.RPRenames.util.rename.renderer.CEMRenameRenderer;
import com.HiWord9.RPRenames.util.rename.type.CEMRename;

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
