package ru.alexfitness.trainingschedule.model;

import android.support.annotation.NonNull;

public class Version implements Comparable<Version> {

    private String version;

    public Version(String version){
        if(version == null)
            throw new IllegalArgumentException("Version can not be null");
        if(!version.matches("^(\\d+\\.)(\\d+\\.)(\\d+\\.)(\\d+)$")) {
            throw new IllegalArgumentException("Invalid version format " + version);
        }
        this.version = version;
    }

    @Override
    public int compareTo(@NonNull Version o) {
        String[] parts1 = this.version.split("\\.");
        String[] parts2 = o.version.split("\\.");
        int i;
        for(i=0; i < parts1.length; i++){
            if(parts2.length < i+1) return 1;
            if(Integer.parseInt(parts1[i]) > Integer.parseInt(parts2[i])){
                return 1;
            } else if(Integer.parseInt(parts1[i]) < Integer.parseInt(parts2[i])){
                return -1;
            }
        }
        if(parts2.length > parts1.length) return -1;
        return 0;
    }

    @Override
    public String toString() {
        return version;
    }
}
