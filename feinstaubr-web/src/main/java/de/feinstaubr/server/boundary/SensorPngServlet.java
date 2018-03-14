package de.feinstaubr.server.boundary;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.feinstaubr.server.control.DisplayController;

@WebServlet("/display.png")
public class SensorPngServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	@Inject
	private DisplayController displayController;
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		BufferedImage bi = displayController.generateImage(req.getParameter("id"));

		if (bi == null) {
			resp.sendError(404);
			return;
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


}

