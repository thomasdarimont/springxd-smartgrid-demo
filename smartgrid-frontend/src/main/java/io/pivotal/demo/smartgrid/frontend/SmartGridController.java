package io.pivotal.demo.smartgrid.frontend;

import java.util.Map;

import io.pivotal.demo.smartgrid.frontend.timeseries.DataPointResolution;
import io.pivotal.demo.smartgrid.frontend.timeseries.TimeSeriesCollection;
import io.pivotal.demo.smartgrid.frontend.timeseries.TimeSeriesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Thomas Darimont
 */
@RestController
public class SmartGridController {

	private final TimeSeriesRepository timeSeriesRepository;

	@Autowired
	public SmartGridController(TimeSeriesRepository timeSeriesRepository) {
		this.timeSeriesRepository = timeSeriesRepository;
	}

	@RequestMapping("/data")
	public Map<String, TimeSeriesCollection> getDataSet(
			@RequestParam(value = "houseId", defaultValue ="-1") int houseId,
			@RequestParam(value = "from",defaultValue = "2013-09-01T00:00:00.000Z") String fromDateTime,
			@RequestParam(value = "to",defaultValue = "2013-09-02T00:00:00.000Z") String toDateTime,
			@RequestParam(value = "resolution",defaultValue = "MINUTE") DataPointResolution resolution
			) {

		DataRequest dr = new DataRequest();
		dr.setHouseId(houseId);
		dr.setFromDateTime(fromDateTime);
		dr.setToDateTime(toDateTime);
		dr.setResolution(resolution);

		return timeSeriesRepository.getTimeSeriesData(dr);
	}

	@RequestMapping("/dump")
	public String dump() {

		System.out.println(getDataSet(-1,"2013-09-01T00:00:00.000Z", "2013-09-02T00:00:00.000Z", DataPointResolution.MINUTE));

		return "ok";
	}
}
