package tufts.vue;

import javax.swing.JFrame;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.JPopupMenu;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseEvent;
import javax.swing.Action;
import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import javax.swing.AbstractButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.*;
import java.util.Vector;
import java.util.Enumeration;
import java.io.*;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.tree.TreePath;

// castor classes
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;

import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.mapping.MappingException;
import org.xml.sax.InputSource;
import osid.dr.*;
/**
 *
 * @author  Ranjani Saigal
 */
public class FavoritesWindow extends JPanel implements ActionListener, ItemListener 
{
    private DisplayAction displayAction = null;
    private VueDandDTree favoritesTree ;
    private JScrollPane browsePane;
    final static String XML_MAPPING = "lw_mapping.xml";
    
    /** Creates a new instance of HierarchyTreeWindow */
    public FavoritesWindow(String displayName ) 
    {
        setLayout(new BorderLayout());
        //super(displayName);
        //setSize(300, 400); 
       JTabbedPane favoritesPane = new JTabbedPane();
       
      //   TreeModel testModel = favoritesTree.getModel();
        //JTree sertree = new JTree();
        // sertree.setModel(testModel);
        FavoritesNode browseRoot = new FavoritesNode("Bookmarks");
 
         favoritesTree = new VueDandDTree(browseRoot);
          
       
        JPanel searchResultsPane = new JPanel();
        FavSearchPanel favSearchPanel = new FavSearchPanel(favoritesTree,favoritesPane,searchResultsPane);
            
         favoritesPane.add("Search",favSearchPanel);
         
          
         
        favoritesPane.add("Search Results",searchResultsPane);
       
       
       
          browsePane = new JScrollPane(favoritesTree);
         favoritesPane.add("Browse", browsePane); 
          
        
         
          
          createPopupMenu();
        
         add(favoritesPane,BorderLayout.CENTER);
         // pack();
          //show();

       
//---------------------------------------------closing the window businees
        
    }
    
  
    public Action getDisplayAction()
    {
        if (displayAction == null)
            displayAction = new DisplayAction("My Favorites");
        
        return (Action)displayAction;
    }
    
 
    
    private class DisplayAction extends AbstractAction
    {
        private AbstractButton aButton;
        
        public DisplayAction(String label)
        {
            super(label);
        }
        
        public void actionPerformed(ActionEvent e)
        {
            aButton = (AbstractButton) e.getSource();
            setVisible(aButton.isSelected());
        }
        
        public void setButton(boolean state)
        {
            aButton.setSelected(state);
        }
    }
 
    //----------------------------Closing Favorites window
    
