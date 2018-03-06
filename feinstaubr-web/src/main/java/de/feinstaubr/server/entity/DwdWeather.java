package de.feinstaubr.server.entity;

public enum DwdWeather {
	//as per https://www.dwd.de/DE/leistungen/opendata/help/schluessel_datenformate/poi_present_weather_zuordnung_pdf.pdf?__blob=publicationFile&v=2
	WOLKENLOS("1","wolkenlos"),
	HEITER("2", "heiter"),
	BEWOELKT("3", "bewoelkt"),
	BEDECKT("4", "bedeckt"),
	NEBEL("5", "Nebel"),
	GEFRIERENDER_NEBEL("6", "gefrierender Nebel"),
	LEICHTER_REGEN("7", "leichter Regen"),
	REGEN("8", "Regen"),
	KRAEFTIGER_REGEN("9", "kraeftiger Regen"),
	GEFRIERENDER_REGEN("10", "gefrierender Regen"),
	KRAEFTIGER_GEFRIERENDER_REGEN("11", "kraeftiger gefrierender Regen"),
	SCHNEEREGEN("12", "Schneeregen"),
	KRAEFTIGER_SCHNEEREGEN("13", "kraeftiger Schneeregen"),
	LEICHTER_SCHNEEFALL("14", "leichter Schneefall"),
	SCHNEEFALL("15", "Schneefall"),
	KRAEFTIGER_SCHNEEFALL("16", "kraeftiger Schneefall"),
	EISKOERNER("17", "Eiskoerner"),
	REGENSCHAUER("18", "Regenschauer"),
	KRAEFTIGER_REGENSCHAUWER("19", "kraeftiger Regenschauer"),
	SCHNEEREGENSCHAUER("20", "Schneeregenschauer"),
	KRAEFTIGER_SCHNEEREGENSCHAUER("21", "kraeftiger Schneeregenschauer"),
	SCHNEESCHAUER("22", "Schneeschauer"),
	KRAEFTIGER_SCHNEESCHAUER("23", "kraeftiger Schneeschauer"),
	GRAUPELSCHAUER("24", "Graupelschauer"),
	KRAEFTIGER_GRAUPELSCHAUER("25", "kraeftiger Graupelschauer"),
	GEWITTER_OHNE_NIEDERSCHLAG("26", "Gewitter ohne Niederschlag"),
	GEWITTER("27", "Gewitter"),
	KRAEFTIGES_GEWITTER("28", "kraeftiges Gewitter"),
	GEWITTER_MIT_HAGEL("29", "Gewitter mit Hagel"),
	KRAEFTIGES_GEWITTER_MIT_HAGEL("30", "kraeftiges Gewitter mit Hagel"),
	STURM("31", "Sturm");
	
	public String getLabel() {
		return label;
	}
	
	private String id;
	private String label;
	
	private DwdWeather(String id, String label) {
		this.id = id;
		this.label = label;
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
}
