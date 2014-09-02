import org.springframework.xd.tuple.TupleBuilder

enum LoadEventAggregator {

    INSTANCE

    long lastTimestampSeen = -1

    //accumulated loads within the current time interval (= 1 second)
    def loadByHouse = [:].withDefault { key -> 0.0 }
    def loadByGrid = [:].withDefault { key -> 0.0 }

    def emptyList = []

    def  update(payloadTuple){

        //println(payloadTuple)

        //timestamp: ts defines our "logical group"
        def ts = payloadTuple.getLong("ts")
        def house_id = payloadTuple.getString("h_id")
        def grid_id = payloadTuple.hasFieldName("g_id") ? payloadTuple.getString("g_id") : "0"
        def load_current = payloadTuple.getDouble("load_actual");

        def result = emptyList

        if(lastTimestampSeen != -1 && ts > lastTimestampSeen){
            //we are past our logical group -> thus we flush the aggregated loadByHouse

            //Build aggregated loadByHouse by house
            result = []

            loadByHouse.each{ k, v ->

                def aggregatedTuple = TupleBuilder.tuple()
                        .put("ts",lastTimestampSeen)
                        .put("h_id", k)
                        .put("load_actual", v)
                        .build()
                result.add(aggregatedTuple)
            }

            loadByGrid.each {
                def aggregatedTuple = TupleBuilder.tuple()
                        .put("ts",lastTimestampSeen)
                        .put("h_id", -1)
                        .put("load_actual", it.value)
                        .build()
                result.add(aggregatedTuple)
            }

            //Cleanup now stale aggregate data
            loadByHouse.clear()
            loadByGrid.clear()
        }

        //Aggreate load per house
        loadByHouse[house_id] += load_current
        loadByGrid[grid_id] += load_current

        lastTimestampSeen = ts

        return result
    }
}

LoadEventAggregator.INSTANCE.update(payload)