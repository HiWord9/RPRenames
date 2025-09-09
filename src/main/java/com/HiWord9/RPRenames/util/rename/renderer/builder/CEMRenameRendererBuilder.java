package com.HiWord9.RPRenames.util.rename.renderer.builder;

import com.HiWord9.RPRenames.util.gui.widget.RPRWidget;
import com.HiWord9.RPRenames.util.rename.renderer.CEMRenameRenderer;
import com.HiWord9.RPRenames.util.rename.type.CEMRename;

public class CEMRenameRendererBuilder extends RenameRendererBuilder<CEMRename> implements AcceptsRPRWidget, AcceptsFavorite {
    private boolean favorite = false;
    private RPRWidget rprWidget = null;

    public CEMRenameRendererBuilder(CEMRename rename) {
        super(rename);
    }

    @Override
    public CEMRenameRendererBuilder setFavorite(boolean favorite) {
        this.favorite = favorite;
        return this;
    }

    @Override
    public CEMRenameRendererBuilder setRPRWidget(RPRWidget rprWidget) {
        this.rprWidget = rprWidget;
        return this;
    }

    @Override
    public CEMRenameRenderer build() {
        return new CEMRenameRenderer(rename, rprWidget, favorite);
    }
}
