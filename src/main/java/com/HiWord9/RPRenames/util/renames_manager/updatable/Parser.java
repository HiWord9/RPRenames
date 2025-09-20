package com.HiWord9.RPRenames.util.renames_manager.updatable;

import net.minecraft.resource.ResourceManager;
import net.minecraft.util.profiler.Profiler;

public interface Parser {
    void parse(ResourceManager resourceManager, Profiler profiler);
}
