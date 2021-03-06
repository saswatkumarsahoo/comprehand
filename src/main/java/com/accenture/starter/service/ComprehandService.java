/**
 * 
 */
package com.accenture.starter.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.accenture.starter.util.AwsClientBuilder;
import com.amazonaws.services.comprehend.AmazonComprehend;
import com.amazonaws.services.comprehend.model.DetectEntitiesRequest;
import com.amazonaws.services.comprehend.model.DetectEntitiesResult;
import com.amazonaws.services.comprehend.model.DetectKeyPhrasesRequest;
import com.amazonaws.services.comprehend.model.DetectKeyPhrasesResult;
import com.amazonaws.services.comprehend.model.DetectSentimentRequest;
import com.amazonaws.services.comprehend.model.DetectSentimentResult;
import com.amazonaws.services.comprehend.model.DetectSyntaxRequest;
import com.amazonaws.services.comprehend.model.Entity;
import com.amazonaws.services.comprehend.model.KeyPhrase;
import com.amazonaws.services.comprehend.model.SyntaxToken;

/**
 * @author saswat.kumar.sahoo
 *
 */
@Service("comprehandService")
public class ComprehandService {

	@Autowired
	private AwsClientBuilder awsClientBuilder;
	
	public Map <String, Object> analyzeText(String text){
		Map<String, Object> map = new HashMap<String, Object>();
		try{
		map.put("keyPhrases", getKeyPhrases(text, awsClientBuilder.getComprehendClient()));
		map.put("sentiments", detectSentiments(text, awsClientBuilder.getComprehendClient()));
		map.put("entities", extractEntities(text, awsClientBuilder.getComprehendClient()));
		return map;
		} catch(Exception ex){
			return null;
		}
		
	}
	
	
	private static List<KeyPhrase> getKeyPhrases(String text,
			AmazonComprehend comprehendClient) {
		DetectKeyPhrasesRequest detectKeyPhrasesRequest = new DetectKeyPhrasesRequest()
				.withText(text).withLanguageCode("en");
		DetectKeyPhrasesResult detectKeyPhrasesResult = comprehendClient
				.detectKeyPhrases(detectKeyPhrasesRequest);

		return detectKeyPhrasesResult.getKeyPhrases();

	}

	private static DetectSentimentResult detectSentiments(String text,
			AmazonComprehend comprehendClient) {
		DetectSentimentRequest detectSentimentRequest = new DetectSentimentRequest()
				.withText(text).withLanguageCode("en");
		DetectSentimentResult detectSentimentResult = comprehendClient
				.detectSentiment(detectSentimentRequest);
		return detectSentimentResult;
	}

	private static Map<String,List<Entity>> extractEntities(String text,
			AmazonComprehend comprehendClient) {
		DetectEntitiesRequest detectEntitiesRequest = new DetectEntitiesRequest()
				.withText(text).withLanguageCode("en");
		DetectEntitiesResult detectEntitiesResult = comprehendClient
				.detectEntities(detectEntitiesRequest);
		
		Map<String,List<Entity>> entities = new HashMap<String,List<Entity>>();
		for (Entity entity : detectEntitiesResult.getEntities()){
			
			if (entities.get(entity.getType()) == null){
				List<Entity> e = new ArrayList<Entity>();
				e.add(entity);
				entities.put(entity.getType(), e);
			}
			else{
				List<Entity> e = entities.get(entity.getType());
				e.add(entity);
				entities.put(entity.getType(), e);
			}
			
		}
		return entities;
	}
	
	private static void extractSyntaxes(String text,
			AmazonComprehend comprehendClient) {
		DetectSyntaxRequest detectSyntaxRequest = new DetectSyntaxRequest()
				.withText(text).withLanguageCode("en");
		for (SyntaxToken token :comprehendClient.detectSyntax(detectSyntaxRequest).getSyntaxTokens()){
			
		}
		
	}
}
