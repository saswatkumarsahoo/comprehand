/**
 * 
 */
package com.accenture.starter.controller;

import java.util.concurrent.atomic.AtomicLong;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.accenture.starter.model.Outcome;
import com.accenture.starter.service.QueryService;
import com.accenture.starter.service.TwitterService;

/**
 * @author saswat.kumar.sahoo
 *
 */
@RestController
public class QueryController {

	@Autowired
	private QueryService queryService;
	@Autowired
	private TwitterService twitterServiceImpl;


	private final AtomicLong counter = new AtomicLong();

	@RequestMapping(value = "/tweets")
	@ResponseBody
	public Outcome query(@RequestParam("query") String query) {
		try {
			twitterService.getTweets(query);
		} catch (Exception e) {

			e.printStackTrace();
		}
		return new Outcome(counter.incrementAndGet(), query, null);
	}

}
