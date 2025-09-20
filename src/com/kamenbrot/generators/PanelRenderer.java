package com.kamenbrot.generators;

import javax.swing.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PanelRenderer {

  private final ScheduledExecutorService exec;

  public PanelRenderer(ScheduledExecutorService exec) {
	this.exec = exec;
  }

  public PanelRenderer() {
	this(Executors.newSingleThreadScheduledExecutor());
  }

  public RenderTask addRenderTask(Runnable runnable) {
	final RenderTask task = new RenderTask(runnable);
	this.exec.scheduleAtFixedRate(task, 0, 16, TimeUnit.MILLISECONDS);
	return task;
  }

  public static class RenderTask implements Runnable {

	private volatile boolean needsRender;
	private final Runnable task;


	private RenderTask(Runnable task) {
	  this.task = task;
	}

	public synchronized void setNeedsRender(boolean needsRender) {
	  this.needsRender = needsRender;
	}

	@Override
	public void run() {
	  if (needsRender) {
		needsRender = false;
		SwingUtilities.invokeLater(task);
	  }
	}
  }
}