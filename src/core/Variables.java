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
	
	public JSONObject getTaskVar(String taskId) throws JSONException, IOException {
		String url = String.format("%s/runtime/com.henry:henry_project:1.0/workitem/%s", baseURL, taskId);
		JSONObject json = new JSONObject(jbpmRestEntity.connect(url, "GET").readLine());
		return json.getJSONObject("param-map");
	}

}
