package siege.common.siege.util;

public interface Wire {

	public abstract boolean checkTripped();

	public abstract boolean trip();

	public static Wire create() {
		return new Wire() {
			private boolean tripped = false;
			@Override
			public boolean checkTripped() {
				return tripped;
			}
			@Override
			public boolean trip() {
				return tripped = true;
			}
		};
	}

}
