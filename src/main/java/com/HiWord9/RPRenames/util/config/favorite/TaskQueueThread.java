package com.HiWord9.RPRenames.util.config.favorite;

import com.HiWord9.RPRenames.RPRenames;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class TaskQueueThread extends Thread {
    private final BlockingQueue<Runnable> taskQueue = new LinkedBlockingQueue<>();

    public TaskQueueThread() {
        this.setDaemon(true);
    }

    public synchronized boolean addTask(Runnable task) {
        return taskQueue.offer(task);
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Runnable method = taskQueue.take();
                method.run();
            } catch (Exception e) {
                RPRenames.LOGGER.error("Something went wrong in " + this, e);
                // Handle interruption
            }
        }
    }
}