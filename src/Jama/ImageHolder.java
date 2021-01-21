package Jama;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.*;
import java.util.*;
import java.io.*;

// class to hold images and operate on those images
public class ImageHolder extends JPanel implements MouseListener, MouseMotionListener {

    private int x, y, diffX, diffY;
    private Point dragStart, dragFinish;
    private int numPoints;
    private boolean selected;
    private boolean imageSet, previewing = false, donePreviewing, clickInBox, boxSet, showOtherPoints;
    private BufferedImage bim = null;
    private BufferedImage originalImage = null;
    private BufferedImage startImage = null, endImage = null, originalStartImage = null, originalEndImage = null;
    private AlphaComposite ac;
    public PointArray pointArray, startArray, endArray;
    private Point2D.Double currPoint;
    private int currIndex, correspondingIndex, tweenFrames, iterations;
    private boolean showCorr, draggingBox;
    private Timer timer;
    private float transitionStep = 0;
    private double alpha;
    private boolean runMorph, export;
    private ArrayList<Triangle[]> startTriangles, endTriangles, triangles;
    private Rectangle rect;
    private Point boxClick;
    private int[] bp;

    private final int THRESHOLD_DISTANCE = 10;
    public ImageHolder() {}

    // constructor
    public ImageHolder(int cps) {
        setSize(getPreferredSize());
        addMouseListener(this);
        addMouseMotionListener(this);
        selected = false;
        imageSet = false;
        showCorr = false; boxSet = false;
        donePreviewing = false; clickInBox = false;
        runMorph = false; export = false; draggingBox = false;
        showOtherPoints = false;
        iterations = 0;
        diffX = 0; diffY = 0;
        rect = new Rectangle();
        setNumPoints(cps);
        createNewGrid();
    }

    public void setNumPoints(int cps) {
        numPoints = cps * cps;
    } // set the total number of points
    public int getNumPoints() {
        return numPoints;
    }   // return total number of points

    public void createNewGrid() {   // create a new grid of points
        selected = false;
        showCorr = false;
        rect = new Rectangle();
        draggingBox = false;
        clickInBox = false; boxSet = false;
        iterations = 0;
        showOtherPoints = false;
        if (previewing) {
            this.setSize(618, 522);
            pointArray = new PointArray(this.getWidth(), this.getHeight(), numPoints);
            pointArray.setTriangles();
        } else {
            pointArray = new PointArray(this.getWidth(), this.getHeight(), numPoints);
        }
        drawMesh();
    }

    public boolean getSelected() {
        return selected;
    }
    public boolean getImageSet() {
        return imageSet;
    }
    public boolean getBoxSet() { return boxSet; }
    public void setImageSet(boolean b) { imageSet = b; }
    public int getCurrIndex() { return currIndex; }
    public void setShowCorr(boolean b) {
        showCorr = b;
    }
    public void setCorrespondingIndex(int c) {  // set the index of the point on the other imageholder
        correspondingIndex = c;
        setShowCorr(true);
        drawMesh();
    }
    public void stopTimer() {
        timer.stop();
    }

