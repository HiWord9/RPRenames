package com.HiWord9.RPRenames.api.rename.renderer.builder;

import com.HiWord9.RPRenames.api.rename.Rename;
import com.HiWord9.RPRenames.api.rename.renderer.SimpleRenameRenderer;

public class SimpleRenameRendererBuilder<R extends Rename> extends RenameRendererBuilder<R> {

    public SimpleRenameRendererBuilder(R rename) {
        super(rename);
    }

    @Override
    public SimpleRenameRenderer<R> build() {
        return new SimpleRenameRenderer<>(rename);
    }
}
