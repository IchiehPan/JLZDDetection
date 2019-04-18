package com.example.demo.entity;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;

import com.sun.jna.Structure;
import com.sun.jna.examples.win32.W32API.HWND;
import com.sun.jna.ptr.ByteByReference;

import com.sun.jna.ptr.NativeLongByReference;
import com.sun.jna.win32.StdCallLibrary.StdCallCallback;

public interface PlayCtrl extends Library {

	PlayCtrl INSTANCE = (PlayCtrl) Native.loadLibrary("PlayCtrl", PlayCtrl.class);

	public static final int STREAME_REALTIME = 0;
	public static final int STREAME_FILE = 1;

	public static final int T_AUDIO16 = 101;
	public static final int T_AUDIO8 = 100;
	public static final int T_UYVY = 1;
	public static final int T_YV12 = 3;
	public static final int T_RGB32 = 7;

	boolean PlayM4_GetPort(NativeLongByReference nPort);

	boolean PlayM4_OpenStream(NativeLong nPort, ByteByReference pFileHeadBuf, int nSize, int nBufPoolSize);

	boolean PlayM4_InputData(NativeLong nPort, ByteByReference pBuf, int nSize);

	boolean PlayM4_CloseStream(NativeLong nPort);

	boolean PlayM4_SetStreamOpenMode(NativeLong nPort, int nMode);

	boolean PlayM4_Play(NativeLong nPort, HWND hWnd);

	boolean PlayM4_Stop(NativeLong nPort);

	boolean PlayM4_SetSecretKey(NativeLong nPort, NativeLong lKeyType, String pSecretKey, NativeLong lKeyLen);

	boolean PlayM4_SetDecCBStream(NativeLong nPort, int nSream);

	boolean PlayM4_SetDecCallBack(NativeLong nPort, DecCallBack decCallBack);

	boolean PlayM4_SetDecCallBackEx(NativeLong nPort, DecCallBack decCallBack, String pDest, NativeLong nDestSize);

	boolean PlayM4_ConvertToJpegFile(String pBuf, NativeLong nSize, NativeLong nWidth, NativeLong nHeight,
			NativeLong nType, String sFileName);

	boolean PlayM4_SetDisPlayType(NativeLong nPort, NativeLong nType);

	/*
	 * boolean PlayM4_GetJPEG(NativeLong nPort,ByteByReference pJpeg,int
	 * nBufSize,IntByReference intByReference );
	 */public static interface DecCallBack extends StdCallCallback {
		public void invoke(NativeLong nPort, ByteByReference pBuffer, NativeLong nSize, FRAME_INFO frameInfo,
				NativeLong nReserved1, NativeLong nReserved2);
	}

	public static class FRAME_INFO extends Structure {
		public NativeLong nWidth; /* 画面宽，单位像素。如果是音频数据，则为音频声道数 */
		public NativeLong nHeight; /* 画面高，单位像素。如果是音频数据，则为样位率 */
		public NativeLong nStamp; /* 时标信息，单位毫秒 */
		public NativeLong nType; /* 数据类型，T_AUDIO16, T_RGB32, T_YV12 */
		public NativeLong nFrameRate; /* 编码时产生的图像帧率，如果是音频数据则为采样率 */
		public int dwFrameNum; /* 帧号 */
	}

	boolean PlayM4_PlaySound(NativeLong nPort);

}
