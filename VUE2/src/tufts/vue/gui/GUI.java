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


package tufts.vue.gui;

import tufts.Util;
import tufts.vue.VueUtil;
import tufts.vue.VUE;
import tufts.vue.VueResources;
import tufts.vue.DEBUG;

import java.awt.*;
import java.awt.event.*;
import java.awt.dnd.*;
import java.awt.datatransfer.DataFlavor;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.metal.MetalLookAndFeel;

/**
 * Various constants for GUI variables and static method helpers.
 *
 * @version $Revision: 1.3 $ / $Date: 2006-01-27 02:59:52 $ / $Author: sfraize $
 * @author Scott Fraize
 */

// todo: move most of VueConstants to here
public class GUI
    implements tufts.vue.VueConstants/*, java.beans.PropertyChangeListener*/
{
    /** the special name AWT/Swing gives to pop-up Windows (menu's, rollovers, etc) */
    public static final String OVERRIDE_REDIRECT = "###overrideRedirect###";
    public static final String POPUP_NAME = "###VUE-POPUP###";

    public static final Color AquaFocusBorderLight = new Color(157, 191, 222);
    public static final Color AquaFocusBorderDark = new Color(137, 170, 201);
    
    public static boolean UseAlwaysOnTop = false;
    
    static Toolkit GToolkit;
    static GraphicsEnvironment GEnvironment;
    static GraphicsDevice GDevice;
    static GraphicsConfiguration GConfig;
    static Rectangle GBounds;
    static Rectangle GMaxWindowBounds;
    public static Insets GInsets;
    public static int GScreenWidth;
    public static int GScreenHeight;

    private static Window FullScreenWindow;

    private static boolean initUnderway = true;
    private static boolean isMacAqua;
    private static boolean isMacAquaBrushedMetal;
    private static boolean isOceanTheme = false;
    private static javax.swing.plaf.metal.MetalTheme Theme;

    private static Color ToolbarColor = null;
    static final Color VueColor = new ColorUIResource(VueResources.getColor("menubarColor"));
    //private static final Color VueColor = new ColorUIResource(Color.orange);
    
    public static Color getVueColor() {
        return VueColor;
    }
    
    public static Color getToolbarColor() {
        if (initUnderway) throw new InitError();
        return ToolbarColor;
    }
    
    public static void applyToolbarColor(Component c) {
        if (!isMacAqua)
            c.setBackground(getToolbarColor());
    }
    
    public static Color getTextHighlightColor() {
        if (initUnderway) throw new InitError();
        if (Theme == null)
            return Color.yellow;
        else
            return Theme.getTextHighlightColor();
    }
    
    private static Color initToolbarColor() {
        if (isMacAqua) {
            //if (true) return new ColorUIResource(Color.red);
            if (false && isMacAquaBrushedMetal) {
                // FYI/BUG: Mac OS X 10.4+ Java 1.5: applying SystemColor.window is no longer 
                // working for some components (e.g., palette button menu's (a JPopupMenu))
                return new ColorUIResource(SystemColor.window);
            } else
                return new ColorUIResource(SystemColor.control);
        } else
            return new ColorUIResource(VueResources.getColor("toolbar.background"));
    }

    public static void init() {
        init(false);
    }

    public static void init(boolean forceWindowsLookAndFeel)
    {
        if (!initUnderway) {
            if (DEBUG.INIT) out("init: already run");
            return;
        }
        
        org.apache.log4j.NDC.push("GUI");

        VUE.Log.debug("init: forceWindowsLookAndFeel=" + forceWindowsLookAndFeel);
        
        /* VUE's JIDE open-source license if we end up using this:
        tufts.Util.executeIfFound("com.jidesoft.utils.Lm", "verifyLicense",
                                  new Object[] { "Scott Fraize",
                                                 "VUE",
                                                 "p0HJOS:Y049mQb8BLRr9ntdkv9P6ihW" });
        */

        isMacAqua = Util.isMacPlatform() && !forceWindowsLookAndFeel;

        isMacAquaBrushedMetal =
            isMacAqua &&
            VUE.isSystemPropertyTrue("apple.awt.brushMetalLook");

        ToolbarColor = initToolbarColor();

        // Note that it is essential that the theme be set before a single GUI object of
        // any kind is created.  If, for instance, a static member in any class
        // initializes a swing gui object, this will end up having no effect here, and
        // the entire theme will be silently ignored.  This includes the call below to
        // UIManager.setLookAndFeel, or even UIManager.getLookAndFeel, which is also why
        // we need to tell the VueTheme about LAF depended variable sinstead of having
        // it ask for the LAF itself, as it may not have been set yet.  Note that when
        // using the Mac Aqua L&F, we don't need to set the theme for Metal (as it's not
        // being used and would have no effect), but we still need to initialize the
        // theme, as it's still queried througout some of the older code.
        
        if (isMacAqua) {

            installAquaLAFforVUE();

        } else if (Util.isMacPlatform()) {

            // We're forcing Swing Metal Look & Feel on the mac (our current Windows L&F)
            // (Not to be confused with Mac Aqua Brushed Metal).
            
            installMetalTheme();
            setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());

        } else {

            // We're leaving the default look and feel alone (e.g. Windows)

            // if on Windows and forcing windows look, these meants try the native win L&F
            if (forceWindowsLookAndFeel && Util.isWindowsPlatform())
                setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
            else
                installMetalTheme();
        }

        javax.swing.JPopupMenu.setDefaultLightWeightPopupEnabled(false);
        
        if (Util.getJavaVersion() < 1.5f)
            UseAlwaysOnTop = false;

        FocusManager.install();
        //tufts.Util.executeIfFound("tufts.vue.gui.WindowManager", "install", null);
        
        VUE.Log.debug("LAF  name: " + UIManager.getLookAndFeel().getName());
        VUE.Log.debug("LAF descr: " + UIManager.getLookAndFeel().getDescription());
        VUE.Log.debug("LAF class: " + UIManager.getLookAndFeel().getClass());

        org.apache.log4j.NDC.pop();

        initUnderway = false;
    }

    private static void installMetalTheme() {
        
        CommonMetalTheme common = new CommonMetalTheme();

        if (Util.getJavaVersion() >= 1.5f) {
            //Theme = new OceanMetalTheme(common);
            String className = "tufts.vue.gui.OceanMetalTheme";
            try {
                Theme = (javax.swing.plaf.metal.MetalTheme)
                    Class.forName(className).newInstance();
                Util.execute(Theme, className, "setCommonTheme", new Object[] { common });
                isOceanTheme = true;
            } catch (Exception e) {
                Theme = null;
                e.printStackTrace();
                out("failed to load java 1.5+ Ocean theme: " +  className);
            }
        }

        if (Theme == null)
            Theme = new DefaultMetalTheme(common);

        MetalLookAndFeel.setCurrentTheme(Theme);
        
        VUE.Log.debug("installed theme: " + Theme);
    }

    /** @return true if was successful */
    private static boolean setLookAndFeel(String name) {
        try {
            if (name != null)
                javax.swing.UIManager.setLookAndFeel(name);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    

    public static void installAquaLAFforVUE() {
        try {
            javax.swing.UIManager.setLookAndFeel(new VueAquaLookAndFeel());
        } catch (javax.swing.UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
    }

    private static class InitError extends Error {
        InitError(String s) {
            super("GUI.init hasn't run; indeterminate result for: " + s);
        }
        InitError() {
            super("GUI.init hasn't run; indeterminate result");
        }
    }

    public static boolean isMacAqua() {
        if (initUnderway) throw new InitError("isMacAqua");
        return isMacAqua;
    }
    
    public static boolean isMacBrushedMetal() {
        if (initUnderway) throw new InitError("isMacAquaBrushedMetal");
        return isMacAquaBrushedMetal;
    }
    
    public static boolean isOceanTheme() {
        if (initUnderway) throw new InitError("isOceanTheme");
        return isOceanTheme;
    }

    private static void initGraphicsInfo() {
        GToolkit = Toolkit.getDefaultToolkit();
        GEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GDevice = GEnvironment.getDefaultScreenDevice();
        GConfig = GDevice.getDefaultConfiguration(); // this changes when display mode changes
        GBounds = GConfig.getBounds();
        GInsets = GToolkit.getScreenInsets(GConfig); // this may change at any time
        GScreenWidth = GBounds.width;
        GScreenHeight = GBounds.height;
        GMaxWindowBounds = GEnvironment.getMaximumWindowBounds();

        if (DEBUG.INIT) {
            System.out.println("GUI.initGraphicsInfo:"
                               + "\n\t" + GToolkit
                               + "\n\t" + GEnvironment
                               + "\n\t" + GDevice + " " + GDevice.getIDstring()
                               + "\n\t" + GConfig
                               + "\n\tscreen bounds " + GBounds
                               + "\n\tscreen insets " + GInsets
                               );
        }
        
    }

    public static void refreshGraphicsInfo() {
        // GraphicsConfiguration changes on DisplayMode change -- update everything.
        if (GDevice == null) {
            initGraphicsInfo();
        } else {
            GraphicsConfiguration currentConfig = GDevice.getDefaultConfiguration();
            if (currentConfig != GConfig)
                initGraphicsInfo();
            else
                GInsets = GToolkit.getScreenInsets(GConfig); // this may change at any time
        }

        if (!VUE.isStartupUnderway() && VUE.getApplicationFrame() != null) {
            VUE.getApplicationFrame().setMaximizedBounds(VueMaxWindowBounds(GMaxWindowBounds));

            if (DockWindow.MainDock != null) {
                if (DockWindow.MainDock.mGravity == DockRegion.BOTTOM) {
                    DockWindow.MainDock.moveToY(VUE.ApplicationFrame.getY());
                } else {
                    Point contentLoc = VUE.mViewerSplit.getLocation();
                    SwingUtilities.convertPointToScreen(contentLoc, VUE.ApplicationFrame.getContentPane());
                    DockWindow.MainDock.moveToY(contentLoc.y);
                }
            }
        }
    }


    public static int stringLength(Font font, String s) {
        return (int) Math.ceil(stringWidth(font, s));
    }
    
    public static double stringWidth(Font font, String s) {
        return font.getStringBounds(s, GUI.DefaultFontContext).getWidth();
    }
    


    public static Rectangle getMaximumWindowBounds() {
        refreshGraphicsInfo();
        return VueMaxWindowBounds(GMaxWindowBounds);
    }

    public static int getMaxWindowHeight() {
        refreshGraphicsInfo();
        return GMaxWindowBounds.height;
    }

    /** VUE specific threshold for configuring UI */
    public static boolean isSmallScreen() {
        refreshGraphicsInfo();
        return GScreenWidth < 1024;
    }
    
    /** center the given window on default physical screen, but never let it go above whatever
     * our max window bounds are.
     */
    public static void centerOnScreen(java.awt.Window window)
    {
        refreshGraphicsInfo();

        int x = GScreenWidth/2 - window.getWidth()/2;
        int y = GScreenHeight/2 - window.getHeight()/2;

        Rectangle wb = getMaximumWindowBounds();

        if (y < wb.y)
            y = wb.y;
        if (x < wb.x)
            x = wb.x;

        window.setLocation(x, y);
    }
    
    private static Rectangle VueMaxWindowBounds(Rectangle systemMax) {
        Rectangle r = new Rectangle(systemMax);
        
        // force 0 at left, ignoring any left inset
        //r.width += r.x;
        //r.x = 0;
        // MacOSX won't let us override this when we set a frame's state to MAXIMIZED_BOTH,.
        // although the window can still be manually positioned there (user or setLocation)
        
        int min =
            DockWindow.isTopDockEmpty() ?
            0 : 
            DockWindow.getCollapsedHeight();

        //Util.printStackTrace("min="+min);
        
        r.y += min;
        r.height -= min;
        
        /*
        //int dockHeight = DockWindow.getCollapsedHeight();
        if (!DockWindow.TopDock.isEmpty()) {
            r.y += dockHeight;
            r.height -= dockHeight;
        }
        */

        // At least on mac, it's not allowing us to reduce the max window
        // bounds at the bottom: any reduction in height is taken off the top.
        // In short: the y-value in the bounds is completely ignored.
        
        //if (!DockWindow.BottomDock.isEmpty())
        //r.height -= dockHeight;

        // ignore dock gap
        /*
          // apparently can't override this on at least mac
        if (r.x <= 4) {
            r.width += r.x;
            r.x = 0;
        }
        */

        // System.out.println(r);

        return r;
        
    }


    /** these may change at any time, so we must fetch them newly each time */
    public static Insets getScreenInsets() {
        refreshGraphicsInfo();
        return GInsets;
    }
    
    
    /**
     * Factory method for creating frames in VUE.  On PC, this
     * is same as new new JFrame().  In Mac Look & Feel it adds a duplicate
     * menu-bar to the frame as every frame needs one
     * or we lose the mebu-bar (todo: no longer true)
     */
    public static JFrame createFrame()
    {
        return createFrame(null);
    }
    
    public static JFrame createFrame(String title)
    {
        JFrame newFrame = new JFrame(title);
        /*
        if (isMacAqua()) {
            JMenuBar menu = new VueMenuBar();
            newFrame.setJMenuBar(menu);
        }
        */
        return newFrame;
    }

    
    /** @return a new VUE application DockWindow */
    public static DockWindow createDockWindow(String title, boolean asToolbar) {

        // In Java 1.5 we can set the Window's to be always on top.  In this case, we
        // can use a hidden parent for the DockWindow, set them to be never focusable, and
        // use our FocusManager to force focus to it as needed.  We also need our
        // WindowManager in this case to hide the DockWindow's when the application goes
        // inactive, otherwise they'll stay on top of all other applications.

        // Doing this allows the main VUE frame to keep application focus even when
        // using tool widges (the title bar stays active), will allow us to create an
        // MDI (multiple document interface) because the DockWindow's aren't parented to
        // just one window, and allows DockWindows to be used in full-screen mode for
        // the same reason: they always stay on top, and don't need to be children of
        // the currently active window to be seen.  If we could re-parent java Window's
        // on the fly we could avoiid all this, but alas, we can't.  (Java Window's
        // always stay on top of their parent, but not on top of other windows,
        // including "grandparents".)
        
        if (UseAlwaysOnTop && Util.getJavaVersion() >= 1.5f) {
            DockWindow dockWindow
                = new DockWindow(title, DockWindow.getHiddenFrame(), null, asToolbar);
            dockWindow.setFocusableWindowState(false);
            setAlwaysOnTop(dockWindow, true);
            return dockWindow;
        } else {
            // TODO: create method in VUE for getting DockWindow parent for use elsewhere
            return new DockWindow(title, getFullScreenWindow(), null, asToolbar);
            //return new DockWindow(title, VUE.getRootWindow());
        }
    }

    public static DockWindow createDockWindow(String title) {
        return createDockWindow(title, false);
    }
    
    /**
     * Convience method.
     * @return a new VUE application DockWindow
     * @param content - a component to put in the dock window
     */
    public static DockWindow createDockWindow(String title, javax.swing.JComponent content) {
        DockWindow dw = createDockWindow(title);
        dw.add(content);
        return dw;
    }

    /**
     * @return a new VUE application Dockable Toolbar
     * @param content - a component to use for the roolbar
     */
    public static DockWindow createToolbar(String title, javax.swing.JComponent content) {
        DockWindow dw = createDockWindow(title, true);
        dw.add(content);
        return dw;
    }

    static Frame HiddenDialogParent = null;

    // based on SwingUtilities.getSharedOwnerFrame() 
    public static Frame getHiddenDialogParentFrame() { 
        if (HiddenDialogParent == null) {
            HiddenDialogParent = new Frame() {
                    public void show() {} // This frame can never be shown
                    // this will prevent children from going behind other windows?
                    public boolean isShowing() { return true; }
                    public String toString() { return name(this); }
                };
            HiddenDialogParent.setName("*VUE-DIALOG-PARENT*");
        }
        return HiddenDialogParent;
    }

    private static class FullScreenWindow extends JWindow
    {
        private static final String FULLSCREEN_NAME = "*FULLSCREEN*";
        
        FullScreenWindow() {
            super(VUE.getApplicationFrame());
            if (isMacBrushedMetal())
                setName(OVERRIDE_REDIRECT); // so no background flashing of the texture
            else
                setName(FULLSCREEN_NAME);

            GUI.setRootPaneNames(this, FULLSCREEN_NAME);

            // mac already has the menu bar at the top of the screen
            if (!isMacAqua())
                getRootPane().setJMenuBar(new VueMenuBar());
            
            setOffScreen(this);

            
            if (Util.isMacPlatform()) {
                // On the Mac, this must be shown before any DockWindows display,
                // or they won't stay on top of their parents (this is a mac bug).
                setVisible(true);
                setVisible(false);
            }
        }
        public void setVisible(boolean show) {

            setFocusableWindowState(show);
            
            // if set we allow it to actually go invisible
            // all children will hide (hiding all the DockWindow's)
            
            if (show)
                super.setVisible(true);
            else 
                setOffScreen(this);

            if (show) // just in case
                DockWindow.raiseAll();
        }
        
    }

    public static Window getFullScreenWindow() {
        if (FullScreenWindow == null) {

            //FullScreenWindow = new Window(VUE.getRootFrame());
            //FullScreenWindow = new JWindow(VUE.getRootFrame());
            //FullScreenWindow = new DockWindow("***VUE-FULLSCREEN***", VUE.getRootFrame());

            FullScreenWindow = new FullScreenWindow();
        }
        return FullScreenWindow;
    }

    public static Window getCurrentRootWindow() {
        if (tufts.vue.FullScreen.inFullScreen())
            return FullScreenWindow;
        else
            return VUE.getRootWindow();
    }
    
    /**
     * Size a normal Window to the maximum size usable in the current
     * platform & desktop configuration, <i>without</i> using the java
     * special full-screen mode, which can't have any other windows
     * on top of it, and changes the way user input events are handled.
     * On the PC, this will just be the whole screen (bug: probably
     * not good enough if they have non-hiding menu bar set to always-
     * on-top). On the Mac, it will be adjusted for the top menu
     * bar and the dock if it's visible.
     */
    // todo: test in multi-screen environment
    private static void XsetFullScreen(Window window)
    {
        refreshGraphicsInfo();
        
        Dimension screen = window.getToolkit().getScreenSize();
        
        if (Util.isMacPlatform()) {
            // mac won't layer a regular window over the menu bar, so
            // we need to limit the size
            Rectangle desktop = GEnvironment.getMaximumWindowBounds();
            out("setFullScreen: mac maximum bounds  " + Util.out(desktop));
            if (desktop.x > 0 && desktop.x <= 4) {
                // hack for smidge of space it attempts to leave if the dock is
                // at left and auto-hiding
                desktop.width += desktop.x;
                desktop.x = 0;
            } else {
                // dock at bottom & auto-hiding
                int botgap = screen.height - (desktop.y + desktop.height);
                if (botgap > 0 && botgap <= 4) {
                    desktop.height += botgap;
                } else {
                    // dock at right & auto-hiding
                    int rtgap = screen.width - desktop.width;
                    if (rtgap > 0 && rtgap <= 4)
                        desktop.width += rtgap;
                }
            }
            
            if (DEBUG.FOCUS) desktop.height /= 5;
            
            out("setFullScreen: mac adjusted bounds " + Util.out(desktop));

            window.setLocation(desktop.x, desktop.y);
            window.setSize(desktop.width, desktop.height);
        } else {
            if (DEBUG.FOCUS) screen.height /= 5;
            window.setLocation(0, 0);
            window.setSize(screen.width, screen.height);
        }
        out("setFullScreen: set to " + window);
    }
    
    private static Rectangle getFullScreenBounds()
    {
        refreshGraphicsInfo();

        //out(" GBounds: " + GBounds);

        return new Rectangle(GBounds);

    }    
    

    /**
     * In case window is off screen, size it, then set visible on screen, then place it.
     * This avoids reshape flashing -- when a window is setVisible, it sometimes
     * displays for a moment before it's taken it's new size and been re-validated.
     */
    
    public static void setFullScreenVisible(Window window)
    {
        Rectangle bounds = getFullScreenBounds();

        if (Util.isMacPlatform()) {
            // place us under the mac menu bar
            bounds.y += GInsets.top;
            bounds.height -= GInsets.top;
        }

        if (DEBUG.FOCUS) bounds.height /= 5; // so we can see terminal
        
        //out("FSBounds: " + bounds);

        window.setSize(bounds.width, bounds.height);
        window.setVisible(true);
        window.setLocation(bounds.x, bounds.y);
    }

    
    /** set window to be as off screen as possible */
    public static void setOffScreen(java.awt.Window window)
    {
        refreshGraphicsInfo();
        window.setLocation(-999, 8000);
    }
    
    public static void setAlwaysOnTop(Window w, boolean onTop) {
        out("setAlwaysOnTop " + onTop + " " + name(w));
        Util.invoke(w, "setAlwaysOnTop", onTop ? Boolean.TRUE : Boolean.FALSE);
    }

    /**
     * Given location and size (of presumably a Window) modify location
     * such that the resulting bounds are on screen.  If the window
     * is bigger than the screen, it will keep the upper left corner
     * visible.
     * 
     * Can be used to keep tool-tips on-screen.
     *
     * Note: this does not refresh the graphics config as may be used
     * several times while attempting a placement.
     */
    public static void keepLocationOnScreen(Point loc, Dimension size) {

        // if would go off bottom, move up
        if (loc.y + size.height >= GScreenHeight)
            loc.y = GScreenHeight - (size.height + 1);
        
        // if would go off top, move back down
        // Avoid top insets as windows don't normally go
        // over the menu bar at the top.
        if (loc.y < GUI.GInsets.top)
            loc.y = GUI.GInsets.top;
        
        // if would go off right, move back left
        if (loc.x + size.width > GScreenWidth)
            loc.x = GScreenWidth - size.width;

        // if would go off left, just put at left
        if (loc.x < 0)
            loc.x = 0;
    }

    /*
    public interface KeyBindingRelayer {
        public boolean processKeyBindingUp(KeyStroke ks, KeyEvent e, int condition, boolean pressed);
    }


     * We need this to hand off command keystrokes to the VUE menu bar.  This isn't as
     * important given the way do things in java 1.5, but we really need in 1.4 and on
     * the PC -- otherwise when a DockWindow has the focus, you can't use all the global
     * short-cut keys, such as the ability to hide or show a DockWindow with a shortcut!
     *
     * Allowing this in java 1.5 means we can access the VueMenuBar
     * shortcuts even while editing a text field.

    // todo: we could probably have our FocusManager handle this for us, which may be a bit cleaner
    // todo: or, we could just get the input map from menu bar and invoke the action ourselves...
    public static boolean processKeyBindingToMenuBar(KeyBindingRelayer relayer,
                                                     KeyStroke ks,
                                                     KeyEvent e,
                                                     int condition,
                                                     boolean pressed)
    {
        // We want to ignore vanilla typed text as a quick-reject culling mechanism.
        // So only if any modifier bits are on (except SHIFT), do we attempt to
        // send the KeyStroke to the JMenuBar to check against any accelerators there.
            
        final int PROCESS_MASK =
            InputEvent.CTRL_MASK |
            InputEvent.META_MASK |
            InputEvent.ALT_MASK |
            InputEvent.ALT_GRAPH_MASK;

        // On Mac java 1.4.2, and on the PC (TODO: also java 1.5 or only java 1.4.2?)
        // we have to manually redirect the key events to the main VueMenuBar.
            
        if ((e.getModifiers() & PROCESS_MASK) == 0 && !e.isActionKey())
            return relayer.processKeyBindingUp(ks, e, condition, pressed);
                
        if (DEBUG.TOOL||DEBUG.FOCUS) out(name(relayer) + " processKeyBinding [" + ks + "] condition="+condition);
            
        // processKeyBinding never appears to return true, and this key event never
        // gets marked as consumed (I guess we'd have to listen for that in our text fields
        // and consume it), so we're always passing the character up the tree...
            
        if (relayer.processKeyBindingUp(ks, e, condition, pressed) || e.isConsumed())
            return true;
            
        // Condition usually comes in as WHEN_ANCESTOR_OF_FOCUSED_COMPONENT (1), which doesn't do it for us.
        // We need condition (2): WHEN_IN_FOCUSED_WINDOW
            
        int newCondition = JComponent.WHEN_IN_FOCUSED_WINDOW;
            
        if (DEBUG.TOOL||DEBUG.FOCUS)
            out(name(relayer) + " processKeyBinding [" + ks + "] handing to VueMenuBar w/condition=" + newCondition);
            
        try {
            return false; //return VUE.getJMenuBar().doProcessKeyBinding(ks, e, newCondition, pressed);
        } catch (NullPointerException ex) {
            out("no menu bar: " + ex);
            return false;
        }
    }
     */
    
    public static void invokeAfterAWT(Runnable runnable) {
        java.awt.EventQueue.invokeLater(runnable);
    }
    
    public static void messageAfterAWT(final String s) {
        java.awt.EventQueue.invokeLater(new Runnable() {
                public void run() { System.out.println(s); }
            });
    }

    public static void postEvent(AWTEvent e) {
        Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(e);
    }
    
    public static void postEventLater(final AWTEvent e) {
        invokeAfterAWT(new Runnable() { public void run() { postEvent(e); }});
    }

    /** @return a short name for given object, being smart about it if it's a java.awt.Component */
    public static String name(Object c) {

        if (c == null)
            return "null";

        if (c instanceof AWTEvent)
            return eventName((AWTEvent) c);
        
        String title = null;
        String name = null;
        
        if (c instanceof java.awt.Frame) {
            title = ((java.awt.Frame)c).getTitle();
            if ("".equals(title))
                title = null;
        }

        if (title == null && c instanceof Component) {
            title = ((Component)c).getName();
            if (OVERRIDE_REDIRECT.equals(title))
                title = "###";
            if (title == null) {
                if (c instanceof javax.swing.JLabel)
                    title = ((javax.swing.JLabel)c).getText();
                else if (c instanceof javax.swing.AbstractButton)
                    title = ((javax.swing.AbstractButton)c).getText();
            }
        } else if (c instanceof java.awt.MenuComponent) {
            title = ((java.awt.MenuComponent)c).getName();
        }

        name = baseObjectName(c);
        if (title == null || title.startsWith("###"))
            name += "@" + Integer.toHexString(c.hashCode());
        if (title != null)
            name += "(" + title + ")";
        
        
        String text = null;
        if (c instanceof javax.swing.text.JTextComponent) {
            text = ((javax.swing.text.JTextComponent)c).getText();
        }
        
        if (text != null)
            name += "[" + text + "]";

        return name;
    }
    
    public static String name(AWTEvent e) {
        return eventName(e);
    }
    
    public static String eventName(AWTEvent e) {
        return ""
            + VueUtil.pad(37, name(e.getSource()))
            //+ " " + VueUtil.pad(25, baseClassName(e.getClass().getName() + "@" + Integer.toHexString(e.hashCode())))
            + " " + VueUtil.pad(20, baseClassName(e.getClass().getName()))
            + eventParamString(e)
            ;
    }

    public static String eventParamString(AWTEvent e) 
    {
        String s = "[" + e.paramString();
        if (e instanceof MouseEvent && ((MouseEvent)e).isPopupTrigger())
            s += ",IS-POPUP-TRIGGER";
        return s + "]";
    }
    

    protected static String baseClassName(String className) {
        return className.substring(className.lastIndexOf('.') + 1);
    }
    protected static String baseClassName(Class clazz) {
        return baseClassName(clazz.getName());
    }
    protected static String baseObjectName(Object o) {
        return o == null ? "null" : baseClassName(o.getClass().getName());
    }
    
    
    /** 1 click, button 2 or 3 pressed, button 1 not already down & ctrl not down */
    public static boolean isRightClick(MouseEvent e) {
        return e.getClickCount() == 1
            && (e.getButton() == java.awt.event.MouseEvent.BUTTON3 ||
                e.getButton() == java.awt.event.MouseEvent.BUTTON2)
            && (e.getModifiersEx() & java.awt.event.InputEvent.BUTTON1_DOWN_MASK) == 0
            && !e.isControlDown();
    }
    
    public static boolean isMenuPopup(ComponentEvent ce) {
        if (ce instanceof MouseEvent) {
            MouseEvent e = (MouseEvent) ce;
            return e.isPopupTrigger() || isRightClick(e);
        } else
            return false;
    }
 
    private static class LocalData extends DataFlavor {
        private Class clazz;
        LocalData(Class clazz) throws ClassNotFoundException {
            super(DataFlavor.javaJVMLocalObjectMimeType
                  + "; class=" + clazz.getName()
                  + "; humanPresentableName=" + baseClassName(clazz));
            //super(DataFlavor.javaJVMLocalObjectMimeType, baseClassName(clazz));
            this.clazz = clazz;
        }

        /*
         * DataFlavor.equals make's all javaJVMLocalObjectMimeType's look the same.  As
         * we don't support cross VM transfer for these yet (serialization), we override
         * equals to do an object compare (otherwise, Transferable's get confused, as
         * they can't distinguish between these types).
         *
         * Okay -- don't need this if we construct above by building up a MimeType
         * specification.
         */
        /*
        public boolean equals(DataFlavor that) {
            return this == that;
        }
        */
        
    }
    
    // probably move this & LocalData to something like LWDataTransfer with MapViewer.LWTransfer
    public static DataFlavor makeDataFlavor(Class clazz) {
        
        // We don't generally support serialization yet, so we need to make sure to
        // tag non-supporting flavors as javaVM local only.

        if (java.io.Serializable.class.isAssignableFrom(clazz)) {
            
            return new DataFlavor(clazz, baseClassName(clazz));

        } else {
            
            try {
                return new LocalData(clazz);
            } catch (ClassNotFoundException e) {
                // Should never happen, as we already have a live Class object
                Util.printStackTrace(e);
            }
            
        }
        return null;
    }

    private static final class EmptyIcon16 implements Icon {
        public final void paintIcon(java.awt.Component c, java.awt.Graphics g, int x, int y) {}
        public final int getIconWidth() { return 16; }
        public final int getIconHeight() { return 16; }
        public final String toString() { return "EmptyIcon16"; }
    }
    
    public static final Icon EmptyIcon16 = new EmptyIcon16();
    public static final Icon NO_ICON = new EmptyIcon16();
        
    private static boolean anyIcons(Component[] items) {
        for (int i = 0; i < items.length; i++) {
            if (items[i] instanceof AbstractButton) {
                if (((AbstractButton)items[i]).getIcon() != null)
                    return true;
            }
        }
        return false;
    }
    
    private static void enforceIcons(Component[] items) {
        for (int i = 0; i < items.length; i++) {
            if (items[i] instanceof AbstractButton) {
                AbstractButton menuItem = (AbstractButton) items[i];
                if (menuItem.getIcon() == null)
                    menuItem.setIcon(EmptyIcon16);
            }
        }
    }
    
    public static void adjustMenuIcons(javax.swing.JMenu menu) {
        //out("ADJUSTING " + name(menu));
        if (anyIcons(menu.getMenuComponents()))
            enforceIcons(menu.getMenuComponents());
    }
    
    public static void adjustMenuIcons(javax.swing.JPopupMenu menu) {
        //out("ADJUSTING " + name(menu));
        if (anyIcons(menu.getComponents()))
            enforceIcons(menu.getComponents());
    }

    public static JMenu buildMenu(JMenu menu, Action[] actions) {
        for (int i = 0; i < actions.length; i++) {
            Action a = actions[i];
            if (a == null)
                menu.addSeparator();
            else
                menu.add(a);
        }
        adjustMenuIcons(menu);
        return menu;
    }

    public static JMenu buildMenu(String name, Action[] actions) {
        return buildMenu(new JMenu(name), actions);
    }
    
    public static JPopupMenu buildMenu(Action[] actions) {
        JPopupMenu menu = new JPopupMenu();
        for (int i = 0; i < actions.length; i++) {
            Action a = actions[i];
            if (a == null)
                menu.addSeparator();
            else
                menu.add(a);
        }
        adjustMenuIcons(menu);
        return menu;

    }

    /**
     * Given a trigger component (such as a label), when mouse is
     * pressed on it, pop the given menu.  Default location is below
     * the given trigger.
     */
    public static class PopupMenuHandler extends tufts.vue.MouseAdapter
        implements javax.swing.event.PopupMenuListener
    {
        private long mLastHidden;
        private JPopupMenu mMenu;
        
        public PopupMenuHandler(Component trigger, JPopupMenu menu)
        {
            trigger.addMouseListener(this);
            menu.addPopupMenuListener(this);
            mMenu = menu;
        }

        public void mousePressed(MouseEvent e) {
            long now = System.currentTimeMillis();
            if (now - mLastHidden > 100)
                showMenu(e.getComponent());
        }

        /** show the menu relative to the given trigger that activated it */
        public void showMenu(Component trigger) {
            mMenu.show(trigger, getMenuX(trigger), getMenuY(trigger));
        }

        /** get menu X location relative to trigger: default is 0 */
        public int getMenuX(Component trigger) { return 0; }
        /** get menu Y location relative to trigger: default is trigger height (places below trigger) */
        public int getMenuY(Component trigger) { return trigger.getHeight(); }

        public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
            mLastHidden = System.currentTimeMillis();
            //out("HIDING");
        }
        
        public void popupMenuWillBecomeVisible(PopupMenuEvent e) { /*out("SHOWING");*/ }
        public void popupMenuCanceled(PopupMenuEvent e) { /*out("CANCELING");*/ }
        
        // One gross thing about a pop-up menu is that there's no way to know that it
        // was just hidden by a click on the component that popped it.  That is, if you
        // click on the menu launcher once, you want to pop it, and if you click again,
        // you want to hide it.  But the AWT system autmatically cancels the pop-up as
        // soon as the mouse-press happens ANYWYERE, and even before we'd get a
        // processMouseEvent, so by the time we get this MOUSE_PRESSED, the menu is
        // already hidden, and it looks like we should show it again!  So we have to use
        // a simple timer.
        
    }


    
    
    public static void setRootPaneNames(RootPaneContainer r, String name) {
        r.getRootPane().setName(name + ".root");
        r.getContentPane().setName(name + ".content");
        r.getLayeredPane().setName(name + ".layer");
        r.getGlassPane().setName(name + ".glass");
    }

    public static String dropName(int dropAction) {
        String name = "";
        if ((dropAction & DnDConstants.ACTION_COPY) != 0) name += "COPY";
        if ((dropAction & DnDConstants.ACTION_MOVE) != 0) name += "MOVE";
        if ((dropAction & DnDConstants.ACTION_LINK) != 0) name += "LINK";
        if (name.length() < 1)
            name = "NONE";
        name += "(0x" + Integer.toHexString(dropAction) + ")";
        return name;
    }

    public static String dragName(DragSourceDragEvent e) {
        return VueUtil.pad(20, baseObjectName(e))
            + " drop=" + dropName(e.getDropAction())
            + "  user=" + dropName(e.getUserAction())
            + "  target=" + dropName(e.getTargetActions());
    }

    public static String dragName(DropTargetDragEvent e) {
        return VueUtil.pad(20, baseObjectName(e))
            + " drop=" + dropName(e.getDropAction())
            + "  source=" + dropName(e.getSourceActions())
            ;
    }
    
    public static String dropName(DropTargetDropEvent e) {
        return baseObjectName(e)
            + " drop=" + dropName(e.getDropAction())
            + "  source=" + dropName(e.getSourceActions())
            + "  isLocal=" + e.isLocalTransfer()
            ;

    }
    
    public static String dragName(DragSourceDropEvent e) {
        return VueUtil.pad(20, baseObjectName(e)) + " drop=" + dropName(e.getDropAction()) + " success=" + e.getDropSuccess();
    }    
        
    public static class DragSourceAdapter implements DragSourceListener {
    
        public void dragOver(DragSourceDragEvent dsde) {
            if (DEBUG.DND & DEBUG.META) out("dragOver " + dragName(dsde));
        }
        
        public void dragEnter(DragSourceDragEvent dsde) {
            if (DEBUG.DND) out("        dragEnter " + dragName(dsde));
        }
        public void dropActionChanged(DragSourceDragEvent dsde) {
            if (DEBUG.DND) out("dropActionChanged " + dragName(dsde));
        }
        public void dragExit(DragSourceEvent dse) {
            if (DEBUG.DND) out("         dragExit " + dse);
        }
        public void dragDropEnd(DragSourceDropEvent dsde) {
            if (DEBUG.DND) out("      dragDropEnd " + dragName(dsde));
        }
    }
    

    private static class DefaultMetalTheme extends javax.swing.plaf.metal.DefaultMetalTheme
    {
        private CommonMetalTheme common;
        
        DefaultMetalTheme(CommonMetalTheme common) { this.common = common; }
        
        public FontUIResource getMenuTextFont() { return common.fontMedium;  }
        public FontUIResource getUserTextFont() { return common.fontSmall; }
        public FontUIResource getControlTextFont() { return common.fontControl; }
        
        protected ColorUIResource getSecondary1() { return common.VueSecondary1; }
        protected ColorUIResource getSecondary2() { return common.VueSecondary2; }
        protected ColorUIResource getSecondary3() { return common.VueSecondary3; }

        public void addCustomEntriesToTable(UIDefaults table) {
            super.addCustomEntriesToTable(table);
            common.addCustomEntriesToTable(table);
        }
        
        public String getName() { return super.getName() + " (VUE)"; }
    }

    static class CommonMetalTheme
    {
        // these are gray in Metal Default Theme
        final ColorUIResource VueSecondary1;
        final ColorUIResource VueSecondary2;
        final ColorUIResource VueSecondary3;

        protected FontUIResource fontSmall  = new FontUIResource("SansSerif", Font.PLAIN, 11);
        protected FontUIResource fontMedium = new FontUIResource("SansSerif", Font.PLAIN, 12);

        // controls: labels, buttons, tabs, tables, etc.
        protected FontUIResource fontControl  = new FontUIResource("SansSerif", Font.PLAIN, 12);
        
        CommonMetalTheme() {
            Color VueColor = getVueColor();
            //Color VueColor = new ColorUIResource(200, 221, 242); // #c8ddf2 from OceanTheme
        
            VueSecondary3 = new ColorUIResource(VueColor);
            VueSecondary2 = new ColorUIResource(VueColor.darker());
            VueSecondary1 = new ColorUIResource(VueColor.darker().darker());

            TabbedPaneUI.toolbarColor = GUI.ToolbarColor;
            TabbedPaneUI.vueSecondary2Color = VueSecondary2;
        }

        public void addCustomEntriesToTable(UIDefaults table)
        {
            //table.put("ComboBox.background", Color.white);
            table.put("Button.font", fontSmall);
            table.put("Label.font", fontSmall);
            table.put("TitledBorder.font", fontMedium.deriveFont(Font.BOLD));
            table.put("TabbedPaneUI", "tufts.vue.gui.GUI$TabbedPaneUI");
        }

    }
        



    /**
     * This tweaks the background color of unselected tabs in the tabbed pane,
     * and completely turns off painting any kind of focus indicator.
     */
    public static class TabbedPaneUI extends javax.swing.plaf.metal.MetalTabbedPaneUI {
        static Color toolbarColor;
        static Color vueSecondary2Color;
        
        public static ComponentUI createUI( JComponent x ) {
            if (DEBUG.INIT) System.out.println("Creating GUI.TabbedPaneUI");
            return new TabbedPaneUI();
            
            /*
            return new TabbedPaneUI(new ColorUIResource(Color.blue),
                                    new ColorUIResource(Color.green));
            */
        }
        
        protected void XinstallDefaults() {
            super.installDefaults();
            
            super.tabAreaBackground = Color.blue;
            super.selectColor = Color.green;
            super.selectHighlight = Color.red;

            super.highlight = Color.green;
            super.lightHighlight = Color.magenta;
            super.shadow = Color.black;
            super.darkShadow = Color.black;
            super.focus = Color.orange;
        }


        protected void paintFocusIndicator(Graphics g, int tabPlacement,
                                           Rectangle[] rects, int tabIndex, 
                                           Rectangle iconRect, Rectangle textRect,
                                           boolean isSelected) {}
        
        protected void paintTabBackground( Graphics g, int tabPlacement,
                                           int tabIndex, int x, int y, int w, int h, boolean isSelected ) {
            int slantWidth = h / 2;
            if (isSelected) {
                // in Ocean Theme, this is a ligher blue, so we've overriden it in defaults.
                g.setColor(selectColor);
            } else {
                //g.setColor( tabPane.getBackgroundAt( tabIndex ) );
                Color c = tabPane.getBackgroundAt( tabIndex );
                // for now, allow toolbar color as optional tabbed bg, but all others override
                if (toolbarColor.equals(c))
                    g.setColor(c);
                else
                    g.setColor(vueSecondary2Color);
            }
            switch ( tabPlacement ) {
            case LEFT:
                g.fillRect( x + 5, y + 1, w - 5, h - 1);
                g.fillRect( x + 2, y + 4, 3, h - 4 );
                break;
            case BOTTOM:
                g.fillRect( x + 2, y, w - 2, h - 4 );
                g.fillRect( x + 5, y + (h - 1) - 3, w - 5, 3 );
                break;
            case RIGHT:
                g.fillRect( x + 1, y + 1, w - 5, h - 1);
                g.fillRect( x + (w - 1) - 3, y + 5, 3, h - 5 );
                break;
            case TOP:
            default:
                g.fillRect( x + 4, y + 2, (w - 1) - 3, (h - 1) - 1 );
                g.fillRect( x + 2, y + 5, 2, h - 5 );
            }
        }
    }
        
    

        
    /*
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        out(e);
    }
    */

    // static { Toolkit.getDefaultToolkit().addPropertyChangeListener(new GUI()); }

    //private static boolean sIgnoreEvents = false;
    // todo: move to a PropertyDispatch handler or something
    private static boolean sIgnoreLWCEvents = false;
    
    static void propertyProducerChanged(tufts.vue.LWPropertyProducer producer)
    {
        final Object key = producer.getPropertyKey().toString();

        if (sIgnoreLWCEvents) {
            if (DEBUG.TOOL) out("propertyProducerChanged: skipping " + key + "  for " + producer);
            return;
        }
            
        if (DEBUG.TOOL) out("propertyProducerChanged: [" + key + "] on " + producer);
	  		
        sIgnoreLWCEvents = true;
        
        tufts.vue.beans.VueBeans
            .applyPropertyValueToSelection(VUE.getSelection(),
                                           key.toString(),
                                           producer.getPropertyValue());
        sIgnoreLWCEvents = false;
        
        if (VUE.getUndoManager() != null)
            VUE.getUndoManager().markChangesAsUndo(key.toString());

    }

    private static void out(String s) {
        System.out.println("GUI: " + s);
    }

    private static PropertyChangeHandler PropertyChangeHandler = new PropertyChangeHandler();
    static java.beans.PropertyChangeListener getPropertyChangeHandler() {
        if (PropertyChangeHandler == null)
            PropertyChangeHandler = new PropertyChangeHandler();
        return PropertyChangeHandler;
    }
        
    static class PropertyChangeHandler implements java.beans.PropertyChangeListener
    {
        
        private PropertyChangeHandler() {}

        private boolean mIgnoreActionEvents = false;

        public void propertyChange(java.beans.PropertyChangeEvent e) {
            if (DEBUG.Enabled) out("propertyChange: " + e);
        }

        public void actionPerformed(java.awt.event.ActionEvent e) {
            if (mIgnoreActionEvents) {
                if (DEBUG.TOOL) System.out.println(this + " ActionEvent ignored: " + e);
            } else {
                if (DEBUG.TOOL) System.out.println(this + " actionPerformed " + e);
                //fireFontChanged(null, makeFont());
            }
        }
        
    }


    private GUI() {}


    public static void main(String args[]) {

        // Can't find any property names on Mac OS X -- are there any?
        // Is it possible to get an update when the screen DisplayMode changes?
        
        String propName = null;
        if (args.length > 0)
            propName = args[0];
        if (propName == null)
            propName = "win.propNames";

        //new java.awt.Frame("A Frame").show();

        Object propValue = Toolkit.getDefaultToolkit().getDesktopProperty(propName);

        System.out.println("Property " + propName + " = " + propValue);

        if (propValue != null && propValue instanceof String[]) {
            System.out.println("Supported windows property names:");
            String[] propNames = (String[]) propValue;
            for (int i = 0; i < propNames.length; i++) {
                Object value = Toolkit.getDefaultToolkit().getDesktopProperty(propNames[i]);
                System.out.println(propNames[i] + " = " + value);
            }
        }
    }

    
        
    
    
}
