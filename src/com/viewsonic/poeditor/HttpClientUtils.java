package com.viewsonic.poeditor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.json.JSONException;
import org.json.JSONObject;

public class HttpClientUtils {
	String line;
	String list_API = "https://api.poeditor.com/v2/projects/list";
	// String export_API = "https://api.poeditor.com/v2/projects/export";
	// String token = "api_token=\"4967fd58e0f2e0d00331f17ede05bd21\"";
	// String id = "id=\"415711\"";
	// String language = "language=\"en\"";
	// String type = "type=\"xlsx\"";
	// String[] commands = { token, type };
	BufferedReader is;
	String downloadURL = "";

	public void doCurlPost(String token, String export_API, String id, String language, String tag) {
		String command = curlUrlBuilder(token, export_API, id, language, tag);
		try {
			Process process = Runtime.getRuntime().exec(command);
			is = new BufferedReader(new InputStreamReader(process.getInputStream()));
			while ((line = is.readLine()) != null) {
				System.out.println(line);
				processJSON(line);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String curlUrlBuilder(String token, String export_API, String id, String langauage, String tag) {
		String type = "type=\"xlsx\"";
		String realToken = "api_token=" + token;
		String[] commands = { realToken, type };
		StringBuilder sb = new StringBuilder();
		sb.append("curl -X POST " + export_API);
		sb.append(" -d " + "id=" + "\"" + id + "\"");
		sb.append(" -d " + "language=" + "\"" + langauage + "\"");
		if (!tag.equals("")) {
			// sb.append(" -d " + "tags=" + "{" + "\"all\":" + "\"" + tag + "\"" + "}");
			// sb.append(" -d " + "tags=" + "[" + "\"" + tag + "\"" + "]");
			sb.append(" -d " + "tags=" + tag);
		}
		for (int i = 0; i < commands.length; i++) {
			sb.append(" -d " + commands[i]);
		}
		String result = sb.toString();
		System.out.println(result);
		return result;
	}

	private void processJSON(String jsonString) {
		JSONObject obj = new JSONObject(jsonString);
		try {
			String downloadLink = obj.getJSONObject("result").getString("url");
			String workDirectory = System.getProperty("user.dir");
			DownloadHelper.downloadFile(downloadLink, workDirectory);
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (JSONException je) {
			// Not install curl
			System.out.println("請先安裝CURL，才能繼續使用此工具");
		}
	}
}
