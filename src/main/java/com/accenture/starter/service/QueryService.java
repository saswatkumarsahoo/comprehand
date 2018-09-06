/**
 * 
 */
package com.accenture.starter.service;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

import com.accenture.starter.model.Input;
import com.accenture.starter.model.Log;
import com.accenture.starter.util.AwsClientBuilder;
import com.accenture.starter.util.PropertyfileReader;
import com.amazonaws.services.comprehend.AmazonComprehend;
import com.amazonaws.services.comprehend.model.DetectEntitiesRequest;
import com.amazonaws.services.comprehend.model.DetectEntitiesResult;
import com.amazonaws.services.comprehend.model.DetectKeyPhrasesRequest;
import com.amazonaws.services.comprehend.model.DetectKeyPhrasesResult;
import com.amazonaws.services.comprehend.model.DetectSentimentRequest;
import com.amazonaws.services.comprehend.model.DetectSentimentResult;
import com.amazonaws.services.comprehend.model.Entity;
import com.amazonaws.services.comprehend.model.KeyPhrase;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author saswat.kumar.sahoo
 *
 */

@Service("queryService")
public class QueryService {
	// static final String hashTag = "#edfenergy";
	static final int count = 10;
	static long sinceId = 0;
	static long numberOfTweets = 0;
	static final String LOG_SERVICE_URL = "log";
	static final String COMPREHAND_SERVICE_URL = "comprehand";

	@Autowired
	private PropertyfileReader propertyfileReader;
	@Autowired
	private LogService esLogService;
	@Autowired
	private AwsClientBuilder awsClientBuilder;

	@Async
	public CompletableFuture<?> findTweets(String query)
			throws InterruptedException {

		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true)
				.setOAuthConsumerKey(
						System.getenv("TWITTER_CONSUMER_KEY") != null ? System
								.getenv("TWITTER_CONSUMER_KEY")
								: propertyfileReader
										.getProp("TWITTER_CONSUMER_KEY"))
				.setOAuthConsumerSecret(
						System.getenv("TWITTER_CONSUMER_SECRET") != null ? System
								.getenv("TWITTER_CONSUMER_SECRET")
								: propertyfileReader
										.getProp("TWITTER_CONSUMER_SECRET"))
				.setOAuthAccessToken(
						System.getenv("TWITTER_ACCESS_TOKEN") != null ? System
								.getenv("TWITTER_ACCESS_TOKEN")
								: propertyfileReader
										.getProp("TWITTER_ACCESS_TOKEN"))
				.setOAuthAccessTokenSecret(
						System.getenv("TWITTER_ACCESS_TOKEN_SECRET") != null ? System
								.getenv("TWITTER_ACCESS_TOKEN_SECRET")
								: propertyfileReader
										.getProp("TWITTER_ACCESS_TOKEN_SECRET"));
		TwitterFactory tf = new TwitterFactory(cb.build());
		Twitter twitter = tf.getInstance();
		// Twitter twitter = new TwitterFactory().getInstance();
		Query queryMax = new Query(query);
		queryMax.setCount(count);
		queryMax.setLang("en");
		// Thread.sleep(4000L);
		System.out.println(query);
		AmazonComprehend comprehendClient = awsClientBuilder
				.getComprehendClient();
		getTweets(queryMax, twitter, "maxId", comprehendClient);
		queryMax = null;
		do {
			Query querySince = new Query(query);
			querySince.setCount(count);
			querySince.setSinceId(sinceId);
			System.out.println("sinceId=" + sinceId);
			getTweets(querySince, twitter, "sinceId", comprehendClient);
			querySince = null;
		} while (checkIfSinceTweetsAreAvaliable(twitter, query));
		return CompletableFuture.completedFuture(null);
	}

	private static boolean checkIfSinceTweetsAreAvaliable(Twitter twitter,
			String q) {
		Query query = new Query(q);
		query.setCount(count);
		query.setSinceId(sinceId);
		try {
			QueryResult result = twitter.search(query);
			if (result.getTweets() == null || result.getTweets().isEmpty()) {
				query = null;
				return false;
			}
		} catch (TwitterException te) {
			System.out.println("Couldn't connect: " + te);
			System.exit(-1);
		} catch (Exception e) {
			System.out.println("Something went wrong: " + e);
			System.exit(-1);
		}
		return true;
	}

	private void getTweets(Query query, Twitter twitter, String mode,
			AmazonComprehend comprehendClient) {
		boolean getTweets = true;
		long maxId = 0;
		long whileCount = 0;
		RestTemplate restTemplate = new RestTemplate();
		Input input = new Input();

		while (getTweets) {
			try {
				QueryResult result = twitter.search(query);
				if (result.getTweets() == null || result.getTweets().isEmpty()) {
					getTweets = false;
				} else {
					int forCount = 0;
					try {
						for (Status status : result.getTweets()) {
							if (whileCount == 0 && forCount == 0) {
								sinceId = status.getId();
							}
							input.setText(status.getText());
							Object data = postText(restTemplate, input);
							
							System.out.println("data->"+data);
							String log = createLog(status.getText(),
									query.getQuery(),
									data);
							System.out.println(postLog(restTemplate, log));
							if (forCount == result.getTweets().size() - 1) {
								maxId = status.getId();
							}
							forCount++;
						}
						numberOfTweets = numberOfTweets
								+ result.getTweets().size();
						query.setMaxId(maxId - 1);
					} catch (Exception ex) {
						System.out.println("Error: " + ex);
						System.exit(-1);
					}
				}
			} catch (TwitterException te) {
				System.out.println("Couldn't connect: " + te);

			} catch (Exception e) {
				System.out.println("Something went wrong: " + e);
				System.exit(-1);

			}
			whileCount++;
		}
		System.out.println("Total tweets count=======" + numberOfTweets);
	}

	private static Object postText(RestTemplate restTemplate, Input input) {
		HttpEntity<Input> httpEntity = new HttpEntity<Input>(input, null);
		ResponseEntity<String> response = restTemplate.exchange("http://"
				+ getComprehandServiceUrl() + ":8080/comprehand",
				HttpMethod.POST, httpEntity, String.class);
		System.out.println(response.getBody());
		return response.getBody();
	}

	private static Object postLog(RestTemplate restTemplate, String log) {
		System.out.println("postLog"+log);	
		HttpEntity<String> httpEntity = new HttpEntity<String>(log, null);
		ResponseEntity<String> response = restTemplate.exchange("http://"
				+ getLogServiceUrl() + ":8080/logs/sentiments", HttpMethod.POST,
				httpEntity, String.class);
		System.out.println(response);
		return response;
	}

	private String createLog(String text, String query, Object obj) {
		Log log = new Log();
		String jsonLog = null;
		try {
			ObjectMapper mapper = new ObjectMapper();
			mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
			DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
			sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
			Date date = new Date();
			log.addValue("log", mapper.readValue(obj.toString(), Object.class));
			log.setTimeStamp(sdf.format(date));
			log.setHandle(query);
			log.addValue("text", text);
			jsonLog = mapper.writeValueAsString(log);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return jsonLog;

	}
	private static String getLogServiceUrl() {
		System.out.println(LOG_SERVICE_URL);
		return System.getenv("LOG_SERVICE_URL") != null ? System
				.getenv("LOG_SERVICE_URL") : LOG_SERVICE_URL;
	}

	private static String getComprehandServiceUrl() {
		return System.getenv("COMPREHAND_SERVICE_URL") != null ? System
				.getenv("COMPREHAND_SERVICE_URL") : COMPREHAND_SERVICE_URL;
	}

}