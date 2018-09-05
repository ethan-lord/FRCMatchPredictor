package org.usfirst.frc.team3042;

import java.util.ArrayList;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.internal.util.Base64;
import org.json.JSONArray;
import org.json.JSONObject;

public class DataCollector {
	
	private static final String username = "Thardro";
	private static final String key = "DA8F5678-B49D-4FC9-A470-C6D4E3362879";
	private static final String token = username + ":" + key;
	private static final String eventCode = "mnmi2";
	private static FileIO IO = new FileIO();
	
	private static final String path = "";
	
	static Client client;
	static MultivaluedMap<String, Object> headers;
	
	public static void main (String[] args) {
		client = ClientBuilder.newClient();
		
		headers = 
			    new MultivaluedHashMap<String, Object>();
		headers.add("Accept",  "application/json");
		headers.add("Authorization", "Basic " + new String(Base64.encode(token.getBytes())));
		
		
		JSONObject rankingObj = getRankings(eventCode);
		JSONArray rankings = rankingObj.getJSONArray("Rankings");
		
		IO.create(path, "rankings.txt");
		for (int i = 0; i < rankings.length(); i++) {
			JSONObject currentTeam = rankings.getJSONObject(i);
			
			String output = "";
			
			output += currentTeam.getInt("teamNumber");
			output += ", " + currentTeam.getInt("rank");
			output += ", " + currentTeam.getDouble("sortOrder1");
			output += ", " + currentTeam.getInt("sortOrder2");
			output += ", " + currentTeam.getInt("sortOrder3");
			output += ", " + currentTeam.getInt("sortOrder4");
			output += ", " + currentTeam.getInt("sortOrder5");
			output += ", " + currentTeam.getInt("sortOrder6");
			output += ", " + currentTeam.getInt("wins");
			output += ", " + currentTeam.getInt("losses");
			output += ", " + currentTeam.getInt("ties");
			output += ", " + currentTeam.getInt("qualAverage");
			output += ", " + currentTeam.getInt("dq");
			output += ", " + currentTeam.getInt("matchesPlayed");
			
			IO.write(output);
		}
		IO.close();
		
		JSONObject scheduleObj = getSchedule(eventCode);
		JSONArray schedule = scheduleObj.getJSONArray("Matches");
		
		JSONObject scoresObj = getScoreDetails(eventCode);
		JSONArray scores = scoresObj.getJSONArray("MatchScores");
		
		IO.create(path,  "match_data.txt");
		for (int i = 0; i < scores.length(); i++) {
			JSONArray currentTeams = schedule.getJSONObject(i).getJSONArray("teams");
			JSONObject currentMatch = scores.getJSONObject(i);
			JSONArray currentAlliances = currentMatch.getJSONArray("alliances");
			
			String output = "";
			
			output += currentMatch.getInt("matchNumber");
			
			for (int j = 0; j < currentTeams.length(); j++) {
				output += ", " + currentTeams.getJSONObject(j).getInt("teamNumber");
			}
			
			for (int j = 0; j < currentAlliances.length(); j++) {
				JSONObject alliance = currentAlliances.getJSONObject(j);
				
				output += ", " + alliance.getInt("autoSwitchOwnershipSec");
				output += ", " + alliance.getInt("autoScaleOwnershipSec");
				output += ", " + alliance.getInt("teleopSwitchOwnershipSec");
				output += ", " + alliance.getInt("teleopScaleOwnershipSec");
				output += ", " + alliance.getInt("vaultForcePlayed");
				output += ", " + alliance.getInt("vaultLevitatePlayed");
				output += ", " + alliance.getInt("vaultBoostPlayed");
				output += ", " + alliance.getInt("vaultPoints");
				output += ", " + alliance.getInt("foulCount");
				output += ", " + alliance.getInt("techFoulCount");
			}
			
			IO.write(output);
		}
		IO.close();
		
		
		
	}
	
