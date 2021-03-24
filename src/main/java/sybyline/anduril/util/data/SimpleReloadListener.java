package sybyline.anduril.util.data;

import java.util.function.Consumer;

import net.minecraft.client.resources.ReloadListener;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.Unit;
import sybyline.anduril.util.function.Noop;

public final class SimpleReloadListener extends ReloadListener<Unit> {

	private SimpleReloadListener(Consumer<IResourceManager> prepare, Consumer<IResourceManager> apply) {
		this.prepare = prepare != null ? prepare : Noop.accept();
		this.apply = apply != null ? apply : Noop.accept();
	}

	private final Consumer<IResourceManager> prepare;
	private final Consumer<IResourceManager> apply;

	@Override
	protected Unit prepare(IResourceManager resources, IProfiler profiler) {
		prepare.accept(resources);
		return Unit.INSTANCE;
	}

	@Override
	protected void apply(Unit unit, IResourceManager resources, IProfiler profiler) {
		apply.accept(resources);
	}

	public SimpleReloadListener register(IResourceManager resources) {
		((IReloadableResourceManager)resources).addReloadListener(this);
		return this;
	}

	public static SimpleReloadListener prepare(Consumer<IResourceManager> prepare) {
		return of(prepare, null);
	}

	public static SimpleReloadListener apply(Consumer<IResourceManager> apply) {
		return of(null, apply);
	}

	public static SimpleReloadListener of(Consumer<IResourceManager> prepare, Consumer<IResourceManager> apply) {
		return new SimpleReloadListener(prepare, apply);
	}

	// Only call if you don't use the variable
	public SimpleReloadListener call() {
		prepare.accept(null);
		apply.accept(null);
		return this;
	}

}
