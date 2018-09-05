/**
 * 
 */
package com.accenture.starter.model;

import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import com.amazonaws.DefaultRequest;
import com.amazonaws.Request;
import com.amazonaws.http.HttpMethodName;
import com.amazonaws.services.comprehend.model.KeyPhrase;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author saswat.kumar.sahoo
 *
 */
public class KeyPhrases {
	
	   
		private KeyPhrase[] keyPhrases;
	    @JsonProperty("@timestamp")
		private String timeStamp; 

	    public KeyPhrase[] getKeyPhrases ()
	    {
	        return keyPhrases;
	    }

	    public void setKeyPhrases (KeyPhrase[] keyPhrases)
	    {
	        this.keyPhrases = keyPhrases;
	    }

	    public String getTimeStamp() {
			return timeStamp;
		}

		
		public void setTimeStamp(String timeStamp) {
			this.timeStamp = timeStamp;
		}

		@Override
		public String toString() {
			return "KeyPhrases [keyPhrases=" + Arrays.toString(keyPhrases)
					+ ", timeStamp=" + timeStamp + "]";
		}
		
		public static void main(String[] args) {
			KeyPhrases p = new KeyPhrases();
			KeyPhrase[] x = new KeyPhrase[1];
			KeyPhrase px = new KeyPhrase();
			px.setText("hello");
			x[0]= px;
			KeyPhrase ph = new KeyPhrase();
			ph.setScore(12324F);
			p.setKeyPhrases(x);
			DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
			Date date = new Date();
			p.setTimeStamp(sdf.format(date));
			ObjectMapper mapper = new ObjectMapper();
			String jsonInString = null;
			try {
				jsonInString = mapper.writeValueAsString(p);
				System.out.println(jsonInString);
			
			System.out.println(jsonInString);
			
		
			RestTemplate restTemplate = new RestTemplate();
			
			System.out.println(jsonInString);
			HttpEntity<?> httpEntity = new HttpEntity<Object>(jsonInString, null);
			for(int i=0; i <1000; i ++){
				ResponseEntity<String> response = restTemplate.exchange("http://"+"54.146.38.210:9200/logstash/type1", HttpMethod.POST, httpEntity, String.class);
				System.out.println("Response:"+response.getBody());
			}
			
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
	}
