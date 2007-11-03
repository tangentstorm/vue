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

import tufts.Util;
import tufts.vue.gui.GUI;
import tufts.vue.NodeTool.NodeModeTool;

import java.util.*;
import java.awt.Color;
import java.awt.Composite;
import java.awt.BasicStroke;
import java.awt.geom.*;

/**
 *
 * Container for displaying slides.
 *
 * @author Scott Fraize
 * @version $Revision: 1.77 $ / $Date: 2007-11-03 20:39:30 $ / $Author: sfraize $
 */
public class LWSlide extends LWContainer
{
    protected static final org.apache.log4j.Logger Log = org.apache.log4j.Logger.getLogger(LWSlide.class);
    
    public static final int SlideWidth = 800;
    public static final int SlideHeight = 600;
    // 640/480 == 1024/768 == 1.333...
    // Keynote uses 800/600 (1.3)
    // PowerPoint defaut 720/540  (1.3)  Based on 72dpi?  (it has a DPI option)
    public static final float SlideAspect = ((float)SlideWidth) / ((float)SlideHeight);
    protected static final int SlideMargin = 30;

    // todo: shouldn't we be able to pick the source node out of mEntry?
    private LWComponent mSourceNode;

    private transient LWPathway.Entry mEntry;

    /** public only for persistance */
    public LWSlide() {
        disableProperty(LWKey.Label);
        disablePropertyTypes(KeyType.STYLE);
        enableProperty(LWKey.FillColor);
        takeSize(SlideWidth, SlideHeight);
        takeScale(LWComponent.SlideIconScale);
    }

    /** set a runtime entry marker for this slide for use in presentation navigation */
    protected void setPathwayEntry(LWPathway.Entry e) {
        mEntry = e;
        if (DEBUG.Enabled && mSourceNode != null && e.node != mSourceNode)
            Util.printStackTrace(this + " sourceNode != entry node! " + mSourceNode + " " + e);
    }
    /** get the LWPathway.Entry for this slide if it is a pathway slide, otherwise null */
    // TODO: only need one of these!
    LWPathway.Entry getPathwayEntry() {
        return mEntry;
    }

    public final LWPathway.Entry getEntry() {
        return mEntry;
    }

    @Override
    public void setNotes(String s) {
        if (mEntry == null) {
            super.setNotes(s);
        } else {
            mEntry.setNotes(s);
            // may need to trigger an event so any listeners will know if this updated, tho
            // current only a single notes panel has access to this, so we can skip it.
        }
    }
    
    @Override
    public String getNotes() {
        if (mEntry == null) {
            return super.getNotes();
        } else {
            return mEntry.getNotes();
        }
    }
    
    
    @Override
    public boolean supportsUserResize() {
        return isMoveable() && DEBUG.Enabled;
    }

    /** @return false: slides can't be selected with anything else */
    public boolean supportsMultiSelection() {
        return isMoveable();
    }

    /** @return false: slides can never have slides */
    @Override
    public final boolean supportsSlide() {
        return false;
    }
 	
    /** @return true: slides never have slide-icon entries of their own */
    @Override
    public final boolean hasEntries() {
        return false;
    }

    @Override
    public final boolean fullyContainsChildren() {
        return true;
    }

    @Override
    public boolean isVisible() {
        if (mEntry != null)
            return super.isVisible() && mEntry.pathway.isShowingSlides();
        else
            return super.isVisible();
    }
    

    /** @return true only if we're not a pathway generated slide */
    public boolean supportsReparenting() {
        if (!super.supportsReparenting())
            return false;
        else
            return mEntry == null;
    }
    
    @Override
    public boolean isPathwayOwned() {
        return mEntry != null;
    }
    
    
    @Override
    public boolean isMoveable() {
        //return getParent() != null && getParent() instanceof LWPathway == false;
        //return getParent() instanceof LWMap; // hack for now -- still not a truly supported feature yet
        return isPathwayOwned() == false;
    }

