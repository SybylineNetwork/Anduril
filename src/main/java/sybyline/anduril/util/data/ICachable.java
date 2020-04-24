package sybyline.anduril.util.data;

public interface ICachable<D> {

	public void construct();

	public void readFrom(D dataObject);

	public void firstLoad();

	public void writeTo(D dataObject);

	public boolean shouldKeep();

}
