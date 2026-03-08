package com.HiWord9.RPRenames.mod.impl.renames_manager.updatable;

import com.HiWord9.RPRenames.mod.impl.renames_manager.updatable.parser.Parser;
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
