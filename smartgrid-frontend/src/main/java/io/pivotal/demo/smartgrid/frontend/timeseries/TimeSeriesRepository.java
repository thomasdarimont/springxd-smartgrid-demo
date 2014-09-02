package io.pivotal.demo.smartgrid.frontend.timeseries;

import java.util.Map;

import io.pivotal.demo.smartgrid.frontend.DataRequest;

/**
 * @author Thomas Darimont
 */
public interface TimeSeriesRepository {

	Map<String,TimeSeriesCollection> getTimeSeriesData(DataRequest dataRequest);
}
