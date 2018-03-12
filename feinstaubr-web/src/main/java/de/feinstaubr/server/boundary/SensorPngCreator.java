package de.feinstaubr.server.boundary;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.data.Range;
import org.jfree.data.RangeType;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.time.Hour;
import org.jfree.data.time.TimePeriodAnchor;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYSeries;

import de.feinstaubr.server.control.WeatherIconCreator;
import de.feinstaubr.server.entity.ForecastSource;
import de.feinstaubr.server.entity.MvgDeparture;
import de.feinstaubr.server.entity.MvgStation;
import de.feinstaubr.server.entity.SensorMeasurement;
import de.feinstaubr.server.entity.WeatherForecast;

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
	
	@Inject
	private WeatherIconCreator weatherIconCreator;
	
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
		Font verySmallWriteFont = writeFont.deriveFont(Font.PLAIN, 10);
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
		if (next != null) {
			Integer weatherIconForWeather = weatherIconCreator.getWeatherIconForWeather(next);
			if (weatherIconForWeather != null) {
				String upcomingWeather = new String(Character.toChars(weatherIconForWeather));
				ig2.setFont(weatherIconFont);
				ig2.drawString(upcomingWeather, 5, 35);
			}
		}
		ig2.setFont(headerFont);
		ig2.drawString("Wetter", 50, 30);
		

		List<WeatherForecast> forecast24 = forecast.getForecastFor24hours("10865", ForecastSource.OPEN_WEATHER);


		TimeSeries series = new TimeSeries("TemperatureForecast");
		TimeSeries series2 = new TimeSeries("TemperatureCurrent");
		TimeSeries series3 = new TimeSeries("PrecipitationForecast");
		for (WeatherForecast fc : forecast24) {
			Hour hour = new Hour(fc.getForecastDate());
			series.add(hour, fc.getTemperature());
			series2.add(hour, fc.getPrecipitation());
			
			SensorMeasurement measures = sensor.getMeasures("7620363", "temperature", fc.getForecastDate());
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

        plot.setDataset(dataset1);
        plot.setDataset(1, dataset3);
        plot.setDataset(2, dataset2);
        plot.setBackgroundPaint(Color.black);
        plot.setOutlinePaint(Color.black);
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);
        plot.setAxisOffset(new RectangleInsets(4, 4, 4, 4));
        
        XYSplineRenderer renderer1 = new XYSplineRenderer();
        renderer1.setSeriesStroke(0, new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 5.0f, new float[]{5.0f}, 0.0f));
        renderer1.setSeriesShapesVisible(0, false);
        XYSplineRenderer renderer2 = new XYSplineRenderer();
        renderer2.setSeriesStroke(0, new BasicStroke(3));

        XYBarRenderer xyBarRenderer = new XYBarRenderer();
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
				ig2.drawString(time + " - " + dep.getLine() + " - " + dep.getDestination(), offsetX + 6, 18 * i + offsetY);
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

