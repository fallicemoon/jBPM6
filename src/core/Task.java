package core;

import java.io.BufferedReader;
import java.io.IOException;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class Task {
	
	private JbpmRestEntity jbpmRestEntity;
	private String baseURL;
	
	public Task (JbpmRestEntity jbpmRestEntity) {
		this.jbpmRestEntity = jbpmRestEntity;
		this.baseURL = jbpmRestEntity.getBaseURL();
	}
	


	public JSONObject getAllTasks() throws JSONException {
		String url = String.format("%s/task/query", baseURL);
		BufferedReader reader = jbpmRestEntity.connect(url, "GET");
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
	
//	public JSONObject getAllTasksByVar(String deploymentId, String processDefId, String key, String value) throws JSONException, IOException{
//		JSONObject json = getAllTasks();
//		JSONObject tempJson = new JSONObject();
//		JSONObject responseJson = new JSONObject();
//		Variables variables = new Variables(jbpmRestEntity);
//		
//		for (int i = 1; i <= json.length(); i++) {
//			String index = String.valueOf(i);
//			if (!json.getJSONObject(index).getString("deployment-id").equals(deploymentId)){
//				continue;
//			}
//			if (!json.getJSONObject(index).getString("process-id").equals(processDefId)){
//				continue;
//			}			
//			tempJson.put(String.valueOf(tempJson.length()+1), json.getJSONObject(index));
//			
//		}
//		System.out.println(tempJson);
//		for (int i = 1; i <= tempJson.length(); i++) {
//			String index = String.valueOf(i);
//			String taskId = String.valueOf(tempJson.getJSONObject(index).getInt("id"));
//
//			String var = String.valueOf(variables.getTaskVar(deploymentId, taskId).get(key));
//			if (var.equals(value)) {
//				responseJson.put(String.valueOf(responseJson.length()+1), tempJson.getJSONObject(index));
//			}
//				
//		}
//		
//		return responseJson;
//	}
	
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
	
	public JSONObject getReadyTaskIdsByProcessInstanceId(String processInstanceId) throws IOException, JSONException {
		JSONObject json = getAllTasks();
		JSONObject responseJson = new JSONObject();
		JSONArray taskIds = new JSONArray();
		
		for(int i=1; i<=json.length(); i++){
			JSONObject object = json.getJSONObject(String.valueOf(i));
			String procInstId = String.valueOf(object.getInt("process-instance-id"));
			
			if(procInstId.equals(processInstanceId)&&object.getString("status").equals(TaskStatus.Ready.toString()))
				taskIds.put(String.valueOf(object.getInt("id")));
		
		}
		
		responseJson.put(processInstanceId,taskIds);
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
	
	public JSONObject getTask(String taskId) throws JSONException {
		String url = String.format("%s/task/%s", baseURL, taskId);
		BufferedReader reader = jbpmRestEntity.connect(url, "GET");
		JSONObject json;
		try {
			json = new JSONObject(reader.readLine().toString());
		} catch (IOException e) {
			json = new JSONObject();
			System.out.println("BufferReader has error......");
			json.put("success", "false");
			e.printStackTrace();
		}
		
		return json;
	}
	


}
