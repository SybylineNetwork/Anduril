package sybyline.anduril.scripting.common;

import java.util.concurrent.*;

import sybyline.anduril.util.function.Noop;

public class PendingTask implements Future<Runnable> {

	public static PendingTask of(Future<Runnable> future) {
		return new PendingTask(null, -2) {
			private final Future<Runnable> wrapped = future;
			@Override
			public boolean cancel(boolean mayInterruptIfRunning) {
				return wrapped.cancel(mayInterruptIfRunning);
			}
			@Override
			public boolean isCancelled() {
				return wrapped.isCancelled();
			}
			@Override
			public boolean isDone() {
				return wrapped.isDone();
			}
			@Override
			public Runnable get() {
				if (wrapped.isDone()) try {
					this.runnable = wrapped.get();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
				return super.get();
			}
		};
	}

	public static PendingTask of(Runnable runnable) {
		return of(runnable, 0);
	}

	public static PendingTask of(Runnable runnable, int ticks) {
		return new PendingTask(runnable, ticks);
	}

	private PendingTask(Runnable runnable, int ticks) {
		this.runnable = runnable;
		this.ticks = ticks;
	}

	protected Runnable runnable;
	protected int ticks;

	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		if (mayInterruptIfRunning) {
			if (ticks > 0) {
				ticks = -1;
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isCancelled() {
		return ticks < 0;
	}

	@Override
	public boolean isDone() {
		return ticks < 1;
	}

	@Override
	public Runnable get() {
		return isCancelled() ? Noop.run() : runnable;
	}

	@Override
	public Runnable get(long timeout, TimeUnit unit) {
	    return get();
	}

	public void tick() {
		int _ticks = ticks;
		if (_ticks > 0)
			ticks = _ticks - 1;
	}

}
