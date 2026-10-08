package com.hiword9.rprenames.api.ext.rename;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;
import java.util.Locale;

public enum Visibility implements StringRepresentable {
    LISTED,
    HIDDEN;

    public static final Codec<Visibility> CODEC = StringRepresentable.fromEnum(Visibility::values);

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
