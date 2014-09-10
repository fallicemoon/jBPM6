package utility;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.ejb.Stateless;
import javax.enterprise.inject.New;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;


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
	public String createProcessInstance(String deploymentId, String processDefId, Map<String, String> map) throws JSONException, IOException, ScriptException {
		String queryString = map.toString().replaceAll(", ", "&").replaceAll("[{}]", "");

        ScriptEngineManager factory = new ScriptEngineManager();
        ScriptEngine engine = factory.getEngineByName("JavaScript");
        String query = (String)engine.eval("encodeURI('"+queryString+"')");

		String url = String.format("%s/runtime/%s/process/%s/start?%s", baseURL, deploymentId, processDefId, query);
		BufferedReader reader = connect(url, "POST");
		JSONObject json = new JSONObject(reader.readLine().toString());
		return String.valueOf(json.getInt("id"));
	}
	
	public JSONObject startTask(String taskId) throws IOException, JSONException {
		String url = String.format("%s/task/%s/start", baseURL, taskId);
		return new JSONObject(connect(url, "POST").readLine());
	}
	
	public JSONObject completeTask(String taskId, Map<String, String> map) throws IOException, JSONException {
		String query = map.toString().replaceAll(", ", "&").replaceAll("[{}]", "");
		String url = String.format("%s/task/%s/complete?%s", baseURL, taskId, query);
		return new JSONObject(connect(url, "POST").readLine());
	}
	
	
	//-----------
	public JSONObject getTaskIdByProcessInstanceId(String deploymentId, String processInstanceId) throws IOException, JSONException {
		JSONObject json = getAllTasks();
		JSONObject responseJson = new JSONObject();
		
		for(int i=1; i<=json.length(); i++){
			JSONObject object = json.getJSONObject(String.valueOf(i));
			String procInstId = String.valueOf(object.getInt("process-instance-id"));
			
			if(procInstId.equals(processInstanceId))
				responseJson.put(procInstId,String.valueOf(object.getInt("id")));
		}
		
		return responseJson;
	}
	
	public JSONObject getTaskByProcessInstanceId(String processInstanceId) throws JSONException, IOException {
		JSONObject json = getAllTasks();
		JSONObject responseJson = new JSONObject();
		
		for (int i = 1; i <= json.length(); i++) {
			String index = String.valueOf(i);
			if (!String.valueOf(json.getJSONObject(index).getInt("process-instance-id")).equals(processInstanceId))
				continue;
	
			responseJson.put(String.valueOf(responseJson.length()+1), json.getJSONObject(index));
		}
		
		return responseJson;
	}
	
	public JSONObject getTask(String workItemId) throws JSONException, IOException {
		String url = String.format("%s/runtime/com.henry:henry_project:1.0/workitem/%s", baseURL, workItemId);
		JSONObject json = new JSONObject(connect(url, "GET").readLine());
		return json.getJSONObject("param-map");
	}
	
	public JSONObject getProcessInstanceByCreator(String creator) throws JSONException, IOException {
		String url = String.format("%s/history/instances", baseURL);
		JSONObject json = new JSONObject(connect(url, "GET").readLine());
		JSONObject responseJson = new JSONObject();
		
		JSONArray historyLogList = json.getJSONArray("historyLogList");
		
		for (int i = 0; i < historyLogList.length(); i++) {
			JSONObject j = historyLogList.getJSONObject(i).getJSONObject("process-instance-log");
			if (creator.equals(j.getString("identity"))) 
				responseJson.put(String.valueOf(j.getInt("process-instance-id")), j);
		}
		
		return responseJson;
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
	
	public JSONObject getAllTasks(String deploymentId, String processDefId) throws JSONException{
		JSONObject json = getAllTasks();
		JSONObject responseJson = new JSONObject();
		
		for (int i = 1; i <= json.length(); i++) {
			String index = String.valueOf(i);
			if (!json.getJSONObject(index).getString("deployment-id").equals(deploymentId)){
				continue;
			}
			if (!json.getJSONObject(index).getString("process-id").equals(processDefId)){
				continue;
			}
			responseJson.put(String.valueOf(responseJson.length()+1), json.getJSONObject(index));
		}
		
		return responseJson;
	}
	
	public JSONObject getAllTasksByTaskName(String deploymentId, String processDefId, String taskName) throws JSONException{
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
	
	public JSONObject getAllTasksByTaskName(String deploymentId, String processDefId, String taskName, TaskStatus status) throws JSONException{
		JSONObject json = getAllTasksByTaskName(deploymentId, processDefId, taskName);
		JSONObject responseJson = new JSONObject();
		
		for (int i = 1; i <= json.length(); i++) {
			String index = String.valueOf(i);
			if (!json.getJSONObject(index).getString("status").equals(status.toString()))
				continue;
	
			responseJson.put(String.valueOf(responseJson.length()+1), json.getJSONObject(index));
		}
		
		return responseJson;
	}
	
	public JSONObject getAllTasksByApprover(String deploymentId, String processDefId, String approver) throws JSONException{
		JSONObject json = getAllTasks();
		JSONObject responseJson = new JSONObject();
		
		for (int i = 1; i <= json.length(); i++) {
			String index = String.valueOf(i);
			String actualOwner;
			try {
				actualOwner = json.getJSONObject(index).getString("actual-owner");
			} catch (JSONException e) {
				actualOwner = "";
			}

			if (!approver.equals(actualOwner))
				continue;

			responseJson.put(String.valueOf(responseJson.length()+1), json.getJSONObject(index));
		}
		
		return responseJson;
	}
	
	public JSONObject getAllTasksByApprover(String deploymentId, String processDefId, String approver, TaskStatus status) throws JSONException{
		JSONObject json = getAllTasksByTaskName(deploymentId, processDefId, approver);
		JSONObject responseJson = new JSONObject();
		
		for (int i = 1; i <= json.length(); i++) {
			String index = String.valueOf(i);
			if (!json.getJSONObject(index).getString("status").equals(status.toString()))
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
	
	public static void main(String[] args) throws IOException, JSONException, ScriptException {
		RemoteRest remoteRest = new RemoteRest();
		String deploymentId = "com.henry:henry_project:1.0";
		String processDefId = "henry_project.leave_request";
		
		String taskName = "Manager approve";
		
		//1.user side, create process
//		Map<String, String> map = new HashMap<String, String>();
//		map.put("map_reason","XXXX");
//		map.put("map_days", "3");
//		
//		String processInstanceId = remoteRest.createProcessInstance(deploymentId, processDefId, map);
//		JSONObject json = remoteRest.getTaskIdByProcessInstanceId(deploymentId, processInstanceId);
		
		//remoteRest.setUser("xxxxxx");
		//remoteRest.startTask(json.getString(processInstanceId));
		
		//2.user side, get my process
		//JSONObject json = remoteRest.getTaskByProcessInstanceId("46");
		//JSONObject json = remoteRest.getProcessInstanceByCreator("john");
		
		//-------------------------------------------------------
		
		//3.approver side, list all (id is taskId)
		//JSONObject json = remoteRest.getAllTasks(deploymentId, processDefId);
		
		//4.approver side, list task by my role(ex:Manager approve)
		//JSONObject json = remoteRest.getAllTasksByTaskName(deploymentId, processDefId, taskName);

		//5.approver side, list none approve task by my role
		//JSONObject json = remoteRest.getAllTasksByTaskName(deploymentId, processDefId, taskName, TaskStatus.Ready.toString());
		
		//6.approve side, list my task by me
		//JSONObject json = remoteRest.getAllTasksByApprover(deploymentId, processDefId, "henry");
		
		
		//7.approve side, list none approve my task by me
		//JSONObject json = remoteRest.getAllTasksByApprover(deploymentId, processDefId, taskName, TaskStatus.Completed);
		
		//8.approve
//		Map<String, String> map = new HashMap<String, String>();
//		map.put("map_isApproved", "true");
//		JSONObject json = remoteRest.completeTask("63", map);
		//---------------------------------------------------
		//9.get var
		//JSONObject json = remoteRest.getProcessInstanceVar(processInstanceId);
		
		
		//System.out.println(json);
		
		
	}

}
