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

/*
 * SpashScreen.java
 *
 * Created on February 28, 2004, 6:28 PM
 */

package tufts.vue;

/**
 *
 * @author  akumar03
 */

import java.awt.*;
import javax.swing.*;

public class SplashScreen extends JWindow {
    
    /** Creates a new instance of SpashScreen */
    //public final int width = 424;
    //public final int height = 291;
    public final long sleepTime = 5000;
    public SplashScreen() {
        super();
        createSplash();
        getContentPane().setBackground(Color.BLACK);
    }
    
    private void createSplash() {
        Dimension screen =  Toolkit.getDefaultToolkit().getScreenSize();
        ImageIcon icon = VueResources.getImageIcon("splashScreen");
        final int width = icon.getIconWidth();
        final int height = icon.getIconHeight();
        int x = (screen.width - width)/2;
        int y = (screen.height - height)/2;
        this.setBounds(x, y, width, height);
        JLabel logoLabel = new JLabel(icon);
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(logoLabel,BorderLayout.CENTER);
        this.setVisible(true);
        System.out.println("SplashScreen: visible");
        try {
          //  Thread.sleep(sleepTime);
        } catch(Exception ex) {}
        //this.setVisible(false);
    }
    
}
