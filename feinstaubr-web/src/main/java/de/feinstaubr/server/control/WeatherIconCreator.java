package de.feinstaubr.server.control;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;

import de.feinstaubr.server.entity.WeatherForecast;

@Stateless
public class WeatherIconCreator {
	private static final Logger LOGGER = Logger.getLogger(WeatherIconCreator.class.getName());
	
	@Inject
	private ConfigurationController config;
	
	public Integer getWeatherIconForWeather(WeatherForecast fc) {
		//there are different factors which steer the icon which we need to show
		//we first identify this based on the fc and then check the codepoint
		//in the config database
		String type;
		
		// if clouds are covered more than 80% we use the neutral icons
		// (without having a sun or a moon on it)
		if (fc.getCloudCoverTotal() != null && fc.getCloudCoverTotal().compareTo(new BigDecimal(80)) > 0) {
			type = "neutral";
		} else {
			// if there are not only clouds we also show a sun or a moon
			// (depending on the time)
			Calendar cal = Calendar.getInstance();
			cal.setTime(fc.getForecastDate());
			Calendar sunRise = SunSetRiseCalculator.getSunRise(cal, fc.getLocation());
			Calendar sunSet = SunSetRiseCalculator.getSunSet(cal, fc.getLocation());
			if (cal.after(sunRise) && cal.before(sunSet)) {
				type = "day";
			} else {
				type = "night";
			}
		}
		
		//now lookup the weather mapping for this forecast
		String weather = config.getConfiguration(fc.getForecastSource().getId(), "weather." + fc.getWeather());
		if (weather == null) {
			LOGGER.warning("unexpected weather code '" + fc.getWeather() + "' for weather provider " + fc.getForecastSource().getId());
			return null;
		}
		
		String key = weather + "." + type;
		String configuration = config.getConfiguration("weathericons", key);
		if (configuration == null) {
			LOGGER.warning("could not find any icon for " + key);
			return null;
		}
		return Integer.parseInt(configuration);
	}
}
