package core;

import java.io.BufferedReader;
import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Variables {
	
	private JbpmRestEntity jbpmRestEntity;
	private String baseURL;

	public Variables(JbpmRestEntity jbpmRestEntity) {
		this.jbpmRestEntity = jbpmRestEntity;
		this.baseURL = jbpmRestEntity.getBaseURL();
	}
	
	
	
	public JSONObject getProcessInstanceVar(String processInstanceId) throws JSONException, IOException {
		String url = String.format("%s/history/instance/%s/variable", baseURL, processInstanceId);
		JSONArray historyLogList = new JSONObject(jbpmRestEntity.connect(url, "GET").readLine()).getJSONArray("historyLogList");
		
		JSONObject responseJson = new JSONObject();
		
		for (int i = 0; i < historyLogList.length(); i++) {
			JSONObject j = historyLogList.getJSONObject(i).getJSONObject("variable-instance-log");
			String key = j.getString("variable-id");
			String value = j.getString("value");
			responseJson.put(key, value);
		}
		
		return responseJson;
	}
	
	public JSONObject getProcessInstanceVar(String processInstanceId, String varId) throws JSONException {
		String url = String.format("%s/history/instance/%s/variable/ReqType", baseURL, processInstanceId, varId);
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
	
	/**
	 * get task variables, but only 'Ready' and 'InProgress' status task (don't use this method to get status is 'Completed' task)
	 * @param deploymentId
	 * @param taskId
	 * @return Task variables, TaskName, GroupId and NodeName.
	 * @throws JSONException
	 * @throws IOException
	 */
	public JSONObject getTaskVar(String deploymentId, String taskId) throws JSONException, IOException {
		Task task = new Task(jbpmRestEntity);
		String workitemId = String.valueOf(task.getTask(taskId).getJSONObject("jaxbTaskData").getInt("work-item-id"));
		String url = String.format("%s/runtime/%s/workitem/%s", baseURL, deploymentId, workitemId);
		JSONObject json = new JSONObject(jbpmRestEntity.connect(url, "GET").readLine());
		return json.getJSONObject("param-map");
	}

}