    /** @return false */
    @Override
    public boolean canLinkToImpl(LWComponent target) {
        return isMoveable();
    }
    

//     /** @return false -- slides themseleves never have slide icons: only nodes that own them */
//     @Override
//     public final boolean isDrawingSlideIcon() {
//         return false;
//     }

//     @Override
//     protected final void addEntryRef(LWPathway.Entry e) {
//         Util.printStackTrace(this + " slides can't addEntryRef " + e);
//     }
//     @Override
//     protected final void removeEntryRef(LWPathway.Entry e) {
//         Util.printStackTrace(this + " slides can't removeEntryRef " + e);
//     }

    @Override
    public int getFocalMargin() {
        return 0;
    }


//     @Override
//     public boolean handleDoubleClick(MapMouseEvent e)
//     {
//         // a hack to do this in the model: better in the viewer and/or selection tool...
//         // TODO: not if we're the focal!

//         final MapViewer viewer = e.getViewer();

//         if (viewer.getFocal() == this)
//             return false;

        
//         final Rectangle2D viewerBounds = viewer.getVisibleMapBounds();
//         final Rectangle2D slideBounds = getMapBounds();
//         final Rectangle2D overlap = viewerBounds.createIntersection(slideBounds);
//         final double overlapArea = overlap.getWidth() * overlap.getHeight();
//         //final double viewerArea = viewerBounds.getWidth() * viewerBounds.getHeight();
//         final double slideArea = slideBounds.getWidth() * slideBounds.getHeight();
//         final boolean slideClipped = overlapArea < slideArea;
        
//         final double overlapWidth = slideBounds.getWidth() / viewerBounds.getWidth();
//         final double overlapHeight = slideBounds.getHeight() / viewerBounds.getHeight();

//         final boolean focusSlide; // otherwise, re-focus map

//         //outf("slideClip=" + slideClipped + " overlapWidth  %4.1f%%", overlapWidth * 100);
//         //outf("slideClip=" + slideClipped + " overlapHeight %4.1f%%", overlapHeight * 100);
        
//         if (slideClipped) {
//             focusSlide = true;
//         } else if (overlapWidth > 0.8 || overlapHeight > 0.8) {
//             focusSlide = false;
//         } else
//             focusSlide = true;

//         if (focusSlide) {
//             viewer.clearRollover();
//             ZoomTool.setZoomFitRegion(viewer,
//                                       getMapBounds(),
//                                       -LWPathway.PathBorderStrokeWidth / 2,
//                                       true);
//         } else {
//             // re-focus the map:
//             viewer.fitToFocal(true);
//         }
        
//         return true;
//     }
    
    
    
    @Override
    protected void setParent(LWContainer parent) {
        super.setParent(parent);
        if (mEntry == null && parent instanceof LWPathway == false) { // parent will be pathway for master slides
            // if no longer a slide icon (is directly part of a map), set a fill if we didn't have one
            if (getFillColor() == null)
                setFillColor(Color.gray);
            takeScale(0.5);
            enableProperty(LWKey.Label);            
        }
        
//         if (parent instanceof LWPathway) {
//             ; // default
//         } else {
//             // This should only ever happen if a slide is drag-copied onto the map
//             if (getFillColor() == null)
//                 setFillColor(Color.black);
//         }
    }


//     protected LWComponent getSourceNode() {
//         return mEntry == null ? null : mEntry.node;
//     }
    protected LWComponent getSourceNode() {

        // todo: clean this up: should only need an entry, not a special source node
        if (DEBUG.Enabled && mEntry != null && mSourceNode != null && mEntry.node != mSourceNode) {
            Util.printStackTrace("sourceNode != entry node! srcNode=" + mSourceNode + " entry=" + mEntry);
        }
        if (mEntry == null) {
            if (DEBUG.Enabled) Util.printStackTrace(this + " source node w/no entry: " + mSourceNode);
            return mSourceNode;
        } else
            return mEntry.node;
    }
    protected void setSourceNode(LWComponent node) {
        mSourceNode = node;
    }

    
    /** implemented to return the bg color of the master slide (for proper on-slide text edit fill color) */
    @Override
    public Color getRenderFillColor(DrawContext dc) {
         if (mFillColor.isTransparent()) {
             final LWSlide master = getMasterSlide();
             if (master == null)
                 return getFillColor();
             else
                 return master.getFillColor();
         } else
            return getFillColor();
    }

