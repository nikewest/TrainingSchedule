package ru.alexfitness.trainingschedule.restApi;

import java.util.Date;

import ru.alexfitness.trainingschedule.util.Converter;

public final class ApiUrlBuilder {

    private static String hostUrl;
    private final static String SERVICE_URL = "hs/TrainerScheduleApi/";

    private ApiUrlBuilder(){
    }

    private static String getServiceUrl(){
        return hostUrl + SERVICE_URL;
    }

    public static String getLoginUrl(String cardHexCode){
        return getServiceUrl() + "Login/" + cardHexCode;
    }

    private static String getEventsUrl(){
        return getServiceUrl() + "Events";
    }

    public static String getEventsByTrainerUrl(String trainerUID, Date beginDate, Date endDate){
        return getEventsUrl() + "?trainerUid=" + trainerUID + "&startDate=" + Converter.dateToString1C(beginDate) + "&endDate=" + Converter.dateToString1C(endDate);
    }

    public static String getEventUrl(String eventId){
        return getServiceUrl() + "Events/" + eventId;
    }

    public static String getEventEditDatesUrl(String eventId, String startDate, String endDate){
        return getEventUrl(eventId) + "?startDate=" + startDate + "&endDate=" + endDate;
    }

    public static String getEventWriteOffUrl(String eventId, String cardId){
        return getServiceUrl() + "WriteOff/" + eventId + "?cardId=" + cardId;
    }

    public static String getClientsTrainingsUrl(String trainerID){
        return getServiceUrl() + "ClientsTrainings/" + trainerID;
    }

    public static String getNewEventUrl(String trainerUid, String clientUid, String trainingUid, String startDate, boolean paid, String subUid){
        return getEventsUrl() + "?trainerUid=" + trainerUid + "&clientUid=" + clientUid + "&startDate=" + startDate + "&paid=" + paid + (paid ? ("&trainingUid=" + trainingUid) : ("&subscriptionUid=" + subUid));
    }

    public static String getSubscriptionsUrl(){
        return getServiceUrl() + "Subscriptions";
    }

    public static String getCardUrl(String cardHexCode){
        return getServiceUrl() + "Cards/" + cardHexCode;
    }

    public static String getHostUrl() {
        return hostUrl;
    }

    public static void setHostUrl(String hostUrl) {
        ApiUrlBuilder.hostUrl = hostUrl;
    }
}
