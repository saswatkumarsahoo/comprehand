/**
 * 
 */
package com.accenture.starter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.CompletableFuture;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.accenture.starter.model.Log;
import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonWebServiceRequest;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.retry.RetryPolicy;
import com.amazonaws.retry.RetryPolicy.BackoffStrategy;
import com.amazonaws.retry.RetryPolicy.RetryCondition;
import com.amazonaws.services.comprehend.AmazonComprehend;
import com.amazonaws.services.comprehend.AmazonComprehendClientBuilder;
import com.amazonaws.services.comprehend.model.DetectEntitiesRequest;
import com.amazonaws.services.comprehend.model.DetectEntitiesResult;
import com.amazonaws.services.comprehend.model.DetectKeyPhrasesRequest;
import com.amazonaws.services.comprehend.model.DetectKeyPhrasesResult;
import com.amazonaws.services.comprehend.model.DetectSentimentRequest;
import com.amazonaws.services.comprehend.model.DetectSentimentResult;
import com.amazonaws.services.comprehend.model.Entity;
import com.amazonaws.services.comprehend.model.KeyPhrase;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;

@Service("twitterService")
public class TwitterAPIconsumer_Twitter4J {
	static final String hashTag = "#edfenergy";
	static final int count = 10;
	static long sinceId = 0;
	static long numberOfTweets = 0;
	static final String ES_URL = "18.206.99.87"; 

		@Async
	    public CompletableFuture<?> findTweets(String query) throws InterruptedException {
	        Twitter twitter = new TwitterFactory().getInstance();
			Query queryMax = new Query(query);
			queryMax.setCount(count);
			queryMax.setLang("en");
			//Thread.sleep(4000L);
			System.out.println(query);
			AmazonComprehend comprehendClient = init();
			getTweets(queryMax, twitter, "maxId", comprehendClient);
			queryMax = null;
			do {
				Query querySince = new Query(hashTag);
				querySince.setCount(count);
				querySince.setSinceId(sinceId);
				System.out.println("sinceId="+sinceId);
				getTweets(querySince, twitter, "sinceId", comprehendClient);
				querySince = null;
			} 
			while (checkIfSinceTweetsAreAvaliable(twitter));
	        return CompletableFuture.completedFuture(null);
	    }

	public static void main(String[] args) {

		Twitter twitter = new TwitterFactory().getInstance();
	    // get latest tweets as of now
		// At this point store sinceId in database
		Query queryMax = new Query(hashTag);
		queryMax.setCount(count);
		queryMax.setLang("en");
		AmazonComprehend comprehendClient = init();
		getTweets(queryMax, twitter, "maxId", comprehendClient);
		queryMax = null;

		// get tweets that may have occurred while processing above data
		// Fetch sinceId from database and get tweets, also at this point store
		// the sinceId

		do {
			Query querySince = new Query(hashTag);
			querySince.setCount(count);
			querySince.setSinceId(sinceId);
			System.out.println("sinceId="+sinceId);
			getTweets(querySince, twitter, "sinceId", comprehendClient);
			querySince = null;
		} while (checkIfSinceTweetsAreAvaliable(twitter));

	}

