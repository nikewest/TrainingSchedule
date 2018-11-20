package ru.alexfitness.trainingschedule.model;

public enum AFWeekViewEventType {

    IND_TRAINING("Individaul training"),
    GROUP_TRAINING("Group training");

    private final String name;

    AFWeekViewEventType(String name) {
        this.name = name;
    }

    public static AFWeekViewEventType fromInt(int intType) throws Exception {
        switch (intType){
            case 1:
                return IND_TRAINING;
            case 2:
                return GROUP_TRAINING;
            default:
                throw new Exception("wrong int event type");
        }
    }

    public String getName(){
        return name;
    }
}
