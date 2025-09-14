package com.HiWord9.RPRenames.util.rename.renderer.builder;

import java.util.function.Supplier;

public interface AcceptsFavoriteSupplier {
    void setFavoriteSupplier(Supplier<Boolean> favoriteSupplier);
}
