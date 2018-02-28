package de.feinstaubr.server.entity;

public enum MvgProduct {
	BUS("directions-bus"), SUBWAY("directions-subway"), TRAM(""), TRAIN("directions-railway");

	private String icon;
	private MvgProduct(String icon) {
		this.icon =  icon;
	}
	public String getIcon() {
		return icon;
	}

	public static MvgProduct getEnum(String value) {
		switch (value) {
		case "b": return BUS; 
		case "u": return SUBWAY; 
		case "t": return TRAM; 
		case "s": return TRAIN;
		}
		return null;
	}
}
