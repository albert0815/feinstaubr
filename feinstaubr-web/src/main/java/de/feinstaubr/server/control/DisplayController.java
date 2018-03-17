package de.feinstaubr.server.control;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.data.RangeType;
import org.jfree.data.time.Hour;
import org.jfree.data.time.TimePeriodAnchor;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import de.feinstaubr.server.boundary.ForecastApi;
import de.feinstaubr.server.boundary.MvgApi;
import de.feinstaubr.server.boundary.SensorApi;
import de.feinstaubr.server.boundary.SensorPngServlet;
import de.feinstaubr.server.entity.DisplayConfiguration;
import de.feinstaubr.server.entity.MvgDeparture;
import de.feinstaubr.server.entity.MvgStation;
import de.feinstaubr.server.entity.Sensor;
import de.feinstaubr.server.entity.SensorMeasurement;
import de.feinstaubr.server.entity.WeatherForecast;

@Stateless
public class DisplayController {
	private static final Logger LOGGER = Logger.getLogger(DisplayController.class.getName());
	
	@PersistenceContext
	private EntityManager em;
	
	@Inject 
	private SensorApi sensorApi;
	
	@Inject
	private ForecastApi forecast;
	
	@Inject
	private MvgApi mvg;
	
	@Inject
	private WeatherIconCreator weatherIconCreator;
	
	public BufferedImage generateImage(String displayId) {
		Long displayIdLong;
		try {
			displayIdLong = Long.parseLong(displayId);
		} catch (NumberFormatException e) {
			LOGGER.info("Unable to generate an image because an unknown id was passed - " + displayId);
			return null;
		}
		DisplayConfiguration config = em.find(DisplayConfiguration.class, displayIdLong);
		if (config == null) {
			LOGGER.info("Unable to generate an image because no configuration could be found for display id " + displayId);
			return null;
		}
		
		BufferedImage bi;
		try {
			switch (config.getDisplayType()) {
			case "2.9":  bi = create29(config); break;
			case "7.5":  bi = create75(config); break;
			default: throw new RuntimeException("unknown display type found in config db " + config.getDisplayType());
			}
		} catch (IOException | FontFormatException e) {
			LOGGER.log(Level.SEVERE, "error while generating picture ", e);
			return null;
		}
		
		return bi;
	}
	
