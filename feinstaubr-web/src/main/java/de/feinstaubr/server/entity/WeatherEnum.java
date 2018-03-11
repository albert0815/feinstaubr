package de.feinstaubr.server.entity;

public enum WeatherEnum {
	//as per https://www.dwd.de/DE/leistungen/opendata/help/schluessel_datenformate/poi_present_weather_zuordnung_pdf.pdf?__blob=publicationFile&v=2
	//icons as per http://erikflowers.github.io/weather-icons/
	WOLKENLOS("1","Clear", "wolkenlos", 61453),
	HEITER("2", null, "heiter", 61442),
	BEWOELKT("3", "Clouds", "bewoelkt", 61459),
	BEDECKT("4", null, "bedeckt", 61459),
	NEBEL("5", null, "Nebel", 61460),
	GEFRIERENDER_NEBEL("6", null,"gefrierender Nebel", 61460),
	LEICHTER_REGEN("7", "Rain", "leichter Regen", 61465),
	REGEN("8", null, "Regen", 61465),
	KRAEFTIGER_REGEN("9", null, "kraeftiger Regen", 61465),
	GEFRIERENDER_REGEN("10", null, "gefrierender Regen", 61461),
	KRAEFTIGER_GEFRIERENDER_REGEN("11", null, "kraeftiger gefrierender Regen", 61461),
	SCHNEEREGEN("12", "Snow", "Schneeregen", 61463),
	KRAEFTIGER_SCHNEEREGEN("13", null, "kraeftiger Schneeregen", 61463),
	LEICHTER_SCHNEEFALL("14", null, "leichter Schneefall", 61467),
	SCHNEEFALL("15", null, "Schneefall", 61467),
	KRAEFTIGER_SCHNEEFALL("16", null, "kraeftiger Schneefall", 61467),
	EISKOERNER("17", null, "Eiskoerner", 61621),
	REGENSCHAUER("18", null, "Regenschauer", 61466),
	KRAEFTIGER_REGENSCHAUWER("19", null, "kraeftiger Regenschauer", 61466),
	SCHNEEREGENSCHAUER("20", null, "Schneeregenschauer", 61467),
	KRAEFTIGER_SCHNEEREGENSCHAUER("21", null, "kraeftiger Schneeregenschauer", 61467),
	SCHNEESCHAUER("22", null, "Schneeschauer", 61467),
	KRAEFTIGER_SCHNEESCHAUER("23", null, "kraeftiger Schneeschauer", 61467),
	GRAUPELSCHAUER("24", null, "Graupelschauer", 61621),
	KRAEFTIGER_GRAUPELSCHAUER("25", null, "kraeftiger Graupelschauer", 61621),
	GEWITTER_OHNE_NIEDERSCHLAG("26", null, "Gewitter ohne Niederschlag", 61462),
	GEWITTER("27", "Thunderstorm", "Gewitter", 61470),
	KRAEFTIGES_GEWITTER("28", null, "kraeftiges Gewitter", 61470),
	GEWITTER_MIT_HAGEL("29", null, "Gewitter mit Hagel", 61469),
	KRAEFTIGES_GEWITTER_MIT_HAGEL("30", null, "kraeftiges Gewitter mit Hagel", 61469),
	STURM("31", null, "Sturm", 61520);
	
	public String getLabel() {
		return label;
	}
	
	private String dwdId;
	private String openWeatherId;
	private String label;
	private int codepoint;
	
	private WeatherEnum(String dwdId, String openWeatherId, String label, int codepoint) {
		this.dwdId = dwdId;
		this.openWeatherId = openWeatherId;
		this.label = label;
		this.codepoint = codepoint;
	}
	
	public static WeatherEnum getEnumForDwdId(String id) {
        for(WeatherEnum v : values()) {
            if(v.dwdId.equals(id.trim())) {
            	return v;
            }
        }
        return null;
	}
	
	public String getId() {
		return dwdId;
	}
	
	public int getCodepoint() {
		return codepoint;
	}

	public static WeatherEnum getEnumForOpenWeatherId(String openweahtherId) {
        for(WeatherEnum v : values()) {
            if(openweahtherId.equals(v.openWeatherId)) {
            	return v;
            }
        }
        return null;
	}
}
