/*
 * OsidManager.java
 *
 * Created on October 23, 2003, 3:07 PM
 */

package tufts.oki;
import osid.OsidOwner;

/**
 *
 * @author  Mark Norton
 */
public class OsidManager implements osid.OsidManager{
     /* Use this constant for the organizational authority string.  */
    public static final String AUTHORITY = "tufts.edu";
   
    private osid.OsidOwner mgr_owner;
    
    /**
     *  Creates a new instance of OsidManager.  Since no owner is provided, a 
     *  default empty owner is created.
     *
     *  @author Mark Norton
     */
    public OsidManager() {
        mgr_owner = new osid.OsidOwner();
    }
    
    /**
     *  Create a new instance of OsidManager using owner provide. 
     *
     *  @author Mark Norton
     */
    public OsidManager(osid.OsidOwner owner) {
     //   assert (owner != null) :  "Owner is null";
        mgr_owner = owner;
    }
    
    /**
     *  Get the owner of this manager.
     *
     *  @author Mark Norton
     *
     *  @return The owner of this manager.
     */
    public osid.OsidOwner getOwner() {
        return mgr_owner;
    }
    
    /**
     *  The OsidLoader uses this method to determine which version of the OSID is being used.
     */
    public void osidVersion_1_0() {
    }
    
    /**
     *  Update the the configuration map.
     *  <p>
     *  Currently unimplemented.
     *
     *  @author Mark Norton
     */
    public void updateConfiguration(java.util.Map configuration) throws osid.OsidException {
        //throw new osid.OsidException (osid.OsidException.UNIMPLEMENTED);
    }
    
    /**
     *  Change the owner of this manager to the one provided.
     *
     *  @author Mark Norton
     */
    public void updateOwner(osid.OsidOwner owner) throws osid.OsidException {
       // assert (owner != null) :  "Owner is null";
        mgr_owner = owner;
    }
    
}
