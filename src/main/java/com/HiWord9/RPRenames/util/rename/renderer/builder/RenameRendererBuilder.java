package com.HiWord9.RPRenames.util.rename.renderer.builder;

import com.HiWord9.RPRenames.util.rename.renderer.RenameRenderer;
import com.HiWord9.RPRenames.util.rename.type.Rename;

public abstract class RenameRendererBuilder<R extends Rename> {
    protected final R rename;

    public RenameRendererBuilder(R rename) {
        this.rename = rename;
    }

    public abstract RenameRenderer build();
}
