/*
 * -----------------------------------------------------------------------------
 *
 * <p><b>License and Copyright: </b>The contents of this file are subject to the
 * Mozilla Public License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License
 * at <a href="http://www.mozilla.org/MPL">http://www.mozilla.org/MPL/.</a></p>
 *
 * <p>Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.</p>
 *
 * <p>The entire file consists of original code.  Copyright &copy; 2003, 2004 
 * Tufts University. All rights reserved.</p>
 *
 * -----------------------------------------------------------------------------
 */

package tufts.vue.action;

import javax.imageio.*;
import java.awt.image.*;
import java.io.*;
import java.awt.*;
import javax.swing.*;
import java.awt.geom.Rectangle2D;
import tufts.vue.*;

/**
 * @version $Revision: 1.9 $ / $Date: 2006-01-20 17:46:54 $ / $Author: sfraize $ *
 * @author  Jay Briedis
 */
public class ImageMap extends VueAction {
    
    private int xOffset, yOffset;
   
    /** Creates a new instance of ImageConversion */
    public ImageMap() {}
    
    public ImageMap(String label) {
        super(label);
    }
    
    public void act()
    {
        File selectedFile = ActionUtil.selectFile("Saving Imap", "imap");
        
        if (selectedFile != null)
            createImageMap(selectedFile);
    }

    public void createImageMap(File file)
    {    
       String imageLocation = file.getAbsolutePath().substring(0, file.getAbsolutePath().length()-5)+".jpeg";
       String imageName = file.getName().substring(0, file.getName().length()-5)+".jpeg";
       String fileName = file.getAbsolutePath().substring(0, file.getAbsolutePath().length()-5)+".html";
       
       //createJpeg(imageLocation, "jpeg", currentMap, size);
       ImageConversion.createActiveMapJpeg(new File(imageLocation));
       createHtml(imageName, fileName);
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
            else href = "href=\"" + res + "\" target=\"_blank\"";
            
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
    
    private void createHtml(String imageName, String fileName){
        
        LWMap currentMap = VUE.getActiveMap();   
        Rectangle2D bounds = currentMap.getBounds();
        xOffset = (int)bounds.getX(); 
        yOffset = (int)bounds.getY();
        Dimension size = new Dimension((int)bounds.getWidth(), (int)bounds.getHeight());
        
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
