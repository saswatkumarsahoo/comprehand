/**
 * 
 */
package com.accenture.starter.model;

/**
 * @author saswat.kumar.sahoo
 *
 */
public class Outcome {

	private final long id;
	private String query;
	private String message;

	public Outcome(long id, String query, String message) {
		this.id = id;
		this.setQuery(query);
		this.setMessage("query-received");
	}

	public long getId() {
		return id;
	}

	public String getMessage() {
		return message;
	}


	public void setMessage(String message) {
		this.message = message;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}


}