/*******
**  NodeInspectorPanel.java
**
**
*********/

package tufts.vue;


import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;


/**
* NodeInspectorPanel
*
* The Node  Inspector Panel!
*
\**/
public class NodeInspectorPanel  extends JPanel 
{


	/////////////
	// Fields
	//////////////
	
	/** The tabbed panel **/
	JTabbedPane mTabbedPane = null;
	
	/** The node we are inspecting **/
	LWNode mNode = null;
	
	/** info tab panel **/
	InfoPanel mInfoPanel = null;
	
	/** pathways panel **/
	TreePanel mTreePanel = null;
	
	/** filter panel **/
	NotePanel mNotePanel = null;
	
	///////////////////
	// Constructors
	////////////////////
	
	public NodeInspectorPanel() {
		super();
		
		setMinimumSize( new Dimension( 150,200) );
		setLayout( new BorderLayout() );
		setBorder( new EmptyBorder( 5,5,5,5) );
		mTabbedPane = new JTabbedPane();
		VueResources.initComponent( mTabbedPane, "tabPane");

		mInfoPanel = new InfoPanel();
		mTreePanel = new TreePanel();
		mNotePanel = new NotePanel();
		
		mTabbedPane.addTab( mInfoPanel.getName(), mInfoPanel);
		mTabbedPane.addTab( mTreePanel.getName(),  mTreePanel);
		mTabbedPane.addTab( mNotePanel.getName(), mNotePanel);
	
		add( BorderLayout.CENTER, mTabbedPane );
	}
	
	
	
	////////////////////
	// Methods
	///////////////////
	
	
	/**
	 * setNode
	 * Sets the LWMap component and updates teh display
	 *
	 * @param pNode - the LWMap to inspect
	 **/
	public void setNode( LWNode pNode) {
		
		// if we have a change in maps... 
		if( pNode != mNode) {
			mNode = pNode;
			updatePanels();
			}
	}
	
	
	/**
	 * updatePanels
	 * This method updates the panel's content pased on the selected
	 * Map
	 *
	 **/
	public void updatePanels() {
		
		mInfoPanel.updatePanel( mNode);
		mTreePanel.updatePanel( mNode);
		mNotePanel.updatePanel( mNode);
	}
	
	//////////////////////
	// OVerrides
	//////////////////////


	public Dimension getPreferredSize()  {
		Dimension size =  super.getPreferredSize();
		if( size.getWidth() < 200 ) {
			size.setSize( 200, size.getHeight() );
			}
		if( size.getHeight() < 250 ) {
			size.setSize( size.getWidth(), 250);
			}
		return size;
	}

	
	
	
	
	
	
	/////////////////
	// Inner Classes
	////////////////////
	
	
	
	
	/**
	 * InfoPanel
	 * This is the tab panel for displaying Map Info
	 *
	 **/
	public class InfoPanel extends JPanel {
	
		JScrollPane mInfoScrollPane = null;
                
		Box mInfoBox = null;
		
		public InfoPanel() {
			
			setLayout( new BorderLayout() );
			setBorder( new EmptyBorder(4,4,4,4) );
			
			mInfoBox = Box.createVerticalBox();
		
			// DEMO FIXX:  Demo hack
			mInfoBox.add( new LWCInspector() );
		
			mInfoScrollPane = new JScrollPane();
			mInfoScrollPane.setVerticalScrollBarPolicy( JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
			mInfoScrollPane.setHorizontalScrollBarPolicy( JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			mInfoScrollPane.setLocation(new Point(8, 9));
			mInfoScrollPane.setVisible(true);
			mInfoScrollPane.getViewport().add( mInfoBox);
		
			add( BorderLayout.CENTER, mInfoScrollPane );
		
			//mInfoBox.add( new JLabel("Node Info") );
			// mInfoBox.add( new PropertyPanel() );;
		}
		
		
		public String getName() {
			String name = VueResources.getString("mapInfoTabName") ;
			
			if( name == null) {
				name = "Info";
				}
			return name;
		}
		
		
		
		/**
		 * updatePanel
		 * Updates the Map info panel
		 * @param LWMap the map
		 **/
		public void updatePanel( LWNode pNode) {
			// update the display
		}
	}
	
	
	/**
	 * This is the Pathway Panel for the Map Inspector
	 *
	 **/
	public class TreePanel extends JPanel {
		
		/** the path scroll pane **/
		JScrollPane mTreeScrollPane = null;
                OutlineViewTree tree = null;
                
		/**
		 * TreePanel
		 * Constructs a pathway panel
		 **/
		public TreePanel() {
				
                        //fix the layout?
                        setLayout( new BorderLayout() );
			setBorder( new EmptyBorder(4,4,4,4) );
                        
                        
                        tree = new OutlineViewTree();
                        
			mTreeScrollPane = new JScrollPane(tree);
			mTreeScrollPane.setVerticalScrollBarPolicy( JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
			mTreeScrollPane.setHorizontalScrollBarPolicy( JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			mTreeScrollPane.setLocation(new Point(8, 9));
			mTreeScrollPane.setVisible(true);
		
                        add(mTreeScrollPane, BorderLayout.CENTER);
			//add( mTreeScrollPane );
		}
		
		
		public String getName() {
			String name = VueResources.getString("nodeTreeTabName") ;
			if( name == null) {
				name = "Node Tree";
				}
			return name;
		}
		
		
		/**
		 * updatePanel
		 * This updates the Panel display based on a new LWMap
		 *
		 **/
		 public void updatePanel( LWNode pNode) {
		 // update display based on the LWNode
                    tree.setModel(new OutlineViewTreeModel(pNode));
		 }
	}
	
}





