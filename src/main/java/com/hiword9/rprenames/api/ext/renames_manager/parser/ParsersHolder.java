package com.hiword9.rprenames.api.ext.renames_manager.parser;

import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import java.util.List;

public interface ParsersHolder {
    default void parseAll(ResourceManager resourceManager, ProfilerFiller profiler) {
        for (Parser parser : parsers()) {
            parser.parse(resourceManager, profiler);
        }
    }

    List<Parser> parsers();
}
