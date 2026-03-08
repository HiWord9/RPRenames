package com.HiWord9.RPRenames.mod.impl.renames_manager.updatable;

import com.HiWord9.RPRenames.api.ext.renames_manager.parser.ParsersHolder;
import com.HiWord9.RPRenames.mod.RPRenames;
import com.HiWord9.RPRenames.mod.item_group.RPRenamesItemGroup;
import com.HiWord9.RPRenames.api.ext.renames_manager.RenamesManagerImpl;
import com.HiWord9.RPRenames.api.core.rename.Rename;
import com.HiWord9.RPRenames.api.ext.renames_manager.parser.Parser;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.profiler.Profilers;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static com.HiWord9.RPRenames.mod.util.Util.*;

public class UpdatableRenamesManager
        extends RenamesManagerImpl<Rename>
        implements ParsersHolder, ResourceReloader
{
    protected final ArrayList<Parser> parsers = new ArrayList<>();

    public void updateRenames() {
        updateRenames(client().getResourceManager(), Profilers.get());
    }

    public void updateRenames(ResourceManager resourceManager, Profiler profiler) {
        profiler.push("rprenames:reloading_renames");

        RPRenames.LOGGER.info("Started collecting resource pack renames");
        long startTime = System.currentTimeMillis();

        clearRenames();
        ParsersHolder.super.parseAll(resourceManager, profiler);

        RPRenamesItemGroup.update();

        long finishTime = System.currentTimeMillis() - startTime;
        String ms = String.valueOf(finishTime % 1000);
        switch (ms.length()) {
            case 1 -> ms = "00" + ms;
            case 2 -> ms = "0" + ms;
        }
        RPRenames.LOGGER.info(
                "Finished collecting resource pack renames [{}.{}s] ({} in total)",
                finishTime / 1000, ms, getAllRenames().size()
        );

        profiler.pop();
    }

    @Override
    public List<Parser> parsers() {
        return parsers;
    }

    @Override
    public CompletableFuture<Void> reload(Synchronizer synchronizer, ResourceManager manager, Executor prepareExecutor, Executor applyExecutor) {
        return CompletableFuture.supplyAsync(() -> {
            if (config().updateConfig) updateRenames(manager, Profilers.get());
            return null;
        }, prepareExecutor).thenCompose(synchronizer::whenPrepared).thenAcceptAsync(o -> {}, applyExecutor);
    }
}