	private BufferedImage create75(DisplayConfiguration config)  throws FontFormatException, IOException {
		int width = 640;
		int height = 384;

		BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D ig2 = bi.createGraphics();
		ig2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		Font iconFont = Font.createFont(Font.TRUETYPE_FONT, SensorPngServlet.class.getResourceAsStream("/MaterialIcons-Regular.ttf"));
		iconFont = iconFont.deriveFont(Font.PLAIN, 30);
		Font weatherIconFont = Font.createFont(Font.TRUETYPE_FONT, SensorPngServlet.class.getResourceAsStream("/weathericons-regular-webfont.ttf"));
		weatherIconFont = weatherIconFont.deriveFont(Font.PLAIN, 30);
		Font weatherIconFontSmall = weatherIconFont.deriveFont(Font.PLAIN, 16);
		Font iconFontSmall = iconFont.deriveFont(Font.PLAIN, 14);
		Font writeFont = Font.createFont(Font.TRUETYPE_FONT, SensorPngServlet.class.getResourceAsStream("/NotoSans-Regular.ttf"));
		writeFont = writeFont.deriveFont(Font.PLAIN, 22);
		Font smallWriteFont = writeFont.deriveFont(Font.PLAIN, 16);
		Font verySmallWriteFont = writeFont.deriveFont(Font.PLAIN, 10);
		Font headerFont = Font.createFont(Font.TRUETYPE_FONT, SensorPngServlet.class.getResourceAsStream("/NotoSans-Bold.ttf"));
		headerFont = headerFont.deriveFont(Font.PLAIN, 24);
		Font smallHeaderFont = headerFont.deriveFont(Font.PLAIN, 16);
		Font writeFontSmall = writeFont.deriveFont(Font.PLAIN, 10);
		ig2.setPaint(Color.black);
		ig2.setBackground(Color.white);
		ig2.clearRect(0, 0, width, height);


		//weather forecast
		if (config.getSensorLocation() != null) {
			List<WeatherForecast> forecast24 = forecast.getForecastFor24hours(config.getSensorLocation().getExternalId());
			ig2.drawRect(0, 0, 639, 197);
			drawForecast(ig2, weatherIconFont, weatherIconFontSmall, verySmallWriteFont, headerFont, forecast24);
		}



		//weather current
		SimpleDateFormat df = new SimpleDateFormat("dd.MM. HH:mm");
		int offsetY = 10;
		int offsetX = 270;

		for (Sensor s : config.getSensorLocation().getSensors()) {
			List<SensorMeasurement> currentSensorData = sensorApi.getCurrentSensorData(s.getSensorId());
			if (currentSensorData.isEmpty()) {
				continue;
			}
			ig2.setFont(writeFontSmall);
			ig2.drawString(s.getName() + " (" + df.format(currentSensorData.get(0).getDate()) + ")", 5 + offsetX, 10 + offsetY);
			int i = 1;
			for (SensorMeasurement m : currentSensorData) {
				String icon = new String(Character.toChars(m.getType().getCodePoint()));
				ig2.setFont(iconFont);
				ig2.drawString(icon, offsetX + 2, 36 * i + offsetY + 10);
				String trend = new String(Character.toChars(m.getTrend().getCodepoint()));
				ig2.setFont(iconFontSmall);
				ig2.drawString(trend, offsetX + 35, 36 * i + offsetY + 3);
				ig2.setFont(writeFont);
				ig2.drawString(m.getValue().setScale(1, RoundingMode.HALF_UP).toString().replace('.', ',') + m.getType().getLabel(), offsetX + 50, 36 * i + 2 + offsetY);
				i++;
			}
			offsetX += 180;
		}

		//mvg
		drawMvg(config, width, ig2, smallWriteFont, smallHeaderFont);
		
		

		
		return bi;
	}

	private void drawMvg(DisplayConfiguration config, int width, Graphics2D ig2, Font smallWriteFont,
			Font smallHeaderFont) throws IOException {
		int offsetY;
		int offsetX;
		ig2.drawRect(0, 199, 639, 184);
        final BufferedImage mvgLogo = ImageIO.read(SensorPngServlet.class.getResourceAsStream("/MVG.png"));
        ig2.drawImage(mvgLogo, 5, 205, null);

		offsetY = 250;
		offsetX = 10;
		List<MvgStation> stations = mvg.getStations(config.getSensorLocation().getLatitude(), config.getSensorLocation().getLongitude());
		int i = 0;
		int maxEntriesPerColumn = 7;
		for (MvgStation station : stations) {
			if (station.getDepartures().isEmpty() || station.getDistanceTo(config.getSensorLocation().getLatitude(), config.getSensorLocation().getLongitude()) > 1) {
				continue;
			}
			if (i >= maxEntriesPerColumn) {//second column
				offsetY = 225;
				offsetX = offsetX + width / 2;
				i = 0;
				maxEntriesPerColumn = 8;
			}
			ig2.setFont(smallHeaderFont);
			ig2.drawString(station.getName(), offsetX + 1, 18 * i + offsetY);
			ig2.setFont(smallWriteFont);
			i++;
			for (MvgDeparture dep : station.getDepartures()) {
				SimpleDateFormat format = new SimpleDateFormat("HH:mm");
				String time = format.format(dep.getDepartureTime());
				ig2.drawString(time + " - " + dep.getLine() + " - " + dep.getDestination(), offsetX + 6, 18 * i + offsetY);
				if (i >= maxEntriesPerColumn) {//second column
					offsetY = 225;
					offsetX = offsetX + width / 2;
					i = 0;
					maxEntriesPerColumn = 8;
				} else {
					i++;
				}
			}
			offsetY += 2;
		}
	}

