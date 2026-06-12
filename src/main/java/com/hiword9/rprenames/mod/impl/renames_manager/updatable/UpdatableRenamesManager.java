package com.hiword9.rprenames.mod.impl.renames_manager.updatable;

import com.hiword9.rprenames.api.ext.renames_manager.parser.ParsersHolder;
import com.hiword9.rprenames.mod.RPRenames;
import com.hiword9.rprenames.mod.item_group.RPRenamesItemGroup;
import com.hiword9.rprenames.api.ext.renames_manager.RenamesManagerImpl;
import com.hiword9.rprenames.api.core.rename.Rename;
import com.hiword9.rprenames.api.ext.renames_manager.parser.Parser;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static com.hiword9.rprenames.util.Util.*;

public class UpdatableRenamesManager
        extends RenamesManagerImpl<Rename>
        implements ParsersHolder, PreparableReloadListener
{
    protected final ArrayList<Parser> parsers = new ArrayList<>();

    public void updateRenames() {
        updateRenames(client().getResourceManager(), Profiler.get());
    }

    public void updateRenames(ResourceManager resourceManager, ProfilerFiller profiler) {
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
            PreparableReloadListener.SharedState store,
            Executor prepareExecutor,
            PreparableReloadListener.PreparationBarrier reloadSynchronizer,
            Executor applyExecutor
    ) {
        return CompletableFuture.supplyAsync(() -> {
            if (config().updateConfig) updateRenames(store.resourceManager(), Profiler.get());
            return null;
        }, prepareExecutor).thenCompose(reloadSynchronizer::wait).thenAcceptAsync(o -> {}, applyExecutor);
    }
}