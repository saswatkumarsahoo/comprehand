/**
 * 
 */
package com.accenture.starter.service;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.accenture.starter.model.Input;
import com.accenture.starter.model.Log;
import com.accenture.starter.util.AwsClientBuilder;
import com.accenture.starter.util.PropertyfileReader;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import twitter4j.Query;
import twitter4j.Query.ResultType;
import twitter4j.QueryResult;
import twitter4j.RateLimitStatus;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.auth.OAuth2Token;
import twitter4j.conf.ConfigurationBuilder;

/**
 * @author saswat.kumar.sahoo
 *
 */
@Service("twitterServiceImpl")
public class TwitterService {

	@Autowired
	private static PropertyfileReader propertyfileReader;
	@Autowired
	private LogService esLogService;
	@Autowired
	private AwsClientBuilder awsClientBuilder;
	/*private static final String CONSUMER_KEY = System
			.getenv("TWITTER_CONSUMER_KEY") != null ? System
			.getenv("TWITTER_CONSUMER_KEY") : propertyfileReader
			.getProp("TWITTER_CONSUMER_KEY");
	private static final String CONSUMER_SECRET = System
			.getenv("TWITTER_CONSUMER_SECRET") != null ? System
			.getenv("TWITTER_CONSUMER_SECRET") : propertyfileReader
			.getProp("TWITTER_CONSUMER_SECRET");*/
	private static final int TWEETS_PER_QUERY = 100;
	private static final int MAX_QUERIES = 10;
//	/private static final String SEARCH_TERM = "#a";
	static final String LOG_SERVICE_URL = "log";
	static final String COMPREHAND_SERVICE_URL = "localhost";

	public static String cleanText(String text) {
		text = text.replace("\n", "\\n");
		text = text.replace("\t", "\\t");

		return text;
	}

	public static OAuth2Token getOAuth2Token() {
		OAuth2Token token = null;
		ConfigurationBuilder cb;

		cb = new ConfigurationBuilder();
		cb.setApplicationOnlyAuthEnabled(true);

		cb.setOAuthConsumerKey(System
				.getenv("TWITTER_CONSUMER_KEY") != null ? System
						.getenv("TWITTER_CONSUMER_KEY") : propertyfileReader
						.getProp("TWITTER_CONSUMER_KEY")).setOAuthConsumerSecret(
								System
								.getenv("TWITTER_CONSUMER_SECRET") != null ? System
								.getenv("TWITTER_CONSUMER_SECRET") : propertyfileReader
								.getProp("TWITTER_CONSUMER_SECRET"));

		try {
			token = new TwitterFactory(cb.build()).getInstance()
					.getOAuth2Token();
		} catch (Exception e) {
			System.out.println("Could not get OAuth2 token");
			e.printStackTrace();
			System.exit(0);
		}

		return token;
	}

	/**
	 * Get a fully application-authenticated Twitter object useful for making
	 * subsequent calls.
	 *
	 * @return Twitter4J Twitter object that's ready for API calls
	 */
	public static Twitter getTwitter() {
		OAuth2Token token;
		token = getOAuth2Token();
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setApplicationOnlyAuthEnabled(true);
		cb.setOAuthConsumerKey(System
				.getenv("TWITTER_CONSUMER_KEY") != null ? System
						.getenv("TWITTER_CONSUMER_KEY") : propertyfileReader
						.getProp("TWITTER_CONSUMER_KEY"));
		cb.setOAuthConsumerSecret(System
				.getenv("TWITTER_CONSUMER_SECRET") != null ? System
						.getenv("TWITTER_CONSUMER_SECRET") : propertyfileReader
						.getProp("TWITTER_CONSUMER_SECRET"));
		cb.setOAuth2TokenType(token.getTokenType());
		cb.setOAuth2AccessToken(token.getAccessToken());
		return new TwitterFactory(cb.build()).getInstance();

	}

	@Async
	public CompletableFuture<?> findTweets(String query)
			throws InterruptedException  {
		int totalTweets = 0;
		long maxID = -1;
		RestTemplate restTemplate = new RestTemplate();
		Twitter twitter = getTwitter();
		Input input = new Input();
		try {
			Map<String, RateLimitStatus> rateLimitStatus = twitter
					.getRateLimitStatus("search");
			RateLimitStatus searchTweetsRateLimit = rateLimitStatus.get("/search/tweets");
			System.out.printf("You have %d calls remaining out of %d, Limit resets in %d seconds\n",
							searchTweetsRateLimit.getRemaining(),
							searchTweetsRateLimit.getLimit(),
							searchTweetsRateLimit.getSecondsUntilReset());

			for (int queryNumber = 0; queryNumber < MAX_QUERIES; queryNumber++) {
				System.out.printf("\n\n!!! Starting loop %d\n\n", queryNumber);

				if (searchTweetsRateLimit.getRemaining() == 0) {
					// Yes we do, unfortunately ...
					System.out.printf(
							"!!! Sleeping for %d seconds due to rate limits\n",
							searchTweetsRateLimit.getSecondsUntilReset());
					Thread.sleep((searchTweetsRateLimit.getSecondsUntilReset() + 2) * 1000l);
				}

				Query q = new Query(query); 
				q.setCount(TWEETS_PER_QUERY); 
				q.setResultType(ResultType.recent); 
				q.setLang("en"); 
				if (maxID != -1) {
					q.setMaxId(maxID - 1);
				}

				QueryResult r = twitter.search(q);
				if (r.getTweets().size() == 0) {
					break; // Nothing? We must be done
				}
				for (Status s : r.getTweets()) {
					totalTweets++;
					if (maxID == -1 || s.getId() < maxID) {
						maxID = s.getId();
					}
					input.setText(s.getText());
					Object data = postText(restTemplate, input);
					System.out.println("data->"+data);
					String log = createLog(s.getText(),query, data);
					System.out.println(postLog(restTemplate, log));
					System.out.printf("At %s, @%-20s said:  %s\n", 
							s.getCreatedAt().toString(), 
							s.getUser().getScreenName(), 
							cleanText(s.getText()));

				}
				searchTweetsRateLimit = r.getRateLimitStatus();
			}

		} catch (Exception e) {
			System.out.println("Unknown Exception");
			e.printStackTrace();

		}
		System.out.printf("\n\nA total of %d tweets retrieved\n", totalTweets);
		return CompletableFuture.completedFuture(null);

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
		ResponseEntity<String> response = null;

		try {
			HttpEntity<String> httpEntity = new HttpEntity<String>(log, null);
			response = restTemplate.exchange("http://" + getLogServiceUrl()+ ":8080/logs/sentiments", HttpMethod.POST, httpEntity,
					String.class);
			System.out.println(response);
		} catch (Exception ex) {
			System.out.println("Error while posting log"+ex);
		}
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
			log.setData(mapper.readValue(obj.toString(), Object.class));
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