	private void drawForecast(Graphics2D ig2, Font weatherIconFont, Font weatherIconFontSmall, Font verySmallWriteFont,
			Font headerFont, List<WeatherForecast> forecast24) {
		
		WeatherForecast next = null;
		Date now = new Date();
		for (WeatherForecast forecast : forecast24) {
			if (forecast.getForecastDate().after(now)) {
				next = forecast;
				break;
			}
		}
		Integer weatherIconForWeather = weatherIconCreator.getWeatherIconForWeather(next);
		if (weatherIconForWeather != null) {
			String upcomingWeather = new String(Character.toChars(weatherIconForWeather));
			ig2.setFont(weatherIconFont);
			ig2.drawString(upcomingWeather, 5, 35);
		}
		ig2.setFont(headerFont);
		
		String suffix = "";
		if (next != null) {
			Calendar forecastCal = Calendar.getInstance();
			forecastCal.setTime(next.getForecastDate());
			Calendar tomorrow = Calendar.getInstance();
			tomorrow.add(Calendar.DATE, 1);
			if (tomorrow.get(Calendar.DAY_OF_YEAR) == forecastCal.get(Calendar.DAY_OF_YEAR)) {
				suffix = " morgen";
			} else {
				suffix = " heute";
			}
		}
		ig2.drawString("Wetter" + suffix, 50, 30);
		
		TimeSeries series = new TimeSeries("TemperatureForecast");
		TimeSeries series2 = new TimeSeries("TemperatureCurrent");
		TimeSeries series3 = new TimeSeries("PrecipitationForecast");
		for (WeatherForecast fc : forecast24) {
			Hour hour = new Hour(fc.getForecastDate());
			series.add(hour, fc.getTemperature());
			series2.add(hour, fc.getPrecipitation());
			
			SensorMeasurement measures = sensorApi.getMeasures("7620363", "temperature", fc.getForecastDate());
			if (measures != null) {
				series3.add(hour, measures.getValue());
			}

		}
		TimeSeriesCollection dataset1 = new TimeSeriesCollection();
		dataset1.addSeries(series);
		TimeSeriesCollection dataset2 = new TimeSeriesCollection();
		dataset2.addSeries(series2);
		TimeSeriesCollection dataset3 = new TimeSeriesCollection();
		dataset3.addSeries(series3);
		dataset3.setXPosition(TimePeriodAnchor.MIDDLE);

		
        XYPlot plot = new XYPlot();

        DateAxis xAxis = new DateAxis();
		xAxis.setAxisLinePaint(Color.black);
		xAxis.setTickMarkPaint(Color.black);
		xAxis.setTickLabelFont(verySmallWriteFont);
        NumberAxis yAxis = new NumberAxis();
        yAxis.setAxisLinePaint(Color.black);
        yAxis.setTickMarkPaint(Color.black);
        yAxis.setTickLabelFont(verySmallWriteFont);
        NumberAxis yAxis2 = new NumberAxis();
        yAxis2.setAxisLinePaint(Color.black);
        yAxis2.setTickMarkPaint(Color.black);
        yAxis2.setAutoRangeMinimumSize(20);
        yAxis2.setRangeType(RangeType.POSITIVE);
        yAxis2.setTickLabelFont(verySmallWriteFont);
        plot.setDomainAxis(xAxis); 
        plot.setRangeAxis(0, yAxis);
        plot.setRangeAxis(1, yAxis2);
        plot.addRangeMarker(new ValueMarker(0, Color.black, new BasicStroke()));

        plot.setDataset(dataset1);
        plot.setDataset(1, dataset3);
        plot.setDataset(2, dataset2);
        plot.setBackgroundPaint(Color.black);
        plot.setOutlinePaint(Color.black);
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);
        plot.setAxisOffset(new RectangleInsets(4, 4, 4, 4));
        plot.getFixedDomainAxisSpace();
        
