package de.feinstaubr.server.boundary;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.GlyphVector;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;

import org.knowm.xchart.CategoryChart;
import org.knowm.xchart.CategoryChartBuilder;
import org.knowm.xchart.CategorySeries.CategorySeriesRenderStyle;
import org.knowm.xchart.VectorGraphicsEncoder;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.style.Styler.YAxisPosition;
import org.knowm.xchart.style.lines.SeriesLines;
import org.knowm.xchart.style.markers.SeriesMarkers;

import de.feinstaubr.server.entity.WeatherForecast;
import de.feinstaubr.server.entity.ForecastSource;
import de.feinstaubr.server.entity.MvgDeparture;
import de.feinstaubr.server.entity.MvgStation;
import de.feinstaubr.server.entity.SensorMeasurement;
import de.feinstaubr.server.entity.SensorMeasurementType;

@WebServlet("/display.png")
public class SensorPngCreator extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private static final Logger LOGGER = Logger.getLogger(SensorPngCreator.class.getName());
	
	@Inject 
	private SensorApi sensor;
	
	@Inject
	private ForecastApi forecast;
	
	@Inject
	private MvgApi mvg;
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		
		String displayType = req.getParameter("display");
		if (displayType == null) {
			displayType = "7.5";
		}
		BufferedImage bi;
		try {
			switch (displayType) {
			case "2.9":  bi = create29(); break;
			case "7.5":  bi = create75(); break;
			default: throw new RuntimeException("unknown display");
			}
		} catch (IOException | FontFormatException e) {
			LOGGER.log(Level.SEVERE, "error while generating picture ", e);
			throw new RuntimeException(e);
		}
			

		if (req.getParameter("debug") != null) {
			resp.setHeader("Content-type", "image/png");
			ImageIO.write(bi, "PNG", resp.getOutputStream());
		} else {
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			StringBuilder buf = new StringBuilder();
			for (int y = 0; y < bi.getHeight(); y++) {
				for (int x = 0; x < bi.getWidth(); x++) {
					int rgb = bi.getRGB(x, y);
					Color c = new Color(rgb);
		            int gray = (c.getRed() + c.getGreen() + c.getBlue()) / 3;

					if (gray < 150) {
						buf.append('1');
					} else {
						buf.append('0');
					}
					if (buf.length() == 8) {//this only works if size of generated pic can be diveded by 8
						Integer byteAsInt = Integer.parseInt(buf.toString(), 2);
						bytes.write(byteAsInt.byteValue());
						buf = new StringBuilder();
					}
				}
			}
			byte[] byteArray = bytes.toByteArray();
			resp.setContentLength(byteArray.length);
			resp.getOutputStream().write(byteArray);
		}

	}

	private BufferedImage create75()  throws FontFormatException, IOException {
		int width = 640;
		int height = 384;

		BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D ig2 = bi.createGraphics();
		ig2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		Font iconFont = Font.createFont(Font.TRUETYPE_FONT, SensorPngCreator.class.getResourceAsStream("/MaterialIcons-Regular.ttf"));
		iconFont = iconFont.deriveFont(Font.PLAIN, 30);
		Font weatherIconFont = Font.createFont(Font.TRUETYPE_FONT, SensorPngCreator.class.getResourceAsStream("/weathericons-regular-webfont.ttf"));
		weatherIconFont = weatherIconFont.deriveFont(Font.PLAIN, 30);
		Font weatherIconFontSmall = weatherIconFont.deriveFont(Font.PLAIN, 16);
		Font iconFontSmall = iconFont.deriveFont(Font.PLAIN, 14);
		Font writeFont = Font.createFont(Font.TRUETYPE_FONT, SensorPngCreator.class.getResourceAsStream("/NotoSans-Regular.ttf"));
		writeFont = writeFont.deriveFont(Font.PLAIN, 22);
		Font smallWriteFont = writeFont.deriveFont(Font.PLAIN, 16);
		Font headerFont = Font.createFont(Font.TRUETYPE_FONT, SensorPngCreator.class.getResourceAsStream("/NotoSans-Bold.ttf"));
		headerFont = headerFont.deriveFont(Font.PLAIN, 24);
		Font smallHeaderFont = headerFont.deriveFont(Font.PLAIN, 16);
		Font writeFontSmall = writeFont.deriveFont(Font.PLAIN, 10);
		ig2.setPaint(Color.black);
		ig2.setBackground(Color.white);
		ig2.clearRect(0, 0, width, height);


		//weather forecast
		ig2.drawRect(0, 0, 639, 197);

		WeatherForecast next = forecast.getNextForecast("10865", ForecastSource.OPEN_WEATHER);
		String upcomingWeather = new String(Character.toChars(next.getWeather().getCodepoint()));
		ig2.setFont(weatherIconFont);
		ig2.drawString(upcomingWeather, 5, 35);
		ig2.setFont(headerFont);
		ig2.drawString("Wetter", 50, 30);
		
		CategoryChart chart = new CategoryChartBuilder().build();
		chart.getStyler().setLegendVisible(false);
		chart.getStyler().setChartBackgroundColor(Color.white);
		chart.getStyler().setDatePattern("HH:mm");
		chart.getStyler().setYAxisGroupPosition(2, YAxisPosition.Right);
		chart.getStyler().setChartPadding(0);

		// Series
		List<BigDecimal> tempsForecast = new ArrayList<>();
		List<BigDecimal> tempsCurrent = new ArrayList<>();
		List<BigDecimal> precipation = new ArrayList<>();
		List<Date> datesForecast = new ArrayList<>();
		List<Date> datesCurrent = new ArrayList<>();
		List<WeatherForecast> forecast24 = forecast.getForecastFor24hours("10865", ForecastSource.OPEN_WEATHER);
		int interval = 220 / forecast24.size();
		int currentWeatherLocation = 0;
		ig2.setFont(weatherIconFontSmall);
		for (WeatherForecast fc : forecast24) {
			tempsForecast.add(fc.getTemperature());
			SensorMeasurement measures = sensor.getMeasures("7620363", "temperature", fc.getForecastDate());
			if (measures != null) {
				datesCurrent.add(measures.getDate());
				tempsCurrent.add(measures.getValue());
			}
			datesForecast.add(fc.getForecastDate());
			precipation.add(fc.getPrecipitation());
			ig2.drawString(new String(Character.toChars(fc.getWeather().getCodepoint())), 30 + currentWeatherLocation, 58);
			currentWeatherLocation += interval;
		}
		chart.addSeries("Vorhersage Temperatur", datesForecast, tempsForecast).setChartCategorySeriesRenderStyle(CategorySeriesRenderStyle.Line).setMarker(SeriesMarkers.NONE).setLineWidth(3).setLineStyle(SeriesLines.DASH_DOT);
		if (!tempsCurrent.isEmpty()) {
			chart.addSeries("Ist", datesCurrent, tempsCurrent).setChartCategorySeriesRenderStyle(CategorySeriesRenderStyle.Line).setMarker(SeriesMarkers.DIAMOND).setMarkerColor(Color.black).setLineWidth(3).setLineStyle(SeriesLines.SOLID).setLineColor(Color.black);
		}
		chart.addSeries("Vorhersage Regen", datesForecast, precipation).setChartCategorySeriesRenderStyle(CategorySeriesRenderStyle.Stick).setMarker(SeriesMarkers.NONE).setLineWidth(3).setLineStyle(SeriesLines.SOLID).setLineColor(Color.black).setYAxisGroup(2);
		BufferedImage biChart = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D ig2Chart = biChart.createGraphics();

		chart.paint(ig2Chart, 270, 130);
		ig2.drawImage(biChart, 5, 65, null);
		ig2.setColor(Color.black);

		//weather current
		ig2.setFont(writeFontSmall);
		SimpleDateFormat df = new SimpleDateFormat("dd.MM. HH:mm");
		int offsetY = 10;
		int offsetX = 270;
		List<SensorMeasurement> balkon = sensor.getCurrentSensorData("7620363");
		List<SensorMeasurement> wohnzimmer = sensor.getCurrentSensorData("30:ae:a4:22:ca:f4");
		if (!wohnzimmer.isEmpty()) {
			ig2.drawString("Wohnzimmer (" + df.format(wohnzimmer.get(0).getDate()) + ")", 5 + offsetX, 10 + offsetY);
		}
		if (!balkon.isEmpty()) {
			ig2.drawString("Balkon (" + df.format(balkon.get(0).getDate()) + ")", 180 + offsetX, 10 + offsetY);
		}
		
		offsetY += 10;

		for (List<SensorMeasurement> list : Arrays.asList(wohnzimmer, balkon)) {
			int i = 1;
			for (SensorMeasurement m : list) {
				String icon = new String(Character.toChars(m.getType().getCodePoint()));
				ig2.setFont(iconFont);
				ig2.drawString(icon, offsetX + 2, 40 * i + offsetY);
				String trend = new String(Character.toChars(m.getTrend().getCodepoint()));
				ig2.setFont(iconFontSmall);
				ig2.drawString(trend, offsetX + 35, 40 * i + offsetY - 7);
				ig2.setFont(writeFont);
				ig2.drawString(m.getValue().setScale(1, RoundingMode.HALF_UP).toString().replace('.', ',') + m.getType().getLabel(), offsetX + 50, 40 * i - 8 + offsetY);
				i++;
			}
			offsetX += 180;
		}

		//mvg
		ig2.drawRect(0, 199, 639, 184);
        final BufferedImage mvgLogo = ImageIO.read(SensorPngCreator.class.getResourceAsStream("/MVG.png"));
        ig2.drawImage(mvgLogo, 5, 205, null);

		offsetY = 250;
		offsetX = 10;
		double latitude = 48.1093;
		double longitude = 11.5804; 
		List<MvgStation> stations = mvg.getStations(latitude, longitude);
		int i = 0;
		for (MvgStation station : stations) {
			if (station.getDepartures().isEmpty() || station.getDistanceTo(latitude, longitude) > 1) {
				continue;
			}
			if (i > 6) {//second column
				offsetY = 225;
				offsetX = offsetX + width / 2;
				i = 0;
			}
			ig2.setFont(smallHeaderFont);
			ig2.drawString(station.getName(), offsetX + 1, 18 * i + offsetY);
			ig2.setFont(smallWriteFont);
			i++;
			for (MvgDeparture dep : station.getDepartures()) {
				SimpleDateFormat format = new SimpleDateFormat("HH:mm");
				String time = format.format(dep.getDepartureTime());
				ig2.drawString(time + " - " + dep.getDestination(), offsetX + 6, 18 * i + offsetY);
				if (i > 6) {//second column
					offsetY = 225;
					offsetX = offsetX + width / 2;
					i = 0;
				} else {
					i++;
				}
			}
			offsetY += 2;
		}
		
		

		
		return bi;
	}

	private BufferedImage create29() throws FontFormatException, IOException {
		int width = 296;
		int height = 128;

		BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D ig2 = bi.createGraphics();

		Font iconFont = Font.createFont(Font.TRUETYPE_FONT, SensorPngCreator.class.getResourceAsStream("/MaterialIcons-Regular.ttf"));
		iconFont = iconFont.deriveFont(Font.PLAIN, 26);
		Font iconFontSmall = iconFont.deriveFont(Font.PLAIN, 14);
		Font writeFont = Font.createFont(Font.TRUETYPE_FONT, SensorPngCreator.class.getResourceAsStream("/LiberationSans-Regular.ttf"));
		writeFont = writeFont.deriveFont(Font.PLAIN, 18);
		Font writeFontSmall = writeFont.deriveFont(Font.PLAIN, 10);
		ig2.setPaint(Color.black);
		ig2.setBackground(Color.white);
		ig2.clearRect(0, 0, width, height);

		List<SensorMeasurement> balkon = sensor.getCurrentSensorData("7620363");
		List<SensorMeasurement> wohnzimmer = sensor.getCurrentSensorData("30:ae:a4:22:ca:f4");

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