    @Override
    public String getLabel() {

        if (supportsProperty(LWKey.Label))
            return super.getLabel();
        
        if (mEntry != null) {
            if (getSourceNode() == null)
                return "Slide in " + mEntry.pathway.getDisplayLabel();
            else
                return "Slide for " + getSourceNode().getDisplayLabel() + " in " + mEntry.pathway.getDisplayLabel();
        } else
            return "<LWSlide:initializing>"; // true during persist restore
    }

    /** create a default LWSlide */
    public static LWSlide Create()
    {
        final LWSlide s = new LWSlide();
        s.setFillColor(new Color(0,0,0,64));
        s.setStrokeWidth(1);
        s.setStrokeColor(Color.black);
        s.setSize(SlideWidth, SlideHeight);
        //setAspect(((float)GUI.GScreenWidth) / ((float)GUI.GScreenHeight));
        s.setAspect(SlideAspect);
        return s;
    }

    public static LWSlide CreatePathwaySlide()
    {
        final LWSlide s = Create();
        s.setStrokeWidth(0f);
        s.setFillColor(null);
        return s;
    }
    
    public static LWSlide CreateForPathway(LWPathway pathway, LWComponent node) {
        return CreateForPathway(pathway, node.getDisplayLabel(), node, node.getAllDescendents(), false);
    }
        
    public static LWSlide CreateForPathway(LWPathway pathway,
                                           String titleText,
                                           LWComponent mapNode,
                                           Iterable<LWComponent> contents,
                                           boolean syncTitle) 
    {
        final LWSlide slide = CreatePathwaySlide();
        final LWNode title = NodeModeTool.buildTextNode(titleText);
        final MasterSlide master = pathway.getMasterSlide();
        final CopyContext cc = new CopyContext(false);
        final LinkedList<LWComponent> toLayout = new java.util.LinkedList();

        slide.setSourceNode(mapNode);
        title.setStyle(master.getTitleStyle());
        // if (syncTitle) title.setSyncSource(slide); doesn't seem to work in this direction (reverse is okay, but not what we want)
        if (mapNode != null) {
            //if (mapNode.isImageNode())
            if (LWNode.isImageNode(mapNode) || mapNode instanceof LWImage)
                ; // don't sync titles of images
            else
                title.setSyncSource(mapNode);
        }

        for (LWComponent c : contents) {
            if (LWNode.isImageNode(c) || c instanceof LWLink) {
                Log.debug("IGNORING " + c);
                
                // ignore the node itself -- just use it's image, which
                // will already be in the list as it's first child

                // ignore links
                
                continue;
            }
            Log.debug(" COPYING " + c);
            final LWComponent copyForSlide = c.duplicate(cc);
            copyForSlide.setScale(1);
            applyMasterStyle(master, copyForSlide);
            copyForSlide.setSyncSource(c);
            toLayout.add(copyForSlide);
        }

        toLayout.addFirst(title);
        
        //slide.setParent(pathway); // must do before import
        slide.importAndLayout(toLayout);
        pathway.ensureID(slide);
        
        //slide.setLocked(true);
        
        return slide;
    }