	private static boolean checkIfSinceTweetsAreAvaliable(Twitter twitter) {
		Query query = new Query(hashTag);
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

	private static void getTweets(Query query, Twitter twitter, String mode,
			AmazonComprehend comprehendClient) {
		boolean getTweets = true;
		long maxId = 0;
		long whileCount = 0;

		while (getTweets) {
			try {
				QueryResult result = twitter.search(query);
				if (result.getTweets() == null || result.getTweets().isEmpty()) {
					getTweets = false;
				} else {
					System.out
							.println("***********************************************");
					System.out.println("Gathered " + result.getTweets().size()
							+ " tweets");
					int forCount = 0;
					for (Status status : result.getTweets()) {
						if (whileCount == 0 && forCount == 0) {
							sinceId = status.getId();// Store sinceId in
														// database
							System.out.println("sinceId= " + sinceId);
						}
						System.out.println("Id= " + status.getId());
						ingestToEs(createLog(status.getText(),
								getKeyPhrases(status.getText(), comprehendClient), 
								detectSentiments(status.getText(), comprehendClient),
								extractEntities(status.getText(), comprehendClient)));
						
						System.out.println("@"
								+ status.getUser().getScreenName() + " : "
								+ status.getUser().getName() + "--------"
								+ status.getText());
						if (forCount == result.getTweets().size() - 1) {
							maxId = status.getId();
							System.out.println("maxId= " + maxId);
						}
						System.out.println("");
						forCount++;
					}
					numberOfTweets = numberOfTweets + result.getTweets().size();
					query.setMaxId(maxId - 1);
				}
			} catch (TwitterException te) {
				System.out.println("Couldn't connect: " + te);
				System.exit(-1);
			} catch (Exception e) {
				System.out.println("Something went wrong: " + e);
				System.exit(-1);
			}
			whileCount++;
		}
		System.out.println("Total tweets count=======" + numberOfTweets);
	}

	private static AmazonComprehend init() {

		AWSCredentialsProvider awsCreds = DefaultAWSCredentialsProviderChain.getInstance();

		AmazonComprehend comprehendClient = AmazonComprehendClientBuilder
				.standard().withCredentials(awsCreds).withRegion("us-east-1").withClientConfiguration(getConf(5))
				.build();
		return comprehendClient;
	}
	private static ClientConfiguration getConf( int maxRetry ) {
		 ClientConfiguration conf = new ClientConfiguration(); 
		 RetryPolicy.BackoffStrategy strategy = new BackoffStrategy() {
		  public long delayBeforeNextRetry(AmazonWebServiceRequest originalRequest,
						AmazonClientException exception, int retriesAttempted) {
			  System.out.println("Waiting for "+ getWaitTimeExponential(retriesAttempted) + " Milli sec");
					return getWaitTimeExponential(retriesAttempted);
				}
			};
		  
			
		 RetryPolicy.RetryCondition condition = new RetryCondition() {

				public boolean shouldRetry(AmazonWebServiceRequest originalRequest,
						AmazonClientException exception, int retriesAttempted) {		
					System.out.println("Retrying for " + retriesAttempted +1 +"  time(s)");
					return true;
				}
			};

			RetryPolicy retryPolicy = new RetryPolicy(condition, strategy, maxRetry , true);
			conf.setRetryPolicy(retryPolicy);
			return conf;

		}
	
	private static long getWaitTimeExponential(int retryCount) {
		long waitTime = ((long) Math.pow(2, retryCount) * 50L);
		return waitTime;
	}
	private static List<KeyPhrase> getKeyPhrases(String text,
			AmazonComprehend comprehendClient) {

		// Call detectKeyPhrases API
		System.out.println("Calling DetectKeyPhrases");
		DetectKeyPhrasesRequest detectKeyPhrasesRequest = new DetectKeyPhrasesRequest()
				.withText(text).withLanguageCode("en");
		DetectKeyPhrasesResult detectKeyPhrasesResult = comprehendClient.detectKeyPhrases(detectKeyPhrasesRequest);
		return detectKeyPhrasesResult.getKeyPhrases();
		//ingestToEs(detectKeyPhrasesResult,null);
		//System.out.println("End of DetectKeyPhrases\n");
	}

	private static DetectSentimentResult detectSentiments(String text, AmazonComprehend comprehendClient) {
		DetectSentimentRequest detectSentimentRequest = new DetectSentimentRequest().withText(text).withLanguageCode("en");
		DetectSentimentResult detectSentimentResult = comprehendClient.detectSentiment(detectSentimentRequest);
		return detectSentimentResult;
	}
	
	private static List<Entity> extractEntities(String text,AmazonComprehend comprehendClient){
		DetectEntitiesRequest detectEntitiesRequest = new DetectEntitiesRequest().withText(text)
                .withLanguageCode("en");
     DetectEntitiesResult detectEntitiesResult  = comprehendClient.detectEntities(detectEntitiesRequest);
     return detectEntitiesResult.getEntities();
	}
	
	
	
	private static void ingestToEs(String log){
		try {
		ObjectMapper mapper = new ObjectMapper();
		mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
	    RestTemplate restTemplate = new RestTemplate();
		HttpEntity<?> httpEntity = new HttpEntity<String>( log, null);
		ResponseEntity<String> response = restTemplate.exchange("http://"+getEsUrl()+":9200/"+getEsIndex()+"/sentiments", HttpMethod.POST, httpEntity, String.class);
		System.out.println("Response:"+response.getBody());
			
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}

	}
	
	private static String createLog(String text, Object... obj) {
		Log log = new Log();
		String jsonLog = null;
		try {
			ObjectMapper mapper = new ObjectMapper();
			mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
			DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
			sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
			Date date = new Date();
			for (int i=0; i < obj.length; i++){
				if (obj[i].getClass().isArray()){
					System.out.println("Array");
				}
				System.out.println(obj[i].getClass().getSimpleName());
			  log.addValue(obj[i].getClass().getSimpleName(), obj);
			}
			log.setTimeStamp(sdf.format(date));
			log.setHandle(hashTag);
			log.addValue("text", text);
			jsonLog = mapper.writeValueAsString(log);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		return jsonLog;

	}
	private static String getEsUrl(){
		return  System.getenv("ES_URL") != null ? System.getenv("ES_URL"): ES_URL;
		
	}
	private static String getEsIndex(){
		 DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	     sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		 Date date = new Date();
		return "logstash-"+ sdf.format(date);
		
	}
}