package com.pspt.cardetection;

import com.pspt.detection.Constants;
import com.pspt.detection.HikBean;
import com.pspt.detection.HikCache;

public class CarDetectionThread extends Thread {
    private String ip;
    private HikBean mHikBean;
    private int actionType;
    private CarAlarmBusiness carAlarmBusiness = new CarAlarmBusiness();

    public CarDetectionThread(String ip, HikBean hikBean, int actionType) {
        this.ip = ip;
        this.mHikBean = hikBean;
        this.actionType = actionType;
    }

    public void run() {
        switch (actionType) {
            case Constants.ACTION_TYPE_OPEN:
                while (!HikCache.hikBusinessMap.containsKey(ip)) {
                    if (carAlarmBusiness.open(ip, mHikBean)) {
                        HikCache.hikBusinessMap.put(ip, carAlarmBusiness.hikBusiness);
                    }
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case Constants.ACTION_TYPE_CLOSE:
                carAlarmBusiness.close(ip);
                break;
            default:
                break;
        }

    }

}