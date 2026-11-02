package com.hiword9.rprenames.api.ext.rename;

import net.minecraft.server.packs.PackLocationInfo;
import org.jetbrains.annotations.Nullable;
import java.util.List;

public interface HasResourcePack {
    @Nullable PackLocationInfo getPack();
    List<String> getPaths();
}
