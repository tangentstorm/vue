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

import java.awt.Color;
import java.awt.Shape;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Rectangle;
import java.awt.AlphaComposite;
import java.awt.geom.Rectangle2D;
import java.awt.geom.AffineTransform;

/**
 * Includes a Graphics2D context and adds VUE specific flags and helpers
 * for rendering a tree of LWComponents.
 *
 * @version $Revision: 1.35 $ / $Date: 2007-04-14 22:36:59 $ / $Author: sfraize $
 * @author Scott Fraize
 *
 */
public class DrawContext
{
    public final Graphics2D g;
    public final double zoom;
    public final boolean drawAbsoluteLinks = false;
    
    public final float offsetX;
    public final float offsetY;
    
    
    private int index;
    private int maxLayer = Short.MAX_VALUE; // don't draw layers above this level
    private boolean disableAntiAlias = false;
    private boolean isInteractive = false;
    private boolean isDraftQuality = false;
    private boolean isBlackWhiteReversed = false;
    private boolean isPresenting = false;
    private boolean isEditMode = false;
    public final Rectangle frame; // if we have the pixel dimensions of the surface we're drawing on, they go here

    public LWComponent focal;

    private float alpha = 1f;

    private VueTool activeTool;

    //private boolean inMapDraw = false;
    private final Shape rawClip;
    private final AffineTransform rawTransform;
    private final AffineTransform mapTransform;
    private Rectangle2D masterClipRect; // for drawing map nodes
    

    // todo: consider including a Conatiner arg in here, for
    // MapViewer, etc.  And replace zoom with a getZoom
    // that grabs transform scale value.

    // todo: move coord mappers from MapViewer to here?

    public DrawContext(Graphics _g, double zoom, float offsetX, float offsetY, Rectangle frame, LWComponent focal, boolean absoluteLinks)
    {
        this.g = (Graphics2D) _g;
        this.zoom = zoom;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.frame = frame;
        this.focal = focal;

        this.rawClip = g.getClip();
        //this.g.translate(frame.x, frame.y);
        this.rawTransform = g.getTransform();
        this.g.translate(offsetX, offsetY);
        this.g.scale(zoom, zoom);
        this.mapTransform = g.getTransform();
        setMasterClip(g.getClip());

        
        //setMasterClip(rawClip = g.getClip());
        
        //this.drawAbsoluteLinks = absoluteLinks;
        //setPrioritizeSpeed(true);
        //disableAntiAlias(true);
    }
    
    public DrawContext(Graphics g, double zoom)
    {
        this(g, zoom, 0, 0, (Rectangle) null, (LWComponent) null, false);
    }
    public DrawContext(Graphics g)
    {
        this(g, 1.0);
    }

    /** set up for drawing a model: adjust to the current zoom and offset.
     * MapViewer, MapPanner, VueTool, etc, to use.*/
    // todo: change to single setMapDrawing(boolean)
    /*
    public void setMapDrawing() {
        if (rawTransform != null)
            throw new Error("DrawContext: map paramaters already established");
        
        //if (!inMapDraw) {
            rawTransform = g.getTransform();
            g.translate(offsetX, offsetY);
            g.scale(zoom, zoom);
            mapTransform = g.getTransform();
            setMasterClip(g.getClip());
            //System.out.println("DC SCALE TO " + zoom);
            //System.out.println("DC SCALE TO " + g.getTransform());
            //inMapDraw = true;
            //}
    }
    public void resetMapDrawing() {
        if (mapTransform != null)
            g.setTransform(mapTransform);
        else
            throw new Error("DrawContext: initial map transform not established");
    }
    public void setRawDrawing() {
        //if (inMapDraw) {
            if (rawTransform == null)
                throw new IllegalStateException("attempt to revert to raw draw in a derivative DrawContext");
            //System.out.println("DC REVER TO " + savedTransform);
            g.setTransform(rawTransform);
            //setMasterClip(rawClip);
            //            inMapDraw = false;
            //        }
    }
    
    */
    
