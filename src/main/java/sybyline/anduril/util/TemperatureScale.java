package sybyline.anduril.util;

import net.minecraft.util.math.MathHelper;

public enum TemperatureScale {

	KELVIN("kelvin"),
	CELSIUS("celsius"),
	RANKINE("rankine"),
	FAHRENHEIT("fahrenheit");

	private TemperatureScale(String name) {
		this.tempname = "anduril.temp." + name;
		this.temptyname = "anduril.tempty." + name;
		this.dataname = "anduril.itemdata." + name;
		this.burnname = "anduril.burntemp." + name;
	}

	private final String tempname;
	private final String temptyname;
	private final String dataname;
	private final String burnname;

	public String getTempName() {
		return tempname;
	}

	public String getTemptyName() {
		return temptyname;
	}

	public final String getItemDataName() {
		return dataname;
	}

	public final String getBurnTempName() {
		return burnname;
	}

	public int convertTo(int value, TemperatureScale other) {
		return MathHelper.floor(this.convertTo((double)value, other));
	}

	public double convertTo(double value, TemperatureScale other) {
		if (other == null) return value;
		switch(other) {
		case KELVIN:
			if (this == KELVIN) {
				return value;
			} else if (this == CELSIUS) {
				return value + 273.15D;
			} else if (this == RANKINE) {
				return value * (5D/9D);
			} else if (this == FAHRENHEIT) {
				return value * (5D/9D) + 273.15D;
			}
		case CELSIUS:
			if (this == KELVIN) {
				return value - 273.15D;
			} else if (this == CELSIUS) {
				return value;
			} else if (this == RANKINE) {
				return value * (5D/9D) - 273.15D;
			} else if (this == FAHRENHEIT) {
				return value * (5D/9D) - 17.77D;
			}
		case RANKINE:
			if (this == KELVIN) {
				return value * (9D/5D);
			} else if (this == CELSIUS) {
				return value * (9D/5D) + 459.67D;
			} else if (this == RANKINE) {
				return value;
			} else if (this == FAHRENHEIT) {
				return value + 459.67D;
			}
		case FAHRENHEIT:
			if (this == KELVIN) {
				return value * (9D/5D) - 459.67D;
			} else if (this == CELSIUS) {
				return value * (9D/5D) + 32.00D;
			} else if (this == RANKINE) {
				return value - 459.67D;
			} else if (this == FAHRENHEIT) {
				return value;
			}
		default:
			return value;
		}
	}

}
