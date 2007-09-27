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
 * Publisher.java
 *
 * Created on January 7, 2004, 10:09 PM
 */

package tufts.vue;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.border.LineBorder;
import java.util.Vector;
import java.util.Iterator;
import javax.swing.table.*;
import java.io.*;
import java.net.*;
import org.apache.commons.net.ftp.*;
import java.util.*;

import fedora.server.management.FedoraAPIM;
import fedora.server.utilities.StreamUtility;
//import fedora.client.ingest.AutoIngestor;

import tufts.vue.action.*;

//required for publishing to Fedora

import fedora.client.FedoraClient;
import fedora.client.utility.ingest.AutoIngestor;
import fedora.client.utility.AutoFinder;
import fedora.server.types.gen.Datastream;
import fedora.client.Uploader;
/**
 *
 * @author  akumar03
 * @version $Revision: 1.49 $ / $Date: 2007-09-27 17:37:52 $ / $Author: anoop $
 */
public class Publisher extends JDialog implements ActionListener,tufts.vue.DublinCoreConstants   {
    
    /** Creates a new instance of Publisher */
    //todo: Create an interface for datasources and have separate implementations for each type of datasource.
    
    public static final String FILE_PREFIX = "file://";
    public static final int SELECTION_COL = 0; // boolean selection column in resource table
    public static final int RESOURCE_COL = 1; // column that displays the name of resource
    public static final int SIZE_COL = 2; // the column that displays size of files.
    public static final int STATUS_COL = 3;// the column that displays the status of objects that will be ingested.
    public static final int X_LOCATION = 300; // x co-ordinate of location where the publisher appears
    public static final int Y_LOCATION = 300; // y co-ordinate of location where the publisher appears
    public static final int PUB_WIDTH = 500;
    public static final int PUB_HEIGHT = 250;
    public static final String[] PUBLISH_INFORMATION = {"The �Export� function allows a user to deposit a concept map into a registered digital repository. Select the different modes to learn more.",
    "�Publish Map� saves only the map. Digital resources are not attached, but the resources� paths are maintained. �Export Map� is the equivalent of the �Save� function for a registered digital repository.",
    "�Publish IMSCP Map� embeds digital resources within the map. The resources are accessible to all users viewing the map. This mode creates a �zip� file, which can be uploaded to a registered digital repository or saved locally. VUE can open zip files it originally created. (IMSCP: Instructional Management Services Content Package.)",
    "�Publish All� creates a duplicate of all digital resources and uploads these resources and the map to a registered digital repository. The resources are accessible to all users viewing the map.",
    "�Publish IMSCP Map to Sakai� saves concept map in Sakai content hosting system.","Zips map with local resources."
    };
    
    public static final String[] MODE_LABELS = {"Map only","Map and resources","Zip bundle"};
    
    
    public static final String NEXT = "Next";
    public static final String CANCEL = "Cancel";
    public static final String PUBLISH = "Publish";
    private int publishMode = Publishable.PUBLISH_MAP;
    
    private int stage; // keep tracks of the screen
    private String IMSManifest; // the string is written to manifest file;
    private static final  String VUE_MIME_TYPE = VueResources.getString("vue.type");
    private static final  String BINARY_MIME_TYPE = "application/binary";
    private static final  String ZIP_MIME_TYPE = "application/zip";
    
    private static final String IMSCP_MANIFEST_ORGANIZATION = "%organization%";
    private static final String IMSCP_MANIFEST_METADATA = "%metadata%";
    private static final String IMSCP_MANIFEST_RESOURCES = "%resources%";
    
    
    private org.osid.shared.Type sakaiRepositoryType = new edu.tufts.vue.util.Type("sakaiproject.org","repository","contentHosting");
    private org.osid.shared.Type fedoraRepositoryType = new edu.tufts.vue.util.Type("tufts.edu","repository","fedora_2_2");
    
    
    int count = 0;
    
    JPanel modeSelectionPanel;
    JPanel resourceSelectionPanel;
    
    JButton backButton;
    JButton finishButton;
    JRadioButton publishMapRButton ;
    JRadioButton    publishMapAllRButton ;
    JRadioButton    publishZipRButton ;
    
    // JRadioButton publishCMapRButton;
    JRadioButton publishSakaiRButton;
    
