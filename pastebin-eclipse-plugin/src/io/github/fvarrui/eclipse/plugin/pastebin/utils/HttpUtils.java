package io.github.fvarrui.eclipse.plugin.pastebin.utils;

import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class HttpUtils {

	public static String ofFormData(Map<String, Object> data) {
	    var builder = new StringBuilder();
	    for (Map.Entry<String, Object> entry : data.entrySet()) {
	        if (builder.length() > 0) {
	            builder.append("&");
	        }
	        builder.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));
	        builder.append("=");
	        builder.append(URLEncoder.encode(entry.getValue().toString(), StandardCharsets.UTF_8));
	    }
	    return builder.toString();
	}
	
	public static HttpRequest.BodyPublisher asBodyPublisher(Map<String, Object> data) {        
        return HttpRequest.BodyPublishers.ofString(ofFormData(data));
    }
	
}
