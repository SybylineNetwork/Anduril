package sybyline.anduril.scripting.common;

import java.util.function.Consumer;

import net.minecraft.item.ItemStack;
import net.minecraft.util.text.StringTextComponent;
import sybyline.anduril.scripting.api.common.IMCItem;

public class MCItem implements IMCItem {

	MCItem(ItemStack stack) {
		this.__stack = stack;
	}

	public final ItemStack __stack;

	@Override
	public String name() {
		return __stack.getDisplayName().getFormattedText();
	}

	@Override
	public void name(String name) {
		__stack.setDisplayName(new StringTextComponent(name));
	}

	@Override
	public int size() {
		return __stack.getCount();
	}

	@Override
	public void size(int size) {
		__stack.setCount(size);
	}

	@Override
	public void data_read(Consumer<Object> nbtConsumer) {
		throw new UnsupportedOperationException("TODO : implement or respecify");
	}

	@Override
	public void data_change(Consumer<Object> nbtConsumer) {
		throw new UnsupportedOperationException("TODO : implement or respecify");
	}

}
