package io.github.fvarrui.eclipse.plugin.pastebin.utils;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class PasteBinApi {
	
	public static String paste(String apiKey, String text, String filename, String type) throws Exception {
		
		if (apiKey == null || apiKey.trim().isEmpty()) {
			throw new Exception("You must set an API KEY in PasteBin Plugin preferences page.");
		}
		
        Map<String, Object> data = new HashMap<>();
        data.put("api_dev_key", apiKey);
        data.put("api_option", "paste");
        if (type != null && !type.isEmpty()) {
            data.put("api_paste_format", type);
        }
        data.put("api_paste_code", text);
        data.put("api_paste_name", filename);

	    HttpRequest request = HttpRequest.newBuilder()
	    		.uri(URI.create("https://pastebin.com/api/api_post.php"))
                .header("Content-Type", "application/x-www-form-urlencoded")
	    		.POST(HttpUtils.asBodyPublisher(data))
	    		.timeout(Duration.ofSeconds(15))
	    		.build();
	    
		try {
			
			HttpResponse<String> response = HttpClient.newHttpClient().send(request, BodyHandlers.ofString(StandardCharsets.UTF_8));
			
			if (response.statusCode() == 200) {
				return response.body();
			} else {
				throw new Exception("Error creating paste: " + response.body() + " (" + response.statusCode() + ")");
			}
			
		} catch (IOException | InterruptedException e) {
			
			throw new Exception("Error creating paste (" + e.getClass().getSimpleName() + "): " + e.getMessage());				
			
		}
		
	}

}
