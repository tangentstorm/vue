/*
 * LWPathway.java
 *
 * Created on June 18, 2003, 1:37 PM
 *
 * @author  Jay Briedis
 */

package tufts.vue;

import java.util.LinkedList;
import java.util.List;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.BasicStroke;
import java.awt.AlphaComposite;
import java.awt.geom.Line2D;
import java.awt.geom.Area;
import java.util.ArrayList;

public class LWPathway extends LWContainer
    implements LWComponent.Listener
{
    private int weight = 1;
    private boolean ordered = false;
    private int mCurrentIndex = -1;
    private boolean locked = false;
    private ArrayList elementPropertyList = new ArrayList();

    private transient boolean open = true;
    private transient boolean mDoingXMLRestore = false;

    private static Color[] ColorTable = {
        new Color(153, 51, 51),
        new Color(204, 51, 204),
        new Color(51, 204, 51),
        new Color(51, 204, 204),
        new Color(255, 102, 51),
        new Color(51, 102, 204),
    };
    private static int sColorIndex = 0;
    
    /**default constructor used for marshalling*/
    public LWPathway() {
        mDoingXMLRestore = true;
    }

    LWPathway(String label) {
        this(null, label);
    }

    /** Creates a new instance of LWPathway with the specified label */
    public LWPathway(LWMap map, String label) {
        setMap(map);
        setLabel(label);
        setStrokeColor(getNextColor());
    }

    private static Color getNextColor()
    {
        if (sColorIndex >= ColorTable.length)
            sColorIndex = 0;
        return ColorTable[sColorIndex++];
        /*
        LWPathwayManager manager = VUE.getActiveMap().getPathwayManager();
        System.out.println("manager: " + manager.toString());
        if(manager != null && manager.getPathwayList() != null){
            int num = manager.getPathwayList().size();
            borderColor = (Color)colorArray.get(num % 6);
        }
        System.out.println("pathway border color: " + borderColor.toString());
        */
    }
     
    public int getWeight() { return weight; }
    public void setWeight(int weight) { this.weight = weight; }

    int setIndex(int i)
    {
        if (DEBUG.PATHWAY) System.out.println(this + " setIndex " + i);
        if (i >= 0 && VUE.getActivePathway() == this)
            VUE.ModelSelection.setTo(getElement(i));
        return mCurrentIndex = i;
    }

    /**
     * Overrides LWContainer addChildren.  Pathways aren't true
     * parents, so all we want to do is add a reference to them,
     * and raise a change event.
     */
    public void addChildren(Iterator i)
    {
        if (DEBUG.PATHWAY||DEBUG.PARENTING) System.out.println(this + " addChildren " + VUE.ModelSelection);
        ArrayList added = new java.util.ArrayList();
        while (i.hasNext()) {
            LWComponent c = (LWComponent) i.next();
            if (DEBUG.PATHWAY||DEBUG.PARENTING) System.out.println(this + " addChild " + added.size() + " " + c);
            if (!contains(c)) {
                c.addPathwayRef(this);
                c.addLWCListener(this);
            }
            super.children.add(c);

            // For now you can only have one set of properties per element in the list,
            // even if the element is in the path more than once.  We probably
            // want to ultimately support a set of properties for each index
            // within the pathway.
            if (!hasElementProperties(c))
                elementPropertyList.add(new LWPathwayElementProperty(c.getID()));
            
            added.add(c);
        }
        if (DEBUG.PATHWAY||DEBUG.PARENTING) System.out.println(this + " ADDEDALL " + added);
        //if (mCurrentIndex == -1) setIndex(length() - 1);
        if (added.size() > 0) {
            if (added.size() == 1)
                setIndex(length()-1);
            notify(LWCEvent.ChildrenAdded, added);
        }
    }

    public void add(LWComponent c) {
        addChild(c);
    }
    public void remove(LWComponent c) {
        removeChild(c);
    }
    public void add(Iterator i) {
        addChildren(i);
    }
    public void remove(Iterator i) {
        removeChildren(i);
    }

    /**
     * As a LWComponent may appear more than once in a pathway, we
     * need to make sure we can remove pathway entries by index, and
     * not just by content.
     */
    private LWComponent removingComponent = null;
    public synchronized void remove(int index) {
        remove(index, false);
    }

    private void disposeElementProperties(LWComponent c)
    {
        for (Iterator i = elementPropertyList.iterator(); i.hasNext();) {
            LWPathwayElementProperty prop = (LWPathwayElementProperty) i.next();
            if (prop.getElementID().equals(c.getID())) {
                if (DEBUG.PATHWAY&&DEBUG.META) System.out.println(this + " dumping property " + prop);
                i.remove();
                break;
            }
        }
    }
    
    /**
     * Overrides LWContainer removeChildren.  Pathways aren't true
     * parents, so all we want to do is remove the reference to them
     * and raise a change event.  Removes all items in iterator
     * COMPLETELY from the pathway -- all instances are removed.
     * The iterator may contains elements that are not in this pathway:
     * we just make sure any that are in this pathway are removed.
     */
    //  Todo: factor & comine with remove(int index, bool deleting)
    public void removeChildren(Iterator i)
    {
        if (DEBUG.PATHWAY||DEBUG.PARENTING) System.out.println(this + " removeChildren " + VUE.ModelSelection);
        ArrayList removed = new java.util.ArrayList();
        while (i.hasNext()) {
            LWComponent c = (LWComponent) i.next();
            boolean contained = false;
            while (children.contains(c)) {
                if (DEBUG.PATHWAY||DEBUG.PARENTING) System.out.println(this + " removeChild " + removed.size() + " " + c);
                contained |= children.remove(c);
            }
            if (contained) {
                c.removePathwayRef(this);
                c.removeLWCListener(this);       
                disposeElementProperties(c);
                removed.add(c);
            }
        }
        if (DEBUG.PATHWAY||DEBUG.PARENTING) System.out.println(this + " REMOVEDALL " + removed);
        if (removed.size() > 0) {
            if (mCurrentIndex >= length())
                setIndex(length() - 1);
            else
                setIndex(mCurrentIndex);
            notify(LWCEvent.ChildrenRemoved, removed);
        }
    }
    
    private synchronized void remove(int index, boolean deleting) {
        if (DEBUG.PATHWAY||DEBUG.PARENTING) System.out.println(this + " remove index " + index + " deleting=" + deleting);
        LWComponent c = (LWComponent) children.remove(index);
        if (DEBUG.PATHWAY||DEBUG.PARENTING) System.out.println(this + " removed " + c);
        if (length() == 0)
            mCurrentIndex = -1;

        if (!contains(c)) { // in case in multiple times
            c.removePathwayRef(this);
            if (!deleting) {
                // If deleting, component will remove us as listener itself.
                // If we remove it here while deleting, we'll get a concurrent
                // modification exception from LWCompononent.notifyLWCListeners
                c.removeLWCListener(this);
            }
            disposeElementProperties(c);  // only remove property if last time appears in list.
        }

        // If what we just deleted was the current item, the currentIndex
        // doesn't change, but we call this to make sure we set the selection.
        // Or, if we just deleted the last item in the list, mCurrentIndex
        // needs to shrink by one.
        if (mCurrentIndex >= length())
            setIndex(length() - 1);
        else
            setIndex(mCurrentIndex);

        removingComponent = c; // todo: should be able to remove this now that we don't deliver events back to source
        notify(LWCEvent.ChildRemoved, c);
        removingComponent = null;
    }
    
    public synchronized void LWCChanged(LWCEvent e)
    {
        if (e.getComponent() == removingComponent) {
            //if (DEBUG.PATHWAY || DEBUG.EVENTS) System.out.println(e + " ignoring: already deleting in " + this);
            new Throwable(e + " ignoring: already deleting in " + this).printStackTrace();
            return;
        }
        if (e.getWhat() == LWCEvent.Deleting)
            removeAll(e.getComponent());
    }

    /**
     * Remove all instances of @param deleted from this pathway
     * Used when a component has been deleted.
     */
    protected void removeAll(LWComponent deleted)
    {
        while (contains(deleted))
            remove(children.indexOf(deleted), true);
    }

    public LWMap getMap(){
        return (LWMap) getParent();
    }
    public void setMap(LWMap map) {
        setParent(map);
    }

    public void setLocked(boolean t) {
        this.locked = t;
        notify(t ? "pathway.lock" : "pathway.unlock");
    }
    public boolean isLocked(){
        return locked;
    }
    
    public void setOpen(boolean open){
        this.open = open;
    }
    
    public boolean isOpen() {
        return open;
    }

    public boolean contains(LWComponent c) {
        return children.contains(c);
    }
    public boolean containsMultiple(LWComponent c) {
        return children.indexOf(c) != children.lastIndexOf(c);
    }

    public int length() {
        return children.size();
    }
    
    public LWComponent setFirst()
    {
        if (length() > 0) {
            setIndex(0);
            return getElement(0);
        }
        return null;
    }
    
    public boolean atFirst(){
        return (mCurrentIndex == 0);
    }
    public boolean atLast(){
        return (mCurrentIndex == (length() - 1));
    }
    
    public LWComponent setLast() {
        if (length() > 0) {
            setIndex(length() - 1);
            return getElement(length() - 1);
        }
        return null;
    }
    
    public LWComponent setPrevious(){
        if (mCurrentIndex > 0)
            return getElement(setIndex(mCurrentIndex - 1));
        else
            return null;
    }
    
    public LWComponent setNext(){
        if (mCurrentIndex < (length() - 1))
            return getElement(setIndex(mCurrentIndex + 1));
        else 
            return null;
    }

    public LWComponent getElement(int index){
        LWComponent c = null;
        try {
            c = (LWComponent) children.get(index);
        } catch (IndexOutOfBoundsException e) {
            System.err.println(this + " getElement " + index + " " + e);
        }    
        return c;
    }

    /**
     * Make sure we've completely cleaned up the pathway when it's
     * been deleted (must get rid of LWComponent references to this
     * pathway)
     */
    protected void removeFromModel()
    {
        Iterator i = children.iterator();
        while (i.hasNext()) {
            LWComponent c = (LWComponent) i.next();
            c.removePathwayRef(this);
       }
    }
    
    public boolean getOrdered() {
        return ordered;
    }
    
    public void setOrdered(boolean ordered) {
        this.ordered = ordered;
    }

    /**
     * for persistance: override of LWContainer: pathways never save their children
     * as they don't own them -- they only save ID references to them.
     */
    public ArrayList getChildList() {
        return null;
    }
    
    /** Pathway interface */
    public java.util.Iterator getElementIterator() {
        return super.children.iterator();
    }

    public java.util.List getElementList() {
        //System.out.println(this + " getElementList type  ="+elementList.getClass().getName()+"  size="+elementList.size());
        return super.children;
    }

    /** for persistance: XML save/restore only */
    public java.util.List getElementPropertyList() {
        return elementPropertyList;
    }
    
    private List idList = new ArrayList();
    /** for persistance: XML save/restore only */
    public List getElementIDList() {
        if (mDoingXMLRestore) {
            return idList;
        } else {
            idList.clear();
            Iterator i = getElementIterator();
            while (i.hasNext()) {
                LWComponent c = (LWComponent) i.next();
                idList.add(c.getID());
            }
        }
        System.out.println(this + " getElementIDList: " + idList);
        return idList;
    }


    /*
    public void setElementIDList(List idList) {
        System.out.println(this + " setElementIDList: " + idList);
        this.idList = idList;
    }
    */
    
    void completeXMLRestore(LWMap map)
    {
        System.out.println(this + " completeXMLRestore, map=" + map);
        setParent(map);
        Iterator i = this.idList.iterator();
        while (i.hasNext()) {
            String id = (String) i.next();
            LWComponent c = getMap().findChildByID(id);
            System.out.println("\tpath adding " + c);
            add(c);
        }
        mDoingXMLRestore = false;
    }
    
    /** return the current element */
    public LWComponent getCurrent() { 
        LWComponent c = null;
        if (mCurrentIndex < 0 && length() > 0) {
            System.out.println(this + " lazy default of index to 0");
            mCurrentIndex = 0;
        }
        try {
            c = (LWComponent) children.get(mCurrentIndex);
        } catch (IndexOutOfBoundsException ie){
            c = null;
        }      
        return c;
    }
    
    /** for PathwayTable */
    void setCurrentElement(LWComponent c) {
        setIndex(children.indexOf(c));
        notify(LWCEvent.Repaint);
    }
    
    public int getCurrentIndex(){
        return mCurrentIndex;
    }

    public String getElementNotes(LWComponent c)
    {
        if (c == null) return null;
        
        String notes = null;
        for (Iterator i = elementPropertyList.iterator(); i.hasNext();) {
            LWPathwayElementProperty prop = (LWPathwayElementProperty) i.next();
            if (prop.getElementID().equals(c.getID())) {
                notes = prop.getElementNotes();
                break;
            }
        }
        //System.out.println("returning notes for " + c + " [" + notes + "]");
        return notes;
    }
    
    public boolean hasElementProperties(LWComponent c)
    {
        if (c == null) return false;
        for (Iterator i = elementPropertyList.iterator(); i.hasNext();) {
            LWPathwayElementProperty prop = (LWPathwayElementProperty) i.next();
            if (prop.getElementID().equals(c.getID()))
                return true;
        }
        return false;
    }

    public void setElementNotes(LWComponent c, String notes)
    {   
        if (notes == null || c == null) {
            System.err.println("argument(s) to setElementNotes is null");
            return;
        }
        
        for (Iterator i = elementPropertyList.iterator(); i.hasNext();) {
            LWPathwayElementProperty element = (LWPathwayElementProperty)i.next();
            if (element.getElementID().equals(c.getID())) {
                Object oldNotes = element.getElementNotes();
                element.setElementNotes(notes);
                notify("pathway.element.notes", oldNotes); // not enough info for undo...
                break;
            }
        }
    }

    
    private static final AlphaComposite PathTranslucence = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.75f);
    private static final AlphaComposite PathSelectedTranslucence = PathTranslucence;
    //private static final AlphaComposite PathSelectedTranslucence = AlphaComposite.Src;
    //private static final AlphaComposite PathSelectedTranslucence = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.8f);

    private static float dash_length = 4;
    private static float dash_phase = 0;
    public void drawPathway(DrawContext dc){
        Iterator i = this.getElementIterator();
        Graphics2D g = dc.g;

        if (DEBUG.PATHWAY) {
        if (dc.getIndex() % 2 == 0)
            dash_phase = 0;
        else
            dash_phase = 0.5f;
        }
        if (DEBUG.PATHWAY&&DEBUG.BOXES) System.out.println("Drawing " + this + " index=" + dc.getIndex() + " phase=" + dash_phase);
        
        g.setColor(getStrokeColor());
        LWComponent last = null;
        Line2D connector = new Line2D.Float();
        BasicStroke connectorStroke =
            new BasicStroke(5, BasicStroke.CAP_BUTT
                            , BasicStroke.JOIN_BEVEL
                            , 0f
                            , new float[] { dash_length, dash_length }
                            , dash_phase);

        final int BaseStroke = 3;
        
        while (i.hasNext()) {
            LWComponent c = (LWComponent)i.next();

            int strokeWidth;
            boolean selected = (getCurrent() == c && VUE.getActivePathway() == this);
            strokeWidth = BaseStroke;

            // because we're drawing under the object, only half of
            // the amount we add to to the stroke width is visible
            // outside the edge of the object, except for links,
            // which are one-dimensional, so we use a narrower
            // stroke width for them.
            if (c instanceof LWLink)
                ;//strokeWidth++;
            else
                strokeWidth *= 2;

            if (selected)
                g.setComposite(PathSelectedTranslucence);
            else
                g.setComposite(PathTranslucence);
        
            strokeWidth += c.getStrokeWidth();
            if (selected) {
                g.setStroke(new BasicStroke(strokeWidth*2));
            } else {
                if (DEBUG.PATHWAY && dc.getIndex() % 2 != 0) dash_phase = c.getStrokeWidth();
                g.setStroke(new BasicStroke(strokeWidth
                                            , BasicStroke.CAP_BUTT
                                            , BasicStroke.JOIN_BEVEL
                                            , 0f
                                            , new float[] { dash_length, dash_length }
                                            , dash_phase));
            }
            g.draw(c.getShape());

            // If there was an element in the path before this one,
            // draw a connector line from that last component to this
            // one.
            if (last != null) {
                g.setComposite(PathTranslucence);
                connector.setLine(last.getCenterPoint(), c.getCenterPoint());
                g.setStroke(connectorStroke);
                g.draw(connector);
            }
            last = c;
        }
    }
    
    public String toString()
    {
        return "LWPathway[" + label
            + " n="
            + (children==null?-1:children.size())
            + " idx="+mCurrentIndex
            + " map=" + (getMap()==null?"null":getMap().getLabel())
            + "]";
    }

}
