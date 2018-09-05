package org.usfirst.frc.team3042;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;
import org.usfirst.frc.team3042.MatchPredictor.Alliance;
import org.usfirst.frc.team3042.MatchPredictor.RankingPoints;

public class DivisionPredictor {
	

	static boolean isTest = true;


	public static void main(String[] args) {
		ArrayList<String> divisionCodes = new ArrayList<String>();
		divisionCodes.add("HOPPER");
				
		DataCollector.initialize();
		OPRCalculator.openFiles();
		
		for (String divisionCode : divisionCodes) {
			predictDivision(divisionCode);
		}
		
		
		OPRCalculator.writeFiles();
		
	}
		
	public static void predictDivision(String divisionCode) {
		ArrayList<Integer> teamList = getTeamList(divisionCode);
		ArrayList<RankingPoints[]> matchResults = new ArrayList<RankingPoints[]>();
		
		ArrayList<TeamData> teams = new ArrayList<TeamData>();
		for(int teamNumber : teamList) {
			TeamData team = new TeamData(teamNumber);
			
			teams.add(team);
		}
		
		JSONObject scheduleObj = DataCollector.getSchedule(divisionCode);
		JSONArray schedule = scheduleObj.getJSONArray("Matches");
		for (int matchNumber = 0; matchNumber < schedule.length(); matchNumber++) {
			JSONObject match = schedule.getJSONObject(matchNumber);
			JSONArray matchTeams = match.getJSONArray("teams");
			
			int[] redAlliance = new int[3];
			int[] blueAlliance = new int[3];
			redAlliance[0] = matchTeams.getJSONObject(0).getInt("teamNumber");
			redAlliance[1] = matchTeams.getJSONObject(1).getInt("teamNumber");
			redAlliance[2] = matchTeams.getJSONObject(2).getInt("teamNumber");
			blueAlliance[0] = matchTeams.getJSONObject(3).getInt("teamNumber");
			blueAlliance[1] = matchTeams.getJSONObject(4).getInt("teamNumber");
			blueAlliance[2] = matchTeams.getJSONObject(5).getInt("teamNumber");

			System.out.println("Match " + (matchNumber + 1));
			
			RankingPoints[] rankingPoints = MatchPredictor.predictMatchRPs(redAlliance, blueAlliance);
			matchResults.add(rankingPoints);
			
			for (int i = 0; i < 3; i++) {
				int index = findTeam(redAlliance[i], teams);
				
				teams.get(index).addRankingPoints(rankingPoints[Alliance.RED.ordinal()]);
			}
			for (int i = 0; i < 3; i++) {
				int index = findTeam(blueAlliance[i], teams);
				
				teams.get(index).addRankingPoints(rankingPoints[Alliance.BLUE.ordinal()]);
			}
			System.out.println("");
		}
		
		teams = TeamData.sortTeams(teams);
		
		for(TeamData team : teams) {
			System.out.println("Team: " + team.getTeamNumber() + ", RP: " + team.getRankingPoints());
		}
		
		if (isTest) {
			double sumSquaredAutoError = 0;
			double sumSquaredClimbError = 0;
			
			JSONObject resultsObj = DataCollector.getScoreDetails(divisionCode);
			JSONArray results = resultsObj.getJSONArray("MatchScores");
			for (int matchNumber = 0; matchNumber < results.length(); matchNumber++) {
				JSONObject match = results.getJSONObject(matchNumber);
				JSONArray matchAlliances = match.getJSONArray("alliances");
				
				JSONObject redAlliance = matchAlliances.getJSONObject(Alliance.RED.ordinal());
				double actualRedAutoRP = (redAlliance.getBoolean("autoQuestRankingPoint"))? 1 : 0;
				double actualRedClimbRP = (redAlliance.getBoolean("faceTheBossRankingPoint"))? 1 : 0;
				
				JSONObject blueAlliance = matchAlliances.getJSONObject(Alliance.BLUE.ordinal());
				double actualBlueAutoRP = (blueAlliance.getBoolean("autoQuestRankingPoint"))? 1 : 0;
				double actualBlueClimbRP = (blueAlliance.getBoolean("faceTheBossRankingPoint"))? 1 : 0;
				
				RankingPoints[] matchResult = matchResults.get(matchNumber);
				RankingPoints predictedRedResults = matchResult[Alliance.RED.ordinal()];
				RankingPoints predictedBlueResults = matchResult[Alliance.BLUE.ordinal()];
				
				sumSquaredAutoError += Math.pow(predictedRedResults.getAutoRP() - actualRedAutoRP, 2);
				sumSquaredAutoError += Math.pow(predictedBlueResults.getAutoRP() - actualBlueAutoRP, 2);
				
				sumSquaredClimbError += Math.pow(predictedRedResults.getClimbRP() - actualRedClimbRP, 2);
				sumSquaredClimbError += Math.pow(predictedBlueResults.getClimbRP() - actualBlueClimbRP, 2);
			}
			
			double autoBrierScore = sumSquaredAutoError / (2 * results.length());
			double climbBrierScore = sumSquaredClimbError / (2 * results.length());
			
			System.out.println("\nAuto RP Brier Score: " + autoBrierScore + ", Climb RP Brier Score: " + climbBrierScore);
		}
	}

	public static ArrayList<Integer> getTeamList(String divisionCode) {
		ArrayList<Integer> teams = new ArrayList<Integer>();
		
		ArrayList<JSONObject> teamsObjs = DataCollector.getTeamList(divisionCode);
		
		for(JSONObject teamsObj : teamsObjs) {
			JSONArray eventList = teamsObj.getJSONArray("teams");
			for (int i = 0; i < eventList.length(); i++) {
				JSONObject currentTeam = eventList.getJSONObject(i);
			
				teams.add(currentTeam.getInt("teamNumber"));
			}
		}
		
		return teams;
	}
	
	private static int findTeam(int teamNumber, ArrayList<TeamData> teams) {
		int index = -1;
		
		for(int i = 0; i < teams.size(); i++) {
			TeamData team = teams.get(i);
			if (team.getTeamNumber() == teamNumber) {
				index = i;
				break;
			}
		}
		
		return index;
	}
}
