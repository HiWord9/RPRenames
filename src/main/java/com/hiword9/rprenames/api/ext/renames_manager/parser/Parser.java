package com.hiword9.rprenames.api.ext.renames_manager.parser;

import net.minecraft.resource.ResourceManager;
import net.minecraft.util.profiler.Profiler;

public interface Parser {
    void parse(ResourceManager resourceManager, Profiler profiler);
}
