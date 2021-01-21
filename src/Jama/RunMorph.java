package Jama;

import Jama.ImageHolder;
import Jama.PointArray;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class RunMorph extends JFrame implements ActionListener {

    private JPanel bottomView;
    private JButton exportButton;
    private final int tweenFrames;
    private final PointArray startPoints;
    private final PointArray endPoints;
    private final ImageHolder startView;
    private final ImageHolder endView;
    private ImageHolder morphView;
    private JLabel fpsLabel;

    public void addElements() {
        exportButton = new JButton("Export Morph"); exportButton.setFocusPainted(false);
        fpsLabel = new JLabel("Frames Per Second: " + tweenFrames);

        fpsLabel.setHorizontalAlignment(JLabel.CENTER);

        exportButton.addActionListener(this);

        bottomView.add(fpsLabel);
        bottomView.add(exportButton);

    }

    public void createContentPane() {
        Container c = getContentPane();
        c.setBackground(Color.white);

        addListener();
        morphView = new ImageHolder((int)Math.sqrt(startPoints.getNumPoints()));
        morphView.setImageSet(true);
        morphView.setPreviewing(true);
        morphView.setRunMorph(true);
        morphView.setTweenFrames(tweenFrames);
        morphView.createNewGrid();
        morphView.getPoints().setTriangles();
        morphView.getPoints().setPreviewing(true);
        morphView.drawMesh();

        bottomView = new JPanel();
        bottomView.setLayout(new GridLayout(1,2));
        bottomView.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.black));

        addElements();

        c.add(morphView, BorderLayout.CENTER);
        c.add(bottomView, BorderLayout.SOUTH);

        this.setSize(618, 580);
        centerWindow(this);
        show();
    }

    public RunMorph(int f, ImageHolder start, ImageHolder end) {
        super("Run Morph");
        tweenFrames = f;
        startView = start;
        endView = end;
        startPoints = startView.getPoints();
        endPoints = endView.getPoints();
        createContentPane();

    }

    public void startAnimation() {
        morphView.startAnimation(startView, endView);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JButton currButton = (JButton)(e.getSource());
        String s = currButton.toString();

        System.out.println("h");
        morphView.setExport(true);
        startAnimation();
        morphView.drawMesh();

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
                startPoints.setPreviewing(false);
                endPoints.setPreviewing(false);
                startView.setPreviewing(false);
                endView.setPreviewing(false);
                startView.setDonePreviewing(true);
                endView.setDonePreviewing(true);
                morphView.stopTimer();
            }
        });
    }

    public void setBothImages(String startFileName, String endFileName) {
        morphView.setBothImages(startFileName, endFileName);
    }
}