    private void importAndLayout(List<LWComponent> nodes)
    {
        final LWComponent title = nodes.get(0);
        nodes.remove(nodes.get(0));

        title.setLocation(SlideMargin, SlideMargin);
        addChildImpl(title);

        final List<LWImage> images = new ArrayList();
        final List<LWComponent> text = new ArrayList();

        for (LWComponent c : nodes) {
            if (c instanceof LWImage)
                images.add((LWImage)c);
            else
                text.add(c);
        }

        int x, y;

        // Add as children, providing seed relative locations
        // for layout, and establish z-order (images under text)
        // Nodes will be auto-styled with the master slide style
        // in addChildImpl.

        x = y = SlideMargin;
        for (LWComponent c : images) {
            c.takeLocation(x++,y++);
            addChildImpl(c);
        }

        // differences in font sizes mean text below title looks to left of title unless slighly indented
        final int textIndent = 2;
        
        x = SlideMargin + textIndent; 
        y = SlideMargin * 2 + (int) title.getHeight();
        for (LWComponent c : text) {
            c.takeLocation(x++,y++);
            addChildImpl(c);
        }
            
        if (DEBUG.PRESENT || DEBUG.STYLE) out("LAYING OUT CHILDREN, parent=" + getParent());

        setSize(SlideWidth, SlideHeight);

        // layout not currently working unless we force the scale temporarily to 1.0 map
        // bounds are computed for contents, which are tiny if scale is small, as
        // opposed to local bounds.  Arrange actions need to figure out which bounds to
        // use -- best guess would be local bounds, but all would have to have same
        // parent...
        // takeScale(1.0); // 2007-11-02 Should no longer be needed: arrange actions use local bounds
        
        final LWSelection selection = new LWSelection(nodes);
        
        if (text.size() > 1) {

            // Better to distribute text items in their total height + a border IF they'd all fit on the slide:
            // selection.setSize((int) bounds.getWidth(),
            //                   (int) bounds.getHeight() + 15 * selection.size());

            selection.setSize(SlideWidth - SlideMargin*2,
                              (int) (getHeight() - SlideMargin*1.5 - text.get(0).getY()));
            selection.setTo(text);
            Actions.DistributeVertically.act(selection);
            Actions.AlignLeftEdges.act(selection);
        }
        
        if (images.size() == 1) {
            final LWImage image = images.get(0);

            image.userSetSize(0, getHeight() - SlideMargin * 4, null); // aspect preserving
            image.setLocation(getWidth() - image.getWidth() - SlideMargin * 2, SlideMargin * 2);
                
        } else if (images.size() > 1) {

            selection.setSize(SlideWidth - SlideMargin*2, SlideHeight - SlideMargin*2);

            final float commonHeight = (selection.getHeight()-((images.size()-1)*SlideMargin)) / images.size();

            //out("common height " + commonHeight);

            for (LWImage image : images)
                image.userSetSize(0, commonHeight, null); // aspect preserving

            selection.setTo(images);
            Actions.DistributeVertically.act(selection);
            
            for (LWImage image : images) {
                float imageX = getWidth() - image.getWidth() - SlideMargin;
                image.setLocation(imageX, image.getY());
            }

            //if (images.size() > 1) Actions.AlignLeftEdges.act(selection);
            
        }

        takeScale(SlideIconScale);
    }

    protected enum Sync { ALL, TO_NODE, TO_SLIDE };
    
    public void synchronizeAll() {
        synchronizeResources(Sync.ALL);
    }
    public void synchronizeSlideToNode() {
        synchronizeResources(Sync.TO_NODE);
    }
    public void synchronizeNodeToSlide() {
        synchronizeResources(Sync.TO_SLIDE);
    }

    public boolean canSync() {
        return mEntry != null && !mEntry.isMapView();
    }

