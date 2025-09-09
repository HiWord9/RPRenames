package com.HiWord9.RPRenames.util.rename.renderer.builder;

import com.HiWord9.RPRenames.util.rename.renderer.SimpleRenameRenderer;
import com.HiWord9.RPRenames.util.rename.type.Rename;

public class SimpleRenameRendererBuilder<R extends Rename> extends RenameRendererBuilder<R> {

    public SimpleRenameRendererBuilder(R rename) {
        super(rename);
    }

    @Override
    public SimpleRenameRenderer<R> build() {
        return new SimpleRenameRenderer<>(rename);
    }
}
