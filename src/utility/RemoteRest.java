package utility;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collection;
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

import com.sun.mail.imap.protocol.Status;

/**This class is used for your custom REST service */
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


	//----------task life cycle
	/**Create a new process instance 
	 * @param map value's type is only for String or Integer
	 */
	public String createProcessInstance(String deploymentId, String processDefId, Map<String, Object> map) throws JSONException, IOException, ScriptException {
		Set<String> set = map.keySet();
		Map<String, String> queryMap = new HashMap<String, String>();
		
		for (String key : set) {
			Object value = map.get(key);
			if (value instanceof Integer) {
				String stringValue = String.valueOf(value) + "i";
				queryMap.put(key, stringValue);
			} else if (value instanceof String){
				String stringValue = (String)value;
				queryMap.put(key, stringValue);
			} else {
				throw new ClassCastException("This method's parameter, map's value only accept Integer or String");
			}
		}
		
		String queryString = queryMap.toString().replaceAll(", ", "&").replaceAll("[{}]", "");

        ScriptEngineManager factory = new ScriptEngineManager();
        ScriptEngine engine = factory.getEngineByName("JavaScript");
        String query = (String)engine.eval("encodeURI('"+queryString+"')");

		String url = String.format("%s/runtime/%s/process/%s/start?%s", baseURL, deploymentId, processDefId, query);
		BufferedReader reader = connect(url, "POST");
		JSONObject json = new JSONObject(reader.readLine().toString());
		return String.valueOf(json.getInt("id"));
	}
	
	/**
	 * assign this task to user(right group for this task is necessary)
	 * @param taskId
	 * @return JSONObject it's jbpm return
	 * @throws IOException
	 * @throws JSONException
	 */
	public JSONObject startTask(String taskId) throws IOException, JSONException {
		String url = String.format("%s/task/%s/start", baseURL, taskId);
		return new JSONObject(connect(url, "POST").readLine());
	}
	
	
	/**
	 * 
	 * @param taskId
	 * @param map map's key should starts with "map_" (add before key), map's value type is used only 
	 * String or Integer 
	 * @return JSONObject
	 * @throws IOException
	 * @throws JSONException
	 */
	public JSONObject completeTask(String taskId, Map<String, Object> map) throws IOException, JSONException {
		Set<String> set = map.keySet();
		Map<String, String> queryMap = new HashMap<String, String>();
		
		for (String key : set) {
			Object value = map.get(key);
			if (value instanceof Integer) {
				String stringValue = String.valueOf(value) + "i";
				queryMap.put(key, stringValue);
			} else if (value instanceof String){
				String stringValue = (String)value;
				queryMap.put(key, stringValue);
			} else {
				throw new ClassCastException("This method's parameter, map's value only accept Integer or String");
			}
		}
		
		String query = queryMap.toString().replaceAll(", ", "&").replaceAll("[{}]", "");
		String url = String.format("%s/task/%s/complete?%s", baseURL, taskId, query);
		return new JSONObject(connect(url, "POST").readLine());
	}
	
	public JSONObject skipTask(String taskId) throws IOException, JSONException {
		String url = String.format("%s/task/%s/skip", baseURL, taskId);
		return new JSONObject(connect(url, "POST").readLine());
	}
	
	public JSONObject abortProcess(String deploymentId, String processInstanceId) throws JSONException, IOException {
		String url = String.format("%s/runtime/%s/process/instance/%s/abort", baseURL, deploymentId, processInstanceId);
		return new JSONObject(connect(url, "POST").readLine());
	}	
	
	//-----------
	public JSONObject getReadyTaskIdByProcessInstanceId(String processInstanceId) throws IOException, JSONException {
		JSONObject json = getAllTasks();
		JSONObject responseJson = new JSONObject();
		
		for(int i=1; i<=json.length(); i++){
			JSONObject object = json.getJSONObject(String.valueOf(i));
			String procInstId = String.valueOf(object.getInt("process-instance-id"));

			if(procInstId.equals(processInstanceId)&&object.getString("status").equals(TaskStatus.Ready.toString()))
				responseJson.put(procInstId,String.valueOf(object.getInt("id")));
		}
		
		return responseJson;
	}
	
	public JSONObject getTasksByProcessInstanceId(String processInstanceId) throws JSONException, IOException {
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
	
	public JSONObject getProcessInstanceState(String processInstanceId) throws JSONException, IOException {
		String url = String.format("%s/history/instances", baseURL);
		JSONObject json = new JSONObject(connect(url, "GET").readLine());
		JSONObject responseJson = new JSONObject();
		JSONArray historyLogList = json.getJSONArray("historyLogList");
		
		for (int i = 0; i < historyLogList.length(); i++) {
			JSONObject j = historyLogList.getJSONObject(i).getJSONObject("process-instance-log");
			
			if (processInstanceId.equals(String.valueOf(j.getInt("process-instance-id")))){
				String statusString = "";
				int status = j.getInt("status");		
				switch (status) {
				case 1:
					statusString = ProcessInstanceStatus.Active.toString();
					break;
				case 2:
					statusString = ProcessInstanceStatus.Completed.toString();
				    break;
				case 3:
					statusString = ProcessInstanceStatus.Aborted.toString();
					break;
				}
				responseJson.put(String.valueOf(j.getInt("process-instance-id")), statusString);
			}
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


	//---------------------------------------------
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
	
	public JSONObject getTaskVar(String taskId) throws JSONException, IOException {
		String url = String.format("%s/runtime/com.henry:henry_project:1.0/workitem/%s", baseURL, taskId);
		JSONObject json = new JSONObject(connect(url, "GET").readLine());
		return json.getJSONObject("param-map");
	}
	
	public static void main(String[] args) throws IOException, JSONException, ScriptException {
		RemoteRest remoteRest = new RemoteRest();
		String deploymentId = "com.newegg.henry:henry_proj:1.0";
		String processDefId = "henry_proj.take_off_request";
		
		String taskName = "manager_approve";
		
		//1.user side, create process
//		Map<String, Object> map = new HashMap<String, Object>();
//		map.put("map_reasons","i am sick");
//		map.put("map_day", 3);
//		map.put("map_isAppro", 1);
//		
//		String processInstanceId = remoteRest.createProcessInstance(deploymentId, processDefId, map);
		JSONObject json = remoteRest.getReadyTaskIdByProcessInstanceId("103");
		
		//remoteRest.startTask(json.getString("103"));
		
		//2.user side, get my process
		//JSONObject json = remoteRest.getTasksByProcessInstanceId("103");
		//JSONObject json = remoteRest.getProcessInstanceByCreator("john");
		
		//skip
		//remoteRest.skipTask(json.getString(processInstanceId));
		
		//abort
		//JSONObject json = remoteRest.abortProcess(deploymentId, "129");
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
//		remoteRest.setUser("henry_hr");
//		Map<String, Object> map = new HashMap<String, Object>();
//		map.put("map_isApprove_", 1);
//		JSONObject json = remoteRest.completeTask("129", map);
		
		//JSONObject json = remoteRest.getProcessInstanceState("94");
		//---------------------------------------------------
		//9.get global var
		//JSONObject json = remoteRest.getProcessInstanceVar(processInstanceId);
		
		//10.get local var
		//JSONObject json = remoteRest.getTaskVar("63");
		
		System.out.println(json);
		
		
	}

}
