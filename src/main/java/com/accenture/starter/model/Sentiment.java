/**
 * 
 */
package com.accenture.starter.model;

import com.amazonaws.services.comprehend.model.SentimentScore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author saswat.kumar.sahoo
 *
 */
public class Sentiment {
	private String sentiment;

	@JsonProperty("@timestamp")
	private String timeStamp;
	
	private SentimentScore sentimentScore;

	public String getSentiment() {
		return sentiment;
	}

	public void setSentiment(String sentiment) {
		this.sentiment = sentiment;
	}

	public SentimentScore getSentimentScore() {
		return sentimentScore;
	}

	public void setSentimentScore(SentimentScore sentimentScore) {
		this.sentimentScore = sentimentScore;
	}

	@Override
	public String toString() {
		return "Sentiment [sentiment=" + sentiment + ", timeStamp=" + timeStamp
				+ ", sentimentScore=" + sentimentScore + "]";
	}

	public String getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(String timeStamp) {
		this.timeStamp = timeStamp;
	}
}