package com.HiWord9.RPRenames.util.rename.renderer.builder;

import com.HiWord9.RPRenames.util.gui.widget.RPRWidget;
import com.HiWord9.RPRenames.util.rename.renderer.CITRenameRenderer;
import com.HiWord9.RPRenames.util.rename.type.CITRename;

public class CITRenameRendererBuilder extends RenameRendererBuilder<CITRename> implements AcceptsRPRWidget, AcceptsFavorite {
    private boolean favorite = false;
    private RPRWidget rprWidget = null;

    public CITRenameRendererBuilder(CITRename rename) {
        super(rename);
    }

    @Override
    public CITRenameRendererBuilder setFavorite(boolean favorite) {
        this.favorite = favorite;
        return this;
    }

    @Override
    public CITRenameRendererBuilder setRPRWidget(RPRWidget rprWidget) {
        this.rprWidget = rprWidget;
        return this;
    }

    @Override
    public CITRenameRenderer build() {
        return new CITRenameRenderer(rename, rprWidget, favorite);
    }
}
