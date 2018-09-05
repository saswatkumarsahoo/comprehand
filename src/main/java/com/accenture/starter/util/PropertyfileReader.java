/**
 * 
 */
package com.accenture.starter.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.springframework.stereotype.Component;

/**
 * @author saswat.kumar.sahoo
 *
 */

@Component("propertyfileReader")
public class PropertyfileReader {

	public String getProp(String key) {

		Properties prop = new Properties();
		InputStream input = null;
		String value = null;
		try {
			input = new FileInputStream("twitter4j.properties");
			prop.load(input);
			value = prop.getProperty(key);

		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return value;

	}
}
