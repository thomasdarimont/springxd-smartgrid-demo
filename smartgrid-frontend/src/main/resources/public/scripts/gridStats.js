function createGridStats(){


    var GridStats = function GridStatsClass() {

        GridStats.prototype.onDataArrived = function (data) {

            var totalLoad = 0;
            for(var property in data){

                var houseData = data[property];
                var dataPoints = houseData.timeSeries[0].data;

                var lastDataPoint = dataPoints[dataPoints.length-1];

                totalLoad += lastDataPoint.value;
            }

            totalLoad *= 0.25;

            $(".kpi-total-load").text(Math.round(totalLoad));
        };
    };

    return new GridStats();
}