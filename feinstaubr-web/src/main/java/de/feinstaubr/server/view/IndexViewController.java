package de.feinstaubr.server.view;

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
	
	@PostConstruct
	public void init() {
		currentSensorData = sensor.getCurrentSensorData("7620363");
	}
	
	public SensorMeasurement getCurrentSensorData() {
		return currentSensorData;
	}
}
