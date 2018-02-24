package de.feinstaubr.server.boundary;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.GlyphVector;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.feinstaubr.server.entity.SensorMeasurement;
import de.feinstaubr.server.entity.SensorMeasurementType;

@WebServlet("/display.png")
public class SensorPngCreator extends HttpServlet {
	@Inject 
	private SensorApi sensor;
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		List<SensorMeasurement> balkon = sensor.getCurrentSensorData("7620363");
		List<SensorMeasurement> wohnzimmer = sensor.getCurrentSensorData("30:ae:a4:22:ca:f4");
			

		int width = 296;
		int height = 128;
		BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D ig2 = bi.createGraphics();
//		ig2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
//                RenderingHints.VALUE_ANTIALIAS_ON);
//		try {
//			Font f = new Font("Times New Roman", Font.PLAIN, 9);
//			Font f = Font.createFont(Font.TRUETYPE_FONT, SensorPngCreator.class.getResourceAsStream("/MaterialIcons-Regular.ttf"));
//			ig2.setFont(f);
//			ig2.drawString("huhu", 0, 0);
//
//		    GlyphVector gv = f.createGlyphVector(ig2.getFontRenderContext(), Character.toChars(0xE87D));
//		    ig2.drawGlyphVector(gv, 0f, (float)gv.getGlyphMetrics(0).getBounds2D().getHeight());
//		    for (char c = 0x0000; c <= 0xFFFF; c++)
//		    {
//		      if (f.canDisplay(c))
//		      {
//		    	  //System.out.println("jo");
//		      }
//		    }
		try {
			Font writingFont = new Font("Georgia", Font.PLAIN, 12);
			Font iconFont = Font.createFont(Font.TRUETYPE_FONT, SensorPngCreator.class.getResourceAsStream("/MaterialIcons-Regular.ttf"));
			iconFont = iconFont.deriveFont(Font.PLAIN, 20);
			ig2.setFont(iconFont);
			ig2.setPaint(Color.black);
			ig2.setBackground(Color.white);
			ig2.clearRect(0, 0, width, height);
			
			Set<SensorMeasurementType> types = new TreeSet<>(new Comparator<SensorMeasurementType>() {
				@Override
				public int compare(SensorMeasurementType o1, SensorMeasurementType o2) {
					return o1.getSortOrder() - o2.getSortOrder();
				}
			});
			add(types, balkon);
			add(types, wohnzimmer);
			int i = 1;
			for (SensorMeasurementType type : types) {
				String message = new String(Character.toChars(type.getCodePoint()));
				ig2.drawString(message, 5, 21 * i++);
			}
			ig2.setFont(writingFont);
			ig2.setPaint(Color.black);
			ig2.setBackground(Color.white);

			i = 1;
			for (SensorMeasurementType type : types) {
				SensorMeasurement m = getMeasurement(type, balkon);
				if (m != null) {
					ig2.drawString(m.getValue().setScale(1, RoundingMode.HALF_UP).toString().replace('.', ','), 40, 21 * i - 7);
				}
				m = getMeasurement(type, wohnzimmer);
				if (m != null) {
					ig2.drawString(m.getValue().setScale(1, RoundingMode.HALF_UP).toString().replace('.', ','), 100, 21 * i - 7);
				}
				ig2.drawString(type.getLabel(), 150, 21 * i - 7);
				i++;
			}


			if (req.getParameter("debug") != null) {
				resp.setHeader("Content-type", "image/png");
				ImageIO.write(bi, "PNG", resp.getOutputStream());
			} else {
			    System.out.println(bi.getRGB(20, 40));

				StringBuilder buf = new StringBuilder();
				for (int y = 0; y < height; y++) {
					for (int x = 0; x < width; x++) {
						int rgb = bi.getRGB(x, y);
						Color c = new Color(rgb);
			            int gray = (c.getRed() + c.getGreen() + c.getBlue()) / 3;

						if (gray < 150) {
							buf.append('1');
						} else {
							buf.append('0');
						}
					}
				}
				resp.getWriter().print(buf.toString());
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private SensorMeasurement getMeasurement(SensorMeasurementType type, List<SensorMeasurement> balkon) {
		for (SensorMeasurement m : balkon) {
			if (m.getType().getType().equals(type.getType())) {
				return m;
			}
		}
		return null;
	}

	private void add(Set<SensorMeasurementType> types, List<SensorMeasurement> balkon) {
		for (SensorMeasurement m : balkon) {
			types.add(m.getType());
		}
	}
}
