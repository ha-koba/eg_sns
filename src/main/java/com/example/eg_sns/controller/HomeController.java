package com.example.eg_sns.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * ※TODO 適宜実装を入れてください。
 */
@Controller
@RequestMapping("/home")
public class HomeController {

	@GetMapping(path = { "", "/" })
	public String index() {
		return "home/index";
	}
}
