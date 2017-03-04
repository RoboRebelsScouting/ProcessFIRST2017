package com.walpolerobotics.programming;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class Main {

    // api access code - base64 encoded username:authorizationkey;
    public static String accessCode = "cm9ib3JlYmVsczpDN0YyQjlFOC1DQ0JDLTRBRkYtQTFERC04MkJGRTEzMDhGQzgK";

    public String season = "2017";


    int[] robotGalileoList = {};
    int[] robotNewtonList = {};
    int[] robotArchimedesList = {};
    int[] robotHopperList = {};
    int[] robotTeslaList = {};
    int[] robotCurieList = {};
    int[] robotCarsonList = {};
    int[] robotCarverList = {};

    int[] robotList;

    //int[] robotList = {1153};

    // list of teams attending a given event
    public ArrayList<RobotStats> eventTeamList = new ArrayList<RobotStats>();

    // set this if we want to get data on robots attending a specific event
    public boolean getByEvent = true;
    // set this if we only want to get information about the specific event only
    // if false, we will get data on all events attended by all robots attending the specific event
    public boolean getOnlyEvent = true;
    // Week 0 = WEEK0
    // Granite State = NHGRS
    //
    public String eventToGet = "NHGRS";

    public static void main(String[] args) {
        new Main().getDataFromAPI();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    public void getDataFromAPI() {


        // get list of all events
        getEventList(season);


        if (getByEvent == true) {
            getAttending(eventToGet);
        } else {
            for (int rn : robotList) {
                RobotStats rs = new RobotStats();
                rs.teamNumber = rn;
                eventTeamList.add(rs);
            }
        }

        // get score data of all robots attending event
        // get list of qual matches each robot at the event participated in
        getQualMatchData();
        // get list of playoff matches each robot at the event participated in
        getPlayoffMatchData();
        // get scoring data for each robot-match combination
        getScoreDataForEvent();

        getRankData();

        printStats();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public void getRankData() {

        System.out.println("Get Rank Data");

        for (RobotStats eventTeam : eventTeamList) {
            for (RobotEvent rm : eventTeam.eventList) {
                URL url;

                Integer teamNumber = eventTeam.teamNumber;

                //https://frc-api.firstinspires.org/v2.0/season/teams?teamNumber=101&eventCode=CMP
                String queryString = "https://frc-api.firstinspires.org/v2.0/";
                queryString += season;
                queryString += "/rankings/" + rm.eventCode + "?teamNumber=" + teamNumber.toString();

                try {
                    url = new URL(queryString);

                    try {
                        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

                        connection.setRequestMethod("GET");
                        connection.setDoOutput(true);
                        connection.setRequestProperty("Authorization", "Basic " + accessCode);
                        connection.setRequestProperty("Accept", "application/xml ");

                        // get data returned
                        String responseString = "";
                        try {
                            BufferedReader br =
                                    new BufferedReader((new InputStreamReader(connection.getInputStream())));

                            String input;
                            while ((input = br.readLine()) != null) {
                                //System.out.println(input);
                                responseString += input;
                            }
                            br.close();

                            // parse the xml file returned
                            DocumentBuilderFactory dbFactory
                                    = DocumentBuilderFactory.newInstance();
                            try {
                                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

                                try {
                                    InputSource is = new InputSource(new StringReader(responseString));
                                    Document doc = dBuilder.parse(is);
                                    doc.getDocumentElement().normalize();
                                    //System.out.println("Root element :" + doc.getDocumentElement().getNodeName());

                                    NodeList teamList = doc.getElementsByTagName("TeamRanking");
                                    for (int c = 0; c < teamList.getLength(); c++) {
                                        Node eNode = teamList.item(c);
                                        //System.out.println("Event: " + eNode.getNodeName());
                                        if (eNode.getNodeType() == Node.ELEMENT_NODE) {

                                            Element eElement = (Element) eNode;
                                            Integer rank = Integer.parseInt(eElement.getElementsByTagName("rank").item(0).getTextContent());
                                            Float avgScore = Float.parseFloat(eElement.getElementsByTagName("qualAverage").item(0).getTextContent());
                                            //System.out.println("\nNumber : " + eElement.getElementsByTagName("teamNumber").item(0).getTextContent());
                                            if (getTeamByNumber(eventTeam.teamNumber) != null) {
                                                if (getRobotEvent(getTeamByNumber(eventTeam.teamNumber),rm.eventCode) != null) {
                                                    getRobotEvent(getTeamByNumber(eventTeam.teamNumber),rm.eventCode).rank = rank;
                                                    getRobotEvent(getTeamByNumber(eventTeam.teamNumber),rm.eventCode).avgScore = avgScore;
                                                }
                                            }
                                        }
                                    }

                                } catch (SAXException e) {
                                    e.printStackTrace();
                                }
                            } catch (ParserConfigurationException e) {
                                e.printStackTrace();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public void getQualMatchData() {
        System.out.println("Get Qualification Match Data");

        System.out.println("getQualMatchData");
        for (RobotStats eventTeam : eventTeamList) {
            for (RobotEvent re : eventTeam.eventList) {
                URL url;

                Integer teamNumber = eventTeam.teamNumber;

                System.out.println("Get Qual Match Data for team: " + teamNumber);

                //https://frc-api.firstinspires.org/v2.0/season/teams?teamNumber=101&eventCode=CMP
                String queryString = "https://frc-api.firstinspires.org/v2.0/";
                queryString += season;
                queryString += "/matches/" + re.eventCode + "/Qualification?teamNumber=" + teamNumber.toString();

                try {
                    url = new URL(queryString);

                    try {
                        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

                        connection.setRequestMethod("GET");
                        connection.setDoOutput(true);
                        connection.setRequestProperty("Authorization", "Basic " + accessCode);
                        connection.setRequestProperty("Accept", "application/xml ");

                        // get data returned
                        String responseString = "";
                        try {
                            BufferedReader br =
                                    new BufferedReader((new InputStreamReader(connection.getInputStream())));

                            String input;
                            while ((input = br.readLine()) != null) {
                                //System.out.println(input);
                                responseString += input;
                            }
                            br.close();

                            // parse the xml file returned
                            DocumentBuilderFactory dbFactory
                                    = DocumentBuilderFactory.newInstance();
                            try {
                                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

                                try {
                                    InputSource is = new InputSource(new StringReader(responseString));
                                    Document doc = dBuilder.parse(is);
                                    doc.getDocumentElement().normalize();
                                    //System.out.println("Root element :" + doc.getDocumentElement().getNodeName());

                                    NodeList matchList = doc.getElementsByTagName("Match");
                                    //System.out.println("got " + matchList.getLength() + " matches");
                                    for (int c = 0; c < matchList.getLength(); c++) {
                                        Node eNode = matchList.item(c);
                                        if (eNode.getNodeType() == Node.ELEMENT_NODE) {

                                            Element eElement = (Element) eNode;
                                            Integer matchNumber = 0;
                                            Integer blueScore = 0;
                                            Integer redScore = 0;
                                            for (int node_c = 0; node_c < eNode.getChildNodes().getLength(); node_c++) {
                                                if (eNode.getChildNodes().item(node_c).getNodeName().equals("scoreBlueFinal")) {
                                                    Node scoreNode = eNode.getChildNodes().item(node_c);
                                                    for (int node_c2 = 0; node_c2 < scoreNode.getChildNodes().getLength(); node_c2++) {
                                                        //System.out.println("score node text: " + scoreNode.getChildNodes().item(node_c2).getTextContent());
                                                        blueScore = Integer.parseInt(scoreNode.getChildNodes().item(node_c2).getTextContent());
                                                    }
                                                    //blueScore = Integer.parseInt(eNode.getChildNodes().item(node_c).getChildNodes().getTextContent());
                                                }
                                                if (eNode.getChildNodes().item(node_c).getNodeName().equals("scoreRedFinal")) {
                                                    Node scoreNode = eNode.getChildNodes().item(node_c);
                                                    for (int node_c2 = 0; node_c2 < scoreNode.getChildNodes().getLength(); node_c2++) {
                                                        //System.out.println("score node text: " + scoreNode.getChildNodes().item(node_c2).getTextContent());
                                                        redScore = Integer.parseInt(scoreNode.getChildNodes().item(node_c2).getTextContent());
                                                    }
                                                    //redScore = Integer.parseInt(eNode.getChildNodes().item(node_c).getTextContent());
                                                }
                                                if (eNode.getChildNodes().item(node_c).getNodeName().equals("matchNumber")) {
                                                    matchNumber = Integer.parseInt(eNode.getChildNodes().item(node_c).getTextContent());
                                                }
                                            }
                                            //System.out.println("matchNumber:" + matchNumber);
                                            //System.out.println("blueScore:" + blueScore);
                                            //System.out.println("redScore:" + redScore);
                                            // get the list of teams for the match
                                            NodeList teamList = ((Element) eNode).getElementsByTagName("Team");
                                            for (int c2 = 0; c2 < teamList.getLength(); c2++) {
                                                Node eNode2 = teamList.item(c2);
                                                if (eNode.getNodeType() == Node.ELEMENT_NODE) {
                                                    NodeList teamNodeList = eNode2.getChildNodes();
                                                    Integer teamNumberData = 0;
                                                    String station = new String();
                                                    for (int c4 = 0; c4 < teamNodeList.getLength(); c4++) {
                                                        if (teamNodeList.item(c4).getNodeName().equals("teamNumber")) {
                                                            teamNumberData = Integer.parseInt(teamNodeList.item(c4).getTextContent());
                                                        }
                                                        if (teamNodeList.item(c4).getNodeName().equals("station")) {
                                                            station = teamNodeList.item(c4).getTextContent();
                                                        }
                                                    }
                                                    System.out.println("Team: " + teamNumberData.intValue());
                                                    if (teamNumberData.intValue() == eventTeam.teamNumber) {
                                                        if (getTeamByNumber(eventTeam.teamNumber) != null) {
                                                            if (getRobotEvent(getTeamByNumber(eventTeam.teamNumber), re.eventCode) != null) {
                                                                // create match data object and add it to robot event list
                                                                RobotMatch rmatch = new RobotMatch();
                                                                rmatch.matchNumber = matchNumber;
                                                                if (station.startsWith("Blue")) {
                                                                    rmatch.alliance = "Blue";
                                                                    rmatch.finalScore = blueScore;
                                                                } else {
                                                                    rmatch.alliance = "Red";
                                                                    rmatch.finalScore = redScore;
                                                                }
                                                                getRobotEvent(getTeamByNumber(eventTeam.teamNumber), re.eventCode).matchList.add(rmatch);
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } catch (SAXException e) {
                                    e.printStackTrace();
                                }
                            } catch (ParserConfigurationException e) {
                                e.printStackTrace();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public void getPlayoffMatchData() {
        System.out.println("Get Playoff Match Data");

        for (RobotStats eventTeam : eventTeamList) {
            for (RobotEvent rm : eventTeam.eventList) {
                URL url;

                Integer teamNumber = eventTeam.teamNumber;

                //https://frc-api.firstinspires.org/v2.0/season/teams?teamNumber=101&eventCode=CMP
                String queryString = "https://frc-api.firstinspires.org/v2.0/";
                queryString += season;
                queryString += "/matches/" + rm.eventCode + "/Playoff?teamNumber=" + teamNumber.toString();

                try {
                    url = new URL(queryString);

                    try {
                        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

                        connection.setRequestMethod("GET");
                        connection.setDoOutput(true);
                        connection.setRequestProperty("Authorization", "Basic " + accessCode);
                        connection.setRequestProperty("Accept", "application/xml ");

                        // get data returned
                        String responseString = "";
                        try {
                            BufferedReader br =
                                    new BufferedReader((new InputStreamReader(connection.getInputStream())));

                            String input;
                            while ((input = br.readLine()) != null) {
                                //System.out.println(input);
                                responseString += input;
                            }
                            br.close();

                            // parse the xml file returned
                            DocumentBuilderFactory dbFactory
                                    = DocumentBuilderFactory.newInstance();
                            try {
                                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

                                try {
                                    InputSource is = new InputSource(new StringReader(responseString));
                                    Document doc = dBuilder.parse(is);
                                    doc.getDocumentElement().normalize();
                                    //System.out.println("Root element :" + doc.getDocumentElement().getNodeName());

                                    NodeList matchList = doc.getElementsByTagName("Match");
                                    //System.out.println("got " + matchList.getLength() + " matches");
                                    for (int c = 0; c < matchList.getLength(); c++) {
                                        Node eNode = matchList.item(c);
                                        if (eNode.getNodeType() == Node.ELEMENT_NODE) {

                                            Element eElement = (Element) eNode;
                                            Integer matchNumber = 0;
                                            Integer blueScore = 0;
                                            Integer redScore = 0;
                                            for (int node_c = 0; node_c < eNode.getChildNodes().getLength(); node_c++) {
                                                String nodeNamea = eNode.getChildNodes().item(node_c).getNodeName();

                                                if (eNode.getChildNodes().item(node_c).getNodeName().equals("matchNumber")) {
                                                    matchNumber = Integer.parseInt(eNode.getChildNodes().item(node_c).getTextContent());
                                                }
                                                if (eNode.getChildNodes().item(node_c).getNodeName().equals("scoreBlueFinal")) {
                                                    blueScore = Integer.parseInt(eNode.getChildNodes().item(node_c).getTextContent());
                                                }
                                                if (eNode.getChildNodes().item(node_c).getNodeName().equals("scoreRedFinal")) {
                                                    redScore = Integer.parseInt(eNode.getChildNodes().item(node_c).getTextContent());
                                                }
                                            }


                                            // get the list of teams for the match
                                            NodeList teamList = ((Element) eNode).getElementsByTagName("Team");
                                            ArrayList<Integer> blueAlliance = new ArrayList<Integer>();
                                            ArrayList<Integer> redAlliance = new ArrayList<Integer>();

                                            for (int c2 = 0; c2 < teamList.getLength(); c2++) {
                                                Node eNode2 = teamList.item(c2);
                                                if (eNode.getNodeType() == Node.ELEMENT_NODE) {
                                                    NodeList teamNodeList = eNode2.getChildNodes();
                                                    Integer teamNumberData = 0;
                                                    String station = new String();

                                                    for (int c4 = 0; c4 < teamNodeList.getLength(); c4++) {
                                                        String nodeName = teamNodeList.item(c4).getNodeName();
                                                        if (teamNodeList.item(c4).getNodeName().equals("teamNumber")) {
                                                            teamNumberData = Integer.parseInt(teamNodeList.item(c4).getTextContent());
                                                        }
                                                        if (teamNodeList.item(c4).getNodeName().equals("station")) {
                                                            station = teamNodeList.item(c4).getTextContent();
                                                        }
                                                    }
                                                    // build alliance data, but don't current robot number
                                                    if (teamNumberData.intValue() != eventTeam.teamNumber) {
                                                        if (station.startsWith("Blue")) {
                                                            blueAlliance.add(teamNumberData);
                                                        } else {
                                                            redAlliance.add(teamNumberData);
                                                        }
                                                    }
                                                    if (teamNumberData.intValue() == eventTeam.teamNumber) {
                                                        if (getTeamByNumber(eventTeam.teamNumber) != null) {
                                                            if (getRobotEvent(getTeamByNumber(eventTeam.teamNumber),rm.eventCode) != null) {
                                                                RobotMatch rmatch = new RobotMatch();
                                                                rmatch.matchNumber = matchNumber;
                                                                if (station.startsWith("Blue")) {
                                                                    rmatch.finalScore = blueScore;
                                                                    rmatch.alliance = "Blue";
                                                                    if (blueScore.intValue() > redScore.intValue()) {
                                                                        rmatch.wonMatch = true;
                                                                    }
                                                                } else {
                                                                    rmatch.finalScore = redScore;
                                                                    if (redScore.intValue() > blueScore.intValue()) {
                                                                        rmatch.wonMatch = true;
                                                                    }
                                                                    rmatch.alliance = "Red";
                                                                }
                                                                //System.out.println("Event: " + rm.eventCode + " Team: " + eventTeam.teamNumber + " playoff match: " + matchNumber + " score: " + rmatch.finalScore + " won: " + rmatch.wonMatch);
                                                                getRobotEvent(getTeamByNumber(eventTeam.teamNumber),rm.eventCode).playoffMatchList.add(rmatch);
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                            // now get robot match, add the alliance
                                            if (getPlayoffMatch(getRobotEvent(getTeamByNumber(eventTeam.teamNumber),rm.eventCode),matchNumber).alliance.equals("Red")) {
                                                for (Integer i : redAlliance) {
                                                    getPlayoffMatch(getRobotEvent(getTeamByNumber(eventTeam.teamNumber),rm.eventCode),matchNumber).partners.add(i);
                                                }
                                                for (Integer i : blueAlliance) {
                                                    getPlayoffMatch(getRobotEvent(getTeamByNumber(eventTeam.teamNumber),rm.eventCode),matchNumber).opponents.add(i);
                                                }
                                            } else {
                                                for (Integer i : blueAlliance) {
                                                    getPlayoffMatch(getRobotEvent(getTeamByNumber(eventTeam.teamNumber),rm.eventCode),matchNumber).partners.add(i);
                                                }
                                                for (Integer i : redAlliance) {
                                                    getPlayoffMatch(getRobotEvent(getTeamByNumber(eventTeam.teamNumber),rm.eventCode),matchNumber).opponents.add(i);
                                                }
                                            }
                                        }
                                    }
                                } catch (SAXException e) {
                                    e.printStackTrace();
                                }
                            } catch (ParserConfigurationException e) {
                                e.printStackTrace();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }

                // now calculate maximum playoff rank
                // do this by determining the number of times the alliance *opponents* changed during the players
                // since each time the opponents changed, it means that robot advanced to the next round
                int[] prevAlliance = new int[3];
                int[] currAlliance = new int[3];
                int allianceChanges = 0;
                String playoffLevel = new String();
                String lastPlayoffResult = new String();
                for (int c = 0; c < rm.playoffMatchList.size(); c++) {
                    if (c == 0) {
                        currAlliance[0] = rm.playoffMatchList.get(c).opponents.get(0);
                        currAlliance[1] = rm.playoffMatchList.get(c).opponents.get(1);
                        currAlliance[2] = rm.playoffMatchList.get(c).opponents.get(2);
                    } else {;
                        prevAlliance[0] = currAlliance[0];
                        prevAlliance[1] = currAlliance[1];
                        prevAlliance[2] = currAlliance[2];

                        currAlliance[0] = rm.playoffMatchList.get(c).opponents.get(0);
                        currAlliance[1] = rm.playoffMatchList.get(c).opponents.get(1);
                        currAlliance[2] = rm.playoffMatchList.get(c).opponents.get(2);

                        if (currAlliance[0] != prevAlliance[0]) {
                            allianceChanges++;
                        }
                    }
                    if (c == rm.playoffMatchList.size()-1) {
                        if (rm.playoffMatchList.get(c).wonMatch == true) {
                            lastPlayoffResult = "Won";
                        } else {
                            lastPlayoffResult = "Lost";
                        }
                    }
                }
                if (allianceChanges == 2) {
                    playoffLevel = "Final";
                } else if (allianceChanges == 1) {
                    playoffLevel = "SF";
                } else if (allianceChanges == 0) {
                    playoffLevel = "QF";
                }
                getRobotEvent(getTeamByNumber(eventTeam.teamNumber),rm.eventCode).playoffLevel = playoffLevel;
                getRobotEvent(getTeamByNumber(eventTeam.teamNumber),rm.eventCode).lastPlayoffLevelResult = lastPlayoffResult;

            }
        }

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public void getScoreDataForEvent() {
        System.out.println("Get Score Data");

        for (RobotStats eventTeam : eventTeamList) {
            for (RobotEvent re : eventTeam.eventList) {
                URL url;

                Integer teamNumber = eventTeam.teamNumber;

                //https://frc-api.firstinspires.org/v2.0/season/teams?teamNumber=101&eventCode=CMP
                String queryString = "https://frc-api.firstinspires.org/v2.0/";
                queryString += season;
                queryString += "/scores/" + re.eventCode + "/Qualification?teamNumber=" + teamNumber.toString();

                try {
                    url = new URL(queryString);

                    try {
                        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

                        connection.setRequestMethod("GET");
                        connection.setDoOutput(true);
                        connection.setRequestProperty("Authorization", "Basic " + accessCode);
                        connection.setRequestProperty("Accept", "application/xml ");

                        // get data returned
                        String responseString = "";
                        try {
                            BufferedReader br =
                                    new BufferedReader((new InputStreamReader(connection.getInputStream())));

                            String input;
                            while ((input = br.readLine()) != null) {
                                //System.out.println(input);
                                responseString += input;
                            }
                            br.close();

                            // parse the xml file returned
                            DocumentBuilderFactory dbFactory
                                    = DocumentBuilderFactory.newInstance();
                            try {
                                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

                                try {
                                    InputSource is = new InputSource(new StringReader(responseString));
                                    Document doc = dBuilder.parse(is);
                                    doc.getDocumentElement().normalize();
                                    //System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
                                    NodeList allianceList = doc.getElementsByTagName("Alliance");
                                    for (int c = 0; c < allianceList.getLength(); c++) {
                                        // get parent node of parent node
                                        //<MatchScores> -> <Alliances> -> <Alliance>
                                        Node pNode = allianceList.item(c).getParentNode().getParentNode();
                                        Element pElement = (Element) pNode;
                                        Integer matchNumber = Integer.parseInt(pElement.getElementsByTagName("matchNumber").item(0).getTextContent());

                                        Node eNode = allianceList.item(c);
                                        if (eNode.getNodeType() == Node.ELEMENT_NODE) {

                                            Element eElement = (Element) eNode;
                                            Integer autoFuelHigh = Integer.parseInt(eElement.getElementsByTagName("autoFuelHigh").item(0).getTextContent());
                                            Integer autoFuelLow = Integer.parseInt(eElement.getElementsByTagName("autoFuelLow").item(0).getTextContent());
                                            Integer teleFuelHigh = Integer.parseInt(eElement.getElementsByTagName("teleopFuelHigh").item(0).getTextContent());
                                            Integer teleFuelLow = Integer.parseInt(eElement.getElementsByTagName("teleopFuelLow").item(0).getTextContent());

                                            // get climbing points from the touchPad statuses
                                            String[] touchPadStates = new String[3];
                                            touchPadStates[0] = eElement.getElementsByTagName("touchpadNear").item(0).getTextContent();
                                            touchPadStates[1] = eElement.getElementsByTagName("touchpadMiddle").item(0).getTextContent();
                                            touchPadStates[2] = eElement.getElementsByTagName("touchpadFar").item(0).getTextContent();
                                            int teleopClimbPoints = 0;
                                            for (int c2 = 0; c2 < touchPadStates.length; c2++) {
                                                if (touchPadStates[c2].equalsIgnoreCase("ReadyForTakeoff")) {
                                                    teleopClimbPoints += 50;
                                                }
                                            }
                                            boolean[] rotorEngaged = new boolean[4];
                                            rotorEngaged[0] = Boolean.parseBoolean(eElement.getElementsByTagName("rotor1Engaged").item(0).getTextContent());
                                            rotorEngaged[1] = Boolean.parseBoolean(eElement.getElementsByTagName("rotor2Engaged").item(0).getTextContent());
                                            rotorEngaged[2] = Boolean.parseBoolean(eElement.getElementsByTagName("rotor3Engaged").item(0).getTextContent());
                                            rotorEngaged[3] = Boolean.parseBoolean(eElement.getElementsByTagName("rotor4Engaged").item(0).getTextContent());
                                            int rotorsEngaged = 0;
                                            for (int c2 = 0; c2 < rotorEngaged.length; c2++) {
                                                    if (rotorEngaged[c2] == true) {
                                                        rotorsEngaged++;
                                                    }
                                            }

                                            String alliance = eElement.getElementsByTagName("alliance").item(0).getTextContent();
                                            if (getTeamByNumber(eventTeam.teamNumber) != null) {
                                                if (getRobotEvent(getTeamByNumber(eventTeam.teamNumber),re.eventCode) != null) {
                                                    if (getRobotMatch(getRobotEvent(getTeamByNumber(eventTeam.teamNumber),re.eventCode),matchNumber) != null) {
                                                        if (alliance.equals(getRobotMatch(getRobotEvent(getTeamByNumber(eventTeam.teamNumber), re.eventCode), matchNumber).alliance)) {
                                                            //System.out.println("Event: " + re.eventCode + " got match number and alliance: " + matchNumber + " alliance: " + alliance);
                                                            //System.out.println("telehighfuel: " + teleFuelHigh + " telelowfuel: " + teleFuelLow);
                                                            getRobotMatch(getRobotEvent(getTeamByNumber(eventTeam.teamNumber), re.eventCode), matchNumber).autoHighFuel = autoFuelHigh;
                                                            getRobotMatch(getRobotEvent(getTeamByNumber(eventTeam.teamNumber), re.eventCode), matchNumber).autoLowFuel = autoFuelLow;
                                                            getRobotMatch(getRobotEvent(getTeamByNumber(eventTeam.teamNumber), re.eventCode), matchNumber).teleHighFuel = teleFuelHigh;
                                                            getRobotMatch(getRobotEvent(getTeamByNumber(eventTeam.teamNumber), re.eventCode), matchNumber).teleLowFuel = teleFuelLow;
                                                            getRobotMatch(getRobotEvent(getTeamByNumber(eventTeam.teamNumber), re.eventCode), matchNumber).climbPoints = teleopClimbPoints;
                                                            getRobotMatch(getRobotEvent(getTeamByNumber(eventTeam.teamNumber), re.eventCode), matchNumber).rotorsEngaged = rotorsEngaged;
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                } catch (SAXException e) {
                                    e.printStackTrace();
                                }
                            } catch (ParserConfigurationException e) {
                                e.printStackTrace();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void printStats() {

        // create output file
        BufferedWriter output = null;
        String text = new String();
        try {
            File file;
            if (Files.isDirectory(Paths.get("C:\\Users\\1153\\Documents"))) {
                file = new File("C:\\Users\\1153\\Documents\\FRC2017_district data.csv");
            } else {
                file = new File("C:\\Users\\jelmhurst\\Documents\\FRC2017_district data.csv");
            }
            output = new BufferedWriter(new FileWriter(file));

            text = "Team Number" + "," +
                    "Event Code" + "," +
                    "Qual Rank" + "," +
                    "Avg Score" + "," +
                    "Avg Auto High Fuel" + "," +
                    "Avg Tele High Fuel" + "," +
                    "Avg Auto Low Fuel" + "," +
                    "Avg Tele Low Fuel" + "," +
                    "Avg Climb Points" + "," +
                    "Avg Rotors Engaged" + "," + "\n";
            output.write(text);

            for (RobotStats rs : eventTeamList) {
                System.out.println("Team: " + rs.teamNumber);
                for (RobotEvent re : rs.eventList) {
                    System.out.println("\tEvent: " + re.eventCode);
                    if (re.rank > 0) {

                        // calculate average bolders high and low
                        int totalAutoHighBoulders = 0;
                        int totalTeleHighBoulders = 0;
                        int totalAutoLowBoulders = 0;
                        int totalTeleLowBoulders = 0;
                        int totalClimbPoints = 0;
                        int totalDefensesCrossed = 0;
                        int totalScore = 0;
                        for (RobotMatch rm : getRobotEvent(rs,re.eventCode).matchList) {
                            //System.out.println("Robot: " + rs.teamNumber + " event: " + re.eventCode + " match: " + rm.matchNumber + " telelow: " + rm.teleLowGoals);
                            totalAutoHighBoulders += rm.autoHighFuel;
                            totalTeleHighBoulders += rm.teleHighFuel;
                            totalAutoLowBoulders += rm.autoLowFuel;
                            totalTeleLowBoulders += rm.teleLowFuel;
                            totalClimbPoints += rm.climbPoints;
                            totalDefensesCrossed += rm.rotorsEngaged;
                            totalScore += rm.finalScore;
                        }
                        //System.out.println("total low: " + totalTeleLowBoulders + " total high: " + totalTeleHighBoulders);
                        re.avgAutoHighFuel = (float)totalAutoHighBoulders/getRobotEvent(rs,re.eventCode).matchList.size();
                        re.avgTeleHighFuel = (float)totalTeleHighBoulders/getRobotEvent(rs,re.eventCode).matchList.size();
                        re.avgAutoLowFuel = (float)totalAutoLowBoulders/getRobotEvent(rs,re.eventCode).matchList.size();
                        re.avgTeleLowFuel = (float)totalTeleLowBoulders/getRobotEvent(rs,re.eventCode).matchList.size();
                        re.avgRotorsEngaged = (float)totalDefensesCrossed/getRobotEvent(rs,re.eventCode).matchList.size();
                        re.avgClimbPoints = (float)totalClimbPoints/getRobotEvent(rs,re.eventCode).matchList.size();
                        re.avgScore = (float)totalScore/getRobotEvent(rs,re.eventCode).matchList.size();
                        //System.out.println("\t\tRank: " + re.rank);
                        //System.out.println("\t\tAvg Score: " + String.format("%2f", re.avgScore));
                        text = rs.teamNumber + "," +
                                re.eventCode + "," +
                                re.rank + "," +
                                re.avgScore + "," +
                                String.format("%.2f", re.avgAutoHighFuel) + "," +
                                String.format("%.2f", re.avgTeleHighFuel) + "," +
                                String.format("%.2f", re.avgAutoLowFuel) + "," +
                                String.format("%.2f", re.avgTeleLowFuel) + "," +
                                String.format("%.2f", re.avgClimbPoints) + "," +
                                String.format("%.2f", re.avgRotorsEngaged) + "," +  "\n";
                        output.write(text);
                    }
                }
            }

            output.close();

        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public void getAttending(String eventCode) {
        URL url;

        System.out.println("Get Teams Attending " + eventCode);

        //https://frc-api.firstinspires.org/v2.0/season/teams?teamNumber=101&eventCode=CMP

        String queryString = "https://frc-api.firstinspires.org/v2.0/";
        queryString += season + "/teams?";
        queryString += "eventCode=" + eventCode;
        //eventQueryString += "/events?";

        try {
            url = new URL(queryString);

            try {
                HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

                connection.setRequestMethod("GET");
                connection.setDoOutput(true);
                connection.setRequestProperty("Authorization", "Basic " + accessCode);
                connection.setRequestProperty("Accept", "application/xml ");

                // get data returned
                String responseString = "";
                try {
                    BufferedReader br =
                            new BufferedReader((new InputStreamReader(connection.getInputStream())));

                    String input;
                    while ((input = br.readLine()) != null) {
                        //System.out.println(input);
                        responseString += input;
                    }
                    br.close();

                    // parse the xml file returned
                    DocumentBuilderFactory dbFactory
                            = DocumentBuilderFactory.newInstance();
                    try {
                        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

                        try {
                            InputSource is = new InputSource(new StringReader(responseString));
                            Document doc = dBuilder.parse(is);
                            doc.getDocumentElement().normalize();
                            //System.out.println("Root element :" + doc.getDocumentElement().getNodeName());

                            NodeList teamList = doc.getElementsByTagName("Team");
                            for (int c = 0; c < teamList.getLength(); c++) {
                                Node eNode = teamList.item(c);
                                //System.out.println("Event: " + eNode.getNodeName());
                                if (eNode.getNodeType() == Node.ELEMENT_NODE) {

                                    Element eElement = (Element) eNode;
                                    //System.out.println("\nNumber : " + eElement.getElementsByTagName("teamNumber").item(0).getTextContent());

                                    RobotStats rs = new RobotStats();
                                    Integer teamNumber = Integer.parseInt(eElement.getElementsByTagName("teamNumber").item(0).getTextContent());
                                    rs.teamNumber = teamNumber;

                                    // create a robot event object and add it to the stats object for this team
                                    RobotEvent re = new RobotEvent();
                                    re.eventCode = eventCode;

                                    // add this event to the event list for the robot
                                    rs.eventList.add(re);
                                    eventTeamList.add(rs);
                                }
                            }

                        } catch (SAXException e) {
                            e.printStackTrace();
                        }
                    } catch (ParserConfigurationException e) {
                        e.printStackTrace();
                    }


                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public RobotStats getTeamByNumber(Integer teamNumber) {
        for (RobotStats rs : eventTeamList) {
            if (rs.teamNumber.equals(teamNumber)) {
                return rs;
            }
        }
        return null;
    }

    public RobotEvent getRobotEvent(RobotStats rs, String eventCode) {
        for (RobotStats rstats : eventTeamList) {
            if (rstats.teamNumber.equals(rs.teamNumber)) {
                for (RobotEvent rm : rstats.eventList) {
                    if (rm.eventCode.equals(eventCode)) {
                        return rm;
                    }
                }
            }
        }
        return null;
    }
    public RobotMatch getRobotMatch(RobotEvent robotEvent,int matchNumber) {
        for (RobotMatch rm : robotEvent.matchList) {
            if (rm.matchNumber == matchNumber) {
                return rm;
            }
        }
        return null;
    }
    public RobotMatch getPlayoffMatch(RobotEvent robotEvent,int matchNumber) {
        for (RobotMatch rm : robotEvent.playoffMatchList) {
            if (rm.matchNumber == matchNumber) {
                return rm;
            }
        }
        return null;
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public void getEventList(String season) {
        URL url;
        String eventQueryString = "https://frc-api.firstinspires.org/v2.0/";
        eventQueryString += season;
        //eventQueryString += "/events?eventCode=" + eventCode;
        eventQueryString += "/events?";

        try {
            url = new URL(eventQueryString);

            try {
                HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

                connection.setRequestMethod("GET");
                connection.setDoOutput(true);
                connection.setRequestProperty("Authorization", "Basic " + accessCode);
                connection.setRequestProperty("Accept", "application/xml ");

                // get data returned
                String responseString = "";
                System.out.println("*** Event List ***");
                try {
                    BufferedReader br =
                            new BufferedReader((new InputStreamReader(connection.getInputStream())));

                    String input;
                    while ((input = br.readLine()) != null) {
                        //System.out.println("Event: " + input);
                        responseString += input;
                    }
                    br.close();

                    // parse the xml file returned
                    DocumentBuilderFactory dbFactory
                            = DocumentBuilderFactory.newInstance();
                    try {
                        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

                        try {
                            InputSource is = new InputSource(new StringReader(responseString));
                            Document doc = dBuilder.parse(is);
                            doc.getDocumentElement().normalize();
                            //System.out.println("Root element :" + doc.getDocumentElement().getNodeName());

                            NodeList eventList = doc.getElementsByTagName("Event");
                            for (int c = 0; c < eventList.getLength(); c++) {
                                Node eNode = eventList.item(c);
                                //System.out.println("Event: " + eNode.getNodeName());
                                if (eNode.getNodeType() == Node.ELEMENT_NODE) {

                                    Element eElement = (Element) eNode;
                                    System.out.println("\nCode : " + eElement.getElementsByTagName("code").item(0).getTextContent());
                                    //System.out.println("City : " + eElement.getElementsByTagName("city").item(0).getTextContent());
                                    //System.out.println("Start : " + eElement.getElementsByTagName("dateStart").item(0).getTextContent());
                                    //System.out.println("End : " + eElement.getElementsByTagName("dateEnd").item(0).getTextContent());

                                }
                            }

                        } catch (SAXException e) {
                            e.printStackTrace();
                        }
                    } catch (ParserConfigurationException e) {
                        e.printStackTrace();
                    }


                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

    }
}
