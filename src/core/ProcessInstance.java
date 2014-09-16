package core;

import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ProcessInstance {

	private JbpmRestEntity jbpmRestEntity;
	private String baseURL;
	
	public ProcessInstance (JbpmRestEntity jbpmRestEntity) {
		this.jbpmRestEntity = jbpmRestEntity;
		this.baseURL = jbpmRestEntity.getBaseURL();
	}
	
	
	public JSONObject getProcessInstanceByCreator(String creator) throws JSONException, IOException {
		String url = String.format("%s/history/instances", baseURL);
		JSONObject json = new JSONObject(jbpmRestEntity.connect(url, "GET").readLine());
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
		JSONObject json = new JSONObject(jbpmRestEntity.connect(url, "GET").readLine());
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

}
