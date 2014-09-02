import groovy.json.JsonSlurper
import org.springframework.xd.tuple.TupleBuilder

enum LoadEventTransformer {

    INSTANCE

    def slurper = new JsonSlurper()

    def transform(payloadJsonString){

        def loadEvent = slurper.parseText(payloadJsonString)

        def eventId = loadEvent.id
        def timestamp = loadEvent.timestamp;

        def grid_id=loadEvent.containsKey("grid_id") ? loadEvent.grid_id : 0
        def house_id = loadEvent.house_id
        def household_id = loadEvent.household_id
        def plug_id = loadEvent.plug_id

        def g_id = grid_id
        def h_id = g_id + ":" + house_id
        def hh_id = h_id +":" + household_id
        def p_id = hh_id + ":" + plug_id

        def date = new Date(timestamp * 1000L)
        def timeWindowDate = new Date(date.getYear(),date.getMonth(),date.getDay()+1,date.getHours(), (int)(date.getMinutes() / 15)*15,0)

        //how many seconds after timewindow start?
        def deltaSeconds = date.getTime() / 1000 - timeWindowDate.getTime() / 1000

        //compute position relative to the current timewindow start in seconds resolution (900 seconds in 15 min)
        def win15minhour_pos = deltaSeconds / 900.0

        def result = TupleBuilder.tuple()
                                 .put("ts", timestamp)
                                 .put("event_id", eventId)
                                 .put("g_id", grid_id)
                                 .put("h_id", house_id)
                                 .put("hh_id", household_id)
                                 .put("p_id", plug_id)
                                 .put("load_actual", loadEvent.value)
                                 .put("win15minhour_pos", win15minhour_pos)
                                 .build()

        return result
    }
}




//LoadEventTransformer.INSTANCE.transform("{ \"timestamp\":1378473401, \"house_id\":1, \"household_id\":1, \"plug_id\":0, \"plug_id\": 1, \"value\":11 }")
LoadEventTransformer.INSTANCE.transform(payload)