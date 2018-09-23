/**
 * 
 */
package com.accenture.starter.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author saswat.kumar.sahoo
 *
 */
public class Topic {

	@JsonProperty("weight")
	private double weight;

	@JsonProperty("@timestamp")
	private String timeStamp;

	@JsonProperty("@topic")
	private String topic;

	@JsonProperty("term")
	private String term;
	
	public double getWeight() {
		return weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}

	public String getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(String timeStamp) {
		this.timeStamp = timeStamp;
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public String getTerm() {
		return term;
	}

	public void setTerm(String term) {
		this.term = term;
	}


}
