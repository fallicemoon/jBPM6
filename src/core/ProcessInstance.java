package core;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

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
		
		String queryString = queryMap.toString().replaceAll(", ", "&").replaceAll("[{}]", "").replace("&", "&map_").replace("?", "?map_");

        ScriptEngineManager factory = new ScriptEngineManager();
        ScriptEngine engine = factory.getEngineByName("JavaScript");
        String query = (String)engine.eval("encodeURI('"+queryString+"')");

		String url = String.format("%s/runtime/%s/process/%s/start?%s", baseURL, deploymentId, processDefId, query);
		BufferedReader reader = jbpmRestEntity.connect(url, "POST");
		JSONObject json = new JSONObject(reader.readLine().toString());
		return String.valueOf(json.getInt("id"));
	}
	
	public JSONObject abortProcess(String deploymentId, String processInstanceId) throws JSONException, IOException {
		String url = String.format("%s/runtime/%s/process/instance/%s/abort", baseURL, deploymentId, processInstanceId);
		return new JSONObject(jbpmRestEntity.connect(url, "POST").readLine());
	}	
	
	
	//--------------
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