   //---------------addPoppup stuff
    
    
          
          
         public void createPopupMenu() {
        JMenuItem menuItem;

        //Create the popup menu.
        JPopupMenu popup = new JPopupMenu();
        menuItem = new JMenuItem("Open Resource");
        menuItem.addActionListener(this);
        popup.add(menuItem);
        menuItem = new JMenuItem("Add Bookmark Folder");
        menuItem.addActionListener(this);
        popup.add(menuItem);
        menuItem = new JMenuItem("Remove Resource");
        menuItem.addActionListener(this);
        popup.add(menuItem);
         menuItem = new JMenuItem("Save Favorites");
        menuItem.addActionListener(this);
        popup.add(menuItem);
         menuItem = new JMenuItem("Restore Favorites");
        menuItem.addActionListener(this);
        popup.add(menuItem);
        
        

        //Add listener to the text area so the popup menu can come up.
        MouseListener popupListener = new PopupListener(popup);
        favoritesTree.addMouseListener(popupListener);
    }
    
          
           public void actionPerformed(ActionEvent e) {
        JMenuItem source = (JMenuItem)(e.getSource());
         
        TreePath tp = favoritesTree.getSelectionPath();
        
     
            
        DefaultMutableTreeNode dn = (DefaultMutableTreeNode)tp.getLastPathComponent();
          
        
            if (e.getActionCommand().toString().equals("Add Bookmark Folder")){
               
                
              if 
              
              (dn.isLeaf()){
                  
                  
                 DefaultTreeModel model = (DefaultTreeModel)favoritesTree.getModel();
                 
               
                     
                 FavoritesNode newNode= new FavoritesNode("New Bookmark Folder");
                   if (model.getRoot() != dn){
                 FavoritesNode node = (FavoritesNode)dn.getParent();
                    model.insertNodeInto(newNode, node, 0);  
                 }
                 else {
                      model.insertNodeInto(newNode, dn, 0);  
                 }
              
           
           
                                                 }
                else
                     {
                   DefaultTreeModel model = (DefaultTreeModel)favoritesTree.getModel();
                                 
                    FavoritesNode newNode= new FavoritesNode("New Bookmark Folder");
                    model.insertNodeInto(newNode, dn, 0);  
            
             
                         }
            
        
       
                }
        
             else if (e.getActionCommand().toString().equals("Remove Resource"))
             {
                 
                    // System.out.println("I am in remove resource in favorites");
                         if (dn.isRoot())
                         {
                             System.out.println("cannot delete root");
                         }
                         else
                         {
           
                             DefaultTreeModel model = (DefaultTreeModel)favoritesTree.getModel();
                            model.removeNodeFromParent(dn);
                                            }
                }
              //-----------------------------Save/Restore Tree
             else if (e.getActionCommand().toString().equals("Save Favorites")){
                 
                         
                            TreePath testp = favoritesTree.getSelectionPath();
                            int ine1 = favoritesTree.getMinSelectionRow();
                            int ine2 = favoritesTree.getMaxSelectionRow();
                            
                           System.out.println("getSelection Path" + testp+ ine1 + ine2);
                           
                         
        
                 
                        SaveVueJTree sfavtree = new SaveVueJTree(favoritesTree);
                       
                          File f  = new File("C:\\temp\\savetree.xml");
                          marshallMap(f,sfavtree);
                            }
             else if (e.getActionCommand().toString().equals("Restore Favorites")){
                 
                          File f  = new File("C:\\temp\\savetree.xml");
                          SaveVueJTree restorefavtree = unMarshallMap(f);
                          System.out.println("Afte Unmarshalling "+restorefavtree.getClass().getName()+ " root"+ restorefavtree.getSaveTreeRoot().getResourceName());
                          VueDandDTree vueTree =restorefavtree.restoreTree();
                         
                         JFrame jf = new JFrame();
                   
                       
                         JScrollPane newPane = new JScrollPane(vueTree);
                         jf.getContentPane().add(newPane);
                         jf.pack();
                         jf.setVisible(true);
                 }
        
            
            //-----------------Save/Restore Tree
        
        
        
             else {
                 // System.out.println("I am in open resource in favorites");
                
                    if (tp.getLastPathComponent() instanceof FavoritesNode){
            
                      System.out.println("a book mark node so nothing to show");
                         }
                     else
                    {
                 
                            if ((dn.getUserObject()) instanceof  Asset){
                                
                           /**
                             AssetViewer a = new AssetViewer((Asset)dn.getUserObject());
                            a.setSize(600,400);
                            a.setLocation(10,10);
                            a.show();
                            */
                            }
                            else
                            {
                            Resource resource =new Resource(dn.getUserObject().toString());
                            resource.displayContent();
                            }
                     }     
               
                           
                        }
                 
             }
    
           

    public void itemStateChanged(ItemEvent e) {
        JMenuItem source = (JMenuItem)(e.getSource());
        
        
    }
   class PopupListener extends MouseAdapter {
        JPopupMenu popup;

        PopupListener(JPopupMenu popupMenu) {
            popup = popupMenu;
        }

        public void mousePressed(MouseEvent e) {
            maybeShowPopup(e);
        }

         public void mouseClicked(MouseEvent e) {
            maybeShowPopup(e);
        }

        public void mouseReleased(MouseEvent e) {
            maybeShowPopup(e);
        }

        private void maybeShowPopup(MouseEvent e) {
            if (e.isPopupTrigger()) {
               
                //System.out.println("ha ha " + e.getX() + e.getY());
                popup.show(e.getComponent(),
                           e.getX(), e.getY());
            }
        }
    }   
 
