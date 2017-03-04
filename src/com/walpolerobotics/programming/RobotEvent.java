package com.walpolerobotics.programming;

import java.util.ArrayList;

/**
 * Created by jelmhurst on 3/1/2017.
 */
public class RobotEvent {
    Integer teamNumber = 0;
    public float avgScore = 0.0f;
    Integer rank = 1;
    String eventCode = new String();

    public float avgAutoLowFuel = 0.0f;
    public float avgAutoHighFuel = 0.0f;
    public float avgTeleLowFuel = 0.0f;
    public float avgTeleHighFuel = 0.0f;
    public float avgRotorPoints = 0.0f;
    public float avgRotorsEngaged = 0.0f;
    public float avgClimbPoints = 0.0f;
    public int totalClimbsInAlliances = 0;
    String playoffLevel = new String();
    String lastPlayoffLevelResult = new String();

    public ArrayList<RobotMatch> matchList = new ArrayList<RobotMatch>();
    public ArrayList<RobotMatch> playoffMatchList = new ArrayList<RobotMatch>();

}
