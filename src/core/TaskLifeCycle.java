package core;


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;



import org.json.JSONArray;
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

	
	/**
	 * assign this task to user(right group for this task is necessary)
	 * @param taskId
	 * @return JSONObject it's jbpm return
	 * @throws IOException
	 * @throws JSONException
	 */
	public boolean startTask(JSONArray taskIds) throws IOException, JSONException {
		JSONArray urls = new JSONArray();
		
		for (int i = 0; i < taskIds.length(); i++) {
			String url = String.format("%s/task/%s/start", baseURL, taskIds.getString(i));
			urls.put(url);
		}
		
		try {
			for (int i = 0; i < urls.length(); i++) {
				JSONObject responseJson = new JSONObject(jbpmRestEntity.connect(urls.getString(i), "POST").readLine());
				
				if (!responseJson.getString("status").equals("SUCCESS"))
					return false;
			}
			return true;
			
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean startTask(String taskId) throws IOException, JSONException {
		String url = String.format("%s/task/%s/start", baseURL, taskId);
		try {
			JSONObject responseJson = new JSONObject(jbpmRestEntity.connect(url, "POST").readLine());
			if (responseJson.getString("status").equals("SUCCESS")) {
				return true;
			}
			
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	
	/**
	 * 
	 * @param taskId
	 * @param map map's key should starts with "map_" (add before key), map's value type is used only 
	 * String or Integer 
	 * 
	 * @throws IOException
	 * @throws JSONException
	 */
	public boolean completeTask(String taskId, Map<String, Object> map) throws IOException, JSONException {
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
		
		String queryString = queryMap.toString().replaceAll(", ", "&map_").replaceAll("[{}]", "");
		queryString = "map_".concat(queryString);
		
		String url = String.format("%s/task/%s/complete?%s", baseURL, taskId, queryString);
		
		try {
			JSONObject responseJson = new JSONObject(jbpmRestEntity.connect(url, "POST").readLine());
			System.out.println("XXX"+responseJson);
			if (responseJson.getString("status").equals("SUCCESS")) {
				return true;
			}
			return false;
			
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

	}
	
	public boolean skipTask(String taskId) throws IOException, JSONException {
		String url = String.format("%s/task/%s/skip", baseURL, taskId);
		
		try {
			JSONObject responseJson = new JSONObject(jbpmRestEntity.connect(url, "POST").readLine());
			if (responseJson.getString("status").equals("SUCCESS")) {
				return true;
			}
			return false;
			
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public JSONObject stopTask(String taskId) throws JSONException, IOException {
		String url = String.format("%s/task/%s/stop", baseURL, taskId);
		JSONObject responseJson = new JSONObject(jbpmRestEntity.connect(url, "POST").readLine());
		return responseJson;
	}
	
	/**
	 * InProgress to Reserved, need to startTask() again.
	 * @param taskId
	 * @param targetId 
	 * @return
	 * @throws JSONException
	 * @throws IOException
	 */
	public boolean delegateTask(String taskId, String targetId) throws JSONException, IOException {
		String url = String.format("%s/task/%s/delegate?targetEntityId=%s", baseURL, taskId, targetId);
		JSONObject responseJson = new JSONObject(jbpmRestEntity.connect(url, "POST").readLine());
		
		if (!responseJson.getString("status").equals("SUCCESS"))
			return false;
		
		return true;
	}
	

	
	


}
