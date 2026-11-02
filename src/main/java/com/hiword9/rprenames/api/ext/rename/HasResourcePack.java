package com.hiword9.rprenames.api.ext.rename;

import net.minecraft.server.packs.PackLocationInfo;
import org.jetbrains.annotations.Nullable;

public interface HasResourcePack {
    @Nullable PackLocationInfo getPack();
    String getPath();
}