    // TODO: this code should be on LWPathway.Entry, as it's entirely
    // dependent upon the relationship between a node and a slide
    // established by the pathway entry.
    protected void synchronizeResources(Sync type) {

        if (getSourceNode() == null) {
            Log.warn("Can't synchronize a slide w/out a source node: " + this);
            return;
        }

        if (getEntry() != null && getEntry().isMapView()) {
            Util.printStackTrace("cannot synchronize virtual slides");
            return;
        }

        final LWComponent node = getSourceNode();

        if (DEBUG.Enabled) outf("NODE/SILDE SYNCHRONIZATION; type(%s) ---\n\t NODE: %s\n\tSLIDE: %s", type, node, this);
        
        final Set<Resource> slideUnique = new HashSet();
        final Set<Resource> nodeUnique = new HashSet();

        // First add all resources in any descendent of the node to nodeUnique (include
        // the node's resource itself), then iterate through all the resources found
        // anywhere inside the slide, removing duplicates from nodeUnique, and adding the
        // remainder to slideUnique.

        if (node.hasResource())
            nodeUnique.add(node.getResource());
        for (LWComponent c : node.getAllDescendents())
            if (c.hasResource())
                nodeUnique.add(c.getResource());

        if (DEBUG.Enabled) {
            for (Resource r : nodeUnique)
                outf("%50s: %s", "UNIQUE NODE RESOURCE", Util.tags(r));
        }
            
        final Set<Resource> nodeDupes = new HashSet();

        for (LWComponent c : this.getAllDescendents()) {
            if (c.hasResource()) {
                final Resource r = c.getResource();
                if (nodeUnique.contains(r)) {
                    nodeDupes.add(r);
                    if (DEBUG.Enabled) outf("%30s: %s", "ALREADY ON NODE, IGNORE FOR SLIDE", Util.tags(r));
                } else {
                    if (slideUnique.add(r)) {
                        if (DEBUG.Enabled) outf("%30s: %s", "ADDED UNIQUE SLIDE", Util.tags(r));
                    }
                }
            }
        }

        nodeUnique.removeAll(nodeDupes);

        if (DEBUG.Enabled) {
            //this.outf("  NODE DUPES: " + nodeDupes + "\n");
            this.outf("SLIDE UNIQUE: " + slideUnique);
            node.outf(" NODE UNIQUE: " + nodeUnique);
        }
        
        if (type == Sync.ALL || type == Sync.TO_NODE) {
            for (Resource r : slideUnique) {
                // TODO: merge MapDropTarget & NodeModeTool node creation code into NodeTool, including resource handling
                final LWNode newNode = new LWNode(r.getTitle(), r);
                node.addChild(newNode);
            }
        }
        
        if (type == Sync.ALL || type == Sync.TO_SLIDE) {
            for (Resource r : nodeUnique) {
                final LWComponent newNode;
                if (r.isImage())
                    newNode = new LWImage(r);
                else
                    newNode = new LWNode(r.getName(), r);
                this.addChild(newNode);
            }
        }
    }

    /** slides never considered translucent: they're not on the map needing backfill when they're the focal */
    @Override
    public boolean isTranslucent() {
        return false;
    }

    // todo: won't be able to use this when we allow variable sized slides...
    private static final Rectangle2D SlideZeroShape = new Rectangle2D.Float(0, 0, SlideWidth, SlideHeight);

    @Override
    public final java.awt.Shape getZeroShape() {
        if (mEntry == null)
            return super.getZeroShape(); // if entry is null, is an on-map slide that may have been resized
        else
            return SlideZeroShape; // as slides all same size for now, can use a constant zeroShape
    }

