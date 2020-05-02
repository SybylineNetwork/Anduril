package sybyline.anduril.scripting.api.common;

import java.util.function.Consumer;

public interface IMCItem {

	public String name();

	public void name(String name);

	public int size();

	public void size(int size);

	public default int decrement() {
		return this.increment(-1);
	}

	public default int increment() {
		return this.increment(1);
	}

	public default int decrement(int amount) {
		return this.increment(-amount);
	}

	public default int increment(int amount) {
		int newsize = size() + amount;
		size(newsize);
		return newsize;
	}

	public void data_read(Consumer<Object> nbtConsumer);

	public void data_change(Consumer<Object> nbtConsumer);

}
