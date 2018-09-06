/**
 * 
 */
package com.accenture.starter.service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author saswat.kumar.sahoo
 *
 */
@Service("esLogService")
public class LogService {
	
	static final String ES_URL = "34.235.161.148"; 
	public Object ingestToEs(String log, String type) {
		
		try {
			ObjectMapper mapper = new ObjectMapper();
			mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
			RestTemplate restTemplate = new RestTemplate();
			HttpEntity<?> httpEntity = new HttpEntity<String>(log, null);
			ResponseEntity<String> response = restTemplate.exchange("http://"+ getEsUrl() + ":9200/" + getEsIndex() + "/"+type,
					HttpMethod.POST, httpEntity, String.class);
			System.out.println(response.getBody());
			return response;

		} catch (Exception e) {
			System.out.println(e.getMessage());
			return e;
		}

	}



	private static String getEsUrl() {
		return System.getenv("ES_URL") != null ? System.getenv("ES_URL"): ES_URL;

	}

	private static String getEsIndex() {
		DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		Date date = new Date();
		return "logstash-" + sdf.format(date);

	}
}
