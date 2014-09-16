package core;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.json.JSONException;
import org.json.JSONObject;

public class TaskLifeCycle {
	
	private JbpmRestEntity jbpmRestEntity;
	private String baseURL;

	public TaskLifeCycle(JbpmRestEntity jbpmRestEntity) {
		this.jbpmRestEntity = jbpmRestEntity;
		this.baseURL = jbpmRestEntity.getBaseURL();
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
		BufferedReader reader = jbpmRestEntity.connect(url, "POST");
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
		return new JSONObject(jbpmRestEntity.connect(url, "POST").readLine());
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
		return new JSONObject(jbpmRestEntity.connect(url, "POST").readLine());
	}
	
	public JSONObject skipTask(String taskId) throws IOException, JSONException {
		String url = String.format("%s/task/%s/skip", baseURL, taskId);
		return new JSONObject(jbpmRestEntity.connect(url, "POST").readLine());
	}
	
	public JSONObject abortProcess(String deploymentId, String processInstanceId) throws JSONException, IOException {
		String url = String.format("%s/runtime/%s/process/instance/%s/abort", baseURL, deploymentId, processInstanceId);
		return new JSONObject(jbpmRestEntity.connect(url, "POST").readLine());
	}	

}
