package de.feinstaubr.server.control;

public class Mq135Calculator {
	private static final double MQ135_SCALINGFACTOR = 116.6020682;
	private static final double MQ135_EXPONENT = -2.769034857;
	public static int getPpm(int analogInput, double ro) {
		double resistacne = getResistance(analogInput);
		double d = MQ135_SCALINGFACTOR * Math.pow(resistacne / ro, MQ135_EXPONENT);
		return (int) d;
	}

	public static double getRo(int minAnalogInput) {
		double resistacne = getResistance(minAnalogInput);
		double d = resistacne * Math.exp(Math.log(MQ135_SCALINGFACTOR / 400) / MQ135_EXPONENT);
		return d;
	}
	
	private static double getResistance(int analogInput) {
		return (((double)4095/analogInput) - 1)*10;
	}
	
	
}
