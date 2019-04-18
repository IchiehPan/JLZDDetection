package com.example.demo.conreoller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

//web页面
@Controller
@RequestMapping("/hk")
public class IndexController {

	@RequestMapping("/demo")
	public String helloHtml(ModelMap map) {

		return "/demo";
	}

	@RequestMapping("/iframe")
	public String iFrame(ModelMap map, String username, String password, String deviceip,
			@RequestParam(defaultValue = "600") String width, @RequestParam(defaultValue = "400") String height) {
		map.addAttribute("username", username);
		map.addAttribute("password", password);
		map.addAttribute("deviceIp", deviceip);
		map.addAttribute("width", width);
		map.addAttribute("height", height);
		return "demo-easy";

	}

	@RequestMapping("/test")
	public String test(ModelMap map) {

		return "/test";
	}

	@RequestMapping("/test-iframe")
	public String testIframe(ModelMap map) {

		return "/test-iframe";
	}

	@RequestMapping("/demo-iframe")
	public String demoIframe(ModelMap map) {

		return "/demo-iframe";
	}

	@RequestMapping("/transparency")
	public String transparency(ModelMap map) {

		return "/transparency";
	}

	@RequestMapping("/h264")
	public String h264(ModelMap map) {

		return "/h264";
	}
	
	@RequestMapping("/small-iframe")
	public String smalliframe(ModelMap map) {

		return "/iframe";
	}
	
	@RequestMapping("/demo-all")
	public String demoAlliframe(ModelMap map,@RequestParam(defaultValue = "800")  String width,  @RequestParam(defaultValue = "600")  String height) {
		map.addAttribute("width", width);
		map.addAttribute("height", height);
		return "/demo-all-windows";
	}

}
