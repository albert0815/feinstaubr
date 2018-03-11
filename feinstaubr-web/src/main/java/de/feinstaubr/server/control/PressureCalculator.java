package de.feinstaubr.server.control;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class PressureCalculator {
	private static final double TEMPERATURE_GRADIENT = 0.0065;

	public static BigDecimal calculatePressure(BigDecimal pressureMeasurement, BigDecimal temperatureMeasurement, int height) {
//		Luftdruck auf Meereshöhe = Barometeranzeige / (1-Temperaturgradient*Höhe/Temperatur + Temperaturgradient * Höhe in Kelvin)^(0,03416/Temperaturgradient)
		
		BigDecimal temperatureOnCurrenLevelInKelvin = temperatureMeasurement.add(new BigDecimal("273.15"));
		BigDecimal TemperatureGradient = new BigDecimal(TEMPERATURE_GRADIENT);
		BigDecimal estimatedTemperatureOnSeaLevell = temperatureOnCurrenLevelInKelvin.add(TemperatureGradient.multiply(new BigDecimal(height)));
		double calculatedPressureOnSeaLevel = pressureMeasurement.doubleValue() / Math.pow((1-TEMPERATURE_GRADIENT*height/estimatedTemperatureOnSeaLevell.doubleValue()), 0.03416/TEMPERATURE_GRADIENT);
		return new BigDecimal(calculatedPressureOnSeaLevel).setScale(2, RoundingMode.HALF_UP);
	}
	
}
