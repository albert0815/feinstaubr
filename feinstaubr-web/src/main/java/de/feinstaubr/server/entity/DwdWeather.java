package de.feinstaubr.server.entity;

public enum DwdWeather {
	//as per https://www.dwd.de/DE/leistungen/opendata/help/schluessel_datenformate/poi_present_weather_zuordnung_pdf.pdf?__blob=publicationFile&v=2
	//icons as per http://erikflowers.github.io/weather-icons/
	WOLKENLOS("1","wolkenlos", 61453),
	HEITER("2", "heiter", 61442),
	BEWOELKT("3", "bewoelkt", 61459),
	BEDECKT("4", "bedeckt", 61459),
	NEBEL("5", "Nebel", 61460),
	GEFRIERENDER_NEBEL("6", "gefrierender Nebel", 61460),
	LEICHTER_REGEN("7", "leichter Regen", 61465),
	REGEN("8", "Regen", 61465),
	KRAEFTIGER_REGEN("9", "kraeftiger Regen", 61465),
	GEFRIERENDER_REGEN("10", "gefrierender Regen", 61461),
	KRAEFTIGER_GEFRIERENDER_REGEN("11", "kraeftiger gefrierender Regen", 61461),
	SCHNEEREGEN("12", "Schneeregen", 61463),
	KRAEFTIGER_SCHNEEREGEN("13", "kraeftiger Schneeregen", 61463),
	LEICHTER_SCHNEEFALL("14", "leichter Schneefall", 61467),
	SCHNEEFALL("15", "Schneefall", 61467),
	KRAEFTIGER_SCHNEEFALL("16", "kraeftiger Schneefall", 61467),
	EISKOERNER("17", "Eiskoerner", 61621),
	REGENSCHAUER("18", "Regenschauer", 61466),
	KRAEFTIGER_REGENSCHAUWER("19", "kraeftiger Regenschauer", 61466),
	SCHNEEREGENSCHAUER("20", "Schneeregenschauer", 61467),
	KRAEFTIGER_SCHNEEREGENSCHAUER("21", "kraeftiger Schneeregenschauer", 61467),
	SCHNEESCHAUER("22", "Schneeschauer", 61467),
	KRAEFTIGER_SCHNEESCHAUER("23", "kraeftiger Schneeschauer", 61467),
	GRAUPELSCHAUER("24", "Graupelschauer", 61621),
	KRAEFTIGER_GRAUPELSCHAUER("25", "kraeftiger Graupelschauer", 61621),
	GEWITTER_OHNE_NIEDERSCHLAG("26", "Gewitter ohne Niederschlag", 61462),
	GEWITTER("27", "Gewitter", 61470),
	KRAEFTIGES_GEWITTER("28", "kraeftiges Gewitter", 61470),
	GEWITTER_MIT_HAGEL("29", "Gewitter mit Hagel", 61469),
	KRAEFTIGES_GEWITTER_MIT_HAGEL("30", "kraeftiges Gewitter mit Hagel", 61469),
	STURM("31", "Sturm", 61520);
	
	public String getLabel() {
		return label;
	}
	
	private String id;
	private String label;
	private int codepoint;
	
	private DwdWeather(String id, String label, int codepoint) {
		this.id = id;
		this.label = label;
		this.codepoint = codepoint;
	}
	
	public static DwdWeather getEnum(String id) {
        for(DwdWeather v : values()) {
            if(v.id.equals(id.trim())) {
            	return v;
            }
        }
        return null;
	}
	
	public String getId() {
		return id;
	}
	
	public int getCodepoint() {
		return codepoint;
	}
}
