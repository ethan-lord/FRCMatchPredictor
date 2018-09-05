package org.usfirst.frc.team3042;

import java.util.ArrayList;

import org.usfirst.frc.team3042.MatchPredictor.RankingPoints;

public class TeamData {
	private int teamNumber;
	private double rankingPoints;
	
	public TeamData(int teamNumber) {
		this.teamNumber = teamNumber;
		this.rankingPoints = 0;
	}
	
	public int getTeamNumber() {
		return teamNumber;
	}
	
	public double getRankingPoints() {
		return rankingPoints;
	}
	public void setRankingPoints(double rankingPoints) {
		this.rankingPoints = rankingPoints;
	}
	public void addRankingPoints(RankingPoints pointsToAdd) {
		this.rankingPoints += pointsToAdd.getTotalRP();
	}
	
	public static ArrayList<TeamData> sortTeams(ArrayList<TeamData> teams) {
		if (teams.size() <= 1) {
			return teams;
		}
		
		int middle = teams.size() / 2;
		ArrayList<TeamData> left = new ArrayList<TeamData>(teams.subList(0, middle));
		ArrayList<TeamData> right = new ArrayList<TeamData>(teams.subList(middle, teams.size()));
		
		left = sortTeams(left);
		right = sortTeams(right);
		
		return mergeLists(left, right);
	}
	
	private static ArrayList<TeamData> mergeLists(ArrayList<TeamData> left, ArrayList<TeamData> right) {
		ArrayList<TeamData> mergedList = new ArrayList<TeamData>();
		int leftId = 0, rightId = 0;
		
		while (leftId < left.size() && rightId < right.size()) {
			if (left.get(leftId).getRankingPoints() > right.get(rightId).getRankingPoints()) {
				mergedList.add(left.get(leftId));
				leftId++;
			} else {
				mergedList.add(right.get(rightId));
				rightId++;
			}
		}
		
		if (leftId == left.size()) {
			for (int i = rightId; i < right.size(); i++) {
				mergedList.add(right.get(i));
			}
		} else {
			for (int i = leftId; i < left.size(); i++) {
				mergedList.add(left.get(i));
			}
		}
		
		return mergedList;
	}

}
