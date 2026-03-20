package com.hiword9.rprenames.mod.impl.renames_manager.updatable;

import com.hiword9.rprenames.api.ext.renames_manager.parser.ParsersHolder;
import com.hiword9.rprenames.mod.RPRenames;
import com.hiword9.rprenames.mod.item_group.RPRenamesItemGroup;
import com.hiword9.rprenames.api.ext.renames_manager.RenamesManagerImpl;
import com.hiword9.rprenames.api.core.rename.Rename;
import com.hiword9.rprenames.api.ext.renames_manager.parser.Parser;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.profiler.Profilers;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static com.hiword9.rprenames.mod.util.Util.*;

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
    public CompletableFuture<Void> reload(
            ResourceReloader.Store store,
            Executor prepareExecutor,
            ResourceReloader.Synchronizer reloadSynchronizer,
            Executor applyExecutor
    ) {
        return CompletableFuture.supplyAsync(() -> {
            if (config().updateConfig) updateRenames(store.getResourceManager(), Profilers.get());
            return null;
        }, prepareExecutor).thenCompose(reloadSynchronizer::whenPrepared).thenAcceptAsync(o -> {}, applyExecutor);
    }
}