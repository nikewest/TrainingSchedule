package ru.alexfitness.trainingschedule.util;

import org.json.JSONObject;

import ru.alexfitness.trainingschedule.model.AFWeekViewEvent;

public class AFWeekViewEventFactory {

    private static AFWeekViewEventFactory instance;

    public static AFWeekViewEventFactory getInstance() {
        if(instance==null){
            instance = new AFWeekViewEventFactory();
        }
        return instance;
    }

    private AFWeekViewEventFactory() {
    }

    public AFWeekViewEvent fromJSONObject(JSONObject jsonObject) throws Exception {
        AFWeekViewEvent weekViewEvent = new AFWeekViewEvent();

        weekViewEvent.setClient(jsonObject.getString("client"));
        weekViewEvent.setTraining(jsonObject.getString("training"));

        //int eventType = jsonObject.getInt("type");
        //weekViewEvent.setType(AFWeekViewEventType.fromInt(eventType));

        StringBuilder sb = new StringBuilder();
        /*switch (weekViewEvent.getType()) {
            case IND_TRAINING:
                sb.append(weekViewEvent.getClient()).append("\n").append(weekViewEvent.getTraining());
                break;
            default:
                sb.append(weekViewEvent.getType().getName());
        }*/
        sb.append(weekViewEvent.getClient()).append("\n").append(weekViewEvent.getTraining());

        weekViewEvent.setUid(jsonObject.getString("uid"));
        weekViewEvent.setName(sb.toString());
        weekViewEvent.setStartTime(Converter.calendarFromString(jsonObject.getString("start")));
        weekViewEvent.setEndTime(Converter.calendarFromString(jsonObject.getString("end")));
        weekViewEvent.setWrittenOff(jsonObject.getBoolean("writtenoff"));

        weekViewEvent.setAppearence();
        return weekViewEvent;
    }
}
