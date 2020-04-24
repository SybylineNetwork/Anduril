package sybyline.anduril.scripting.api.common;

import java.util.function.Consumer;

public interface IMCItem {

	public String name();

	public void name(String name);

	public void data_read(Consumer<Object> nbtConsumer);

	public void data_change(Consumer<Object> nbtConsumer);

}
