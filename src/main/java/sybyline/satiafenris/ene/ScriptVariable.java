package sybyline.satiafenris.ene;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import sybyline.anduril.util.Util;
import sybyline.anduril.util.function.Noop;

public interface ScriptVariable<T> {

	public void implementation_set(Object value) throws Exception;

	public Object implementation_get() throws Exception;

	public default void set(T object) {
		try {
			this.implementation_set(object);
		} catch(Exception e) {
			Util.throwSilent(e);
		}
	}

	public default T get() {
		try {
			@SuppressWarnings("unchecked")
			T ret = (T) this.implementation_get();
			return ret;
		} catch(Exception e) {
			return Util.throwSilent(e);
		}
	}

	public default <Err extends Throwable> ScriptVariable<T> catching(Class<Err> errClass, BiConsumer<Err, T> setter, Function<Err, T> getter) {
		ScriptVariable<T> parent = this;
		return new ScriptVariable<T>() {
			@SuppressWarnings("unchecked")
			@Override
			public void implementation_set(Object value) throws Exception {
				try {
					parent.implementation_set(value);
				} catch(Throwable err) {
					if (errClass.isInstance(err))
						setter.accept((Err)err, (T)value);
					else
						Util.throwSilent(err);
				}
			}
			@SuppressWarnings("unchecked")
			@Override
			public Object implementation_get() throws Exception {
				try {
					return parent.implementation_get();
				} catch(Throwable err) {
					return errClass.isInstance(err)
						? getter.apply((Err)err)
						: Util.throwSilent(err);
				}
			}
		};
	}

	public default ScriptVariable<T> catchingAll(Supplier<T> fallback) {
		return this.catching(Throwable.class, Noop.biaccept(), err -> fallback.get());
	}
	public default ScriptVariable<T> catchingAll(T fallback) {
		return this.catchingAll(Noop.constant(fallback));
	}

	public default ScriptVariable<T> catchingAllPrintErr(Supplier<T> fallback) {
		return this.catching(Throwable.class, (err, value) -> err.printStackTrace(), err -> { err.printStackTrace(); return fallback.get(); });
	}
	public default ScriptVariable<T> catchingAllPrintErr(T fallback) {
		return this.catchingAllPrintErr(Noop.constant(fallback));
	}

}
