package com.example.demo.conreoller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.service.PTZControlService;



@RestController
public class PTZController {

	@Autowired
	PTZControlService ptzControlService;
	
	
	@RequestMapping(value="/starPTZ")
	//Speed [1,7]  iPTZCommand TILT_UP:21,TILT_DOWN:22,PAN_LEFT:23,PAN_RIGHT:24
	public boolean StarPTZ(String iPTZCommand,String ip, String speed) {
		
		if(ptzControlService.StarPTZ(ip, speed, iPTZCommand)) {
			
			return true;
		}
		
		return false;
		
	}
	
	    @RequestMapping(value="/stopPTZ")
	    //Speed [1,7]  iPTZCommand TILT_UP:21,TILT_DOWN:22,PAN_LEFT:23,PAN_RIGHT:24
		public boolean StopPTZ(String iPTZCommand,String ip, String speed) {
			
			if(ptzControlService.StopPTZ(ip, speed, iPTZCommand)) {
				
				return true;
			}
			
			return false;
			
		}
	
}
