package org.tts.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class DocumentationController {

	@GetMapping("/help")
	public String getBaseDocumentation() {
		return "index";
	}
}
