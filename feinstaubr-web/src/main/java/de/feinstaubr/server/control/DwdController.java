package de.feinstaubr.server.control;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.Stateless;

import de.feinstaubr.server.entity.ForecastSource;
import de.feinstaubr.server.entity.WeatherForecast;

@Stateless
public class DwdController {
	private static final Logger LOGGER = Logger.getLogger(DwdController.class.getName());
	
	public List<WeatherForecast> getForecasts(String poi) {
		List<WeatherForecast> resultList = new ArrayList<>();
		try {
			URL url = new URL("https://opendata.dwd.de/weather/local_forecasts/poi/" + poi + "-MOSMIX.csv");
			LOGGER.info("requesting forcast data from dwd from " + url);
			URLConnection connection = url.openConnection();
			try (Scanner scanner = new Scanner(connection.getInputStream())) {
				//3 comment lines on the top of the csv files
				scanner.nextLine();
				scanner.nextLine();
				scanner.nextLine();
				SimpleDateFormat dateParser = new SimpleDateFormat("dd.MM.yy HH:mm");
				dateParser.setTimeZone(TimeZone.getTimeZone("UTC"));
				while (scanner.hasNext()) {
					String nextLine = scanner.nextLine();
					try {
						String [] forecastValues = nextLine.split(";");
						WeatherForecast forecast = new WeatherForecast();
						forecast.setForecastSource(ForecastSource.DWD);
						Date date = dateParser.parse(forecastValues[0].trim() + " " + forecastValues[1].trim());
						forecast.setForecastDate(date);
						forecast.setPressure(new BigDecimal(forecastValues[31].trim().replace(',', '.')));
						forecast.setTemperature(new BigDecimal(forecastValues[2].trim().replace(',', '.')));
						forecast.setChanceOfRain(new BigDecimal(forecastValues[19].trim().replace(',', '.')));
						forecast.setCloudCoverTotal(new BigDecimal(forecastValues[26].trim().replace(',', '.')).divide(new BigDecimal("8")).multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP));
						forecast.setMeanWindDirection(new BigDecimal(forecastValues[8].trim().replace(',', '.')));
						forecast.setMeanWindSpeed(new BigDecimal(forecastValues[9].trim().replace(',', '.')));
						forecast.setPrecipitation(new BigDecimal(forecastValues[15].trim().replace(',', '.')));
						forecast.setWeather(forecastValues[23].trim());
						resultList.add(forecast);
					} catch (RuntimeException | ParseException e) {
						LOGGER.warning("unable to parse forecast csv value '" + nextLine + "' due to " + e.getMessage());
						//ignore this one
					}
				}
			}
			
		} catch (MalformedURLException e) {
			LOGGER.log(Level.SEVERE, "unable to retrieve forcasts for " + poi, e);
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "unable to retrieve forcasts for " + poi, e);
		} catch (RuntimeException e) {
			LOGGER.log(Level.SEVERE, "unable to retrieve forcasts for " + poi, e);
		}
		return resultList;
	}

}
