package de.feinstaubr.server.entity;

import java.math.BigDecimal;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;

@Entity
public class SensorMeasurementType {
	@Id
	private String type;
	private int sortOrder;
	private String logo;
	private String label;
	private BigDecimal minDiffBetweenTwoValues;
	private double epsilonForSimplify;
	private String title;
	
	@OneToMany(mappedBy="type")
	@OrderBy("date DESC")
	private List<SensorMeasurement> measurements;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}


	public int getSortOrder() {
		return sortOrder;
	}

	public void setSortOrder(int sortOrder) {
		this.sortOrder = sortOrder;
	}

	public String getLogo() {
		return logo;
	}

	public void setLogo(String logo) {
		this.logo = logo;
	}


	public BigDecimal getMinDiffBetweenTwoValues() {
		return minDiffBetweenTwoValues;
	}

	public void setMinDiffBetweenTwoValues(BigDecimal minDiffBetweenTwoValues) {
		this.minDiffBetweenTwoValues = minDiffBetweenTwoValues;
	}

	public List<SensorMeasurement> getMeasurements() {
		return measurements;
	}

	public void setMeasurements(List<SensorMeasurement> measurements) {
		this.measurements = measurements;
	}

	public double getEpsilonForSimplify() {
		return epsilonForSimplify;
	}

	public void setEpsilonForSimplify(double epsilonForSimplify) {
		this.epsilonForSimplify = epsilonForSimplify;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@Override
	public String toString() {
		return "SensorMeasurementType [type=" + type + ", ...]";
	}
	
	
	
	
}
