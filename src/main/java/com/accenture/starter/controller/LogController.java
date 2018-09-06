/**
 * 
 */
package com.accenture.starter.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.accenture.starter.service.LogService;

/**
 * @author saswat.kumar.sahoo
 *
 */
@Controller("logController")
public class LogController {
	@Autowired
	private LogService esLogService;
	
	@PostMapping("/logs/{type}")
	@ResponseBody
	public Object ingestLogs(@RequestBody String log, @PathVariable String type) {
		try {
		   return esLogService.ingestToEs(log,type);
		} catch (Exception e) {
		   return e;
		}
		}
}