    // begin animation
    public void startAnimation(ImageHolder start, ImageHolder end) {
        iterations = 0;
        startArray = new PointArray();
        endArray = new PointArray();
        startArray = start.getPoints();
        setPreviewing(true);
        endArray = end.getPoints();
        startTriangles = startArray.getTriangles();
        endTriangles = endArray.getTriangles();
        triangles = pointArray.getTriangles();
        originalStartImage = start.getImage();
        originalEndImage = end.getImage();
        int totalFrames = tweenFrames;
        float one = 1;
        transitionStep = one / totalFrames;
        alpha = 0;
        timer = new Timer(50, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (alpha >= 1) {
                    timer.stop();
                } else {
                    alpha += transitionStep;
                    iterations(startArray.getPoints(), endArray.getPoints(), alpha);
                    if (runMorph) {
                        for (int i = 0; i < startTriangles.size(); i++) {
                            // if running morph, call warptriangle to provide morphing visual as transition between both images occurs
                            MorphTools.warpTriangle(originalStartImage, startImage, startTriangles.get(i)[0], triangles.get(i)[0], null, null, false);
                            MorphTools.warpTriangle(originalStartImage, startImage, startTriangles.get(i)[1], triangles.get(i)[1], null, null, false);
                            MorphTools.warpTriangle(originalEndImage, endImage, endTriangles.get(i)[0], triangles.get(i)[0], null, null, false);
                            MorphTools.warpTriangle(originalEndImage, endImage, endTriangles.get(i)[1], triangles.get(i)[1], null, null, false);

                            if (alpha >= 1) { alpha = 1; }    // keep alpha in range to prevent error
                            ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float)(alpha));
                            drawMesh();
                        }
                    } else {
                        drawMesh();
                    }
                    iterations++;

                    if (export) {   // if export has been clicked, export images as jpegs
                        createExportImage();
                    }
                }
            }
        });
        timer.start();
    }

    // export current image as a jpeg
    public void createExportImage() {
        BufferedImage i = new BufferedImage(originalStartImage.getWidth(), originalStartImage.getHeight(), BufferedImage.TYPE_INT_RGB);
        paint(i.getGraphics());
        try {
            File f = new File("pic" + iterations + ".jpeg");
            ImageIO.write(i, "jpeg", f);
        } catch (IOException ii) {}
    }

    // iterate through each frame
    public void iterations(ArrayList<Point2D.Double> start, ArrayList<Point2D.Double> end, double currAlpha) {
        ArrayList<Point2D.Double> temp = pointArray.getPoints();
        for (int i = 0; i < temp.size(); i++) {
            if (!pointArray.checkEdge(i) && currAlpha <= 1) {
                temp.get(i).x = (currAlpha * (end.get(i).x - start.get(i).x)) + start.get(i).x;
                temp.get(i).y = (currAlpha * (end.get(i).y - start.get(i).y)) + start.get(i).y;
            }
        }
        pointArray.setPoints(temp);
    }

    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D big = (Graphics2D) g;

        if (imageSet && !runMorph) {    // if image is set and this isn't the Run morph panel, draw grid
            big.drawImage(bim, 0, 0, null);
            pointArray.drawLines(big);
            pointArray.drawPoints(big);
            if (selected) { // color selected point
                pointArray.showSelected(currIndex, g);
            }
            if (showCorr) { // color corresponding point if other panel is selected
                pointArray.showSelected(correspondingIndex, g);
            }
            if (draggingBox) {
                createRectangle(big);
                pointArray.showBoxPoints(rect, big);
            }
            if (boxSet && clickInBox) {
                pointArray.handleBoxPoints(rect, true, big, boxClick, diffX, diffY);
            }
            if (showOtherPoints) {
                pointArray.showOtherBoxPoints(bp, big);
            }
        } else if (runMorph) {
            try {   // if running morph, draw both on top of each other with alphacomposite to provide dissolve
                big.drawImage(startImage, 0, 0, null);
                big.setComposite(ac);
                big.drawImage(endImage, 0, 0, null);
            } catch (IllegalArgumentException i) {}
        }
    }

    // when mouse is clicked, figure out if a point is near enough to grab
    public void mousePressed(MouseEvent e) {
        boxClick = null;
        dragStart = null;
        int curx, cury;
        curx = e.getX();
        cury = e.getY();

        if (imageSet && !previewing) {
            currPoint = pointArray.findPoint(curx, cury);
            if (currPoint != null) {
                setShowCorr(false);
                rect = new Rectangle();
                boxSet = false;
                currIndex = pointArray.getCurrIndex(currPoint);
                selected = true;
                drawMesh();
            } else if (currPoint == null && !boxSet){
                draggingBox = true;
                dragStart = new Point(curx, cury);
            } else if (boxSet && currPoint == null) {
                clickInBox = pointArray.checkInBox(rect, e.getX(), e.getY());
                if (!clickInBox) {
                    boxClick = null;
                    boxSet = false;
                    rect = new Rectangle();
                    drawMesh();
                } else {
                    boxClick = new Point(e.getX(), e.getY());
                    diffX = e.getX() - rect.x;
                    diffY = e.getY() - rect.y;
                    drawMesh();
                }
            }
        }
    }
    public void mouseReleased(MouseEvent e) {
        if (selected) {
            selected = false;
            pointArray.setCurrPoint(e.getX(), e.getY());
        } else if (draggingBox) {
            draggingBox = false;
            dragFinish = new Point(e.getX(), e.getY());
        } else if (boxSet && clickInBox) {
            boxClick = new Point(e.getX(), e.getY());
            clickInBox = false;
        }
    }
    public void mouseDragged(MouseEvent e) {    // if dragged, drag point with it
        x = e.getX();
        y = e.getY();
        if (selected) {
            pointArray.setCurrPoint(x, y);
        } else if (draggingBox) {
            dragFinish = new Point(x, y);
        } else if (boxSet && clickInBox) {
            boxClick = new Point(e.getX(), e.getY());
        }
        drawMesh();
    }

    public void drawMesh() { repaint(); }

    // create rectangle
    public void createRectangle(Graphics2D g) {
        g.setColor(Color.red);

        if (dragStart.x < dragFinish.x) {
            if (dragStart.y < dragFinish.y) {
                rect = new Rectangle(dragStart.x, dragStart.y, (Math.abs(dragStart.x - dragFinish.x)), (Math.abs(dragStart.y - dragFinish.y)));
            } else if (dragStart.y > dragFinish.y){
                rect = new Rectangle(dragStart.x, dragStart.y - (Math.abs(dragStart.y - dragFinish.y)), (Math.abs(dragStart.x - dragFinish.x)), (Math.abs(dragStart.y - dragFinish.y)));
            }
        } else if (dragStart.x > dragFinish.x) {
            if (dragStart.y < dragFinish.y) {
                rect = new Rectangle(dragStart.x - (Math.abs(dragStart.x - dragFinish.x)), dragStart.y, (Math.abs(dragStart.x - dragFinish.x)), (Math.abs(dragStart.y - dragFinish.y)));
            } else if (dragStart.y > dragFinish.y) {
                rect = new Rectangle(dragFinish.x, dragFinish.y, (Math.abs(dragStart.x - dragFinish.x)), (Math.abs(dragStart.y - dragFinish.y)));
            }
        }
        g.draw(rect);
        boxSet = true;
    }

    // read image and draw it on panel
    private BufferedImage readImage(String file) {
        Image image = Toolkit.getDefaultToolkit().getImage(file);
        MediaTracker tracker = new MediaTracker (new Component () {});
        tracker.addImage(image, 0);
        try { tracker.waitForID (0); }
        catch (InterruptedException e) {}
        if (runMorph) {
            BufferedImage b = new BufferedImage(this.getWidth(), this.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D big = b.createGraphics();
            resizeImage(image, big);
            return b;
        }
        bim = new BufferedImage(this.getWidth(), this.getHeight(), BufferedImage.TYPE_INT_RGB);
        if (previewing && !runMorph) { bim = new BufferedImage(this.getWidth(), this.getHeight(), BufferedImage.TYPE_INT_ARGB); }
        Graphics2D big = bim.createGraphics();
        resizeImage(image, big);
        return bim;
    }

    // draw image to conform to panel size
    public void resizeImage(Image image, Graphics2D graphics2D) {
        graphics2D.drawImage(image, 0, 0, this.getWidth(), this.getHeight(), null);
        graphics2D.dispose();
    }

    // set image in panel
    public void setImage(String file) {
        bim = readImage(file);
        originalImage = bim;
        setSize(new Dimension(bim.getWidth(), bim.getHeight()));
        System.out.println(this.getWidth() + " " + this.getHeight());
        imageSet = true;
    }

    // set start and end images when running morph
    public void setBothImages(String startFile, String endFile) {
        startImage = readImage(startFile);
        endImage = readImage(endFile);
        setSize(new Dimension(startImage.getWidth(), startImage.getHeight()));
        imageSet = true;
        drawMesh();
    }

    // change the brightness of current image
    public void changeBrightness(float val) {
        RescaleOp brightOp = new RescaleOp(val, 0, null);
        bim = brightOp.filter(originalImage, null);
        drawMesh();
    }

    public void mouseClicked(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    public void mouseMoved(MouseEvent e) {}

    public PointArray getPoints() {
        return pointArray;
    }
    public void setPoints(PointArray p) {
        pointArray = p;
    }

    public void showOtherBoxPoints(int[] bps, Graphics2D g) {
        bp = bps;
       pointArray.showOtherBoxPoints(bps, g);
    }
    public boolean getShowOtherPoints() {
        return showOtherPoints;
    }
    public void setShowOtherPoints(boolean show) {
        showOtherPoints = show;
    }
    public int[] getBp() {
        return pointArray.getBoxPoints();
    }
    public void setBp(int[] bps) {
        showOtherPoints = true;
        bp = bps;
        drawMesh();
    }

    public void setPreviewing(boolean b) {
        previewing = b;
    }
    public int getTweenFrames() { return tweenFrames; }
    public void setTweenFrames(int f) { tweenFrames = f; }
    public void setDonePreviewing(boolean b) { donePreviewing = b; }
    public boolean getDonePreviewing() { return donePreviewing; }
    public void setRunMorph(boolean b) { runMorph = b; }
    public boolean getRunMorph() { return runMorph; }
    public void setExport(boolean b) { export = b; }
    public boolean getExport() { return export; }
    public BufferedImage getImage() { return bim; }
}