    @Override
    protected void drawImpl(DrawContext dc)
    {
//         final LWSlide master = getMasterSlide();
//         final Color fillColor = getRenderFillColor(dc);

//         //if (dc.focal != this && dc.isInteractive() && (isSelected() || dc.isPresenting()) && isDrawn()) {
//         if (dc.focal != this && (isSelected() || dc.isPresenting()) && isDrawn()) {
            
//             // we check isDrawn above because we may actually be hidden (no slide icons),
//             // but forced drawn temporarily by the selection ghosting code, in which case
//             // we don't want to bother with this transparent stroke: just messy then
            
//             if (mEntry != null) {
//                 //final Composite savedComposite = dc.g.getComposite();
//                 //dc.g.setComposite(LWPathway.PathTranslucence);
//                 dc.g.setColor(mEntry.pathway.getColor());
                
// //                 final BasicStroke s = LWPathway.ConnectorStroke;
// //                 final float scale = getScaleF();
// //                 dc.g.setStroke(new BasicStroke(s.getLineWidth() * 1 / scale,
// //                                     //BasicStroke.CAP_BUTT,
// //                                     s.getEndCap(),
// //                                     BasicStroke.JOIN_ROUND,
// //                                     s.getMiterLimit(),
// //                                     //LWPathway.DashPattern,
// //                                     new float[] { 8/scale, 8/scale },
// //                                     //s.getDashArray(),
// //                                     s.getDashPhase());

//                 // todo: cache this for the common case (we know the fixed icon slide scale)
//                 dc.g.setStroke(new BasicStroke((float) (LWPathway.PathBorderStrokeWidth * 1 / getScale()),
//                                                BasicStroke.CAP_ROUND,
//                                                BasicStroke.JOIN_ROUND));
//                 dc.g.draw(getZeroShape());
//                 //dc.g.setComposite(savedComposite);
//             } else {
//                 dc.g.setColor(COLOR_HIGHLIGHT);
//                 dc.setAbsoluteStroke(getStrokeWidth() + SelectionStrokeWidth);
//                 dc.g.draw(getZeroShape());
//             }
//             //dc.setAbsoluteStroke(SelectionStrokeWidth);
//             //dc.g.setStroke(new java.awt.BasicStroke((float) ((1/getScale()) * (getStrokeWidth() + SelectionStrokeWidth))));
//             //dc.setAbsoluteStroke(getStrokeWidth() + SelectionStrokeWidth);
//             //dc.g.draw(getZeroShape());
//         }

        final LWSlide master = getMasterSlide();
        final Color fillColor = getRenderFillColor(dc);

        if (mEntry == null && isSelected() && dc.isInteractive()) {
            // for on-map slides only: drag regular selection border if selection
            dc.g.setColor(COLOR_HIGHLIGHT);
            dc.setAbsoluteStroke(getStrokeWidth() + SelectionStrokeWidth);
            dc.g.draw(getZeroShape());
        }

        if (fillColor == null)
            Util.printStackTrace("null fill " + this);
        else
            dc.fillArea(getZeroShape(), fillColor);
        

//         if (dc.focal != this || fillColor != dc.getFill() || !fillColor.equals(dc.getFill())) {
//             dc.g.setColor(fillColor);
//             dc.g.fill(getZeroShape());
//         }

        // When just filling the background with the master, only draw
        // what's in the containment box
        //dc.g.setClip(master.getBounds());
        //master.drawChildren(dc);
        //dc.g.setClip(curClip);

        
        if (master != null) {
            // As the master slide isn't in the model, sit's children can't succesfully know
            // their bounds anyway, so we can't clip-optimize further when we draw it.
            // (It would be of little help anyway)
            final DrawContext masterDC = dc.create();
            masterDC.setClipOptimized(false);

            // We only draw the master's children, as we've already
            // done our background fill:
            master.drawChildren(masterDC);
        }

        // Now draw the slide contents:
        drawChildren(dc);
    }


//     /**
//      * Special case for squeezing a slide into a particular
//      * frame draw context -- e.g., used for drawing a master
//      * slide as the background to an arbitrary context.
//      */
//     public void drawIntoFrame(DrawContext dc)
//     {
//         //dc = dc.create();
//         dc.push();
//         dc.setFrameDrawing();
//         final Point2D.Float offset = new Point2D.Float();
//         final double zoom = ZoomTool.computeZoomFit(dc.frame.getSize(), 0, getBounds(), offset);
//         dc.g.translate(-offset.x, -offset.y);
//         dc.g.scale(zoom, zoom);
//         if (DEBUG.BOXES || DEBUG.PRESENT || DEBUG.CONTAINMENT) {
//             dc.g.setColor(Color.green);
//             dc.g.setStroke(VueConstants.STROKE_TWO);
//             dc.g.draw(getZeroShape());
//         }
//         drawZero(dc);
//         dc.pop();
//     }
    
    public void rebuild() {
        
    }

    public void revertToMasterStyle() {
        //out("REVERTING TO MASTER STYLE");
        for (LWComponent c : getAllDescendents()) {
            LWComponent style = c.getStyle();
            if (style != null) {
                c.copyStyle(style);
            } else {
                // TODO: shouldn't really need this, but if anything gets detached from it's
                // style, this should re-attach it, tho we need a bit in the node
                // to know if we never want to do this: e.g. we always want a node
                // to stay "regular" node on the slide.
                applyMasterStyle(c);
            }
        }
        setFillColor(null); // this is how we revert a slide's bg color to that of the master slide
    }

