/*
 * SaveAction.java
 *
 * Created on March 31, 2003, 1:33 PM
 */

package tufts.vue.action;

import javax.swing.*;
import java.awt.event.*;
import java.io.*;

import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;

import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.mapping.MappingException;
import java.io.OutputStreamWriter;
/**
 *
 * @author  akumar03
 */
public class SaveAction extends AbstractAction {
    
    /** Creates a new instance of SaveAction */
    public SaveAction() {
    }
    
    public SaveAction(String label) {
        super(label);
    }
   
    public void actionPerformed(ActionEvent e) {
      try {  
        Mapping mapping;
        Marshaller marshaller;
        marshaller = new Marshaller(new FileWriter("test.xml"));
        mapping =  new Mapping();
        mapping.loadMapping( "mapping.xml" );
        marshaller.setMapping(mapping);
      marshaller.marshal(tufts.vue.VUE.getMap());
        
      }catch(Exception ex) {System.out.println(ex);}
          System.out.println("Action["+e.getActionCommand()+"] performed!");
    }
    
}
