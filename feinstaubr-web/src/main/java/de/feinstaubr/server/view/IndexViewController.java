package de.feinstaubr.server.view;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

import de.feinstaubr.server.boundary.SensorApi;
import de.feinstaubr.server.entity.Sensor;
import de.feinstaubr.server.entity.SensorMeasurement;
import de.feinstaubr.server.entity.SensorMeasurementType;

@RequestScoped
@Named
public class IndexViewController {
	@Inject
	private SensorApi sensor;
	private List<SensorMeasurement> currentSensorData;
	private List<SensorMeasurementType> types;
	private List<Sensor> sensors;
	
	private String version;
	
	private String sensorId;
	
	public void init() {
		currentSensorData = sensor.getCurrentSensorData(sensorId);
		types = sensor.getTypes();
		sensors = sensor.getSensors();
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
	
	public List<Sensor> getSensors() {
		return sensors;
	}

	public String getSensorId() {
		return sensorId;
	}

	public void setSensorId(String sensorId) {
		this.sensorId = sensorId;
	}
	
	
}
