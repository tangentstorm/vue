

package tufts.vue;

import tufts.vue.shape.*;

import java.lang.*;
import java.util.*;
import java.awt.*;
import java.awt.geom.*;
import javax.swing.*;
import tufts.vue.beans.VueBeanState;

public class NodeTool extends VueTool
    implements VueConstants
{
	///////////
	// Fields
	/////////////
	
    
    private static NodeTool singleton = null;
    
    /** the contextual tool panel **/
    private static NodeToolPanel sNodeToolPanel;
    
    
    public NodeTool()
    {
        super();
        if (singleton != null) 
            new Throwable("Warning: mulitple instances of " + this).printStackTrace();
        singleton = this;
    }


    /** return the singleton instance of this class */
    public static NodeTool getTool()
    {
        if (singleton == null)
            throw new IllegalStateException("NodeTool.getTool: class not initialized by VUE");
        return singleton;
    }
    
    static NodeToolPanel getNodeToolPanel()
    {
        if (sNodeToolPanel == null)
            sNodeToolPanel = new NodeToolPanel();
        return sNodeToolPanel;
    }
    
    public JPanel getContextualPanel() {
        return getNodeToolPanel();
    }

    public boolean supportsSelection() { return true; }
    
    private java.awt.geom.RectangularShape currentShape = new tufts.vue.shape.RectangularPoly2D(4);
    // todo: if we had a DrawContext here instead of just the graphics,
    // we could query it for zoom factor (passed in from mapViewer) so the stroke width would
    // look right.
    public void drawSelector(java.awt.Graphics2D g, java.awt.Rectangle r)
    {
        //g.setXORMode(java.awt.Color.blue);

        g.draw(r);
        currentShape = getActiveSubTool().getShape();
        currentShape.setFrame(r);
        g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,  java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        //g.setColor(COLOR_NODE_DEFAULT);
        //g.fill(currentShape);
        //g.setColor(COLOR_BORDER);
        //g.setStroke(STROKE_ONE); // todo: scale based on the scale in the GC affine transform
        //g.setStroke(new BasicStroke(2f * (float) g.getTransform().getScaleX())); // GC not scaled while drawing selector...
        g.setColor(COLOR_SELECTION);
        g.draw(currentShape);
        /*
        if (VueUtil.isMacPlatform()) // Mac 1.4.1 handles XOR differently than PC
            g.draw(currentShape);
        else
            g.fill(currentShape);
        */
    }
    
    public boolean handleSelectorRelease(MapMouseEvent e)
    {
        LWNode node = createNode();
        node.setAutoSized(false);
        node.setFrame(e.getMapSelectorBox());
        e.getMap().addNode(node);
        VUE.ModelSelection.setTo(node);
        e.getViewer().activateLabelEdit(node);
        return true;
    }
    /*
    public void handleSelectorRelease(java.awt.geom.Rectangle2D mapRect)
    {
        LWNode node = createNode();
        node.setAutoSized(false);
        node.setFrame(mapRect);
        VUE.getActiveMap().addNode(node);
        VUE.ModelSelection.setTo(node);
        VUE.getActiveViewer().activateLabelEdit(node);
    }
    */

    public static LWNode createNode(String name)
    {
        LWNode node = new LWNode(name, getActiveSubTool().getShapeInstance());
        VueBeanState state = getNodeToolPanel().getValue();
        if (state != null)
            state.applyState( node);
        node.setAutoSized(true);
        return node;
    }
    public static LWNode createNode()
    {
        return createNode(null);
    }
    
    static LWNode initTextNode(LWNode node)
    {
        node.setIsTextNode( true);
        node.setAutoSized(true);
        node.setShape(new java.awt.geom.Rectangle2D.Float());
        node.setStrokeWidth(0f);
        node.setFillColor(COLOR_TRANSPARENT);
        node.setFont(LWNode.DEFAULT_TEXT_FONT);
        
        return node;
    }
    
    public static LWNode createTextNode(String text)
    {
        LWNode node = new LWNode();
        initTextNode(node);
        VueBeanState state = TextTool.getTextToolPanel().getValue();
        if (state != null)
            state.applyState( node);
        return node;
    }
    
    public static NodeTool.SubTool getActiveSubTool()
    {
        return (SubTool) getTool().getSelectedSubTool();
    }

    private static final Color ToolbarColor = VueResources.getColor("toolbar.background");
    
    public static class SubTool extends VueSimpleTool
    {
        private Class shapeClass = null;
        private RectangularShape cachedShape = null;
            
        public SubTool() {}

	public void setID(String pID) {
            super.setID(pID);
            //System.out.println(this + " ID set");
            //getShape(); // cache it for fast response first time
            setGeneratedIcons(new ShapeIcon());
        }

        public RectangularShape getShapeInstance()
        {
            if (shapeClass == null) {
                String shapeClassName = getAttribute("shapeClass");
                //System.out.println(this + " got shapeClass " + shapeClassName);
                try {
                    this.shapeClass = getClass().getClassLoader().loadClass(shapeClassName);
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }
            RectangularShape rectShape = null;
            try {
                rectShape = (RectangularShape) shapeClass.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return rectShape;
        }
        
        public RectangularShape getShape()
        {
            if (cachedShape == null)
                cachedShape = getShapeInstance();
            return cachedShape;
                    
        }

        static final int nearestEven(double d)
        {
            if (Math.floor(d) == d && d % 2 == 1) // if exact odd integer, just increment
                return (int) d+1;
            if (Math.floor(d) % 2 == 0)
                return (int) Math.floor(d);
            else
                return (int) Math.ceil(d);
        }
        static final int nearestOdd(double d)
        {
            if (Math.floor(d) == d && d % 2 == 0) // if exact even integer, just increment
                return (int) d+1;
            if (Math.floor(d) % 2 == 1)
                return (int) Math.floor(d);
            else
                return (int) Math.ceil(d);
        }
        
        private static final Color sShapeColor = new Color(165,178,208); // melanie's steel blue
        private static int sWidth;
        private static int sHeight;

        static {

            // Select a width/height that will perfectly center within
            // the parent button icon.  If parent width is even, our
            // width should be even, if odd, we should be odd.  This
            // is independent of the 50% size of the parent button
            // we're using as a baseline (before the pixel tweak).
            // This also means if somebody goes to center us in the
            // parent (ToolIcon), that computation will always have an
            // even integer result, thus perfectly pixel aligned.
            
            if (ToolIcon.width % 2 == 0)
                sWidth = nearestEven(ToolIcon.width / 2);
            else
                sWidth = nearestOdd(ToolIcon.width / 2);
            if (ToolIcon.height % 2 == 0)
                sHeight = nearestEven(ToolIcon.height / 2);
            else
                sHeight = nearestOdd(ToolIcon.height / 2);
        }
        
        class ShapeIcon implements Icon
        {
            public int getIconWidth() { return sWidth; }
            public int getIconHeight() { return sHeight; }

            private RectangularShape mShape;
            ShapeIcon()
            {
                mShape = getShapeInstance();
                if (mShape instanceof RoundRectangle2D) {
                    // hack to deal with arcs being too small on a tiny icon
                    ((RoundRectangle2D)mShape).setRoundRect(0, 0, sWidth,sHeight, 8,8);
                } else
                    mShape.setFrame(0,0, sWidth,sHeight);
            }
            
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g;
                mShape.setFrame(x, y, sWidth, sHeight);
                g2.setColor(sShapeColor);
                g2.fill(mShape);
                g2.setColor(Color.black);
                g2.draw(mShape);
            }

            public String toString() {
                return "ShapeIcon[" + mShape + "]";
            }
        }

        
    }
}
