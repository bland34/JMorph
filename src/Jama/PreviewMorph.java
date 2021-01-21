package Jama;

import Jama.ImageHolder;
import Jama.PointArray;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class PreviewMorph extends JFrame {
    private JPanel bottomView;
    private ImageHolder morphView;
    private final ImageHolder startView;
    private final ImageHolder endView;
    private final int tweenFrames;
    private JLabel tweenFramesLabel;
    private final PointArray startPoints;
    private final PointArray endPoints;

    public void addElements() {
        tweenFramesLabel = new JLabel("Frames Per Second: " + tweenFrames);
        tweenFramesLabel.setHorizontalAlignment(JLabel.CENTER);

        bottomView.add(tweenFramesLabel);
    }

    public void createContentPane() {
        Container c = getContentPane();
        c.setBackground(Color.white);

        addListener();
        morphView = new ImageHolder((int)Math.sqrt(startPoints.getNumPoints()));
        morphView.setImageSet(true);
        morphView.setPreviewing(true);
        morphView.createNewGrid();
        morphView.getPoints().setPreviewing(true);
        morphView.setTweenFrames(tweenFrames);
        morphView.drawMesh();
        bottomView = new JPanel();
        bottomView.setLayout(new GridLayout(1, 1));
        bottomView.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.black));

        addElements();

        c.add(morphView, BorderLayout.CENTER);
        c.add(bottomView, BorderLayout.SOUTH);
        this.setSize(640, 600);
        centerWindow(this);
        show();
    }

    public PreviewMorph(int f, ImageHolder start, ImageHolder end) {
        super("Preview Morph");
        tweenFrames = f;
        startView = start;
        endView = end;
        startPoints = startView.getPoints();
        endPoints = endView.getPoints();
        createContentPane();
        morphView.startAnimation(start, end);
    }

    // centers window when it compiles
    public static void centerWindow(Window frame) {
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (int) ((dimension.getWidth() - frame.getWidth()) / 2);
        int y = (int) ((dimension.getHeight() - frame.getHeight()) / 2);
        frame.setLocation(x, y);
    }

    // make sure to initialize everything when exiting window
    public void addListener() {
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                morphView.stopTimer();
                startPoints.setPreviewing(false);
                endPoints.setPreviewing(false);
                startView.setPreviewing(false);
                endView.setPreviewing(false);
                startView.setDonePreviewing(true);
                endView.setDonePreviewing(true);
            }
        });
    }

}
