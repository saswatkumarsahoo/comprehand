/**
 * 
 */
package com.accenture.starter.util;

import org.springframework.stereotype.Component;

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
import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;

/**
 * @author saswat.kumar.sahoo
 *
 */
@Component ("awsClientBuilder")
public class AwsClientBuilder {
	
	private static final int MAX_RETRY_RECOGNITION_CLIENT = 1;
	private static final String REGION = "us-east-1";
	
	public AmazonRekognition  getRekognitionClient(){
		AWSCredentialsProvider awsCreds = DefaultAWSCredentialsProviderChain.getInstance();
		AmazonRekognition rekognitionClient = AmazonRekognitionClientBuilder
				.standard().withCredentials(awsCreds).withRegion(REGION).withClientConfiguration(getConf(MAX_RETRY_RECOGNITION_CLIENT))
				.build();
		return rekognitionClient;
	} 
	
	public AmazonComprehend getComprehendClient() {

		AWSCredentialsProvider awsCreds = DefaultAWSCredentialsProviderChain.getInstance();

		AmazonComprehend comprehendClient = AmazonComprehendClientBuilder
				.standard().withCredentials(awsCreds).withRegion(REGION).withClientConfiguration(getConf(5))
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

}