	public static void initialize() {
		client = ClientBuilder.newClient();
		
		headers = 
			    new MultivaluedHashMap<String, Object>();
		headers.add("Accept",  "application/json");
		headers.add("Authorization", "Basic " + new String(Base64.encode(token.getBytes())));
	}
	
	public static JSONObject getRankings(String eventCode) {
		Response response = client.target("https://frc-api.firstinspires.org/v2.0/2018/rankings/" + eventCode)
				.request(MediaType.TEXT_PLAIN_TYPE)
				.headers(headers)
				.get();
		
		//System.out.println("status: " + response.getStatus());
		//System.out.println("headers: " + response.getHeaders());
		
		String rankingList = response.readEntity(String.class);
		//System.out.println("body: " + rankingList);
		
		JSONObject rankingObj = new JSONObject(rankingList);
		
		return rankingObj;
	}
	
	public static JSONObject getSchedule(String eventCode) {
		Response response = client.target("https://frc-api.firstinspires.org/v2.0/2018/matches/" + eventCode + "?tournamentLevel=qual")
				.request(MediaType.TEXT_PLAIN_TYPE)
				.headers(headers)
				.get();
		
		System.out.println("status: " + response.getStatus());
		System.out.println("headers: " + response.getHeaders());
		
		String schedule = response.readEntity(String.class);
		System.out.println("body: " + schedule);
		
		JSONObject scheduleObj = new JSONObject(schedule);
		
		return scheduleObj;
	}
	
	public static JSONObject getScoreDetails(String eventCode) {
		Response response = client.target("https://frc-api.firstinspires.org/v2.0/2018/scores/" + eventCode + "/qual")
				.request(MediaType.TEXT_PLAIN_TYPE)
				.headers(headers)
				.get();
		
		//System.out.println("status: " + response.getStatus());
		//System.out.println("headers: " + response.getHeaders());
		
		String scores = response.readEntity(String.class);
		//System.out.println("body: " + scores);
		
		JSONObject scoresObj = new JSONObject(scores);
		
		return scoresObj;
	}
	
	public static JSONObject getEvents(int teamNumber) {
		Response response = client.target("https://frc-api.firstinspires.org/v2.0/2018/events?teamNumber=" + teamNumber)
				.request(MediaType.TEXT_PLAIN_TYPE)
				.headers(headers)
				.get();
		
		//System.out.println("status: " + response.getStatus());
		//System.out.println("headers: " + response.getHeaders());
		
		String events = response.readEntity(String.class);
		//System.out.println("body: " + events);
		
		JSONObject eventsObj = new JSONObject(events);
		
		return eventsObj;
	}
	
	public static ArrayList<JSONObject> getTeamList(String eventCode) {
		ArrayList<JSONObject> teamsObjs = new ArrayList<JSONObject>();
		
		Response response = client.target("https://frc-api.firstinspires.org/v2.0/2018/teams?eventCode=" + eventCode)
				.request(MediaType.TEXT_PLAIN_TYPE)
				.headers(headers)
				.get();
		
		//System.out.println("status: " + response.getStatus());
		//System.out.println("headers: " + response.getHeaders());
		
		String teams = response.readEntity(String.class);
		//System.out.println("body: " + teams);
		
		JSONObject teamsObj = new JSONObject(teams);
		teamsObjs.add(teamsObj);
		
		int pages = teamsObj.getInt("pageTotal");
		
		if (pages == 2) {
			Response response2 = client.target("https://frc-api.firstinspires.org/v2.0/2018/teams?eventCode=" + eventCode + "&page=2")
					.request(MediaType.TEXT_PLAIN_TYPE)
					.headers(headers)
					.get();
			
			//System.out.println("status: " + response.getStatus());
			//System.out.println("headers: " + response.getHeaders());
			
			String teams2 = response2.readEntity(String.class);
			//System.out.println("body: " + teams);
			
			JSONObject teamsObj2 = new JSONObject(teams2);
			teamsObjs.add(teamsObj2);
		}
		
		return teamsObjs;
	}

}
