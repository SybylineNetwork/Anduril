package sybyline.anduril.util;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class Box<Thing> implements Supplier<Thing>, Consumer<Thing> {

	public Box() {
		this.thing = null;
	}

	public Box(Thing thing) {
		this.thing = thing;
	}

	private Thing thing;

	@Override
	public void accept(Thing thing) {
		this.thing = thing;
	}

	@Override
	public Thing get() {
		return thing;
	}

	public Thing set(Thing thing) {
		return this.thing = thing;
	}

	public Thing swap(Thing thing) {
		try {
			return this.thing;
		} finally {
			this.thing = thing;
		}
	}

}
