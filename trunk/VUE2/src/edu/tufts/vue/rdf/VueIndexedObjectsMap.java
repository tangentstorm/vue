
/*
 *
 * * <p><b>License and Copyright: </b>The contents of this file are subject to the
 * Mozilla Public License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License
 * at <a href="http://www.mozilla.org/MPL">http://www.mozilla.org/MPL/.</a></p>
 *
 * <p>Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.</p>
 *
 * <p>The entire file consists of original code.  Copyright &copy; 2003-2007
 * Tufts University. All rights reserved.</p>
 *
 *
 */

package edu.tufts.vue.rdf;

import java.net.URI;
import java.util.HashMap;

/*
 * VueIndexedObjectsMap.java
 *
 * Created on July 19, 2007, 3:39 PM
 *
 * @author dhelle01
 */
public class VueIndexedObjectsMap {
    
    // this is a static class, do not instantiate
    private VueIndexedObjectsMap() 
    {
    }
    
    // RDFIndex (or this class) should do low level priority
    // garbage collection on currently unused objects
    public static HashMap objs = new HashMap();
    
    public static void setID(URI uri,Object obj)
    {
        objs.put(uri,obj);
       // System.out.println("VueIndexedObjectsMap: " + objs);
    }
    
    public static Object getObjectForID(URI id)
    {
        return objs.get(id);
    }
    
    /**
     *  not yet implemented
     **/
    public static void garbageCollect(URI id)
    {
        
    }
    
}
