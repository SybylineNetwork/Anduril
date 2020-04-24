package sybyline.satiafenris.ene.obf;

public enum ObfuscationStatus {
	DEOBF,
	SRG,
	;

	public static final ObfuscationStatus OF_CONTEXT;

	public boolean isDeobf() {
		return this == DEOBF;
	}

	public boolean isObf() {
		return this == SRG;
	}

	static {
		boolean ene_status_is_deobf = !Boolean.getBoolean("ene_status_is_deobf");
		OF_CONTEXT = ene_status_is_deobf ? DEOBF : SRG;
	}

}
