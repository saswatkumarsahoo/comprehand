/**
 * 
 */
package com.accenture.starter.controller;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.accenture.starter.model.Outcome;
import com.accenture.starter.service.TwitterService;

import org.apache.commons.io.IOUtils;

/**
 * @author saswat.kumar.sahoo
 *
 */
@RestController
public class QueryController {

	/*@Autowired
	private QueryService queryService;*/
	@Autowired
	private TwitterService TwitterService;


	private final AtomicLong counter = new AtomicLong();

	@RequestMapping(value = "/tweets")
	@ResponseBody
	public Outcome query(@RequestParam("query") String query) {
		try {
			TwitterService.findTweets(query);
		} catch (Exception e) {

			e.printStackTrace();
		}
		return new Outcome(counter.incrementAndGet(), query, null);
	}
	
	@GetMapping( value = "/",produces = MediaType.IMAGE_PNG_VALUE)
			public @ResponseBody byte[] getFile() throws IOException {
			    InputStream in =  new FileInputStream("image.png");;
			    return IOUtils.toByteArray(in);
			}

}