  public  void marshallMap(File file,SaveVueJTree favoritesTree)
    {
        Marshaller marshaller = null;
        
        //if (this.marshaller == null) {
        Mapping mapping = new Mapping();
            
        try 
        {  
            FileWriter writer = new FileWriter(file);
            
            marshaller = new Marshaller(writer);
            mapping.loadMapping(XML_MAPPING);
            marshaller.setMapping(mapping);
            
            System.out.println("start of marshall");
            marshaller.marshal(favoritesTree);
            System.out.println("end of marshall");
            
            writer.flush();
            writer.close();
            
        } 
        catch (Exception e) 
        {
            System.err.println("FavoritesWindow.marshallMap " + e);
        }
        //}
    }
    
    /**A static method which creates an appropriate unmarshaller and unmarshal the given concept map*/
   
    public  SaveVueJTree unMarshallMap(File file)
    {
        Unmarshaller unmarshaller = null;
        SaveVueJTree sTree = null;
        
        //if (this.unmarshaller == null) {   
        Mapping mapping = new Mapping();
            
        try 
        {
            unmarshaller = new Unmarshaller();
            mapping.loadMapping(XML_MAPPING);    
            unmarshaller.setMapping(mapping);  
            
            FileReader reader = new FileReader(file);
            
            sTree= (SaveVueJTree) unmarshaller.unmarshal(new InputSource(reader));
            
            reader.close();
        } 
        catch (Exception e) 
        {
            System.err.println("ActionUtil.unmarshallMap: " + e);
            e.printStackTrace();
            sTree = null;
        }
        //}
        
        return sTree;
    }
    
 class FavSearchPanel extends JPanel{
     
     JTabbedPane fp;
     JPanel sp;
     FavSearchPanel(VueDandDTree favortiesTree, JTabbedPane fp, JPanel sp){
     
        this.fp = fp;
        this.sp = sp;
        JPanel queryPanel =  new JPanel();        
     
       
     
        
        queryPanel.setLayout(new BorderLayout()); 
        final JTextField queryBox = new JTextField();
        queryPanel.add(queryBox,BorderLayout.NORTH);
        JButton searchButton = new JButton("Search");     
        searchButton.setPreferredSize(new Dimension(280,30));      
        queryPanel.add(searchButton,BorderLayout.SOUTH);
        this.add(queryPanel,BorderLayout.NORTH);
     
        
        
         searchButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                
            int index = 0;
              
                JScrollPane jsp = new JScrollPane();
                
                String searchString = queryBox.getText();
                        
                
               if (!searchString.equals("")){
                   
                    VueDragTree serResultTree = new VueDragTree("some","Search Results");
                    serResultTree.setShowsRootHandles(true);
                    TreeModel serTreeModel = serResultTree.getModel();
                    DefaultMutableTreeNode serTreeRoot = (DefaultMutableTreeNode)serTreeModel.getRoot();
                    TreeModel favTreeModel = favoritesTree.getModel();
                    DefaultMutableTreeNode favRoot = (DefaultMutableTreeNode)favTreeModel.getRoot();
                    
                    
                   
                    Enumeration enum = favRoot.depthFirstEnumeration();
                      
                    while (enum.hasMoreElements()){
                        
                        Object o = enum.nextElement();
                
                        if (searchString.compareToIgnoreCase(o.toString()) == 0){
                          
                           serTreeRoot.add((DefaultMutableTreeNode)o);
                            
                        }
                        
                        
                    }
                    
                     
                    JScrollPane fPane = new JScrollPane(serResultTree);
                   
                 
                   
                    FavSearchPanel.this.sp.setLayout(new BorderLayout());
                      FavSearchPanel.this.sp.add(fPane,BorderLayout.CENTER);   
                 
                   FavSearchPanel.this.fp.setSelectedIndex(1);
                   
                   
              
                  
                   
               }
                
                 }
            
            });
                   
        
        
     }
     
 }
     
     
     
     
     
}

