package Jama;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

// provide filtering for jfilechooser to only show images that are compatible to be drawn
public class Filter extends FileFilter {

    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }
        String extension = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');
        if (i > 0 && i < s.length() - 1) {
            extension = s.substring(i + 1).toLowerCase();
        }
        if (extension != null) {
            return extension.equals("gif") || extension.equals("jpg") || extension.equals("png") || extension.equals("tif");
        } else {
            return false;
        }
    }

    public String getDescription() {
        return "*.gif, *.jpg, *.png";
    }
}
