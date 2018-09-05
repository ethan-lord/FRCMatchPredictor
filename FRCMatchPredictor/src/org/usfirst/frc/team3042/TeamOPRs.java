package org.usfirst.frc.team3042;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

public class TeamOPRs {
	private double totalOPR = -1000, climbOPR = -1000, autoOPR = -1000, ownershipOPR = -1000, vaultOPR = -1000;
	
	public TeamOPRs(double total, double climb, double auto, double ownership, double vault) {
		totalOPR = total;
		climbOPR = climb;
		autoOPR = auto;
		ownershipOPR = ownership;
		vaultOPR = vault;
	}
	
	public TeamOPRs(int teamNumber) {
		ArrayList<String> events = OPRCalculator.getEventCodes(teamNumber);
		
		for (String event : events) {
			JSONArray eventOPRs = OPRCalculator.calculateOPRs(event);
			
			int teamIndex = OPRCalculator.findTeamOPRs(teamNumber, eventOPRs);
			JSONObject teamOPRs = eventOPRs.getJSONObject(teamIndex);
			
			double eventTotal = teamOPRs.getDouble("totalOPR");
			double eventClimb = teamOPRs.getDouble("climbOPR");
			double eventAuto = teamOPRs.getDouble("autoOPR");
			double eventOwnership = teamOPRs.getDouble("ownershipOPR");
			double eventVault = teamOPRs.getDouble("vaultOPR");
			
			totalOPR = Math.max(eventTotal, totalOPR);
			climbOPR = Math.max(eventClimb, climbOPR);
			autoOPR = Math.max(eventAuto, autoOPR);
			ownershipOPR = Math.max(eventOwnership, ownershipOPR);
			vaultOPR = Math.max(eventVault, vaultOPR);
		}
	}
	
	public double getTotalOPR() {
		return totalOPR;
	}
	
	public double getClimbOPR() {
		return climbOPR;
	}
	
	public double getAutoOPR() {
		return autoOPR;
	}
	
	public double getOwnershipOPR() {
		return ownershipOPR;
	}
	
	public double getVaultOPR() {
		return vaultOPR;
	}
}
