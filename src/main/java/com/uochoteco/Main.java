package com.uochoteco;
//imports: some of these were things I imported for troiuble shooting but didnt end up using
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
import io.javalin.Javalin;


public class Main extends JPanel {
    private static int vNum = 0;
    private static int pNum = 0;
    private BufferedImage image;
    public static void main(String[] args) 
    {
        //this sets up and connects it to the website
        OpenCV.loadShared();
        Javalin app = Javalin.create(config -> {config.staticFiles.add("/", io.javalin.http.staticfiles.Location.CLASSPATH);}).start(9090);
        app.get("/start-camera", ctx -> {new Thread(() -> {startCamera();}).start(); ctx.result("works");});
    }

    public static void startCamera(){
        System.out.println("Hello world!");
        System.out.println("Version: " + Core.VERSION);
        //this line insantiates the window for the main camera
        JFrame frame = new JFrame("Camera");
        Main panel = new Main();
        //begins video recording by setting up a video capture object
        VideoCapture camera = new VideoCapture(0);
        //create matrix to store the pixels for the window to show
        Mat frameMatrix = new Mat();
        frame.add(panel);
        frame.setSize(640, 360);
        //makes the program close when the main window closes
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        //adds a key listener object this allows me to detect if certain keys are pressed
        frame.addKeyListener(new KeyAdapter()
        // code for space bar press
        { public void keyPressed(KeyEvent e)
            { 
                if(e.getKeyCode() == KeyEvent.VK_SPACE)
                { 
                    //calls the picture taking function and updates the amount of pictures taken
                    System.out.println("Space bar pressed");
                    getPic(panel.image, pNum);
                    pNum++;
                }
            }
        });
        frame.addKeyListener(new KeyAdapter()
        //code for v pressed
        { public void keyPressed(KeyEvent e)
            { 
                if(e.getKeyCode() == KeyEvent.VK_V)
                { 
                    //calls video taking function and updates number of pictures taken
                    System.out.println("V pressed");
                    getVid(vNum, camera);
                    vNum++;
                }
            }
        });
            
        
        //checks if camera set up was succsessful to avoid an exception
        if(camera.isOpened() == false)
            {
                System.out.println("Camera Fail :(");
                return;
            }
        //if camera is working then it keeps on updating the image in the window to actually show the camera feed
        while(true)
            {
                if(camera.read(frameMatrix))
                    {
                        //the flip is because the way it was before was mirrored
                        Core.flip(frameMatrix, frameMatrix, 1);
                        //these methods are what actually like updates the image
                        panel.image = matrixToBufferedImage(frameMatrix);
                        panel.repaint();
                    }
            }
    }

    public static BufferedImage matrixToBufferedImage(Mat matrix)
    {
        //these variables make it so I dont need to retype all of that constantly
        int cols = matrix.cols();
        int rows = matrix.rows();
        int elementSize = (int)matrix.elemSize();
        //createws a byte array that is the right size to hold all the array data
        byte[] data = new byte[cols * rows * elementSize];
        matrix.get(0, 0, data);
        //this gets what color type the buffered image will be in
        int type = matrix.channels() == 1 ? BufferedImage.TYPE_BYTE_GRAY: BufferedImage.TYPE_3BYTE_BGR;
        //this makes the buffered image from what we have from the matrix
        BufferedImage image = new BufferedImage(cols, rows, type);
        System.arraycopy(data, 0, ((DataBufferByte)image.getRaster().getDataBuffer()).getData(), 0, data.length);
        return image;
    }

    //this is the picture taking method
    public static void getPic(BufferedImage cFrame, int count)
    {
        //this sets up a new window where it keeps the frame it captured so you can see it before you save it to your device
        JFrame pFrame = new JFrame("Picture " + count);
        Main picPanel = new Main();
        pFrame.add(picPanel);
        picPanel.image = cFrame;
        pFrame.setSize(640, 360);
        pFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        pFrame.setVisible(true);
        // this makes it so if you press space on this window it saves
        pFrame.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent i) {
                if (i.getKeyCode() == KeyEvent.VK_SPACE) {
                    // new thread lets us run two while loops at once the first being the main camera updating one and the second in here
                    new Thread(() -> {
                        //this will make a folder if you don't have one named picFolder yet
                        try {
                            File folder = new File("picFolder");
                            if (!folder.exists()) folder.mkdirs();
                            int num = 0;
                            //makes an empty file object
                            File temp;
                            //This makes a new file with the count variabel based on how many pictures it took in this setion
                            do {
                                temp = new File(folder, "picture_" + count + "_" + num + ".png");
                                num++;
                            //while a file with this name already exists it increments num by one until there is a unique name
                            } while (temp.exists());
                            ImageIO.write(picPanel.image, "png", temp);
                            System.out.println("Saved");
                        //this is just if any part of this goes wrong for any reason so the program doesn't crash
                        } catch (IOException e) {
                            System.out.println("Save failed");
                            e.printStackTrace();
                        }
                    //end of thread
                    }).start();
                }
            }
        //end of on space pressed part
        });
    }

    //video taking method
    public static void getVid(int count, VideoCapture camera) {
        //you have to make variables like this or else you can't change them in the keyPressed functions unless they are final
        final boolean[] isRunning = {true};
        final boolean[] isSaving = {false};
        //this makes a new from for the video recording
        JFrame vFrame = new JFrame("Video Recorder " + count);
        Main vidPanel = new Main();
        vFrame.add(vidPanel);
        vFrame.setSize(640, 360);
        vFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        vFrame.setVisible(true);
        //listens for space or v pressed
        vFrame.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                //checks for space
                if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                    //changes variable that is checked in thread to see if it will begin saving
                    isSaving[0] = !isSaving[0];
                }
                //checks for v
                if (e.getKeyCode() == KeyEvent.VK_V) {
                    //changes variable for thread to now when to stop running the video and saving
                    isRunning[0] = false;
                }
            }
        });
        //makes a new thread for the window to be updated to show feed
        new Thread(() -> {
            //This creates a second video feed window by taking the data from the same camera as the main feed
            Mat frameMatrix = new Mat();
            VideoWriter writer = new VideoWriter();
            while (isRunning[0]) {
                //by doing this we minimize the amount of memory we take up with camera objects
                if (camera.read(frameMatrix)) {
                    //it alos lets this program work on both windows and mac
                    Core.flip(frameMatrix, frameMatrix, 1);
                    vidPanel.image = matrixToBufferedImage(frameMatrix);
                    vidPanel.repaint();

                    if (isSaving[0]) {
                        //this if statement is incase the save button is hit multiple times
                        if (!writer.isOpened()) {
                            //creates a new vid folder
                            File folder = new File("vidFolder");
                            if (!folder.exists()) folder.mkdirs();
                            //makes video file with new original name
                            String path = new File(folder, "video_" + count + ".mov").getAbsolutePath();
                            writer.open(path, VideoWriter.fourcc('M','J','P','G'), 20.0, frameMatrix.size());
                        }
                        writer.write(frameMatrix);
                    }
                }
            }
            //stops the video feedback and recording only in the video frame
            if (writer.isOpened()) {
                writer.release();
                System.out.println("FINISHED: saved.");
            }
        //end of thread
        }).start();
    }

    @Override
    protected void paintComponent(Graphics x)
    {
        super.paintComponent(x);
        if(image != null)
            {
                //
                x.drawImage(image, 0, 0, 640, 360, this);
            }
    }
}