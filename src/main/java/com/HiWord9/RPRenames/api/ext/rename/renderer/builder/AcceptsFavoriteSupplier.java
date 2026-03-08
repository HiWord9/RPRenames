package com.HiWord9.RPRenames.api.ext.rename.renderer.builder;

import java.util.function.Supplier;

public interface AcceptsFavoriteSupplier {
    void setFavoriteSupplier(Supplier<Boolean> favoriteSupplier);
}
