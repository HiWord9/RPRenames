package com.HiWord9.RPRenames.mod.impl.renames_manager.updatable.parser;

import net.minecraft.resource.ResourceManager;
import net.minecraft.util.profiler.Profiler;

public interface Parser {
    void parse(ResourceManager resourceManager, Profiler profiler);
}
