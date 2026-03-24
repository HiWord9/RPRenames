package com.hiword9.rprenames.api.ext.renames_manager.parser;

import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

public interface Parser {
    void parse(ResourceManager resourceManager, ProfilerFiller profiler);
}
