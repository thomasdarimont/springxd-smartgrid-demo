package io.pivotal.demo.smartgrid.frontend.timeseries;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import io.pivotal.demo.smartgrid.frontend.DataRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * @author Thomas Darimont
 */
@Component
public class AggregateCounterTimeSeriesRepository implements TimeSeriesRepository {

	private static final String AGGREGATE_COUNTER_URL = "http://localhost:9393/metrics/aggregate-counters";

	private final RestTemplate restTemplate = new RestTemplate();

	@Value("${smartgrid.frontend.aggregateCounterUrl}")
	private String aggregateCounterUrl = AGGREGATE_COUNTER_URL;

	@Override
	public Map<String, TimeSeriesCollection> getTimeSeriesData(DataRequest dataRequest) {

		Map<String, TimeSeriesCollection> result = new HashMap<>();

		int houseNum = dataRequest.getHouseId();

		IntStream houseNumStream = houseNum == -1 ? IntStream.range(0, 40) : IntStream.of(houseNum);

		List<AggregateCounterCollection> aggregateCounterCollections = houseNumStream.parallel().mapToObj(i -> new DataRequest(dataRequest, i)).map(this::fetchAggregateCounterData).filter(acc -> acc != null && !acc.getAggregateCounters().isEmpty()).collect(Collectors.toList());

		for (AggregateCounterCollection acc : aggregateCounterCollections) {

			TimeSeriesCollection tsc = convertToTimeSeriesCollection(acc);
			result.put(tsc.getName(), tsc);
		}

		return result;
	}

	private TimeSeriesCollection convertToTimeSeriesCollection(AggregateCounterCollection acc) {

		TimeSeriesCollection tsc = new TimeSeriesCollection(acc.getName());

		for (Map.Entry<String, AggregateCounter> entry : acc.getAggregateCounters().entrySet()) {

			String timeSeriesName = entry.getKey();
			AggregateCounter aggregateCounter = entry.getValue();

			List<String> timeAxis = new ArrayList<>();
			List<String> valueAxis = new ArrayList<>();

			for (Map.Entry<String, String> dataPoint : aggregateCounter.getCounts().entrySet()) {

				String pit = dataPoint.getKey();
				String value = dataPoint.getValue();

				LocalDateTime ldt = LocalDateTime.parse(pit, DateTimeFormatter.ISO_DATE_TIME);
				timeAxis.add("" + ldt.toEpochSecond(ZoneOffset.UTC));
				valueAxis.add(value);
			}

			tsc.registerTimeSeries(timeSeriesName, timeAxis, valueAxis);
		}

		return tsc;
	}

	private String makeAggregateCounterUrl(TimeSeriesType timeSeriesType, DataRequest dataRequest) {

		String aggregateCounterUrl = AGGREGATE_COUNTER_URL + "/smartgrid_"+getHouseId(dataRequest.getHouseId()) + "_load_" + timeSeriesType.name().toLowerCase();

		UriComponentsBuilder ucb = UriComponentsBuilder.fromHttpUrl(aggregateCounterUrl)
				.queryParam("resolution", dataRequest.getResolution().name().toLowerCase())
				.queryParam("from", dataRequest.getFromDateTime())
				.queryParam("to", dataRequest.getToDateTime());

		String url = ucb.build().toString();

		return url;
	}


	public AggregateCounterCollection fetchAggregateCounterData(DataRequest request) {

		int houseNum = request.getHouseId();
		AggregateCounterCollection acc = new AggregateCounterCollection(getHouseId(houseNum));

		try {
			AggregateCounter ac = restTemplate.getForObject(makeAggregateCounterUrl(TimeSeriesType.ACTUAL, request), AggregateCounter.class);
			if (ac != null) {
				acc.register(ac.getName(), ac);
			}
		} catch (Exception ex) {
			return null;
		}

		try {
			AggregateCounter ac = restTemplate.getForObject(makeAggregateCounterUrl(TimeSeriesType.PREDICTED, request), AggregateCounter.class);
			if (ac != null) {
				acc.register(ac.getName(), ac);
			}
		} catch (Exception ex) {
			return null;
		}

		return acc;
	}

	private String getHouseId(int houseNum) {
		return "h_" + houseNum;
	}
}
