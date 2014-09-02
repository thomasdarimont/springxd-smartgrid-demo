package io.pivotal.demo.smartgrid.frontend.timeseries;

import java.io.Serializable;

/**
 * @author Thomas Darimont
 */
public class DataPoint implements Serializable {

	private static final long serialVersionUID = 1L;

	private final long ts;
	private final double value;

	public DataPoint(long ts, double value) {
		this.ts = ts;
		this.value = value;
	}

	public long getTs() {
		return ts;
	}

	public double getValue() {
		return value;
	}
}