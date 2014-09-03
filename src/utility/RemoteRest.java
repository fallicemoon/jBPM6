package utility;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.ejb.Stateless;
import javax.enterprise.inject.New;


import org.apache.commons.codec.binary.Base64;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;


public class RemoteRest {
	
	String baseURL;
	String user;
	String password;

	public RemoteRest() {
		setBaseURL("http://10.16.140.105:8080/jbpm-console/rest");
		setUser("henry");
		setPassword("123");
	}
	
	
	public String getBaseURL() {
		return baseURL;
	}

	public void setBaseURL(String baseURL) {
		this.baseURL = baseURL;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	//private method
	private BufferedReader connect(String url, String method) {
		HttpURLConnection con;
		BufferedReader reader = new BufferedReader(new StringReader("remote return fail"));
		try {
			con = (HttpURLConnection)new URL(url).openConnection();
			con.setRequestMethod(method);
			String authHeader = Base64.encodeBase64String(
					String.format("%s:%s", user, password).getBytes("UTF-8"))
					.trim();
			con.setRequestProperty("Authorization", "Basic ".concat(authHeader));
			con.setRequestProperty("Accept", "application/json");
			
			reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
		
		} catch (IOException e) {
			System.out.println("connection fail, please check internet config");
			e.printStackTrace();
		}
		
		return reader;
	}


	//----------
	public int createProcessInstance(String deploymentId, String processDefId, Map<String, String> map) throws JSONException, IOException {
		String query = map.toString().replaceAll(", ", "&").replaceAll("[{}]", "");
		String url = String.format("%s/runtime/%s/process/%s/start?%s", baseURL, deploymentId, processDefId, query);
		BufferedReader reader = connect(url, "POST");
		JSONObject json = new JSONObject(reader.readLine().toString());
		return json.getInt("id");
	}
	
	public BufferedReader startTask(String taskId) {
		String url = String.format("%s/task/%s/start", baseURL, taskId);
		return connect(url, "POST");
	}
	
	public BufferedReader completeTask(String taskId, String query) {
		String url = String.format("%s/task/%s/complete?%s", baseURL, taskId, query);
		return connect(url, "POST");
	}
	
	public BufferedReader queryTask(String query) {
		//String url = String.format("%s/deployment",baseURL);
		//String url = String.format("%s/runtime/com.newegg.henry:henry_proj:1.0/workitem/205",baseURL);
		//String url = String.format("%s/runtime/com.newegg.henry:henry_proj:1.0/history/instances", baseURL);
		//String url = String.format("%s/runtime/com.newegg.henry:henry_proj:1.0/history/instance/130", baseURL);
		//String url = String.format("%s/runtime/com.newegg.henry:henry_proj:1.0/history/instance/105", baseURL);
		//String url = String.format("%s/task/query?processInstanceId=6", baseURL);
		String url = String.format("%s/task/query?%s", baseURL, query);
		return connect(url, "GET");
	}
	
	public static void main(String[] args) throws IOException, JSONException {
		//int processId = new RemoteRest().createProcessInstance("com.newegg.henry:henry_proj:1.0", "henry_project.leave_request", "map_reason=ggg&map_days=3");
		//BufferedReader reader = new RemoteRest().startTask("202");
		//BufferedReader reader = new RemoteRest().completeTask("202", "map_isApproved_output=true");
		BufferedReader reader = new RemoteRest().queryTask("processInstanceId=128");

		System.out.println(reader.readLine());
		System.out.println(reader.readLine());

		
	}

}
