package com.HiWord9.RPRenames.api.rename.renderer.builder;

import com.HiWord9.RPRenames.api.rename.Rename;
import com.HiWord9.RPRenames.api.rename.renderer.RenameRenderer;

public abstract class RenameRendererBuilder<R extends Rename> {
    protected final R rename;

    public RenameRendererBuilder(R rename) {
        this.rename = rename;
    }

    public abstract RenameRenderer build();
}