        XYSplineRenderer renderer1 = new XYSplineRenderer();
        renderer1.setSeriesStroke(0, new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 5.0f, new float[]{5.0f}, 0.0f));
        renderer1.setSeriesShapesVisible(0, false);
        renderer1.setSeriesPaint(0, Color.black);
        XYSplineRenderer renderer2 = new XYSplineRenderer();
        renderer2.setSeriesStroke(0, new BasicStroke(3));
        renderer2.setSeriesPaint(0, Color.black);

        XYBarRenderer xyBarRenderer = new XYBarRenderer();
        xyBarRenderer.setSeriesPaint(0, Color.black);
        xyBarRenderer.setShadowVisible(false);
        xyBarRenderer.setBarPainter(new StandardXYBarPainter());
        xyBarRenderer.setMargin(0.4);
		plot.setRenderer(0, renderer1);
		plot.setRenderer(1, renderer2);
		plot.setRenderer(2, xyBarRenderer);
        plot.mapDatasetToRangeAxis(2, 1);
        plot.setBackgroundPaint(Color.white);
        
        // create and return the chart panel...
        JFreeChart chart = new JFreeChart(plot);
        chart.removeLegend();
        chart.setBackgroundPaint(Color.white);
        


        
        chart.draw(ig2, new Rectangle2D.Double(1, 57, 270, 140));
        ig2.setColor(Color.black);
		// Series
		int interval = 220 / forecast24.size();
		int currentWeatherLocation = 0;
		ig2.setFont(weatherIconFontSmall);
		for (WeatherForecast fc : forecast24) {
			Integer weatherIconForWeather2 = weatherIconCreator.getWeatherIconForWeather(fc);
			if (weatherIconForWeather2 != null) {
				ig2.drawString(new String(Character.toChars(weatherIconForWeather2)), 30 + currentWeatherLocation, 58);
			}
			currentWeatherLocation += interval;
		}
	}

	private BufferedImage create29(DisplayConfiguration config) throws FontFormatException, IOException {
		int width = 296;
		int height = 128;

		BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D ig2 = bi.createGraphics();

		Font iconFont = Font.createFont(Font.TRUETYPE_FONT, SensorPngServlet.class.getResourceAsStream("/MaterialIcons-Regular.ttf"));
		iconFont = iconFont.deriveFont(Font.PLAIN, 26);
		Font iconFontSmall = iconFont.deriveFont(Font.PLAIN, 14);
		Font writeFont = Font.createFont(Font.TRUETYPE_FONT, SensorPngServlet.class.getResourceAsStream("/LiberationSans-Regular.ttf"));
		writeFont = writeFont.deriveFont(Font.PLAIN, 18);
		Font writeFontSmall = writeFont.deriveFont(Font.PLAIN, 10);
		ig2.setPaint(Color.black);
		ig2.setBackground(Color.white);
		ig2.clearRect(0, 0, width, height);

		List<SensorMeasurement> balkon = sensorApi.getCurrentSensorData("7620363");
		List<SensorMeasurement> wohnzimmer = sensorApi.getCurrentSensorData("30:ae:a4:22:ca:f4");

		ig2.setFont(writeFontSmall);
		SimpleDateFormat df = new SimpleDateFormat("dd.MM. HH:mm");
		if (!wohnzimmer.isEmpty()) {
			ig2.drawString("Wohnzimmer (" + df.format(wohnzimmer.get(0).getDate()) + ")", 5, 10);
		}
		if (!balkon.isEmpty()) {
			ig2.drawString("Balkon (" + df.format(balkon.get(0).getDate()) + ")", 155, 10);
		}

		int offsetX = 0;
		int offsetY = 15;
		for (List<SensorMeasurement> list : Arrays.asList(wohnzimmer, balkon)) {
			int i = 1;
			for (SensorMeasurement m : list) {
				String icon = new String(Character.toChars(m.getType().getCodePoint()));
				ig2.setFont(iconFont);
				ig2.drawString(icon, offsetX + 2, 28 * i + offsetY);
				String trend = new String(Character.toChars(m.getTrend().getCodepoint()));
				ig2.setFont(iconFontSmall);
				ig2.drawString(trend, offsetX + 28, 28 * i + offsetY - 7);
				ig2.setFont(writeFont);
				ig2.drawString(m.getValue().setScale(1, RoundingMode.HALF_UP).toString().replace('.', ',') + m.getType().getLabel(), offsetX + 43, 28 * i - 8 + offsetY);
				i++;
			}
			offsetX += 150;
		}
		return bi;
		
	}


}
