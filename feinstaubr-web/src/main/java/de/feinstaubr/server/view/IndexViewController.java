package de.feinstaubr.server.view;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

import de.feinstaubr.server.boundary.Sensor;
import de.feinstaubr.server.entity.SensorMeasurement;
import de.feinstaubr.server.entity.SensorMeasurementType;

@RequestScoped
@Named
public class IndexViewController {
	@Inject
	private Sensor sensor;
	private List<SensorMeasurement> currentSensorData;
	private List<SensorMeasurementType> types;
	private String version;
	
	@PostConstruct
	public void init() {
		currentSensorData = sensor.getCurrentSensorData("7620363");
		types = sensor.getTypes();
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(IndexViewController.class.getResourceAsStream("/version")));
			version = reader.readLine();
		} catch (IOException e) {
			version = "unknown";
		}

	}
	
	public List<SensorMeasurement> getCurrentSensorData() {
		return currentSensorData;
	}

	public List<SensorMeasurementType> getSensorDataTypes() {
		return types;
	}
	
	public String getVersion() {
		return version;
	}
}
