package org.usfirst.frc.team3042;

public class MatchPredictor {
	
	static final double AUTO_RUN_POINTS = 5.0;
	static final double SWITCH_OWNERSHIP_POINTS = 18.8; //17.71;
	static final double CLIMB_POINTS = 60.0;
	
	public static class RankingPoints {
		private double winRP;
		private double autoRP;
		private double climbRP;
		private double totalRP;
		
		public RankingPoints(double win, double auto, double climb) {
			winRP = win;
			autoRP = auto;
			climbRP = climb;
			
			totalRP = win + auto + climb;
		}
		
		public double getWinRP() {
			return winRP;
		}
		public double getAutoRP() {
			return autoRP;
		}
		public double getClimbRP() {
			return climbRP;
		}
		public double getTotalRP() {
			return totalRP;
		}
	}
	
	
	public static RankingPoints[] predictMatchRPs(int[] redAlliance, int[] blueAlliance) {
		double redWin = 0, blueWin = 0;
		double redAuto = 0, blueAuto = 0;
		double redClimb = 0, blueClimb = 0;
		
		Alliance winner = predictMatch(redAlliance, blueAlliance);
		
		if (winner == Alliance.RED) {
			redWin += 2;
		} else if (winner == Alliance.BLUE) {
			blueWin += 2;
		} else {
			redWin += 1;
			blueWin += 1;
		}
		
		redAuto += calculateAutoRP(redAlliance);
		blueAuto += calculateAutoRP(blueAlliance);
		
		redClimb += calculateClimbRP(redAlliance);
		blueClimb += calculateClimbRP(blueAlliance);
		
		RankingPoints[] rankingPoints = new RankingPoints[2];
		rankingPoints[Alliance.RED.ordinal()] = new RankingPoints(redWin, redAuto, redClimb);
		rankingPoints[Alliance.BLUE.ordinal()] = new RankingPoints(blueWin, blueAuto, blueClimb);
		
		
		return rankingPoints;
	}
	
	public static Alliance predictMatch(int[] redAlliance, int[] blueAlliance) {
		Alliance winningAlliance;
		
		double redScore = scoreAlliance(redAlliance);
		double blueScore = scoreAlliance(blueAlliance);
		
		if (redScore > blueScore) {
			winningAlliance = Alliance.RED;
			System.out.println("Red wins with a score of " + redScore + " over " + blueScore);
		} else if (blueScore > redScore) {
			winningAlliance = Alliance.BLUE;
			System.out.println("Blue wins with a score of " + blueScore + " over " + redScore);
		} else {
			winningAlliance = Alliance.TIE;
		}
		
		return winningAlliance;
	}
	
	private static double scoreAlliance(int[] alliance) {
		double allianceScore = 0;
		
		for (int i = 0; i < alliance.length; i++) {
			int teamNumber = alliance[i];
			
			TeamOPRs teamOPRs = new TeamOPRs(teamNumber);
			
			allianceScore += teamOPRs.getTotalOPR();
		}
		
		return allianceScore;
	}
	
	private static double calculateAutoRP(int[] alliance) {
		double[] autoOPRs = new double[alliance.length];
		
		for (int i = 0; i < alliance.length; i++) {
			int teamNumber = alliance[i];
			
			TeamOPRs teamOPRs = new TeamOPRs(teamNumber);
			
			autoOPRs[i] = teamOPRs.getAutoOPR();
		}
		
		double minOPR = Math.min(autoOPRs[0], Math.min(autoOPRs[1], autoOPRs[2]));
		double autoRunProbability = Math.min(AUTO_RUN_POINTS, minOPR) / AUTO_RUN_POINTS;
		
		double maxOPR = Math.max(autoOPRs[0], Math.max(autoOPRs[1], autoOPRs[2]));
		double switchProbability = Math.min(SWITCH_OWNERSHIP_POINTS, maxOPR) / SWITCH_OWNERSHIP_POINTS;
		
		return autoRunProbability * switchProbability;
	}
	
	private static double calculateClimbRP(int[] alliance) {
		double[] climbOPRs = new double[alliance.length];
		
		for (int i = 0; i < alliance.length; i++) {
			int teamNumber = alliance[i];
			
			TeamOPRs teamOPRs = new TeamOPRs(teamNumber);
			
			climbOPRs[i] = teamOPRs.getClimbOPR();
		}
		
		double maxOPR = Math.max(climbOPRs[0], Math.max(climbOPRs[1], climbOPRs[2]));
		double climbProbability = Math.min(CLIMB_POINTS, maxOPR) / CLIMB_POINTS;
		
		return climbProbability;
	}
	
	public enum Alliance {
		BLUE, RED, TIE;
	}

}
