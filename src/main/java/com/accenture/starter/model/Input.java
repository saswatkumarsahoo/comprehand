/**
 * 
 */
package com.accenture.starter.model;

/**
 * @author saswat.kumar.sahoo
 *
 */
public class Input {
	private String bucketName;
	

	private String keyName;
	private String text;
	private boolean logPush;

	public String getBucketName() {
		return bucketName;
	}

	public void setBucketName(String bucketName) {
		this.bucketName = bucketName;
	}

	public String getKeyName() {
		return keyName;
	}

	public void setKeyName(String keyName) {
		this.keyName = keyName;
	}

	

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public boolean isLogPush() {
		return logPush;
	}

	public void setLogPush(boolean logPush) {
		this.logPush = logPush;
	}
	
	@Override
	public String toString() {
		return "Input [bucketName=" + bucketName + ", keyName=" + keyName
				+ ", text=" + text + ", logPush=" + logPush + "]";
	}

}
