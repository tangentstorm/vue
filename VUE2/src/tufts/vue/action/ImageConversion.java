package tufts.vue.action;

/*
 * ImageConversion.java
 *
 * Created on May 28, 2003, 11:08 AM
 */

import javax.imageio.*;
import java.awt.image.*;
import java.io.*;
import java.awt.*;
import java.awt.geom.*;
import javax.swing.*;
import tufts.vue.*;

/**
 *
 * @author  Daisuke Fujiwara
 */

/**a class which constructs a JPEG image of the current concept map*/
public class ImageConversion extends AbstractAction {
    
    /** Creates a new instance of ImageConversion */
    public ImageConversion() {
    }
    
    /**A constructor */
    public ImageConversion(String label)
    {
        super(label);
        putValue(Action.SHORT_DESCRIPTION,label);        
    }
    
    /**A method which takes in the image object and the location of the file along with the file format
     and saves the current map to the given file*/
    public void convert(BufferedImage image, File location, String format)
    {   
        //the conversion is done using the ImageIO class's static method
        try
        {
            ImageIO.write(image, format, location);
        }
        catch (Exception e)
        {
            System.out.println("Couldn't write to the file:" + e);
        }
    }
    
    /**A method which sets up for converting the active viewer to a Jpeg file*/
    public void createJpeg(File location)
    {
         //retrives the current map and gets its size
         LWMap currentMap = VUE.getActiveMap();
         
         Rectangle2D bounds = currentMap.getBounds();
         Dimension size = new Dimension((int)bounds.getWidth(), (int)bounds.getHeight());
         
         //creates an image object and sets up the graphics object of the image
         BufferedImage mapImage = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_RGB);
         Graphics2D g = (Graphics2D) mapImage.getGraphics();
        
         //draws the background and the border of the image
         g.setColor(Color.white);
         g.fillRect(0, 0, size.width, size.height);
         g.setColor(Color.black);
         g.drawRect(0, 0, size.width-1, size.height-1);
             
         //translate and set the clip for the map content
         g.translate(-(int)bounds.getX(), -(int)bounds.getY());
         g.setClip(0, 0, size.width, size.height);
            
         DrawContext dc = new DrawContext(g);
         dc.setPrinting(true);
         // render the map
         currentMap.draw(dc);
        
         //begins the conversion to the file
         convert(mapImage, location, "jpeg");
    }
    
    /**A method defined in the interface */
    public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
        
       try 
       {
           File selectedFile = ActionUtil.selectFile("Saving JPEG", "jpeg");
           
           if (selectedFile != null)
             createJpeg(selectedFile);
       }
        
       catch(Exception ex) 
       {
            System.out.println("Couldn't convert to jpeg:" + ex);
       }   
    }
    
}
