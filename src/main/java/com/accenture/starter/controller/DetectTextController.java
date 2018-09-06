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
import com.accenture.starter.service.DetectTextService;

/**
 * @author saswat.kumar.sahoo
 *
 */
@Controller
public class DetectTextController {
	@Autowired
	private DetectTextService detectTextService;

	@PostMapping("/texts")
	@ResponseBody
	public Object detectTexts(@RequestBody Input input) {
		try {
			Object result = detectTextService.detectText(input);
			if (result != null) {
				return result;
			} else {
				return "Error";
			}
		} catch (Exception e) {
			e.printStackTrace();
			return e.getMessage();
		}

	}
}
