package de.feinstaubr.server.control;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;
import com.luckycatlabs.sunrisesunset.dto.Location;

import de.feinstaubr.server.entity.SensorLocation;

public class SunSetRiseCalculator {
	public static Calendar getSunSet(Calendar cal, SensorLocation sensorLocation) {
		SunriseSunsetCalculator calculator = createCalculator(sensorLocation);
		return calculator.getOfficialSunsetCalendarForDate(cal);
	}
	public static Calendar getSunRise(Calendar cal, SensorLocation sensorLocation) {
		SunriseSunsetCalculator calculator = createCalculator(sensorLocation);
		return calculator.getOfficialSunriseCalendarForDate(cal);
	}
	private static SunriseSunsetCalculator createCalculator(SensorLocation sensorLocation) {
		Location location = new Location(sensorLocation.getLatitude(), sensorLocation.getLongitude());
		SunriseSunsetCalculator calculator = new SunriseSunsetCalculator(location, TimeZone.getTimeZone("Europe/Berlin"));
		return calculator;
	}
}
