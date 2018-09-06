/**
 * 
 */
package com.accenture.starter.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.accenture.starter.model.Input;
import com.accenture.starter.service.ComprehandService;

/**
 * @author saswat.kumar.sahoo
 *
 */
@Controller
public class ComprehandController {

	@Autowired
	private ComprehandService comprehandService;

	@PostMapping(value = "/comprehand")
	@ResponseBody
	public Object query(@RequestBody Input input) {
		System.out.println(input.getText());
		return  comprehandService.analyzeText(input.getText());
	}

}
