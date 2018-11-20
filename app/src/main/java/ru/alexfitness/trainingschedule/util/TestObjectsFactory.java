package ru.alexfitness.trainingschedule.util;

import android.content.Context;

import com.alamkanak.weekview.WeekView;

import ru.alexfitness.trainingschedule.model.Trainer;

public class TestObjectsFactory {
    private static TestObjectsFactory instance;
    private TestObjectsFactory(){
    }
    public static TestObjectsFactory getInstance() {
        if(instance==null){
            instance = new TestObjectsFactory();
        }
        return instance;
    }

    public Trainer getTestTrainer(){
        Trainer trainer = new Trainer();
        trainer.setUid("4e633116-9a5a-11e8-8127-00155d046a01");
        return trainer;
    }

    private class TestWeekView extends WeekView{

        public TestWeekView(Context context) {
            super(context);
        }

    }
}
