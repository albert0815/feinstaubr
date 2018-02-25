package de.feinstaubr.server.entity;

public enum Trend {
	UP("trending_up", 59621), DOWN("trending_down", 59619), FLAT("trending_flat", 59620);
	
	private String logo;
	private int codepoint;
	Trend(String logo, int codepoint) {
		this.logo = logo;
		this.codepoint = codepoint;
	}
	public int getCodepoint() {
		return codepoint;
	}
	public String getLogo() {
		return logo;
	}
}
