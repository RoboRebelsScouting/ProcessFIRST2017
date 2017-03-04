package com.walpolerobotics.programming;

import java.util.ArrayList;

/**
 * Created by jelmhurst on 3/1/2017.
 */
public class RobotMatch {
    int matchNumber = 0;
    int autoLowFuel = 0;
    int autoHighFuel = 0;
    int teleLowFuel = 0;
    int teleHighFuel = 0;
    int rotorsEngaged = 0;
    int climbPoints = 0;
    int finalScore = 0;
    boolean wonMatch = false;
    String alliance = new String();

    // alliance partners
    ArrayList<Integer> partners = new ArrayList<Integer>();
    ArrayList<Integer> opponents = new ArrayList<Integer>();

}
