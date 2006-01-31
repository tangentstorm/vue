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
 * AddEditDataSourceDialog.java
 * The UI to Add/Edit Datasources.
 * Created on June 8, 2004, 5:07 PM
 */

package tufts.vue;

/**
 * @version $Revision: 1.2 $ / $Date: 2006-01-31 18:08:43 $ / $Author: jeff $
 * @author  akumar03
 */
import javax.swing.*;
import java.awt.event.*;
import javax.swing.event.*;
import java.awt.*;

public class AddLibraryDialog extends JDialog implements ListSelectionListener {
    
    JPanel addLibraryPanel = new JPanel();
    JList addLibraryList;
	DefaultListModel listModel = new DefaultListModel();
	JScrollPane listJsp;
	JScrollPane descriptionJsp;
	JLabel libraryIcon;
	JTextArea libraryDescription;
	edu.tufts.vue.dsm.DataSourceManager dataSourceManager;
	edu.tufts.vue.dsm.Registry registry;
	org.osid.registry.Provider checked[];
	JPanel buttonPanel = new JPanel();
	JButton cancelButton = new JButton("Canel");
	JButton addButton = new JButton("Add");
    
    public AddLibraryDialog()
	{
        super(VUE.getDialogParentAsFrame(),"ADD A LIBRARY",true);
		try {
			addLibraryList = new JList(listModel);
			addLibraryList.setSelectionMode(DefaultListSelectionModel.SINGLE_SELECTION);
			addLibraryList.setPreferredSize(new Dimension(130,160));
			addLibraryList.addListSelectionListener(this);
						
			populate();
			listJsp = new JScrollPane(addLibraryList);
			//if (this.settings.isMac()) 
			{ 
				listJsp.setVerticalScrollBarPolicy(javax.swing.JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
				listJsp.setHorizontalScrollBarPolicy(javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS); 
			}
			
			if (checked.length > 0) {
				// TODO: Replace this with an image, but we need this in the Provider not just the Data Source
				libraryIcon = new JLabel(VueResources.getImageIcon("NoImage"));
				libraryDescription = new JTextArea(checked[0].getDescription());
				addLibraryList.setSelectedIndex(0);
			} else {
				libraryIcon = new JLabel(VueResources.getImageIcon("NoImage"));
				libraryDescription = new JTextArea();
			}

			libraryIcon.setPreferredSize(new Dimension(80,80));
			libraryDescription.setLineWrap(true);
			libraryDescription.setWrapStyleWord(true);
			
			descriptionJsp = new JScrollPane(libraryDescription);
			//if (this.settings.isMac()) 
			{ 
				descriptionJsp.setVerticalScrollBarPolicy(javax.swing.JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
				descriptionJsp.setHorizontalScrollBarPolicy(javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS); 
			}

			libraryDescription.setPreferredSize(new Dimension(220,140));			
			
			addLibraryPanel.setBackground(VueResources.getColor("White"));
			setBackground(VueResources.getColor("White"));

			java.awt.GridBagLayout gbLayout = new java.awt.GridBagLayout();
			java.awt.GridBagConstraints gbConstraints = new java.awt.GridBagConstraints();
			gbConstraints.anchor = java.awt.GridBagConstraints.WEST;
			gbConstraints.insets = new java.awt.Insets(2,2,2,2);
			addLibraryPanel.setLayout(gbLayout);
			
			gbConstraints.gridx = 0;
			gbConstraints.gridy = 0;
			addLibraryPanel.add(new JLabel("Locations Available:"),gbConstraints);
			
			gbConstraints.gridx = 0;
			gbConstraints.gridy = 1;
			addLibraryPanel.add(listJsp,gbConstraints);
			
			gbConstraints.gridx = 1;
			gbConstraints.gridy = 1;
			addLibraryPanel.add(libraryIcon,gbConstraints);
			
			gbConstraints.gridx = 2;
			gbConstraints.gridy = 1;
			addLibraryPanel.add(descriptionJsp,gbConstraints);
			
			buttonPanel.add(cancelButton);
			buttonPanel.add(addButton);
			addButton.setBackground(VueResources.getColor("Orange")); //TODO:  Why is this BLUE??
			getRootPane().setDefaultButton(addButton);
			
			gbConstraints.gridx = 2;
			gbConstraints.gridy = 2;
			addLibraryPanel.add(buttonPanel,gbConstraints);

			getContentPane().add(addLibraryPanel,BorderLayout.CENTER);
			pack();
			setLocation(300,300);
			setSize(new Dimension(480,300));
		} catch (Throwable t) {
			t.printStackTrace();
		}
		setVisible(true);
    }

    private void populate()
	{
		try
		{
			if (dataSourceManager == null) {
				dataSourceManager = edu.tufts.vue.dsm.impl.VueDataSourceManager.getInstance();
				registry = edu.tufts.vue.dsm.impl.VueRegistry.getInstance();
			}
			edu.tufts.vue.dsm.DataSource dataSources[] = dataSourceManager.getDataSources();
			checked = registry.checkRegistryForNew(dataSources);
			listModel.removeAllElements();
			if (checked.length == 0) {
				listModel.addElement("No new Libraries");
			}
			for (int i=0; i < checked.length; i++) {
				listModel.addElement(checked[i].getDisplayName());
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
	
    public void show(int mode) 
	{
		populate();
		System.out.println("showing");
		setVisible(true);
        super.setVisible(true);
    }

    public String toString() 
	{
        return "AddLibraryDialog";
    }

	public void valueChanged(ListSelectionEvent lse) {				
		int index = ((JList)lse.getSource()).getSelectedIndex();
		if (index != -1) {
			try {
				// TODO: update icon
				libraryDescription.setText(checked[index].getDescription());
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
	}
}



