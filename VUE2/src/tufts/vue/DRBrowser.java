package tufts.vue;

import javax.swing.border.TitledBorder;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import osid.dr.*;
import osid.OsidException;
import tufts.oki.dr.fedora.*;
import java.util.Vector;
import java.util.ArrayList;
import java.io.*;




import fedora.server.types.gen.*;
import fedora.server.utilities.DateUtility;

/**
 * Digital Repositor Browser
 */
class DRBrowser extends JPanel {
    JPanel fedoraSearchResults;
    JTabbedPane fedoraPane;
    JTextField keywords;
    JComboBox maxReturns;
    DigitalRepository dr;
    osid.shared.Type assetType;
    public static DataSourceViewer dsViewer = null;
   

   
    public DRBrowser()
    {
       
        setLayout(new BorderLayout());
        dsViewer = new DataSourceViewer(this);
        dsViewer.setName("Data Source Viewer"); 
          
          add(dsViewer,BorderLayout.NORTH);
    }


       
    
}
