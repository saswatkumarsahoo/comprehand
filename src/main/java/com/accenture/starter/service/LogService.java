/**
 * 
 */
package com.accenture.starter.service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.accenture.starter.model.Log;
import com.accenture.starter.model.Topic;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author saswat.kumar.sahoo
 *
 */
@Service("esLogService")
public class LogService {
	
	static final String ES_URL = "elasticsearch"; 
	public  Object ingestToEs(String log, String type) {
		
		try {
			ObjectMapper mapper = new ObjectMapper();
			mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
			RestTemplate restTemplate = new RestTemplate();
			HttpHeaders headers = new HttpHeaders();
			headers.set("Authorization", "Basic ZWxhc3RpYzpjaGFuZ2VtZQ==");
			HttpEntity<?> httpEntity = new HttpEntity<String>(log, headers);
			
			System.out.println("******ES URL********"+getEsUrl());
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
	
	private static String createLog(Topic topic) {
		String jsonLog = null;
		try {
			ObjectMapper mapper = new ObjectMapper();
			mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
			DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
			sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
			Date date = new Date();	
			topic.setTimeStamp(sdf.format(date));
		
			jsonLog = mapper.writeValueAsString(topic);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return jsonLog;

	}
	
	public static void main(String[] args) {

		BufferedReader br = null;
		FileReader fr = null;

		try {

			//br = new BufferedReader(new FileReader(FILENAME));
			fr = new FileReader("topic.csv");
			br = new BufferedReader(fr);

			String sCurrentLine;

			while ((sCurrentLine = br.readLine()) != null) {
				 String[] temp = sCurrentLine.split(",");
				 Topic topic = new Topic();
				 topic.setTopic(temp[0]);
				 topic.setTerm(temp[1]);
				 topic.setWeight(Double.parseDouble(temp[2]));
				 
				System.out.println(createLog(topic));
				//ingestToEs(createLog(topic),"topic");
			}

		} catch (Exception e) {

			e.printStackTrace();

		} finally {

			try {

				if (br != null)
					br.close();

				if (fr != null)
					fr.close();

			} catch (IOException ex) {

				ex.printStackTrace();

			}

		}

	}

}
