package Jama;

import Jama.Matrix;

import java.awt.image.*;
import java.awt.*;
import java.awt.geom.*;
// Use the Java matrix package from Nist
// https://math.nist.gov/javanumerics/jama/


public class MorphTools{

    public MorphTools(){}

    public static void warpTriangle
            (BufferedImage src,
             BufferedImage dest,
             Triangle S,
             Triangle D,
             Object ALIASING,
             Object INTERPOLATION,
             Boolean clearBackground) {

        if( ALIASING == null )
            ALIASING = RenderingHints.VALUE_ANTIALIAS_ON;
        if( INTERPOLATION == null )
            INTERPOLATION = RenderingHints.VALUE_INTERPOLATION_BICUBIC;

        // Solves the two 3x3 systems of equations for the affine
        // mapping of the source triangle onto the destination triangle
        // defined by the coordinates of the two triangles.

        // The 3x3 matrix A is the same for both systems.
        // In one system the b vector is made from x coords
        // of the destination triangle (named BdestX here) and in the
        // other system the b vector is set to y coords of the
        // destination triangle (BdestY)
        // The first system gives the first row of the affine warp
        // The second system gives the second row.

        // The JAMA matrix library expects 2D arrays in its matrix
        // constructor, even for column vectors (only have 1 column)
        // and row vectors (only have 1 row).
        // So the column vectors BdestX and BdestY are actually 2D arrays
        // with 3 rows and 1 column.
        // Also, just like arrays, the JAMA matrix object indexes its
        // rows and columns starting with 0.
        double [][] Aarray = new double [3][3];
        double [][] BdestX = new double [3][1];
        double [][] BdestY = new double [3][1];
        for( int i= 0; i<3; ++i){
            Aarray[i][0] = S.getX(i);
            Aarray[i][1] = S.getY(i);
            Aarray[i][2] = 1.0;
            BdestX[i][0] = D.getX(i);
            BdestY[i][0] = D.getY(i);
        }

        // The matrix "A", which is the same for both systems to be solved,
        // is a 3x3 matrix.
        // The matrix "bx" is a 3x1 "column vector" of x values from the
        // destination triangle.
        // The matrix "by" is a 3x1 "column vector" of y values from the
        // destination triangle.
        // Now create matrix objects from the arrays of doubles in order to
        // use the JAMA systems-of-linear-equations solver via the "solve()"
        // method.
        Matrix A = new Matrix(Aarray);
        Matrix bx = new Matrix(BdestX);
        Matrix by = new Matrix(BdestY);

        // Matrices are ready, now solve using the "solve" method
        // The resulting solution matrices, column vectors from the solver,
        // are the values making up the first row and then the second row
        // of the affine transformation.
        // Yes, the result comes out of the solver as a column vector.
        // But each vector is a row of the affine transformation matrix we
        // seek (mapping the start triangle onto the destination triangle)
        Matrix affineRow1 = A.solve(bx);
        Matrix affineRow2 = A.solve(by);

        // Now that we have solved for the correct affine transformation
        // mapping the source triangle to the destination triangle, we
        // instantiate the Java affine transform object with the solved
        // values in the solution vectors.
        // Again, JAMA represents the solution vector as a 2D matrix
        // even though we know it is a column vector, so the column index
        // is always 0.
        // Note the order of parameters expected in the AffineTransform
        // constructor:  it wants column major order (usually one
        // expects row major...)
        // The order matters, if parameters are transposed the transformation
        // will not be correct.
        AffineTransform af = new
                AffineTransform(affineRow1.get(0,0), affineRow2.get(0,0),
                affineRow1.get(1,0), affineRow2.get(1,0),
                affineRow1.get(2,0), affineRow2.get(2,0));

        // Get the graphics context for the destination image
        // This destination image is the output image, produced by
        // rendering from the source to the destination
        Graphics2D g2 = dest.createGraphics();

        // Set the aliasing and interpolation settings
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, ALIASING);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, INTERPOLATION);

        // If the destination image should be cleared before the
        // triangle is rendered, fill the entire image with black
        // (this is a boolean parameter)
        if (clearBackground) {
            g2.setColor(Color.BLACK);
            g2.fill(new Rectangle(0, 0, dest.getWidth(), dest.getHeight()));
        }

        // Create the clip region in the destination image as a "path.
        // This region is the destination triangle, D
        GeneralPath destPath = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
        destPath.moveTo((float)D.getX(0), (float)D.getY(0));
        destPath.lineTo((float)D.getX(1), (float)D.getY(1));
        destPath.lineTo((float)D.getX(2), (float)D.getY(2) );
        destPath.lineTo((float)D.getX(0), (float)D.getY(0) );

        // Apply the clip region so that any pixels that fall outside
        // this region will be clipped
        g2.clip(destPath);
        // Apply the affine transform, which will map the pixels in
        // the source triangle onto the destination image
        // the destination
        g2.setTransform(af);
        // Map the pixels from the source image into the destination
        // according to the destination image's graphics context
        g2.drawImage(src, 0, 0, null);
        g2.dispose();
    }
}

