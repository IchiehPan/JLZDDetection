package com.example.demo.conreoller;

import org.bytedeco.javacv.FrameRecorder.Exception;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.DeviceInfo;
import com.example.demo.service.PreviewService;
import com.example.demo.service.RTSPtoRTMPService;

@RestController
public class PreviewController {

	@Autowired
	PreviewService previewService;
	
	@Autowired
	RTSPtoRTMPService tRtmpService;

	@RequestMapping(value = "/preview")
	public void testPreview(String ip) {

		//previewService.startPlay(DeviceInfo.getuserMap().get(ip));
		
		// MyHandler.sendMessageToUser(ip, new TextMessage(payload));
		// String inputFile = "rtsp://admin:jhd123456@192.168.8.178/h264/ch1/sub/av_stream";
		String inputFile = "rtsp://admin:jhd123456@192.168.8.65:554/Streaming/Channels/102"; 
		String outputFile="rtmp://192.168.8.250:1935/hls/index";
		
		
		
		try {
			tRtmpService.recordPush(inputFile, outputFile, 25);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (java.lang.Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
	}

}
