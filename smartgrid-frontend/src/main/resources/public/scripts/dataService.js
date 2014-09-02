function createDataService() {

    var DataService = function DataServiceClass() {

        var self = this;
        var pollingInterval = 1000;
        var observers = [];
        var pollingHandler;

        var oneSecond = 1000;
        var oneMinute = 60 * oneSecond;

        var currentDate = appData.minDate;

        var baseUrl = "./data";

        var houseDataCache = {};

        var actualTimeSeriesIdx = 0;
        var predictionTimeSeriesIdx = 1;

        function createRequest(houseId, dateTimeStart, dateTimeEnd, dataResolution) {

            var request = {
                  url: baseUrl
                //, async: false
                , datatype: "json"
                , data: {
                      resolution: dataResolution ? dataResolution : "MINUTE"
                    , from: dateTimeStart ? dateTimeStart.toISOString() : appData.minDate.toISOString()
                    , to: dateTimeEnd ? dateTimeEnd.toISOString() : appData.maxDate.toISOString()
                    , houseId: houseId
                }
            };

            return request;
        }

        DataService.prototype.addObserver = function (observer) {
            observers.push(observer);
        };

        function translateEpochTimestampToMillis(dataPoint) {
            dataPoint.ts = new Date(dataPoint.ts * 1000);
        }

        function onSuccess(data) {

            for (var houseId in data) {

                if (!houseId) {
                    continue;
                }

                var currentHouseData = houseDataCache[houseId];

                var newHouseData = data[houseId];

                var newActualDataPoints = newHouseData.timeSeries[actualTimeSeriesIdx].data;
                newActualDataPoints.map(translateEpochTimestampToMillis);

                var newPredictionDataPoints = newHouseData.timeSeries[predictionTimeSeriesIdx].data;
                newPredictionDataPoints.map(translateEpochTimestampToMillis);

                if (!currentHouseData) {
                    houseDataCache[houseId] = newHouseData;
                    currentHouseData = newHouseData;
                } else {

                    var currentActualTimeSeries = currentHouseData.timeSeries[actualTimeSeriesIdx];
                    currentActualTimeSeries.data = currentActualTimeSeries.data.concat(newActualDataPoints);
                    var currentPredictionTimeSeries = currentHouseData.timeSeries[predictionTimeSeriesIdx];
                    currentPredictionTimeSeries.data = currentPredictionTimeSeries.data.concat(newPredictionDataPoints);
                }
            }

            for (var i = 0; i < observers.length; i++) {
                var observer = observers[i];
                observer.onDataArrived(houseDataCache);
            }
        }

        DataService.prototype.start = function () {

            pollingHandler = window.setInterval(function () {

                var startDateTime = currentDate;
                var endDateTime = new Date(currentDate.getTime() + appData.requestTimeIncrementInMinutes * oneMinute);
                currentDate = endDateTime;

                var houseId = -1; //appData.currentHouseSelection;
                var request = createRequest(houseId, startDateTime, endDateTime, "MINUTE");
                request.success = function (data) {
                    onSuccess(data);
                };

                $.ajax(request);

            }, pollingInterval);

        };
    };

    return new DataService();
}
