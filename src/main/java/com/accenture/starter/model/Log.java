/**
 * 
 */
package com.accenture.starter.model;

import java.io.Serializable;
import java.util.HashMap;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author saswat.kumar.sahoo
 *
 */
public class Log implements Serializable {

	private static final long serialVersionUID = 1L;
	@JsonProperty("data")
	private HashMap<String, Object> values;
	@JsonProperty("@timestamp")
	private String timeStamp;

	@JsonProperty("hashTag")
	private String handle;

	public String getHandle() {
		return handle;
	}

	public void setHandle(String handle) {
		this.handle = handle;
	}

	public String getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(String timeStamp) {
		this.timeStamp = timeStamp;
	}

	public Log() {
		this.values = new HashMap<String, Object>();
	}

	public void addValue(String name, Object value) {
		values.put(name, value);
	}

	public Object getValue(String name) {
		return values.get(name);
	}
	
	public static void main(String[] args) {
		
	}
}
