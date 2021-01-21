package Jama;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.Hashtable;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class Gui extends JFrame implements ActionListener {
    static final int CP_MIN = 5;
    static final int CP_MAX = 20;
    static final int CP_INIT = 5;
    static final int T_MIN = 1, T_MAX = 10, T_INIT = 1;
    static final int FPS_MIN = 20, FPS_MAX = 100, FPS_INIT = 20;
    static final int B_MIN = 0, B_MAX = 20, B_INIT = 10;

    private PreviewMorph previewMorph;
    private RunMorph runMorph;
    private JLabel controlLabel;
    private JPanel imageView, bottomView, leftOptions, middleOptions, rightOptions;
    public ImageHolder leftView, rightView;
    private int numPoints, tweenFrames, preConversionLeft, preConversionRight;
    private float leftBrightness, rightBrightness;
    private Timer checkSelected;
    private boolean left, right;
    private boolean isSetLeft, isSetRight;
    private String leftName, rightName;

    public int currIndex;

    // timer checks if a point has been selected and color it accordingly, along with the corresponding point on the other image
    public void startTimer() {
        checkSelected = new Timer(10, null);
        checkSelected.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                left = leftView.getSelected();
                right = rightView.getSelected();
                isSetLeft = leftView.getImageSet();
                isSetRight = rightView.getImageSet();
                if (left && !right) {
                    currIndex = leftView.getCurrIndex();
                    rightView.setCorrespondingIndex(currIndex);
                } else if (right && !left) {
                    currIndex = rightView.getCurrIndex();
                    leftView.setCorrespondingIndex(currIndex);
                } else if (leftView.getBoxSet() && !rightView.getBoxSet()) {
                    rightView.setShowOtherPoints(false);
                    leftView.setShowOtherPoints(false);
                    int[] boxPoints = leftView.getBp();
                    rightView.setBp(boxPoints);
                } else if (rightView.getBoxSet() && !leftView.getBoxSet()) {
                    leftView.setShowOtherPoints(false);
                    rightView.setShowOtherPoints(false);
                    int[] boxPoints = rightView.getBp();
                    leftView.setBp(boxPoints);
                } else if (rightView.getBoxSet() && leftView.getBoxSet()) {

                }
            }
        });
        checkSelected.start();
    }
    public void stopTimer() { checkSelected.stop(); }
    public void startSTimer() { checkSelected.start(); }

    // add both brightness sliders
    public void addBrightnessSliders() {
        preConversionLeft = 10; preConversionRight = 10;
        leftBrightness = 1.0f;
        JLabel leftLabel = new JLabel("Brightness: " + leftBrightness + " where 1.0 is original");
        JSlider leftSlider = new JSlider(JSlider.HORIZONTAL, B_MIN, B_MAX, B_INIT);
        leftSlider.setMajorTickSpacing(1);
        leftSlider.setPaintLabels(true);
        leftSlider.setPaintTicks(true);
        Hashtable labelTable = new Hashtable();     // changes labels to be more understandable
        labelTable.put(0, new JLabel("Dark"));
        labelTable.put(B_MAX / 2, new JLabel("Original"));
        labelTable.put(B_MAX, new JLabel("Bright"));
        leftSlider.setLabelTable(labelTable);
        leftSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                JSlider source = (JSlider)(e.getSource());
                if (!source.getValueIsAdjusting()) {
                    preConversionLeft = source.getValue();
                    leftBrightness = (float)preConversionLeft / 10;
                    if (isSetLeft) {
                        leftView.changeBrightness(leftBrightness);
                    }
                    leftLabel.setText("Brightness: " + leftBrightness + " where 1.0 is original");
                }
            }
        });
        leftLabel.setHorizontalAlignment(JLabel.CENTER);
        leftSlider.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.gray));
        leftOptions.add(leftLabel);
        leftOptions.add(leftSlider);

        rightBrightness = 1.0f;
        JLabel rightLabel = new JLabel("Brightness: " + rightBrightness + " where 1.0 is original");
        JSlider rightSlider = new JSlider(B_MIN, B_MAX, B_INIT);
        rightSlider.setMajorTickSpacing(1);
        rightSlider.setPaintLabels(true);
        rightSlider.setPaintTicks(true);
        rightSlider.setLabelTable(labelTable);
        rightSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                JSlider source = (JSlider)(e.getSource());
                if (!source.getValueIsAdjusting()) {
                    preConversionRight = source.getValue();
                    rightBrightness = (float)preConversionRight / 10;
                    if (isSetRight) {
                        rightView.changeBrightness(rightBrightness);
                    }
                    rightLabel.setText("Brightness: " + rightBrightness + " where 1.0 is original");
                }
            }
        });
        rightLabel.setHorizontalAlignment(JLabel.CENTER);
        rightSlider.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.gray));
        rightOptions.add(rightLabel);
        rightOptions.add(rightSlider);
    }

    // slider to select total frames to render
    public void createTweenFramesSlider() {
        tweenFrames = 20;
        JLabel fpsLabel = new JLabel("Frames to render: " + tweenFrames);
        JSlider fpsSlider = new JSlider(JSlider.HORIZONTAL, FPS_MIN, FPS_MAX, FPS_INIT);
        fpsSlider.setMajorTickSpacing(10);
        fpsSlider.setPaintTicks(true);
        fpsSlider.setPaintLabels(true);
        fpsSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                JSlider source = (JSlider)(e.getSource());
                if (!source.getValueIsAdjusting()) {
                    tweenFrames = source.getValue();
                    fpsLabel.setText("Frames to render: " + tweenFrames);
                }
            }
        });
        fpsLabel.setHorizontalAlignment(JLabel.CENTER);
        fpsLabel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.black));
        middleOptions.add(fpsLabel);
        middleOptions.add(fpsSlider);
    }

    public void addSlider() {   // create grid resolution slider
        controlLabel = new JLabel("Grid Resolution: " + numPoints + " by " + numPoints);
        JSlider controlPoints = new JSlider(JSlider.HORIZONTAL, CP_MIN, CP_MAX, CP_INIT);
        controlPoints.setMajorTickSpacing(1);
        controlPoints.setPaintTicks(true);
        controlPoints.setPaintLabels(true);
        controlPoints.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                JSlider source = (JSlider)(e.getSource());
                if (!source.getValueIsAdjusting()) {
                    numPoints = source.getValue();
                    leftView.setNumPoints(numPoints);
                    rightView.setNumPoints(numPoints);
                    leftView.createNewGrid();
                    rightView.createNewGrid();
                    leftView.drawMesh();
                    rightView.drawMesh();
                    controlLabel.setText("Grid Resolution: " + numPoints + " by " + numPoints);
                }
            }
        });
        controlLabel.setHorizontalAlignment(JLabel.CENTER);
        middleOptions.add(controlLabel);
        middleOptions.add(controlPoints);
    }

    public void addElements() {     // add all elements to interface
        imageView.setLayout(new GridLayout(1, 2));
        bottomView.setLayout(new GridLayout(1, 3));
        leftOptions.setLayout(new GridLayout(3, 1));
        middleOptions.setLayout(new GridLayout(5, 1));
        rightOptions.setLayout(new GridLayout(3, 1));

        JButton reset = new JButton("Reset Control Points");
        JButton previewMorph = new JButton("Preview Morph");
        JButton runMorph = new JButton("Run Morph");

        reset.setFocusPainted(false); reset.addActionListener(this);
        previewMorph.setFocusPainted(false); previewMorph.addActionListener(this);
        runMorph.setFocusPainted(false); runMorph.addActionListener(this);

        addBrightnessSliders();
        addSlider();
//        createTransitionSlider();
        createTweenFramesSlider();

        leftOptions.setBorder(BorderFactory.createMatteBorder(2, 1, 1, 1, Color.gray));
        middleOptions.setBorder(BorderFactory.createMatteBorder(2, 1, 1, 1, Color.gray));
        rightOptions.setBorder(BorderFactory.createMatteBorder(2, 1, 1, 1, Color.gray));
        leftView.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.ORANGE));
        rightView.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.ORANGE));

        leftOptions.add(previewMorph);
        rightOptions.add(runMorph);
        middleOptions.add(reset);

        bottomView.add(leftOptions);
        bottomView.add(middleOptions);
        bottomView.add(rightOptions);

        imageView.add(leftView);
        imageView.add(rightView);

    }

    // assemble gui
    public void createContentPane() {
        Container c = getContentPane();
        c.setBackground(Color.gray);

        numPoints = 5;

        imageView = new JPanel();
        bottomView = new JPanel();
        leftOptions = new JPanel();
        middleOptions = new JPanel();
        rightOptions = new JPanel();
        leftView = new ImageHolder(5);
        rightView = new ImageHolder(5);

        c.add(imageView, BorderLayout.CENTER);
        c.add(bottomView, BorderLayout.SOUTH);

        addElements();

        startTimer();

        createMenuBar();

        this.setSize(new Dimension(1250, 800));
        centerWindow(this);
        show();
    }

    public Gui() {
        super("JMorph");
        createContentPane();
    }
    public void setRightName(String name) {
        rightName = name;
    }
    public void setLeftName(String name) {
        leftName = name;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JButton currButton = (JButton)(e.getSource());
        String s = currButton.getText();

        if (s.equals("Quit")) {
            System.exit(0);
        } else if (s.equals("Preview Morph")) { // preview the animation of the control points
            if (isSetLeft && isSetRight) {
                previewMorph = new PreviewMorph(tweenFrames, leftView, rightView);
            } else {    // should not be allowed to preview without setting both images
                JOptionPane.showMessageDialog(null, "Please set both images before trying to preview the morph.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else if (s.equals("Run Morph")) { // run the actual morph
            if (isSetLeft && isSetRight) {
                runMorph = new RunMorph(tweenFrames, leftView, rightView);
                runMorph.setBothImages(leftName, rightName);
                runMorph.startAnimation();
            } else {    // should not be allowed to preview without setting both images
                JOptionPane.showMessageDialog(null, "Please set both images before trying to run the morph.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else if (s.equals("Reset Control Points")) {  // reset the control points on both sides
            leftView.createNewGrid();
            rightView.createNewGrid();
        }
    }

    private JMenuBar menuBar;

    public void createMenuBar() {
        menuBar = new JMenuBar();
        JMenu menu;
        JMenuItem menuItem;
        String menuName = "File";
        String[] menuArray = {"Select Left Image", "Select Right Image", "Quit"};

        menu = new JMenu(menuName);
        menuBar.add(menu);
        for (int i = 0; i < menuArray.length; i++) {
            menuItem = new JMenuItem(menuArray[i]);
            menuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    JMenuItem source = (JMenuItem)(e.getSource());
                    String s = source.getText();
                    if (s.equals("Select Left Image")) {    // attempt to select left image
                        JFileChooser fc = new JFileChooser();
                        fc.addChoosableFileFilter(new Filter());
                        fc.setAcceptAllFileFilterUsed(false);
                        int returnVal = fc.showOpenDialog(null);

                        if (returnVal == JFileChooser.APPROVE_OPTION) {
                            File file = fc.getSelectedFile();
                            String fileName = file.getAbsolutePath();
                            leftName = fileName;
                            leftView.setImage(fileName);
                            leftView.createNewGrid();
                        }
                    } else if (s.equals("Select Right Image")) {    // attempt to select right image
                        JFileChooser fc = new JFileChooser();
                        fc.addChoosableFileFilter(new Filter());
                        fc.setAcceptAllFileFilterUsed(false);
                        int returnVal = fc.showOpenDialog(null);

                        if (returnVal == JFileChooser.APPROVE_OPTION) {
                            File file = fc.getSelectedFile();
                            String fileName = file.getAbsolutePath();
                            rightName = fileName;
                            rightView.setImage(fileName);
                            rightView.createNewGrid();
                        }
                    } else if (s.equals("Quit")) {
                        System.exit(0);
                    }
                }
            });
            menu.add(menuItem);
            if (i < menuArray.length-1) {
                menu.addSeparator();
            }
        }
        this.setJMenuBar(menuBar);
    }

    // centers window when it compiles
    public static void centerWindow(Window frame) {
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (int) ((dimension.getWidth() - frame.getWidth()) / 2);
        int y = (int) ((dimension.getHeight() - frame.getHeight()) / 2);
        frame.setLocation(x, y);
    }

    public static void main(String[] args) {
        Gui app = new Gui();
        app.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {System.exit(0);}
        });
    }
}
