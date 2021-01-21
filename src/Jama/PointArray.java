package Jama;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.*;

// class that provides control point arraylist and operations on it
public class PointArray {
    public ArrayList<Point2D.Double> points, originalPoints, temp1, temp2, inBoxPoints;
    private double[] distances;
    private int iWidth, iHeight, yPadding, xPadding, totalPoints, gridSize;
    private double sqWidth, sqHeight;
    private int closestIndex;
    private ArrayList<Triangle[]> triangles;
    private int totalTriangles, diffX, diffY;
    private boolean previewing, showingBox;

    // constructor, calculate certain dimensions for grid sizes
    public PointArray(int imageWidth, int imageHeight, int numPoints) {
        iWidth = imageWidth;
        iHeight = imageHeight;
        gridSize = (int)Math.sqrt(numPoints) + 2;
        sqWidth = (double)iWidth / (gridSize-1);
        sqHeight = (double)iHeight / (gridSize-1);
        totalPoints = numPoints;
        totalTriangles = 2 * (int)Math.pow((gridSize - 1), 2);
        triangles = new ArrayList<>();
        inBoxPoints = new ArrayList<>();
        previewing = false;
        boxPoints = new int[400];
        createArray();
    }
    public int getNumPoints() {
        return totalPoints;
    }

    public PointArray() {}
    public double calcXCoord(int x) {
        return (sqWidth * x);
    }
    public double calcYCoord(int y) {
        return (sqHeight * y);
    }
    public boolean getPreviewing() {
        return previewing;
    }
    public void setPreviewing(boolean b) {
        previewing = b;
    }

