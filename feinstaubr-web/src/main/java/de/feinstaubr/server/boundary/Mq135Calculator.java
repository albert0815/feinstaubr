package de.feinstaubr.server.boundary;

public class Mq135Calculator {
	private static final double MQ135_SCALINGFACTOR = 116.6020682;
	private static final double MQ135_EXPONENT = -2.769034857;
	public static int getPpm(int analogInput, double ro) {
		int resistacne = getResistance(analogInput);
		double d = MQ135_SCALINGFACTOR * Math.pow(resistacne / ro, MQ135_EXPONENT);
		return (int) d;
	}

	public static double getRo(int minAnalogInput) {
		int resistacne = getResistance(minAnalogInput);
		double d = resistacne * Math.exp(Math.log(MQ135_SCALINGFACTOR / 400) / MQ135_EXPONENT);
		return d;
	}
	
	private static int getResistance(int analogInput) {
		return (int) ((((double)1023/analogInput) - 1)*1000);
	}
	
}
