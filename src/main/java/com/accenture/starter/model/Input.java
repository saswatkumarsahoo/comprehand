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

	@Override
	public String toString() {
		return "Input [bucketName=" + bucketName + ", keyName=" + keyName + "]";
	}

}