    public void setMapDrawing() {
        g.setTransform(mapTransform);
    }
    public void setRawDrawing() {
        g.setTransform(rawTransform);
    }
    
    public void setFrameDrawing() {
        g.setTransform(rawTransform);
        g.translate(frame.x, frame.y);
    }
        
    public void setMasterClip(Shape clip)
    {
        g.setClip(clip);
        if (clip instanceof Rectangle2D) {
            masterClipRect = (Rectangle2D) clip;
            //if (DEBUG.PAINT) System.out.println("MASTER CLIP RECT2D=" + tufts.Util.out(masterClipRect));
        } else {
            // we've set the shaped clip in the gc, now extract the master clip rectangle from the gc
            masterClipRect = g.getClipBounds();
            if (DEBUG.PAINT || DEBUG.CONTAINMENT) {
                System.out.println("SET SHAPE CLIP: " + clip);
                //System.out.println("MASTER CLIP RECT2D: " + Util.out(masterClipRect));
            }
        }
        if (DEBUG.PAINT || DEBUG.CONTAINMENT)
            System.out.println("MASTER CLIP RECT2D=" + tufts.Util.out(masterClipRect));
    }

    public Rectangle2D getMasterClipRect() {
        return masterClipRect;
    }

    public void setMaxLayer(int layer) {
        maxLayer = layer;
    }
    public int getMaxLayer() {
        return maxLayer;
        //return 99;
    }

    /*
    public setFrame(Rectangle frame) {
        this.frame = frame;
    }
    */
    public Rectangle getFrame() {
        return new Rectangle(frame);
    }

    public void setActiveTool(VueTool tool) {
        activeTool = tool;
    }
    public VueTool getActiveTool() {
        return activeTool;
    }

    public void setAlpha(double p_alpha, int alphaRule) {
        this.alpha = (float) p_alpha;
        if (alpha == 1f)
            g.setComposite(AlphaComposite.Src);
        else
            g.setComposite(AlphaComposite.getInstance(alphaRule, this.alpha));
        // todo: cache the alpha instance
    }

    public void setAlpha(double alpha) {
        setAlpha(alpha, AlphaComposite.SRC_OVER);
    }
    
    public void checkComposite(LWComponent c) {
        if (alpha != 1f) {
            
            // if we're going to do a non-opaque fill during a general transparent
            // rendering situation, change temporarily to the SRC_OVER rule instead of
            // SRC, so that what's under the translucent node will show through.  If we
            // just left it SRC, color values that had an alpha channel would end up
            // blowing away what's underneath them.

            final Color fill = c.getRenderFillColor();
            
            if (fill != null && fill.getAlpha() != 255)
                setAlpha(alpha, AlphaComposite.SRC_OVER);
            else
                setAlpha(alpha, AlphaComposite.SRC);
        }
    }
    

    //public float getAlpha() { return mAlpha; }
    
    /**
     * Mark us as rendering for interactive usage: e.g., selection will be drawn.
     */
    public void setInteractive(boolean t) {
        isInteractive = t;
    }

    public boolean isInteractive() {
        return isInteractive;
    }

    public boolean isBlackWhiteReversed() {
        return isBlackWhiteReversed;
    }

    public void setBlackWhiteReversed(boolean t) {
        isBlackWhiteReversed = t;
    }

    public void setPresenting(boolean t) {
        isPresenting = t;
    }

    public boolean isPresenting() {
        return isPresenting;
    }

    /** an extra flag to specify a special kind of "editing" mode, that may draw extra items */
    public void setEditMode(boolean t) {
        isEditMode = t;
    }

    public boolean isEditMode() {
        return isEditMode;
    }

    public boolean drawPathways() {
        return true;
        //return !isPresenting() && focal instanceof LWMap;
        //    return !isFocused && !isPresenting();
    }

    public boolean isFocused() {
        return focal instanceof LWMap == false;
    }

    public void setDraftQuality(boolean t) {
        isDraftQuality = t;
    }

    public boolean isDraftQuality() {
        return isDraftQuality;
    }

