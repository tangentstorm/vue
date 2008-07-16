/*
 * Copyright 2003-2008 Tufts University  Licensed under the
 * Educational Community License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 * 
 * http://www.osedu.org/licenses/ECL-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package tufts.vue;

import javax.swing.*;
import java.awt.event.*;
import java.awt.*;

import static edu.tufts.vue.ui.ConfigurationUI.*;

public class EditLibraryPanel extends JPanel implements ActionListener
{
    private static final org.apache.log4j.Logger Log = org.apache.log4j.Logger.getLogger(EditLibraryPanel.class);
    
    private final JButton updateButton = new JButton("Save");
    private final edu.tufts.vue.dsm.DataSource dataSource;
    private final tufts.vue.DataSource oldStyleDataSource;
    private final DataSourceViewer dsv;
    private edu.tufts.vue.ui.ConfigurationUI cui;
	
    public EditLibraryPanel(DataSourceViewer dsv, edu.tufts.vue.dsm.DataSource dataSource)
    {
        this.dsv = dsv;
        this.dataSource = dataSource;
        this.oldStyleDataSource = null;
            
        try {
            final String xml = dataSource.getConfigurationUIHints();

            if (DEBUG.DR) Log.debug("OSID-XML: " + xml);
                
            cui = new edu.tufts.vue.ui.ConfigurationUI(new java.io.ByteArrayInputStream(xml.getBytes()));
            cui.setProperties(dataSource.getConfiguration());
                
            updateButton.addActionListener(this);
                        
            layoutConfig();
                
        } catch (Throwable t) {
            Log.error("init", t);
        }
    }
    
    public EditLibraryPanel(DataSourceViewer dsv, tufts.vue.DataSource dataSource)
    {
        this.dsv = dsv;
        this.dataSource = null;
        this.oldStyleDataSource = dataSource;
                        
        try {
            final String xml = getXMLforOldStyleDataSource(dataSource);
            
            if (DEBUG.DR) Log.debug("VUE-XML: " + xml);
            
            cui = new edu.tufts.vue.ui.ConfigurationUI(new java.io.ByteArrayInputStream(xml.getBytes()));
            
            updateButton.addActionListener(this);

            layoutConfig();
                        
        } catch (Throwable t) {
            Log.error("init", t);
        }
    }
	

    private String getXMLforOldStyleDataSource(tufts.vue.DataSource dataSource)
    {
        // use canned configurations

        final StringBuffer b = new StringBuffer();
        final String name = dataSource.getDisplayName();
        //final String address = dataSource.getAddress();
            
        b.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        b.append("<configuration>\n");

        addField(b, "name", "Display Name", "Name for this data source", name, SINGLE_LINE_CLEAR_TEXT_CONTROL, 0);
                            
        if (dataSource instanceof LocalFileDataSource) {

            addField(b, "address",
                     "Starting Path",
                     "The path to start from",
                     dataSource.getAddress(),
                     FILECHOOSER_CONTROL,
                     0);
            
        } else if (dataSource instanceof FavoritesDataSource) {

            // nothing to add: just uses the name
            
        } else if (dataSource instanceof RemoteFileDataSource) {

            final RemoteFileDataSource ds = (RemoteFileDataSource) dataSource;
            
            addField(b, "address", "Address", "FTP Address", ds.getAddress(), SINGLE_LINE_CLEAR_TEXT_CONTROL, 0);
            addField(b, "username", "Username", "FTP site username", ds.getUserName(), SINGLE_LINE_CLEAR_TEXT_CONTROL, 16);
            addField(b, "pasword", "Password", "FTP site password", ds.getPassword(), SINGLE_LINE_MASKED_TEXT_CONTROL, 16);
            
            
        } else if (dataSource instanceof edu.tufts.vue.rss.RSSDataSource) {
                            
            //final edu.tufts.vue.rss.RSSDataSource ds = (edu.tufts.vue.rss.RSSDataSource) dataSource;
            
            addField(b, "address", "Address", "RSS Feed URL", dataSource.getAddress(), SINGLE_LINE_CLEAR_TEXT_CONTROL, 0);

            if (DEBUG.Enabled)
            addField(b,
                     edu.tufts.vue.rss.RSSDataSource.AUTHORIZATION_COOKIE_KEY,
                     "Authorization",
                     "Any required authorization cookie",
                     edu.tufts.vue.rss.RSSDataSource.DEFAULT_AUTHORIZATION_COOKIE,
                     SINGLE_LINE_CLEAR_TEXT_CONTROL,
                     0);
                     
            
//             // Extra Fields
//             b.append(ds.getConfigurationUI_XML_Fields());
//             b.append('\n');


        }

        b.append("</configuration>");
        
        return b.toString();
    }

    private void addField(StringBuffer b, String key, String title, String description, String value, int uiControl, int max)
    {
        b.append("<field>");

        b.append("<key>" + key + "</key>");
        b.append("<title>" + title + "</title>");
        b.append("<description>" + description + "</description>");

        b.append("<default>");
        if (value != null)
            b.append(org.apache.commons.lang.StringEscapeUtils.escapeXml(value));
        b.append("</default>");
        
        b.append("<mandatory>true</mandatory>"); // currently required as input, but ignored for effect by ConfigurationUI impl
        
        b.append("<maxChars>");
        b.append(max);
        b.append("</maxChars>");
        
        b.append("<ui>" + uiControl + "</ui>");
        
        b.append("</field>\n");
    }

    private void layoutConfig()
    {
        if (DEBUG.BOXES) {
            cui.setBorder(BorderFactory.createLineBorder(Color.green));
            this.setBorder(BorderFactory.createLineBorder(Color.red));
        }

        final GridBagConstraints gbc = new GridBagConstraints();
        final GridBagLayout gridbag = new GridBagLayout();
                            
        setLayout(gridbag);
                            
        // Set up common GBC config:

        gbc.insets = (Insets) tufts.vue.gui.GUI.WidgetInsets.clone();
        gbc.insets.bottom = 0;
        gbc.weightx = 1;
        gbc.weighty = 0;
                            
        //-------------------------------------------------------
        // Add ConfigurationUI
        //-------------------------------------------------------
        
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.gridy = 0;

        add(cui, gbc);
                            
        //-------------------------------------------------------
        // Add Save button
        //-------------------------------------------------------
        
        gbc.gridwidth = 1;
        gbc.gridy = 1;
        gbc.ipadx = 15; // this actually makes the button wider
        gbc.anchor = GridBagConstraints.NORTHEAST;
        gbc.fill = GridBagConstraints.NONE;
                            
        add(updateButton, gbc);

        //-------------------------------------------------------
        // Add a default vertical expander so above content
        // will float to top.
        //-------------------------------------------------------
        
        gbc.ipadx = 0;
        gbc.gridy = 2;
        gbc.weighty = 1; // this is the key for the expander to work (non-zero y-weight)
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridheight = GridBagConstraints.REMAINDER;
        gbc.gridwidth = 0;
        
        final JComponent fill;

        if (DEBUG.BOXES) {
            fill = new JLabel("fill", JLabel.CENTER);
            fill.setBackground(Color.gray);
            fill.setOpaque(true);
        } else {
            fill = new JPanel();
        }
        
        add(fill, gbc);
    }
                            
	
    public void actionPerformed(ActionEvent ae)
    {
        try {
            if (ae.getSource() instanceof JButton) {
                if (this.dataSource != null) {
                    this.dataSource.setConfiguration(cui.getProperties());
                    this.dsv.setActiveDataSource(this.dataSource); // refresh
                    edu.tufts.vue.dsm.DataSourceManager dsm = edu.tufts.vue.dsm.impl.VueDataSourceManager.getInstance();
                    dsm.save();					
                } else if (this.oldStyleDataSource != null) {
                    this.oldStyleDataSource.setConfiguration(cui.getProperties());
                    this.dsv.setActiveDataSource(this.oldStyleDataSource); // refresh
                }
            }
            DataSourceViewer.saveDataSourceViewer();
        } catch (Throwable t) {
            t.printStackTrace();
            VueUtil.alert("Configuration error: "+ t.getMessage(),"Error");
        }
    }
}


