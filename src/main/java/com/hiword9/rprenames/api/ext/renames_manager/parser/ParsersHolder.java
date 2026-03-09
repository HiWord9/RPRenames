package com.hiword9.rprenames.api.ext.renames_manager.parser;

import net.minecraft.resource.ResourceManager;
import net.minecraft.util.profiler.Profiler;

import java.util.List;

public interface ParsersHolder {
    default void parseAll(ResourceManager resourceManager, Profiler profiler) {
        for (Parser parser : parsers()) {
            parser.parse(resourceManager, profiler);
        }
    }

    List<Parser> parsers();
}
