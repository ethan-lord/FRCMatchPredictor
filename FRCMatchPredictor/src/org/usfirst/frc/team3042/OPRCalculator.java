package org.usfirst.frc.team3042;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import Jama.Matrix;

public class OPRCalculator {
	
	static JSONObject eventOPRs, teamEvents;

	public static void main(String[] args) {
		int teamNumber = 254;
		
		DataCollector.initialize();
		
		openFiles();
		
		ArrayList<String> events = getEventCodes(teamNumber);
		
		for (String event : events) {
			calculateOPRs(event);
		}
		
		writeFiles();
	}
	
	
	
	public static ArrayList<String> getEventCodes(int teamNumber) {
		ArrayList<String> events = new ArrayList<String>();
		
		try {
			JSONArray eventArray = teamEvents.getJSONArray("" + teamNumber);
			
			for (int i = 0; i < eventArray.length(); i++) {
				events.add(eventArray.getString(i));
			}
		} catch (JSONException e) {
			System.out.println("Events for team " + teamNumber + " not found in database, searching now");
			
			JSONObject eventsObj = DataCollector.getEvents(teamNumber);
			JSONArray eventList = eventsObj.getJSONArray("Events");
			for (int i = 0; i < eventList.length(); i++) {
				JSONObject currentEvent = eventList.getJSONObject(i);
			
				String eventType = currentEvent.getString("type");
				if(!(eventType.equals("Championship") || eventType.equals("ChampionshipSubdivision"))) {
					String eventCode = currentEvent.getString("code");
					if(!(eventCode.equals("WEEK0") || eventCode.equals("MICMP") || eventCode.equals("ONCMP"))) {
						events.add(eventCode);
					}
				}
			}
			JSONArray eventArray = new JSONArray();
			for (String event : events) {
				eventArray.put(event);
			}
			teamEvents.put("" + teamNumber, eventArray);
		}
		
		return events;
	}
	
	public static JSONArray calculateOPRs(String eventCode) {
		JSONArray OPRArray;
		
		try {
			OPRArray = eventOPRs.getJSONArray(eventCode);
		} catch (JSONException e) {
			System.out.println("Event " + eventCode + " not in database, collecting OPRs");
		
			OPRArray = new JSONArray();
			
			JSONObject rankingObj = DataCollector.getRankings(eventCode);
			JSONArray rankings = rankingObj.getJSONArray("Rankings");
		
			JSONObject scheduleObj = DataCollector.getSchedule(eventCode);
			JSONArray schedule = scheduleObj.getJSONArray("Matches");
		
			JSONObject scoresObj = DataCollector.getScoreDetails(eventCode);
			JSONArray scores = scoresObj.getJSONArray("MatchScores");
		
			int[] teamList = new int[rankings.length()];
			double[][] totalScores = new double[rankings.length()][5];
			double[][] pairings = new double[rankings.length()][rankings.length()];
			for (int i = 0; i < rankings.length(); i++) {
				JSONObject currentTeam = rankings.getJSONObject(i);
			
				teamList[i] = currentTeam.getInt("teamNumber");
				totalScores[i][1] = currentTeam.getInt("sortOrder2");
				totalScores[i][2] = currentTeam.getInt("sortOrder3");
				totalScores[i][3] = currentTeam.getInt("sortOrder4");
				totalScores[i][4] = currentTeam.getInt("sortOrder5");
			
				totalScores[i][0] = totalScores[i][1] + totalScores[i][2] + totalScores[i][3] + totalScores[i][4];
			}
		
			for (int i = 0; i < rankings.length(); i++) {
				for (int matchNumber = 0; matchNumber < schedule.length(); matchNumber++) {
					JSONObject match = schedule.getJSONObject(matchNumber);
					JSONArray teams = match.getJSONArray("teams");
				
					for (int team = 0; team < 6; team++) {
						if (teams.getJSONObject(team).getInt("teamNumber") == teamList[i]) {
							JSONObject score;
							if (team < 3) {
								// Red Alliance
								for (int partner = 0; partner < 3; partner++) {
									int index = findTeam(teams.getJSONObject(partner).getInt("teamNumber"), teamList);
									if (index > i) {
										pairings[i][index]++;
										pairings[index][i]++;
									}
								}
							
								score = scores.getJSONObject(matchNumber).getJSONArray("alliances").getJSONObject(1);
							} else {
								// Blue Alliance
								for (int partner = 3; partner < 6; partner++) {
									int index = findTeam(teams.getJSONObject(partner).getInt("teamNumber"), teamList);
									if (index > i) {
										pairings[i][index]++;
										pairings[index][i]++;
									}
								}
							
								score = scores.getJSONObject(matchNumber).getJSONArray("alliances").getJSONObject(0);
							}
						
							pairings[i][i]++;
						}
					}
				}			
			}
		
			Matrix pairingMatrix = new Matrix(pairings);
			Matrix scoreMatrix = new Matrix(totalScores);
		
			Matrix oprMatrix = pairingMatrix.solve(scoreMatrix);
		
			for (int i = 0; i < teamList.length; i++) {
				JSONObject teamOPRs = new JSONObject();
			
				int teamNumber = teamList[i];
				teamOPRs.put("teamNumber", teamNumber);
				
				teamOPRs.put("totalOPR", oprMatrix.get(i, 0));
				teamOPRs.put("climbOPR", oprMatrix.get(i, 1));
				teamOPRs.put("autoOPR", oprMatrix.get(i, 2));
				teamOPRs.put("ownershipOPR", oprMatrix.get(i, 3));
				teamOPRs.put("vaultOPR", oprMatrix.get(i, 4));
			
				OPRArray.put(teamOPRs);
			}
		
			System.out.println(OPRArray);
		
			eventOPRs.put(eventCode, OPRArray);
		}
		
		return OPRArray;
	}
	
	private static int findTeam(int teamNumber, int[] teamList) {
		int index = -1;
		for (int i = 0; i < teamList.length; i++) {
			if (teamList[i] == teamNumber) {
				index = i;
				break;
			}
		}
		
		return index;
	}
	
	public static int findTeamOPRs(int teamNumber, JSONArray teamOPRs) {
		int index = -1;
		for (int i = 0; i < teamOPRs.length(); i++) {
			JSONObject currentTeam = teamOPRs.getJSONObject(i);
			if (currentTeam.getInt("teamNumber") == teamNumber) {
				index = i;
				break;
			}
		}
		
		return index;
	}
	
	public static void openFiles() {
		try {
			BufferedReader br = new BufferedReader(new FileReader("eventOPRs.json"));
			String jsonObj = br.readLine();
			
			eventOPRs = new JSONObject(jsonObj);
			
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			BufferedReader br = new BufferedReader(new FileReader("teamEvents.json"));
			String jsonObj = br.readLine();
			
			teamEvents = new JSONObject(jsonObj);
			
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void writeFiles() {
		try (FileWriter file = new FileWriter("eventOPRs.json")) {

            file.write(eventOPRs.toString());
            file.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
		
		try (FileWriter file = new FileWriter("teamEvents.json")) {

            file.write(teamEvents.toString());
            file.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
	}

}
