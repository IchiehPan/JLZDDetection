package com.pspt.facedetection;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.*;

public class MainProgress extends JFrame {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    public static MainProgress frame = new MainProgress();
    JTextArea textArea = new JTextArea("正在连接中");

    public static void main(String[] args) {
        FaceAlarmBusiness.load();
        FaceAlarmBusiness.startProgress();
        frame.init();
    }

    public void init() {
        // 创建一个面板
        JPanel paContent = new JPanel(new FlowLayout(FlowLayout.CENTER));

        paContent.setPreferredSize(new Dimension(600, 300));
        // 在面板中创建一个按钮用于关闭窗体
        JButton btn1 = new JButton("关闭");
        btn1.setPreferredSize(new Dimension(100, 25));
        btn1.addActionListener(e -> closeFrame());
        // 向面板中加载按钮
        paContent.add(btn1);

//        // 在面板中创建一个按钮用于触发拍照
//        JButton btn2 = new JButton("触发");
//        btn2.setPreferredSize(new Dimension(100, 25));
//        btn2.addActionListener(e -> {
//            for (HikBusiness hikBusiness : HikCache.hikBusinessMap.values()) {
//                hikBusiness.continuesShoot();
//            }
//        });
//        // 向面板中加载按钮
//        paContent.add(btn2);
        textArea.setPreferredSize(new Dimension(500, 200));
        paContent.add(textArea);

        // 设置面板
        this.setTitle("人脸识别");
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.setContentPane(paContent);
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                closeFrame();
            }

            public void windowClosed(WindowEvent e) {
                FaceAlarmBusiness.endProgress();
            }
        });
    }

    // 关闭窗体
    public void closeFrame() {
        int result = JOptionPane.showConfirmDialog(null, "是否要退出？", "退出确认", JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        if (result == JOptionPane.YES_OPTION) {
            this.dispose();
            System.exit(0);
        }
    }

    public synchronized void insertText(String message) {
        textArea.insert("\r\n", 0);
        textArea.insert(message, 0);
    }
}
