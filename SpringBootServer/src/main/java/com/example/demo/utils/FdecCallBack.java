package com.example.demo.utils;

import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.bytedeco.javacv.FrameRecorder.Exception;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.socket.TextMessage;

import com.example.demo.entity.PlayCtrl;

import com.example.demo.entity.PlayCtrl.FRAME_INFO;
import com.example.demo.service.MyHandler;
import com.example.demo.service.RTSPtoRTMPService;
import com.sun.jna.NativeLong;
import com.sun.jna.ptr.ByteByReference;


 
public class FdecCallBack implements PlayCtrl.DecCallBack{

	static int CountDownLatch = 1;
	
	@SuppressWarnings("unused")
	private  String ip;
	
	public  FdecCallBack(String ip) {
		this.ip=ip;
	}
	
	
	public  FdecCallBack() {
		
	}
	@Override
	public void invoke(NativeLong nPort, ByteByReference pBuffer, NativeLong nSize, FRAME_INFO frameInfo,
			NativeLong nReserved1, NativeLong nReserved2) {
		// TODO Auto-generated method stub
		//System.out.println(nReserved1.intValue());System.out.println(nReserved2.intValue());
		/*System.out.println("width:"+frameInfo.nWidth.intValue()+"==height:"+frameInfo.nHeight.intValue());
		System.out.println(nSize.intValue());*/
		byte[] bys = pBuffer.getPointer().getByteArray(0, nSize.intValue());
		InputStream inputStream = new ByteArrayInputStream(bys);
		try {
			RTSPtoRTMPService.SendYUVToRTMP(inputStream, "rtmp://192.168.8.250:1935/hls/index");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (java.lang.Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//byte[] rgb =YV12_To_RGB24(bys,1920,1080);
	
		//createRGBImage(rgb,frameInfo.nWidth.intValue(),1080);
		
		/*try {
		    BufferedImage inputbig = new BufferedImage(1920, 540, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = (Graphics2D) inputbig.getGraphics();
            g.drawImage(bufferedImage, 0, 0,1920,540,null); //画图
            g.dispose();
        
            inputbig.flush();
            File file2 = new File("./src/main/resources/static/video"); //此目录保存缩小后的关键图
            if (file2.exists()) {
                System.out.println("多级目录已经存在不需要创建！！");
            } else {
                //如果要创建的多级目录不存在才需要创建。
                file2.mkdirs();
            }
           Random r =new Random();
            ImageIO.write(inputbig, "jpg", new File("./src/main/resources/static/video/" + r.nextInt(100) + ".jpg")); //将其保存在C:/imageSort/targetPIC/下
        } catch (Exception ex) {
            ex.printStackTrace();
        }*/
	}
	
	 @Async
	 public  BufferedImage createRGBImage(byte[] matrixRGB,int width,int height){
	        // 检测参数合法性
	        if(null==matrixRGB||matrixRGB.length!=width*height*3)
	            throw new IllegalArgumentException("invalid image description");
	        // 将byte[]转为DataBufferByte用于后续创建BufferedImage对象
	        DataBufferByte dataBuffer = new DataBufferByte(matrixRGB, matrixRGB.length);
	        // sRGB色彩空间对象
	        ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_LINEAR_RGB);
	        int[] nBits = {8, 8, 8};
	        int[] bOffs = {0, 1, 2};
	        ComponentColorModel colorModel = new ComponentColorModel(cs, nBits, false, false,
	                                             Transparency.OPAQUE,
	                                             DataBuffer.TYPE_BYTE);        
	        WritableRaster raster = Raster.createInterleavedRaster(dataBuffer, width, height, width*3, 3, bOffs, null);
	        BufferedImage newImg = new BufferedImage(colorModel,raster,false,null);
	         
	       try {
	            //写入文件测试查看结果
	    	    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
	            ImageIO.write(newImg, "jpeg", byteArrayOutputStream);
	           
	            MyHandler.sendMessageToUser(ip, new TextMessage( UtilHelper.byte2Base64StringFun(byteArrayOutputStream.toByteArray()))) ;
	    	   //ImageIO.write(newImg, "jpeg", new File("./src/main/resources/static/picture/"+CountDownLatch+".jpeg")); 
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	        CountDownLatch++;
	        return  newImg;     
	    }
	
	private byte[] YV12_To_RGB24(byte[] yv12, int width, int height) {
        if(yv12 == null) {
            return null;
        }
        
        int nYLen = (int)width * height;
        int halfWidth = width >> 1;
        
        if(nYLen<1 || halfWidth<1) {
            return null;
        }
  
        
        // Convert YV12 to RGB24
        byte[] rgb24 = new byte[width * height * 3];
        int[] rgb = new int[3];
        int i, j, m, n, x, y;
        m = -width;
        n = -halfWidth;
        for(y=0; y<height; y++) {
            m += width;
            if(y%2 != 0) {
                n += halfWidth;
            }
            
            for(x=0; x<width; x++) {
                i = m+x;
                j = n + (x>>1);
                rgb[0] = (int)((int)(yv12[i]&0xFF) + 1.370705 * ((int)(yv12[nYLen+j]&0xFF) - 128)); // r  
                rgb[1] = (int)((int)(yv12[i]&0xFF) - 0.698001 * ((int)(yv12[nYLen+(nYLen>>2)+j]&0xFF) - 128)  - 0.703125 * ((int)(yv12[nYLen+j]&0xFF) - 128));   // g  
                rgb[2] = (int)((int)(yv12[i]&0xFF) + 1.732446 * ((int)(yv12[nYLen+(nYLen>>2)+j]&0xFF) - 128)); // b  
                
                //j = nYLen - iWidth - m + x;  
                //i = (j<<1) + j;    //图像是上下颠倒的  
                
                j = m + x;
                i = (j<<1) + j;
                    
                for(j=0; j<3; j++) {
                    if(rgb[j]>=0 && rgb[j]<=255) {
                        rgb24[i+j] = (byte)rgb[j];
                    } else {
                        rgb24[i+j] = (byte) ((rgb[j] < 0)? 0 : 255);
                    }
                }
            }
        }
        
        return rgb24;
    }
	
	
	public static String bytesToHexString(byte[] src){   
	    StringBuilder stringBuilder = new StringBuilder("");   
	    if (src == null || src.length <= 0) {   
	        return null;   
	    }   
	    for (int i = 0; i < src.length; i++) {   
	        int v = src[i] & 0xFF;   
	        String hv = Integer.toHexString(v);   
	        if (hv.length() < 2) {   
	            stringBuilder.append(0);   
	        }   
	        stringBuilder.append(hv);   
	    }   
	    return stringBuilder.toString();   
	}   

}
