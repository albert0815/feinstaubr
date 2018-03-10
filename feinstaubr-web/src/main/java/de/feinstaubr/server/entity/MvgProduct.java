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
		case "BUS": return BUS; 
		case "UBAHN": return SUBWAY; 
		case "TRAM": return TRAM; 
		case "SBAHN": return TRAIN;
		}
		return null;
	}
}
