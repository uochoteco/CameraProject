package com.uochoteco;

import nu.pattern.OpenCV;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import org.opencv.imgproc.Imgproc;
import java.util.ArrayList;
import java.util.List;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import org.opencv.videoio.VideoWriter;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;


public class Main extends JPanel {
    private static int vNum = 0;
    private static int pNum = 0;
    private BufferedImage image;
    public static void main(String[] args) 
    {
        OpenCV.loadShared();
        System.out.println("Hello world!");
        System.out.println("Version: " + Core.VERSION);
        JFrame frame = new JFrame("Camera");
        Main panel = new Main();
        VideoCapture camera = new VideoCapture(0);
        Mat frameMatrix = new Mat();
        frame.add(panel);
        frame.setSize(640, 360);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        frame.addKeyListener(new KeyAdapter()
        
        { public void keyPressed(KeyEvent e)
            { 
                if(e.getKeyCode() == KeyEvent.VK_SPACE)
                { 
                    System.out.println("Space bar pressed");
                    getPic(panel.image, pNum);
                    pNum++;
                }
            }
        });
        frame.addKeyListener(new KeyAdapter()
        
        { public void keyPressed(KeyEvent e)
            { 
                if(e.getKeyCode() == KeyEvent.VK_V)
                { 
                    System.out.println("V pressed");
                    getVid(vNum, camera);
                    vNum++;
                }
            }
        });
            
        
        
        if(camera.isOpened() == false)
            {
                System.out.println("Camera Fail :(");
                return;
            }
        while(true)
            {
                if(camera.read(frameMatrix))
                    {
                        Core.flip(frameMatrix, frameMatrix, 1);
                        panel.image = matrixToBufferedImage(frameMatrix);
                        panel.repaint();
                    }
            }
    }

    public static BufferedImage matrixToBufferedImage(Mat matrix)
    {
        int cols = matrix.cols();
        int rows = matrix.rows();
        int elementSize = (int)matrix.elemSize();
        byte[] data = new byte[cols * rows * elementSize];
        matrix.get(0, 0, data);
        int type = matrix.channels() == 1 ? BufferedImage.TYPE_BYTE_GRAY: BufferedImage.TYPE_3BYTE_BGR;
        BufferedImage image = new BufferedImage(cols, rows, type);
        System.arraycopy(data, 0, ((DataBufferByte)image.getRaster().getDataBuffer()).getData(), 0, data.length);
        return image;
    }

    public static void getPic(BufferedImage cFrame, int count)
    {
        JFrame pFrame = new JFrame("Picture " + count);
        Main picPanel = new Main();
        pFrame.add(picPanel);
        picPanel.image = cFrame;
        pFrame.setSize(640, 360);
        pFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        pFrame.setVisible(true);
        pFrame.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent i) {
                if (i.getKeyCode() == KeyEvent.VK_SPACE) {
                    new Thread(() -> {
                        try {
                            File folder = new File("picFolder");
                            if (!folder.exists()) {
                                folder.mkdirs();
                            }
                            int num = 0;
                            File temp;
                            do {
                                temp = new File(folder, "picture_" + count + "_" + num + ".png");
                                num++;
                            } while (temp.exists());
                            ImageIO.write(picPanel.image, "png", temp);
                            System.out.println("Saved");
                        } catch (IOException e) {
                            System.out.println("Save failed");
                            e.printStackTrace();
                        }
                    }).start();
                }
            }
        });
    }

    public static void getVid(int count, VideoCapture camera) {
    final boolean[] isRunning = {true};
    final boolean[] isSaving = {false};
    
    JFrame vFrame = new JFrame("Video Recorder " + count);
    Main vidPanel = new Main();
    vFrame.add(vidPanel);
    vFrame.setSize(640, 360);
    vFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    vFrame.setVisible(true);

    vFrame.addKeyListener(new KeyAdapter() {
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                isSaving[0] = !isSaving[0];
            }
            if (e.getKeyCode() == KeyEvent.VK_V) {
                isRunning[0] = false;
            }
        }
    });

    new Thread(() -> {
        Mat frameMatrix = new Mat();
        VideoWriter writer = new VideoWriter();
        while (isRunning[0]) {
            if (camera.read(frameMatrix)) {
                Core.flip(frameMatrix, frameMatrix, 1);
                vidPanel.image = matrixToBufferedImage(frameMatrix);
                vidPanel.repaint();

                if (isSaving[0]) {
                    if (!writer.isOpened()) {
                        File folder = new File("vidFolder");
                        if (!folder.exists()) folder.mkdirs();
                        String path = new File(folder, "video_" + count + ".mov").getAbsolutePath();
                        writer.open(path, VideoWriter.fourcc('M','J','P','G'), 20.0, frameMatrix.size());
                    }
                    writer.write(frameMatrix);
                }
            }
        }
        if (writer.isOpened()) {
            writer.release();
            System.out.println("FINISHED: saved.");
        }
    }).start();
}



    @Override
    protected void paintComponent(Graphics x)
    {
        super.paintComponent(x);
        if(image != null)
            {
                x.drawImage(image, 0, 0, 640, 360, this);
            }
    }
}