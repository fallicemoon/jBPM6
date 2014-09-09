package utility;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.ejb.Stateless;
import javax.enterprise.inject.New;


import org.apache.commons.codec.binary.Base64;
import org.json.JSONArray;
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
	
	public String startTask(String taskId) throws IOException {
		String url = String.format("%s/task/%s/start", baseURL, taskId);
		return connect(url, "POST").readLine();
	}
	
	public String completeTask(String taskId, Map<String, String> map) throws IOException {
		String query = map.toString().replaceAll(", ", "&").replaceAll("[{}]", "");
		String url = String.format("%s/task/%s/complete?%s", baseURL, taskId, query);
		return connect(url, "POST").readLine();
	}
	
	
	//-----------not use
//	public String getProcessInstances(String deploymentId, String processId) throws IOException, JSONException {
//		String url = String.format("%s/runtime/%s/history/instances", baseURL, deploymentId);
//		String jsonString = connect(url, "GET").readLine();
//
//		JSONObject json = new JSONObject(jsonString);
//		JSONObject responseJson = new JSONObject();
//		
//		JSONArray historyLogList = json.getJSONArray("historyLogList");
//		for(int i=0; i<historyLogList.length(); i++){
//			JSONObject object = historyLogList.getJSONObject(i);
//			if(object.getString("processId").equals(processId)){
//				responseJson.put(String.valueOf(object.getInt("processInstanceId")), object.remove("result"));
//			}
//		}
//		
//		return responseJson.toString();
//	}
//	
	public JSONObject getTaskVar(String workItemId) throws JSONException, IOException {
		String url = String.format("%s/runtime/com.henry:henry_project:1.0/workitem/%s", baseURL, workItemId);
		JSONObject json = new JSONObject(connect(url, "GET").readLine());
		return json.getJSONObject("param-map");
	}
	
	//-----------------------------------------get tasks
	public JSONObject getAllTasks() throws JSONException {
		String url = String.format("%s/task/query", baseURL);
		BufferedReader reader = connect(url, "GET");
		JSONObject json;
		try {
			json = new JSONObject(reader.readLine().toString());
		} catch (IOException e) {
			json = new JSONObject();
			System.out.println("BufferReader has error......");
			json.put("success", "false");
			e.printStackTrace();
		}
		
		JSONObject responseJson = new JSONObject();
		JSONArray taskSummaryList = json.getJSONArray("taskSummaryList");
		for (int i = 0; i < taskSummaryList.length(); i++) {
			JSONObject j = taskSummaryList.getJSONObject(i).getJSONObject("task-summary");
			responseJson.put(String.valueOf(i+1), j);
		}
		
		return responseJson;
	}
	
	public JSONObject getAllTasks(String deploymentId, String processId) throws JSONException{
		JSONObject json = getAllTasks();
		JSONObject responseJson = new JSONObject();
		
		for (int i = 1; i <= json.length(); i++) {
			String index = String.valueOf(i);
			if (!json.getJSONObject(index).getString("deployment-id").equals(deploymentId)){
				continue;
			}
			if (!json.getJSONObject(index).getString("process-id").equals(processId)){
				continue;
			}
			responseJson.put(String.valueOf(responseJson.length()+1), json.getJSONObject(index));
		}
		
		return responseJson;
	}
	
	public JSONObject getAllTasks(String deploymentId, String processId, String taskName) throws JSONException{
		JSONObject json = getAllTasks();
		JSONObject responseJson = new JSONObject();
		
		for (int i = 1; i <= json.length(); i++) {
			String index = String.valueOf(i);
			if (!json.getJSONObject(index).getString("name").equals(taskName))
				continue;
	
			responseJson.put(String.valueOf(responseJson.length()+1), json.getJSONObject(index));
		}
		
		return responseJson;
	}
	
	public JSONObject getAllTasks(String deploymentId, String processId, String name, String status) throws JSONException{
		JSONObject json = getAllTasks(deploymentId, processId, name);
		JSONObject responseJson = new JSONObject();
		
		for (int i = 1; i <= json.length(); i++) {
			String index = String.valueOf(i);
			if (!json.getJSONObject(index).getString("status").equals(status))
				continue;
	
			responseJson.put(String.valueOf(responseJson.length()+1), json.getJSONObject(index));
		}
		
		return responseJson;
	}

	public JSONObject getProcessInstanceVar(String processInstanceId) throws JSONException, IOException {
		String url = String.format("%s/history/instance/%s/variable", baseURL, processInstanceId);
		JSONArray historyLogList = new JSONObject(connect(url, "GET").readLine()).getJSONArray("historyLogList");
		
		JSONObject responseJson = new JSONObject();
		
		for (int i = 0; i < historyLogList.length(); i++) {
			JSONObject j = historyLogList.getJSONObject(i).getJSONObject("variable-instance-log");
			String key = j.getString("variable-id");
			String value = j.getString("value");
			responseJson.put(key, value);
		}
		
		return responseJson;
	}
	
	public static void main(String[] args) throws IOException, JSONException {
		RemoteRest remoteRest = new RemoteRest();
//		Map map = new HashMap();
//		map.put("map_reason", "sick");
//		map.put("map_days", "3");
		//int i = remoteRest.createProcessInstance("com.henry:henry_project:1.0", "henry_project.leave_request", map);
		//String string = remoteRest.getProcessInstances("com.henry:henry_project:1.0","henry_project.teststatus");
		//String i = remoteRest.startTask("31");
		//JSONObject jsonObject = remoteRest.getTask("21");
		JSONObject jsonObject = remoteRest.getTaskVar("7");
		System.out.println(jsonObject);

		
	}

}