    // JRadioButton publishAllRButton;
    JTextArea informationArea;
    JPanel buttonPanel;
    JTextArea modeInfo  = new JTextArea(PUBLISH_INFORMATION[0]);
    JPanel rPanel  = new JPanel();
    JPanel mPanel  = new JPanel();
    JButton nextButton = new JButton(NEXT);
    JButton cancelButton = new JButton(CANCEL);
    JButton publishButton = new JButton(PUBLISH);
    //JButton modeNext = new JButton("Next");
    JButton modeCancel = new JButton("Cancel");
    public static Vector resourceVector;
    File activeMapFile;
    public static ResourceTableModel resourceTableModel;
    public static JTable resourceTable;
    JComboBox dataSourceComboBox;
    
    ButtonGroup modeButtons = new ButtonGroup();
    java.util.List<JRadioButton> modeRadioButtons;
    JList repList;
    public Publisher(Frame owner,String title) {
        //testing
        super(owner,title,true);
        initialize();
    }
    private void initialize() {
        
        finishButton = new JButton("Finish");
        backButton = new JButton("< Back");
        cancelButton.addActionListener(this);
        finishButton.addActionListener(this);
        nextButton.addActionListener(this);
        backButton.addActionListener(this);
        cancelButton.addActionListener(this);
        //modeNext.addActionListener(this);
        publishButton.addActionListener(this);
        //setUpModeSelectionPanel();
        setUpRepositorySelectionPanel();
        getContentPane().add(rPanel,BorderLayout.CENTER);
        // adding the buttonPanel
        buttonPanel = new JPanel();
        buttonPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(7,7,7,7),BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(1,0,0,0,Color.DARK_GRAY),BorderFactory.createEmptyBorder(5,0,0,0))));
        buttonPanel.setLayout(new BorderLayout());
        buttonPanel.add(cancelButton,BorderLayout.WEST);
        buttonPanel.add(nextButton,BorderLayout.EAST);
        getContentPane().add(buttonPanel,BorderLayout.SOUTH);
        stage = 1;
        setLocation(X_LOCATION,Y_LOCATION);
        setModal(true);
        setSize(PUB_WIDTH, PUB_HEIGHT);
        setResizable(false);
        setVisible(true);
    }
    
    private void setUpRepositorySelectionPanel() {
        rPanel.setLayout(new BorderLayout());
        JLabel repositoryLabel = new JLabel("Select a FEDORA Instance");
        repositoryLabel.setBorder(BorderFactory.createEmptyBorder(15,10,0,0));
        rPanel.add(repositoryLabel,BorderLayout.NORTH);
        //adding the repository list
        //TODO: Populate this with actual repositories
        repList = new JList();
        repList.setModel(new DatasourceListModel());
        repList.setCellRenderer(new DatasourceListCellRenderer());
        JScrollPane repPane = new JScrollPane(repList);
        JPanel scrollPanel = new JPanel(new BorderLayout());
        scrollPanel.add(repPane);
        scrollPanel.setBorder(BorderFactory.createEmptyBorder(10,20,10,20));
        rPanel.add(scrollPanel,BorderLayout.CENTER);
    }
    
    private void setUpModeSelectionPanel() {
        //System.out.println("Counter: "+count);
        //count++;
        SpringLayout layout = new SpringLayout();
        
        // adding the modes
        JPanel mainPanel = new JPanel();
        
        //mainPanel.setLayout(new BoxLayout(mainPanel,BoxLayout.X_AXIS));
        mainPanel.setLayout(layout);
        JLabel publishLabel = new JLabel("Publish as");
        publishLabel.setBorder(BorderFactory.createEmptyBorder(10,10,0,0));
        mainPanel.add(publishLabel);
        
        //adding the option Panel
        JPanel optionPanel = new JPanel(new GridLayout(0, 1));
        //optionPanel.setLayout(new BoxLayout(optionPanel,BoxLayout.Y_AXIS));
        publishMapRButton = new JRadioButton(MODE_LABELS[0]);
        publishMapAllRButton = new JRadioButton(MODE_LABELS[1]);
        publishZipRButton = new JRadioButton(MODE_LABELS[2]);
        publishZipRButton.setEnabled(false);
        
        modeButtons.add(publishMapRButton);
        modeButtons.add(publishMapAllRButton);
        modeButtons.add(publishZipRButton);
        optionPanel.add(publishMapRButton);
        
        
        optionPanel.add(publishMapAllRButton);
        optionPanel.add(publishZipRButton);
        
        publishMapRButton.addActionListener(this);
        publishMapAllRButton.addActionListener(this);
        
        //   modeButtons.setSelected(radioButtons.get(0).getModel(),true);
        optionPanel.setBorder(BorderFactory.createEmptyBorder(10,5,0,0));
        optionPanel.validate();
        mainPanel.add(optionPanel);
        
        // addding modeInfo
        modeInfo = new JTextArea(PUBLISH_INFORMATION[0]);
        modeInfo.setEditable(false);
        modeInfo.setLineWrap(true);
        modeInfo.setWrapStyleWord(true);
        modeInfo.setRows(7);
        modeInfo.setColumns(30);
        modeInfo.setVisible(true);
        //modeInfo.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        modeInfo.setBorder( BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.DARK_GRAY),BorderFactory.createEmptyBorder(5,5,5,5)));
        mainPanel.add(modeInfo);
        mainPanel.validate();
        
        // setting up cnstraints
        layout.putConstraint(SpringLayout.WEST, publishLabel,10,SpringLayout.WEST, mainPanel);
        layout.putConstraint(SpringLayout.WEST, optionPanel,3,SpringLayout.EAST, publishLabel);
        layout.putConstraint(SpringLayout.WEST,modeInfo,25,SpringLayout.EAST, optionPanel);
        layout.putConstraint(SpringLayout.NORTH, publishLabel,10,SpringLayout.NORTH, mainPanel);
        layout.putConstraint(SpringLayout.NORTH, optionPanel,10,SpringLayout.NORTH, mainPanel);
        layout.putConstraint(SpringLayout.NORTH, modeInfo,20,SpringLayout.NORTH, mainPanel);
        mPanel.setLayout(new BorderLayout());
        mPanel.add(mainPanel,BorderLayout.CENTER);
        
        // Removing next button and adding publish button
        buttonPanel.remove(nextButton);
        buttonPanel.add(publishButton,BorderLayout.EAST);
    }
    
    
    
    
    
    private void  setUpResourceSelectionPanel() {
        resourceSelectionPanel = new JPanel();
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        resourceSelectionPanel.setLayout(gridbag);
        Insets defaultInsets = new Insets(2,2,2,2);
        
        
        JPanel bottomPanel = new JPanel();
        // bottomPanel.setBorder(new LineBorder(Color.BLACK));
        bottomPanel.add(backButton);
        bottomPanel.add(finishButton);
        bottomPanel.add(cancelButton);
        finishButton.setEnabled(true);
        
        
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 6;
        c.insets = defaultInsets;
        c.anchor = GridBagConstraints.WEST;
        JLabel topLabel = new JLabel("The following objects will be published with the map:");
        gridbag.setConstraints(topLabel,c);
        resourceSelectionPanel.add(topLabel);
        
        c.gridx =0;
        c.gridy =1;
        c.gridheight = 2;
        JPanel resourceListPanel = new JPanel();
        JComponent resourceListPane = getResourceListPane();
        resourceListPanel.add(resourceListPane);
        gridbag.setConstraints(resourceListPanel,c);
        resourceSelectionPanel.add(resourceListPanel);
        
        c.gridy = 3;
        c.anchor = GridBagConstraints.EAST;
        gridbag.setConstraints(bottomPanel, c);
        resourceSelectionPanel.add(bottomPanel);
        
        // c.insets = new Insets(2, 60,2, 2);
        
    }
    
    private JComponent getResourceListPane() {
        Vector columnNamesVector = new Vector();
        columnNamesVector.add("Selection");
        columnNamesVector.add("Display Name");
        columnNamesVector.add("Size ");
        columnNamesVector.add("Status");
        resourceVector = new Vector();
        setLocalResourceVector(resourceVector,VUE.getActiveMap());
        resourceTableModel = new ResourceTableModel(resourceVector, columnNamesVector);
        resourceTable = new JTable(resourceTableModel);
        
        // setting the cell sizes
        TableColumn column = null;
        Component comp = null;
        int headerWidth;
        int cellWidth;
        TableCellRenderer headerRenderer = resourceTable.getTableHeader().getDefaultRenderer();
        resourceTable.getColumnModel().getColumn(0).setPreferredWidth(12);
        for (int i = 0; i < 4; i++) {
            column = resourceTable.getColumnModel().getColumn(i);
            comp = headerRenderer.getTableCellRendererComponent(null, column.getHeaderValue(),false, false, 0, 0);
            headerWidth = comp.getPreferredSize().width;
            cellWidth = resourceTableModel.longValues[i].length();
            column.setPreferredWidth(Math.max(headerWidth, cellWidth));
        }
        resourceTable.setPreferredScrollableViewportSize(new Dimension(450,110));
        return new JScrollPane(resourceTable);
    }
    
    
    private void setLocalResourceVector(Vector vector,LWContainer map) {
        Iterator i = map.getAllDescendents(LWComponent.ChildKind.PROPER).iterator();
        while(i.hasNext()) {
            LWComponent component = (LWComponent) i.next();
            System.out.println("Component:"+component+" has resource:"+component.hasResource());
            if(component.hasResource() && (component.getResource() instanceof URLResource)){
                
                URLResource resource = (URLResource) component.getResource();
                System.out.println("Component:"+component+"file:" +resource.getSpec()+" has file:"+resource.getSpec().startsWith(FILE_PREFIX));
                
                //   if(resource.getType() == Resource.URL) {
                try {
                    // File file = new File(new URL(resource.getSpec()).getFile());
                    if(resource.isLocalFile()) {
                        File file = new File(resource.getSpec());
                        System.out.println("LWComponent:"+component.getLabel() + "Resource: "+resource.getSpec()+"File:"+file+" exists:"+file.exists());
                        Vector row = new Vector();
                        row.add(new Boolean(true));
                        row.add(resource);
                        row.add(new Long(file.length()));
                        row.add("Ready");
                        vector.add(row);
                    }
                }catch (Exception ex) {
                    System.out.println("Publisher.setLocalResourceVector: Resource "+resource.getSpec()+ ex);
                    ex.printStackTrace();
                }
                
            }
            
        }
    }
    
    
    
    public void actionPerformed(ActionEvent e) {
        if(e.getActionCommand().equals(CANCEL)) {
            this.dispose();
        }else if(e.getActionCommand().equals(NEXT)) {
            getContentPane().remove(rPanel);
            //validateTree();
            setUpModeSelectionPanel();
            getContentPane().add(mPanel, BorderLayout.CENTER);
            getContentPane().validate();
            validateTree();
        }else if(e.getActionCommand().equals(PUBLISH)) {
            
            System.out.println("Publishing Map to Default Fedora Repository");
            publishActiveMapToVUEDL();
        }
        if(e.getActionCommand().equals(MODE_LABELS[0])){
            modeInfo.setText(PUBLISH_INFORMATION[1]);
        }
        if(e.getActionCommand().equals(MODE_LABELS[1])){
            modeInfo.setText(PUBLISH_INFORMATION[3]);
        }
        
    }
    private void   publishActiveMapToVUEDL() {
        //      System.out.println("Button: "+ publishMapRButton.getActionCommand()+"class:"+publishMapRButton+" is selected: "+publishMapRButton.isSelected()+" Mode:"+modeButtons.getSelection());
        //        System.out.println("Button: "+ publishMapAllRButton.getActionCommand()+"class:"+publishMapAllRButton+" is selected: "+publishMapAllRButton.isSelected());
        //System.out.println("Button: "+ publishZipRButton.getActionCommand()+"class:"+publishZipRButton+" is selected: "+publishZipRButton.isSelected());
        
        if(publishMapRButton.isSelected())
            FedoraPublisher.uploadMap("https", "vue-dl.tccs.tufts.edu", 8443, "fedoraAdmin", "vuefedora",VUE.getActiveMap());
        else if(publishMapAllRButton.isSelected())
            FedoraPublisher.uploadMapAll("https", "vue-dl.tccs.tufts.edu", 8443, "fedoraAdmin", "vuefedora",VUE.getActiveMap());
        else
            alert(VUE.getDialogParent(), "Publish mode not yet supported", "Mode Not Suported");
        
        System.out.println("Selected"+ repList.getSelectedValue()+ " Class of selected:"+repList.getSelectedValue().getClass());
        
    }
    
    
    
    private void alert(Component parentComponent,String message,String title) {
        javax.swing.JOptionPane.showMessageDialog(parentComponent,message,title,javax.swing.JOptionPane.ERROR_MESSAGE);
    }
    
    
    
    class DatasourceListCellRenderer extends   DefaultListCellRenderer  {
        
        private JPanel composite = new JPanel(new BorderLayout());
        private JRadioButton radioButton;
        
        public Component getListCellRendererComponent(
                JList list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel)super.getListCellRendererComponent(
                    list, (value instanceof edu.tufts.vue.dsm.DataSource? ((edu.tufts.vue.dsm.DataSource)value).getRepositoryDisplayName():value) , index, isSelected, cellHasFocus);
            //label.setOpaque(false);
            label.setBackground(Color.WHITE);
            label.setBorder(null);
            if(isSelected) {
                label.setForeground(Color.BLACK);
            } else {
                label.setForeground(Color.DARK_GRAY);
            }
            if (composite.getComponentCount() == 0) {
                radioButton = new JRadioButton();
                radioButton.setBackground(Color.WHITE);
                composite.add(label, BorderLayout.CENTER);
                composite.add(radioButton, BorderLayout.WEST);
            }
            radioButton.setSelected(isSelected);
            return composite;
        }
    }
    class DatasourceListModel extends DefaultListModel {
        
        edu.tufts.vue.dsm.DataSource[] datasources;
        public DatasourceListModel() {
            datasources = edu.tufts.vue.dsm.impl.VueDataSourceManager.getInstance().getDataSources();
            // check for fedora
        }
        public Object getElementAt(int index) {
            try {
                
                System.out.println("Datesource Name:"+datasources[index].getRepository().getDisplayName()+" Type:"+datasources[index].getRepository().getType().getAuthority()+" keyword:"+datasources[index].getRepository().getType().getKeyword());
            } catch(org.osid.repository.RepositoryException ex) {
                ex.printStackTrace();
            }
            return(getResources(fedoraRepositoryType).get(index));
        }
        
        public int getSize() {
            return getResources(fedoraRepositoryType).size();
             
        }
        
        private java.util.List<edu.tufts.vue.dsm.DataSource> getResources(org.osid.shared.Type type) {
            java.util.List<edu.tufts.vue.dsm.DataSource> resourceList = new ArrayList<edu.tufts.vue.dsm.DataSource>();
            for(int i=0;i<datasources.length;i++) {
                try {
                    if (datasources[i].getRepository().getType().isEqual(type)) {
                        resourceList.add(datasources[i]);
                    }
                } catch(org.osid.repository.RepositoryException ex) {
                    ex.printStackTrace();
                }
            }
            return resourceList;
        }
    }
    public class ResourceTableModel  extends AbstractTableModel {
        
        public final String[] longValues = {"Selection", "12345678901234567890123456789012345678901234567890","12356789","Processing...."};
        Vector data;
        Vector columnNames;
        
        public ResourceTableModel(Vector data,Vector columnNames) {
            super();
            
            this.data = data;
            this.columnNames = columnNames;
        }
        
        
        public int getColumnCount() {
            return columnNames.size();
        }
        
        public int getRowCount() {
            return data.size();
        }
        
        public String getColumnName(int col) {
            return (String)columnNames.elementAt(col);
        }
        
        
        public Object getValueAt(int row, int col) {
            return ((Vector)data.elementAt(row)).elementAt(col);
        }
        
        public Class getColumnClass(int c) {
            return getValueAt(0, c).getClass();
        }
        
        /*
         * Don't need to implement this method unless your table's
         * editable.
         */
        public boolean isCellEditable(int row, int col) {
            //Note that the data/cell address is constant,
            //no matter where the cell appears onscreen.
            if (col > 1) {
                return false;
            } else {
                return true;
            }
        }
        
        /*
         * Don't need to implement this method unless your table's
         * data can change.
         */
        
        public void setValueAt(Object value, int row, int col) {
            ((Vector)data.elementAt(row)).setElementAt(value,col);
            fireTableCellUpdated(row, col);
        }
    }
    
}

