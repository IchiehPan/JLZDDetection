package com.example.demo.service;

import java.io.InputStream;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.avcodec;
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacpp.opencv_objdetect;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.FrameRecorder;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class RTSPtoRTMPService {
	static boolean exit = false;

	@Async
	public void recordPush(String inputFile, String outputFile, int v_rs)
			throws Exception, org.bytedeco.javacv.FrameRecorder.Exception, InterruptedException {

		Loader.load(opencv_objdetect.class);
		// long startTime=0;a
		FrameGrabber grabber = FFmpegFrameGrabber.createDefault(inputFile);

		int width = 704, height = 576;
		grabber.setOption("rtsp_transport", "tcp"); // 使用tcp的方式，不然会丢包很严重
		// 一直报错的原因！！！就是因为是 2560 * 1440的太大了。。
		grabber.setImageWidth(width);
		grabber.setImageHeight(height);
		try {
			grabber.start();
		} catch (Exception e) {

			try {
				grabber.restart();
			} catch (Exception e1) {
				throw e;
			}
		}

		OpenCVFrameConverter.ToIplImage converter = new OpenCVFrameConverter.ToIplImage();
		Frame grabframe = grabber.grab();
		@SuppressWarnings("unused")
		IplImage grabbedImage = null;
		if (grabframe != null) {
			System.out.println("取到第一帧");
			grabbedImage = converter.convert(grabframe);

		} else {
			System.out.println("没有取到第一帧");
		}
		// 如果想要保存图片,可以使用 opencv_imgcodecs.cvSaveImage("hello.jpg", grabbedImage);来保存图片
		FrameRecorder recorder;
		try {
			recorder = FrameRecorder.createDefault(outputFile, width, height);
		} catch (org.bytedeco.javacv.FrameRecorder.Exception e) {
			throw e;
		}
		if (grabbedImage == null) {

			// 最高质量
			recorder.setAudioQuality(0);
			// 音频比特率
			recorder.setAudioBitrate(192000);
			// 音频采样率
			recorder.setSampleRate(44100);
			// 双通道(立体声)
			recorder.setAudioChannels(2);
			// 音频编/解码器
			recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
		}

		recorder.setInterleaved(true);
		recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264); // avcodec.AV_CODEC_ID_H264
		recorder.setFormat("flv");

		recorder.setVideoOption("crf", "28");
		recorder.setFrameRate(v_rs);
		recorder.setGopSize(v_rs);
		recorder.setPixelFormat(0);
		System.out.println("准备开始推流...");

		try {
			recorder.start();
		} catch (org.bytedeco.javacv.FrameRecorder.Exception e) {
			try {
				System.out.println("录制器启动失败，正在重新启动...");
				if (recorder != null) {
					System.out.println("尝试关闭录制器");
					recorder.stop();
					System.out.println("尝试重新开启录制器");
					recorder.start();
				}

			} catch (org.bytedeco.javacv.FrameRecorder.Exception e1) {
				throw e;
			}
		}

		System.out.println("开始推流");
		// CanvasFrame frame = new CanvasFrame("camera", CanvasFrame.getDefaultGamma() /
		// grabber.getGamma());
		// frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		/*
		 * frame.setAlwaysOnTop(true); while (frame.isVisible() &&
		 * (grabframe=grabber.grab()) != null) { System.out.println("推流...");
		 * frame.showImage(grabframe); grabbedImage = converter.convert(grabframe);
		 * Frame rotatedFrame = converter.convert(grabbedImage);
		 * 
		 * if (startTime == 0) { startTime = System.currentTimeMillis(); }
		 * recorder.setTimestamp(1000 * (System.currentTimeMillis() - startTime));//时间戳
		 * if(rotatedFrame!=null){ recorder.record(rotatedFrame); }
		 * 
		 * Thread.sleep(40); }
		 */
		// Frame rotatedFrame = null;
		while (grabframe != null) {

			grabframe = grabber.grab();
			/*
			 * grabbedImage = converter.convert(grabframe); rotatedFrame =
			 * converter.convert(grabbedImage);
			 */

			recorder.record(grabframe);

		}
		Thread.sleep(40);

		recorder.stop();
		recorder.release();
		grabber.stop();
		System.exit(2);
	}

	public static void SendYUVToRTMP(InputStream inputStream, String OutputStream)
			throws org.bytedeco.javacv.FrameRecorder.Exception, Exception {
		Loader.load(opencv_objdetect.class); // long startTime=0;a
		FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(inputStream);
		grabber.setOption("rtsp_transport", "tcp");

		// 流媒体输出地址，分辨率（长，高），是否录制音频（0:不录制/1:录制）
		FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(OutputStream, 1280, 720, 0);
		recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
		recorder.setFormat("flv");
		recordByFrame(grabber, recorder, true);

	}

	private static void recordByFrame(FFmpegFrameGrabber grabber, FFmpegFrameRecorder recorder, Boolean status)
			throws Exception, org.bytedeco.javacv.FrameRecorder.Exception {
		try {// 建议在线程中使用该方法
			grabber.start();
			recorder.start();
			Frame frame = null;
			while (status && (frame = grabber.grabFrame()) != null) {
				recorder.record(frame);
			}
			recorder.stop();
			grabber.stop();
		} finally {
			if (grabber != null) {
				grabber.stop();
			}
		}
	}

}
