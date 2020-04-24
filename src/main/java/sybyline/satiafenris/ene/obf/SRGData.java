package sybyline.satiafenris.ene.obf;

import java.util.List;

import com.google.common.collect.Lists;
import com.mojang.brigadier.StringReader;

import sybyline.satiafenris.ene.obf.repackage.bspkrs.mmv.McpMappingLoader;
import sybyline.satiafenris.ene.obf.repackage.immibis.bon.IProgressListener;

public class SRGData implements IProgressListener {

	//    SRGData srgdata = new SRGData("1.14.4_stable_58");
	//    srgdata.load();
	public SRGData(String version) {
		this.version = version;
	}

	public final String version;
	private McpMappingLoader loader = null;

	public void load() throws Exception {
		this.loader = new McpMappingLoader(version, this);
	}

	public void addMappingsTo(ScriptObfuscation data) {
		if (data == null)
			throw new NullPointerException("data");
		if (loader == null)
			return;
		
		loader.srgFieldData2CsvData.forEach((field, mcp) -> {
			ClassObfuscationData clazz = data.getOrCreate(field.getSrgOwner(), field.getObfOwner());
			clazz.putField(mcp.getMcpName(), field.getSrgName(), field.getObfName());
		});
		
		loader.srgMethodData2CsvData.forEach((method, mcp) -> {
			ClassObfuscationData clazz = data.getOrCreate(method.getSrgOwner(), method.getObfOwner());
			String[][] descriptorInfo = getMethodDescriptorInfo(method.getSrgDescriptor(), loader, data);
			clazz.putMethod(mcp.getMcpName(), method.getSrgName(), method.getObfName(), descriptorInfo[0][0], descriptorInfo[1]);
		});
	}

	public static String[][] getMethodDescriptorInfo(String descriptor, McpMappingLoader loader, ScriptObfuscation data) {
		List<String> argumentTypes = Lists.newArrayList();
		int end = descriptor.indexOf("(");
		String args = descriptor.substring(1, end);
		String returnType = descriptor.substring(end + 1);
		try {
			StringReader str = new StringReader(args);
			while (str.canRead()) {
				StringBuilder array = new StringBuilder();
				char chr = str.read();
				switch (chr) {
				case '[':
					array.append(chr);
					continue;
				case 'Z':
				case 'B':
				case 'C':
				case 'S':
				case 'I':
				case 'J':
				case 'F':
				case 'D':
					argumentTypes.add(array.append(chr).toString());
					continue;
				case 'L':
					argumentTypes.add(array.append(str.readStringUntil(';')).toString());
					continue;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		int length = argumentTypes.size();
		String[] argumentArray = new String[length];
		for (int i = 0; i < length; i++) {
			argumentArray[i] = data.mapType2mcpOrFallthrough(argumentTypes.get(i));
		}
		return new String[][] { { returnType }, argumentArray };
	}

	public String getDebugString() {
		return String.format("%s: %s/%s", text, value, max);
	}

	private int max, value;
	private String text;

	@Override
	public void start(int max, String text) {
		this.max = max;
		this.text = text;
	}

	@Override
	public void set(int value) {
		this.value = value;
	}

	@Override
	public void set(int value, String text) {
		this.value = value;
		this.text = text;
	}

	@Override
	public void setMax(int max) {
		this.max = max;
	}

}