    public void disableAntiAlias(boolean tv)
    {
        this.disableAntiAlias = tv;
        if (tv)
            setAntiAlias(false);
    }

    /* passthru to Graphcs.setColor.  This method available for override 
    public void setColor(Color c) {
        g.setColor(c);
    }*/
        
    /** Turn on or off text & shape anti-aliasing */
    public void setAntiAlias(boolean on)
    {
        if (disableAntiAlias)
            on = false;
        
        Object shapeVal = on ? RenderingHints.VALUE_ANTIALIAS_ON : RenderingHints.VALUE_ANTIALIAS_OFF;
        Object textVal = on ? RenderingHints.VALUE_TEXT_ANTIALIAS_ON : RenderingHints.VALUE_TEXT_ANTIALIAS_OFF;
        this.g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, shapeVal);
        this.g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, textVal);
    }

    public void setPrioritizeQuality(boolean on)
    {
        this.g.setRenderingHint(RenderingHints.KEY_RENDERING, on
                                ? RenderingHints.VALUE_RENDER_QUALITY
                                : RenderingHints.VALUE_RENDER_SPEED);
    }

    public void setPrioritizeSpeed(boolean on)
    {
        setPrioritizeQuality(!on);
    }
    
    public void setFractionalFontMetrics(boolean on)
    {
        this.g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, on
                                ? RenderingHints.VALUE_FRACTIONALMETRICS_ON
                                : RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
    }

    /** set a stroke width that stays constant on-screen at given
     * width independent of any current scaling (presuming
     * scaling is same in X/Y direction's -- only tracks X scale factor).
     */
    public void setAbsoluteStroke(double width)
    {
        this.g.setStroke(new java.awt.BasicStroke((float) (width / this.g.getTransform().getScaleX())));
    }

    /**
     * An arbitrary counter that can be set & read.
     */
    public void setIndex(int i) {
        this.index = i;
    }
    /**
     * An arbitrary counter that can be set & read.
     */
    public int getIndex() {
        return this.index;
    }

    // todo: make this safer
    public void setAbsoluteDrawing(boolean unZoom) {
        if (unZoom)
            g.scale(1/zoom, 1/zoom);
        else
            g.scale(zoom, zoom);
    }


    public DrawContext create()
    {
        return new DrawContext(this);
    }

    public String toString() {
        return String.format("DrawContext[zoom=%.2f mapOffset=%.1f,%.1f focal=%s]", zoom, offsetX, offsetY, focal);
    }
    
    // todo: replace with a faster clone op?
    public DrawContext(DrawContext dc)
    {
        //System.out.println("transform before dupe: " + dc.g.getTransform());
        this.g = (Graphics2D) dc.g.create();
        //System.out.println("transform after  dupe: " + g.getTransform());
        this.zoom = dc.zoom;
        this.offsetX = dc.offsetX;
        this.offsetY = dc.offsetY;
        this.disableAntiAlias = dc.disableAntiAlias;
        this.index = dc.index;
        this.isInteractive = dc.isInteractive;
        this.isDraftQuality = dc.isDraftQuality;
        this.isBlackWhiteReversed = dc.isBlackWhiteReversed;
        this.isPresenting = dc.isPresenting;
        this.activeTool = dc.activeTool;
        //this.inMapDraw = dc.inMapDraw;
        this.mapTransform = dc.mapTransform;
        this.frame = dc.frame;
        this.focal = dc.focal;
        this.alpha = dc.alpha;
        //this.drawAbsoluteLinks = dc.drawAbsoluteLinks;
        this.maxLayer = dc.maxLayer;
        this.isEditMode = dc.isEditMode;
        this.rawClip = dc.rawClip;
        this.rawTransform = dc.rawTransform;
        this.masterClipRect = dc.masterClipRect;
        //this.mAlpha = dc.mAlpha;
    }


    /*
    public DrawContext createScaled(float scale)
    {
        DrawContext dc = new DrawContext(this);
        dc.scale *= scale;
        return dc;
    }
    */
}
