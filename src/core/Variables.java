package core;

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
	
	/**
	 * get task variables, but only 'Ready' and 'InProgress' status task (don't use this method to get status is 'Completed' task)
	 * @param deploymentId
	 * @param taskId
	 * @return Task variables, TaskName, GroupId and NodeName.
	 * @throws JSONException
	 * @throws IOException
	 */
	public JSONObject getTaskVar(String deploymentId, String taskId) throws JSONException, IOException {
		String url = String.format("%s/runtime/%s/workitem/%s", baseURL, deploymentId, taskId);
		JSONObject json = new JSONObject(jbpmRestEntity.connect(url, "GET").readLine());
		return json.getJSONObject("param-map");
	}

}
