package com.hiword9.rprenames.mod.impl.rename.renderer.builder;

import java.util.function.Supplier;

public interface AcceptsFavoriteSupplier {
    void setFavoriteSupplier(Supplier<Boolean> favoriteSupplier);
}