    // build array of control points
    public void createArray() {
        points = new ArrayList<>();

        if (xPadding < 0) {
            xPadding = 0;
        }
        if (yPadding < 0) {
            yPadding = 0;
        }

        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                double newX, newY;
                newX = calcXCoord(j);
                newY = calcYCoord(i);
                points.add(new Point2D.Double(newX, newY));
            }
        }
        originalPoints = points;

        // initialize triangles - based on square to top left of point, 0 is bottom 1 is top
        for (int i = 0; i < points.size(); i++) {
            Point2D.Double p = points.get(i);
            if (checkValid(i)) {
                Point2D.Double north = points.get(i - gridSize);
                Point2D.Double west = points.get(i - 1);
                Point2D.Double leftDiagonal = points.get(i - gridSize - 1);
                Triangle[] tri = new Triangle[2];
                tri[0] = new Triangle(p, west, leftDiagonal);
                tri[1] = new Triangle(p, north, leftDiagonal);
                triangles.add(tri);
            }
        }

    }

    // set triangles
    public void setTriangles() {
        int index = 0;
        for (int i = 0; i < points.size(); i++) {
            Point2D.Double p = points.get(i);
            if (checkValid(i)) {
                Point2D.Double north = points.get(i - gridSize);
                Point2D.Double west = points.get(i - 1);
                Point2D.Double leftDiagonal = points.get(i - gridSize - 1);
                Triangle[] tri = new Triangle[2];
                tri[0] = new Triangle(p, west, leftDiagonal);
                tri[1] = new Triangle(p, north, leftDiagonal);
                triangles.set(index, tri);
                index++;
            }
        }
    }

    // find if square on top left of the point should be calculated as 2 triangles
    public boolean checkValid(int index) {
        if (index % gridSize == 0) { return false; }
        return index >= gridSize;
    }

    // return true if index of point is an edge
    public boolean checkEdge(int index) {
        if (index < 0) { return true; }
        if (index > (gridSize * gridSize) - 1) { return true; }

        if (index < (gridSize)) {
            return true;
        } else if (index % (gridSize) == 0) {
            return true;
        } else if ((index + 1) % (gridSize) == 0) {
            return true;
        } else return index < points.size() && index > (points.size() - 1 - (gridSize));
    }

    // draw control points
    public void drawPoints(Graphics g) {
        for (int i = 0; i < points.size(); i++) {
            if (!checkEdge(i)) {
                g.setColor(Color.white);
                g.fillOval((int)points.get(i).x - 5, (int)points.get(i).y - 5, 10, 10);
                g.setColor(Color.black);
                g.drawOval((int)points.get(i).x - 5, (int)points.get(i).y - 5, 10, 10);
            }
        }
    }

    // draw lines between control points
    public void drawLines(Graphics2D g) {
        g.setColor(Color.orange);
        if (previewing) {
            g.setColor(Color.black);
        }
        for (int i = 0; i < points.size(); i++) {
            Point2D.Double p = points.get(i);
            if (!checkEdge(i)) {
                Point2D.Double north = points.get(i - gridSize);
                Point2D.Double south = points.get(i + gridSize);
                Point2D.Double west = points.get(i - 1);
                Point2D.Double east = points.get(i + 1);
                Point2D.Double leftDiagonal = points.get(i - gridSize - 1);
                Point2D.Double rightDiagonal = points.get(i + gridSize + 1);
                g.draw(new Line2D.Double(p.x, p.y, north.x, north.y));
                g.draw(new Line2D.Double(p.x, p.y, south.x, south.y));
                g.draw(new Line2D.Double(p.x, p.y, west.x, west.y));
                g.draw(new Line2D.Double(p.x, p.y, east.x, east.y));
                g.draw(new Line2D.Double(p.x, p.y, leftDiagonal.x, leftDiagonal.y));
                g.draw(new Line2D.Double(p.x, p.y, rightDiagonal.x, rightDiagonal.y));
            }
        }
        // edge cases, top right and bottom left corners
        Point2D.Double edge1 = points.get(gridSize - 2);
        Point2D.Double edge2 = points.get((gridSize * 2) - 1);
        Point2D.Double edge3 = points.get(points.size() - gridSize + 1);
        Point2D.Double edge4 = points.get(points.size() - (gridSize * 2));

        g.draw(new Line2D.Double(edge1.x, edge1.y, edge2.x, edge2.y));
        g.draw(new Line2D.Double(edge3.x, edge3.y, edge4.x, edge4.y));
    }

    // grab point if click is close enough
    public Point2D.Double findPoint(int x, int y) {
        distances = new double[points.size()];
        closestIndex = 0;
        for (int i = 0; i < distances.length; i++) {
            distances[i] = Math.sqrt(Math.pow((points.get(i).x - x), 2) + Math.pow((points.get(i).y - y), 2));
        }
        double c = distances[0];
        for (int j = 0; j < distances.length; j++) {
            if (distances[j] < c) {
                closestIndex = j;
                c = distances[closestIndex];
            }
        }
        if (distances[closestIndex] < 10) {
            if (checkEdge(closestIndex)) {
                return null;
            }
            return points.get(closestIndex);
        }

        return null;
    }

    // check if click is in rectangle
    public boolean checkInBox(Rectangle rectangle, int x, int y) {
        if (rectangle.contains(x, y)) {
            showingBox = true;
            return true;
        }
        return false;
    }

    // select all points within rectangle and drag them
    public void handleBoxPoints(Rectangle rectangle, boolean dragging, Graphics2D g, Point click, int diffX, int diffY) {
        int startX, startY;
        double pointDiffX, pointDiffY;
        startX = click.x - diffX;
        startY = click.y - diffY;

        int originalX = rectangle.x;
        int originalY = rectangle.y;

        g.setColor(Color.red);
        rectangle.setLocation(startX, startY);
        g.draw(rectangle);
        for (int i = 0; i < points.size(); i++) {  // mark them
            for (int j = 0; j < boxPoints.length; j++) {
                if (i == boxPoints[j] && !checkEdge(i)) {
                    showSelected(i, g);
                    pointDiffX = originalX - points.get(i).x;
                    pointDiffY = originalY - points.get(i).y;
                    double nextX, nextY;
                    nextX = startX - pointDiffX;
                    nextY = startY - pointDiffY;
                    setPoint(i, nextX, nextY);
                }
            }
        }
    }

    private int[] boxPoints = new int[400];

    public int[] getBoxPoints() {
        return boxPoints;
    }

    // show corresponding points on other image
    public void showOtherBoxPoints(int[] ps, Graphics2D g) {
        for (int i = 0; i < points.size(); i++) {
            for (int j = 0; j < ps.length; j++) {
                if (i == ps[j] && !checkEdge(i)) {
                    showSelected(i, g);
                }
            }
        }
    }

    // show points in box
    public void showBoxPoints(Rectangle rectangle, Graphics2D g) {
        boxPoints = new int[400];
        int e = 0;
        for (int i = 0; i < points.size(); i++) {  // mark them
            if (rectangle.contains(points.get(i).x, points.get(i).y) && !checkEdge(i)) {
                showSelected(i, g);
                boxPoints[e] = i;
                e++;
            }
        }
    }

    // print points for debugging
    public void printPoints() {
        for (int i = 0; i < points.size(); i++) {
            System.out.println(points.get(i).x + " " + points.get(i).y);
        }
        System.out.println("----------------------------");
    }
    public void printPoint(int index) {
        System.out.println("point " + index + " : " + points.get(index).x + " " + points.get(index).y);
    }
    // print triangles for debugging
    public void printTriangles() {
        for (int i = 0; i < triangles.size(); i++) {
            System.out.println(Arrays.toString(triangles.get(i)[0].getAllCoords()));
            System.out.println(Arrays.toString(triangles.get(i)[1].getAllCoords()));
        }
        System.out.println("----------------------------");
    }

    // compare two arrays of points, for debugging
    public boolean compareArrays(ArrayList<Point2D.Double> begin, ArrayList<Point2D.Double> end) {
        temp1 = begin;
        temp2 = end;
        for (int i = 0; i < begin.size(); i++) {
            temp1.get(i).x = Math.round(temp1.get(i).x);
            temp1.get(i).y = Math.round(temp1.get(i).y);
            temp2.get(i).x = Math.round(temp2.get(i).x);
            temp2.get(i).y = Math.round(temp2.get(i).y);
        }
        return temp1 == temp2;
    }

    // set point
    public void setCurrPoint(double x, double y) {
        points.get(closestIndex).x = x;
        points.get(closestIndex).y = y;
        setTriangles();
    }
    // find the current index of the given point
    public int getCurrIndex(Point2D.Double p) {
        return points.indexOf(p);
    }
    // set specific point
    public void setPoint(int index, double x, double y) {
        points.get(index).x = x;
        points.get(index).y = y;
        setTriangles();
    }
    public int getTotalTriangles() { return totalTriangles; }
    public ArrayList<Triangle[]> getTriangles() { return triangles; }

    // reflect selected control point on other image grid mesh
    public void showSelected(int index, Graphics g) {
        Point2D.Double p = points.get(index);
        g.setColor(Color.red);
        g.fillOval((int)p.x - 5, (int)p.y - 5, 10, 10);
        g.setColor(Color.black);
        g.drawOval((int)p.x - 5, (int)p.y - 5, 10, 10);
    }

    // reset points to original
    public void resetPoints() {
        points = originalPoints;
    }
    // grab points
    public ArrayList<Point2D.Double> getPoints() {
        return points;
    }
    // set points, and call setTriangles()
    public void setPoints(ArrayList<Point2D.Double> p) {
        points = p;
        setTriangles();
    }
}
