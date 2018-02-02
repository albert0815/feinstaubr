package de.feinstaubr.server.view;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

import de.feinstaubr.server.boundary.Sensor;
import de.feinstaubr.server.entity.SensorMeasurement;

@RequestScoped
@Named
public class IndexViewController {
	@Inject
	private Sensor sensor;
	private SensorMeasurement currentSensorData;
	private String version;
	
	@PostConstruct
	public void init() {
		currentSensorData = sensor.getCurrentSensorData("7620363");
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(IndexViewController.class.getResourceAsStream("/version")));
			version = reader.readLine();
		} catch (IOException e) {
			version = "unknown";
		}

	}
	
	public SensorMeasurement getCurrentSensorData() {
		return currentSensorData;
	}
	
	public String getVersion() {
		return version;
	}
}
