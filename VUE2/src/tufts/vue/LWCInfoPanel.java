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

package tufts.vue;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.JTextComponent;
import javax.swing.border.*;


class LWCInfoPanel extends javax.swing.JPanel
implements VueConstants,
LWSelection.Listener,
LWComponent.Listener,
ActionListener {
    private JTextField labelField = new JTextField(15);
    private tufts.vue.gui.VueTextField resourceField = new tufts.vue.gui.VueTextField();
    
    private JPanel fieldPane = new JPanel();
    private JPanel resourceMetadataPanel = new JPanel();
    private JPanel metadataPane = new JPanel();
    private PropertiesEditor propertiesEditor = null;
    
    private Object[] labelTextPairs = {
        "Label",    labelField,
        "Resource", resourceField,
    };
    
    private LWComponent lwc;
    
    public LWCInfoPanel() {
        //setBorder(BorderFactory.createEmptyBorder(6,6,6,6));
        GridBagLayout gridBag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        fieldPane.setLayout(gridBag);
        addLabelTextRows(labelTextPairs, gridBag, fieldPane);
        // settting metadata
        setUpMetadataPane();
        setLayout(new BorderLayout());
        add(fieldPane, BorderLayout.NORTH);
        add(metadataPane,BorderLayout.CENTER);
        VUE.ModelSelection.addListener(this);
    }
    
    private void setUpMetadataPane() {
        metadataPane.setLayout(new BorderLayout());
        metadataPane.setBorder(BorderFactory.createEmptyBorder(10,0,0,0));
        propertiesEditor = new PropertiesEditor(true);
        metadataPane.add(propertiesEditor,BorderLayout.CENTER);
        validate();
        
    }
    
    private void addLabelTextRows(Object[] labelTextPairs,
    GridBagLayout gridbag,
    Container container) {
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.EAST;
        int num = labelTextPairs.length;
        
        for (int i = 0; i < num; i += 2) {
            c.insets = new Insets(0, 0, 1, 0);
            c.gridwidth = GridBagConstraints.RELATIVE; //next-to-last
            c.fill = GridBagConstraints.NONE;      //reset to default
            c.weightx = 0.0;                       //reset to default
            c.anchor = GridBagConstraints.WEST;
            String txt = (String) labelTextPairs[i];
            boolean readOnly = false;
            if (txt.startsWith("-")) {
                txt = txt.substring(1);
                readOnly = true;
            }
            txt += ": ";
            
            JLabel label = new JLabel(txt);
            //JLabel label = new JLabel(labels[i]);
            //label.setFont(VueConstants.SmallFont);
            gridbag.setConstraints(label, c);
            container.add(label);
            
            c.gridwidth = GridBagConstraints.REMAINDER;     //end row
            c.fill = GridBagConstraints.HORIZONTAL;
            c.insets = new Insets(0, 5, 1, 0);
            c.weightx = 1.0;
            
            JComponent field = (JComponent) labelTextPairs[i+1];
            //field.setFont(VueConstants.SmallFont);
            if (field instanceof JTextField) {
                //((JTextField)field).setHorizontalAlignment(JTextField.LEFT);
                ((JTextField)field).addActionListener(this);
            }
            gridbag.setConstraints(field, c);
            container.add(field);
            
            
            
            if (readOnly) {
                field.setBorder(new EmptyBorder(1,1,1,1));
                if (field instanceof JTextField) {
                    JTextField tf = (JTextField) field;
                    tf.setEditable(false);
                    tf.setFocusable(false);
                }
                if (VueUtil.isMacPlatform())
                    field.setBackground(SystemColor.control);
            }
        }
        /**
         * JLabel field  = new JLabel("Metadata");
         * c.gridwidth = GridBagConstraints.REMAINDER;     //end row
         * c.fill = GridBagConstraints.HORIZONTAL;
         * c.anchor = GridBagConstraints.WEST;
         * gridbag.setConstraints(field, c);
         * container.add(field);
         */
    }
    
    
    public void LWCChanged(LWCEvent e) {
        if (this.lwc != e.getSource())
            return;
        
        if (e.getWhat() == LWKey.Deleting) {
            this.lwc = null;
            setAllEnabled(false);
        } else if (e.getSource() != this)
            loadItem(this.lwc);
    }
    
    public void selectionChanged(LWSelection selection) {
        if (selection.isEmpty() || selection.size() > 1)
            setAllEnabled(false);
        else
            loadItem(selection.first());
    }
    
    private void loadText(JTextComponent c, String text) {
        String hasText = c.getText();
        // This prevents flashing where fields of
        // length greater the the visible area do
        // a flash-scroll when setting the text, even
        // if it's the same as what's there.
        if (hasText != text && !hasText.equals(text))
            c.setText(text);
    }
    
    private void setAllEnabled(boolean tv) {
        int pairs = labelTextPairs.length;
        for (int i = 0; i < pairs; i += 2) {
            JComponent field = (JComponent) labelTextPairs[i+1];
            field.setEnabled(tv);
        }
        resourceMetadataPanel.setEnabled(tv);
        metadataPane.setEnabled(tv);
        propertiesEditor.setEnabled(tv);
    }
    
    private void loadItem(LWComponent lwc) {
        if (this.lwc != lwc) {
            if (this.lwc != null)
                this.lwc.removeLWCListener(this);
            this.lwc = lwc;
            if (this.lwc != null) {
                this.lwc.addLWCListener(this, new Object[] { LWKey.Label, LWKey.Resource, LWKey.Deleting });
                setAllEnabled(true);
            } else
                setAllEnabled(false);
        }
        
        //System.out.println(this + " loadItem " + lwc);
        LWComponent c = this.lwc;
        if (c == null)
            return;
        
        setAllEnabled(true);
        //System.out.println(this + " loading " + c);
        
        if (c.getResource() != null)
            loadText(resourceField, c.getResource().toString());
        else
            loadText(resourceField, "");
        
        loadText(labelField, c.getLabel());
        
        //loading the metadata if it exists
        if(c.getResource() != null){
            if(c.getResource().getProperties() != null){
                if(c.getResource().getType() == Resource.ASSET_FEDORA)
                    propertiesEditor.setProperties(c.getResource().getProperties(), false);
                else
                    propertiesEditor.setProperties(c.getResource().getProperties(), true);
            }
            
        } else{
            propertiesEditor.clear();
        }
        
        
    }
    
    public void actionPerformed(ActionEvent e) {
        if (this.lwc == null)
            return;
        String text = e.getActionCommand();
        Object src = e.getSource();
        LWComponent c = this.lwc;
        try {
            boolean set = true;
            if (src == labelField)
                c.setLabel(text);
            else if (src == resourceField)
                c.setResource(text);
            else
                set = false;
            if (set)
                VUE.getUndoManager().mark();
            else
                return;
        } catch (Exception ex) {
            System.err.println(ex);
            System.err.println("LWCInfoPanel: error setting property value ["+text+"] on " + src);
        }
    }
    
    public String toString() {
        return "LWCInfoPanel@" + Integer.toHexString(hashCode());
    }
    
    
}
