/*
 * ImageMap.java
 *
 * Created on June 6, 2003, 5:01 PM
 */

package tufts.vue.action;

/**
 *
 * @author  Jay Briedis
 */
import javax.imageio.*;
import java.awt.image.*;
import java.io.*;
import java.awt.*;
import javax.swing.*;
import java.awt.geom.Rectangle2D;
import tufts.vue.*;

public class ImageMap extends AbstractAction {
    
    private int xOffset, yOffset;
    private LWMap map;
    private double scale = 1.0;
    /** Creates a new instance of ImageConversion */
    public ImageMap() {
    }
    
    public ImageMap(String label)
    {
        super(label);
        putValue(Action.SHORT_DESCRIPTION,label);        
    }
    
    private void createJpeg(String location, String format, LWMap currentMap, Dimension size)
    {     
        BufferedImage mapImage = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_RGB);
        this.map = currentMap;
        Graphics2D g = (Graphics2D) mapImage.getGraphics();
        
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, size.width, size.height);
        g.setColor(Color.BLACK);
        g.drawRect(0, 0, size.width-1, size.height-1);
             
        g.translate(-xOffset, -yOffset);
        g.setClip(0, 0, size.width, size.height);
            
        DrawContext dc = new DrawContext(g, scale);
        dc.setPrinting(true);
        // render the map
        map.draw(dc);
           
        
        try
        {
            System.out.println(location);
            ImageIO.write(mapImage, format, new File(location));
        }
        catch (Exception e)
        {
            System.out.println("Couldn't write to the file:" + e);
        }
    }
    
    public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
       System.out.println("Performing Conversion for ImageMap:" + actionEvent.getActionCommand());
        
       try 
       {
           File selectedFile = ActionUtil.selectFile("Saving Imap", "imap");
           
           if (selectedFile != null)
             createImageMap(selectedFile);
       }
        
       catch(Exception ex) 
       {
            System.out.println("Couldn't convert to IMAP:" + ex);
       }   
    }

    public void createImageMap(File file)
    {
        LWMap currentMap = VUE.getActiveMap();
        
        Rectangle2D bounds = currentMap.getBounds();
        xOffset = (int)bounds.getX(); 
        yOffset = (int)bounds.getY();
        Dimension size = new Dimension((int)bounds.getWidth(), (int)bounds.getHeight());
            
        /**
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Save Image Map File");
            if(VueUtil.isCurrentDirectoryPathSet()) 
                chooser.setCurrentDirectory(new File(VueUtil.getCurrentDirectoryPath()));  
                
            int option = chooser.showDialog(tufts.vue.VUE.frame, "Save");
                
            if (option == JFileChooser.APPROVE_OPTION) {
                String fileName = chooser.getSelectedFile().getAbsolutePath();
                String imageLocation = fileName; 
                String imageName = chooser.getSelectedFile().getName();
                if(imageName.endsWith(".html")){
                    imageName = imageName.substring(0, imageName.length()-5)+".jpeg";
                    imageLocation = imageLocation.substring(0, imageLocation.length()-5)+".jpeg";
                }else{
                    imageName += ".jpeg";
                    imageLocation += ".jpeg";
                    fileName += ".html";
                }
                createJpeg(imageLocation, "jpeg", currentMap, size);
                createHtml(imageName, fileName, currentMap, size);
            }
           **/
        
       String imageLocation = file.getAbsolutePath().substring(0, file.getAbsolutePath().length()-5)+".jpeg";
       String imageName = file.getName().substring(0, file.getName().length()-5)+".jpeg";
       String fileName = file.getAbsolutePath().substring(0, file.getAbsolutePath().length()-5)+".html";
       
       createJpeg(imageLocation, "jpeg", currentMap, size);
       createHtml(imageName, fileName, currentMap, size);
    }
    
    private String computeImageMapArea(LWContainer container)
    {
        String out = new String();
         
        java.util.Iterator iter =  container.getNodeIterator();
        
        while(iter.hasNext())
        {
            LWNode node = (LWNode)iter.next();
            
            if (node.hasChildren())
              out += computeImageMapArea((LWContainer)node);
            
            String shape = "rect";
            String altLabel = null;
            
            if ((altLabel = node.getNotes()) == null)
              altLabel = "No Notes";
            
            String res = "";
            int ox = (int)node.getX() -  xOffset;
            int oy = (int)node.getY() -  yOffset;
            int ow = (int)node.getWidth();
            int oh = (int)node.getHeight();
            
            String href = "";
            
            if(node.getResource() != null){
                Resource resource = node.getResource();
                res = resource.toString();
                if(!res.startsWith("http://")) res = "file:///" + res;
            } 
            else res = "null";
            
            if(res.equals("null")) href = "nohref";
            else href = "href=\"" + res + "\"";
            
            out += "<area " + href
                +" alt=\""+ altLabel
                +"\" shape=\""+shape
                +"\" coords=\""+ox
                +","+oy
                +","+(ox + ow)
                +","+(oy + oh)
                +"\">\n\n";
        }
         
        return out;
    }
    
    private void createHtml(String imageName, String fileName, LWMap currentMap, Dimension size){
        
        String out = "<HTML><HEAD><TITLE>" + currentMap.getLabel();
        out += "</TITLE></HEAD><BODY>";
        out += "<img src=\""+imageName
            +"\" border=0 usemap=\"#map\" HEIGHT="+size.height+" WIDTH="+size.width+">";
        out += "<map name=\"map\">\n";

        out += computeImageMapArea(currentMap);
        
        out += "\n</map></BODY></HTML>"; 
        
        //write out to the selected file
        try{
            FileWriter output = new FileWriter(fileName);
            output.write(out);
            output.close();
            System.out.println("wrote to the file...");
        }
        catch(IOException ioe){
            System.out.println("Error trying to write to html file: " + ioe);
        }    
    }
}