    @Override
    protected void addChildImpl(final LWComponent c)
    {
        // TODO: need to apply proper style w/generic code
        
        if (this instanceof MasterSlide) {
            super.addChildImpl(c);
            return;
        }
        
        if (DEBUG.PRESENT || DEBUG.STYLE) out("addChildImpl " + c);

        if (c.getStyle() == null)
            applyMasterStyle(c);

        super.addChildImpl(c);

        // TODO: need a size request for LWImage, as the image itself
        // may not be loaded yet (or just auto-handle this in userSetSize,
        // or setSize or something.
        if (c instanceof LWImage) {
            ((LWImage)c).userSetSize(SlideWidth / 3, SlideWidth / 3, null); // more forgiving when image aspect close to slide aspect
//             addCleanupTask(new Runnable() { public void run() {
//                 ((LWImage)c).userSetSize(SlideWidth / 3, SlideWidth / 3, null); // more forgiving when image aspect close to slide aspect
//             }});
//             GUI.invokeAfterAWT(new Runnable() { public void run() {
//                     ((LWImage)c).userSetSize(SlideWidth / 3, SlideWidth / 3, null); // more forgiving when image aspect close to slide aspect
//                   //((LWImage)c).userSetSize(SlideWidth / 3, SlideHeight / 3, null);
//             }});
        }
        
        /*
        LWPathway pathway = (LWPathway) getParent();
        if (pathway != null && c.getStyle() == null)
            c.setStyle(pathway.getMasterSlide().textStyle);
        */
    }

//     /** Return true if our parent is the given pathway (as slides are currently owned by the pathway).
//      * If our parent is NOT a pathway, use the default impl.
//      */
    @Override
    public boolean inPathway(LWPathway p)
    {
        if (mEntry == null)
            return super.inPathway(p);
        else
            return mEntry.pathway == p;
//         if (getParent() instanceof LWPathway)
//             return getParent() == p;
//         else
//             return super.inPathway(p);
    }
    

    private void applyMasterStyle(LWComponent c) {
        applyMasterStyle(getMasterSlide(), c);
    }
            
    private static void applyMasterStyle(MasterSlide master, LWComponent c) {
        if (master == null) {
            Log.error("null master slide applying master style to " + c);
            return;
        }
        if (c.hasResource())
            c.setStyle(master.getLinkStyle());
        //else if (c instanceof LWNode && ((LWNode)c).isTextNode())
        else if (c instanceof LWNode)
            c.setStyle(master.getTextStyle());
        else if (c instanceof LWText)
            c.setStyle(master.getTextStyle());
    }

    public MasterSlide getMasterSlide() {

        if (mEntry == null)
            return null;
        else
            return mEntry.pathway.getMasterSlide();
    }


    @Override
    public boolean isPresentationContext() {
        return true;
    }

    @Override
    public LWComponent duplicate(CopyContext cc)
    {
        LWSlide newSlide = (LWSlide) super.duplicate(cc);

        if (newSlide.mEntry == null && newSlide.isTransparent() && getMasterSlide() != null) {
            // a dupe of an on-pathway slide will have no fill -- if it ended up
            // w/out an entry (the default), grab it's last fill color.
            newSlide.setFillColor(getMasterSlide().getFillColor());
        }

        return newSlide;
    }
    
    

    @Override
    protected LWComponent pickChild(PickContext pc, LWComponent c) {
        //if (DEBUG.PICK) out("PICKING CHILD: " + c);
        if (pc.root == this || (!SwapFocalOnSlideZoom && pc.dc.zoom > 4))
            return c;
        else
            return this;
    }

//     /** @return this slide */
//     @Override
//     protected LWComponent defaultPick(PickContext pc) {
//         //if (DEBUG.PRESENT) out("DEFAULT PICK: THIS");
//         return this;
// //         LWComponent dp = (pc.dropping == null ? null : this);
// //         out("DEFAULT PICK: " + dp);
// //         return dp;
//     }

    @Override
    protected LWComponent defaultDropTarget(PickContext pc) {
        LWComponent c = super.defaultDropTarget(pc);
        if (DEBUG.PRESENT) out("DEFAULT DROP TARGET: " + c);
        return c;
    }
    
}
    
    
