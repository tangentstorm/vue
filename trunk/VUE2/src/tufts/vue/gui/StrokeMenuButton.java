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

import java.awt.Color;
import javax.swing.Icon;

/**
 * StrokeMenuButton
 *
 * This class provides a popup button selector component for stroke widths.
 *
 * @author Scott Fraize
 * @version March 2004
 **/
public class StrokeMenuButton extends MenuButton
{
    static private int sIconWidth = 24;
    static private int sIconHeight = 16;
	
    /** The value of the currently selected stroke width item--if any **/
    protected float mStroke = 1;
			
    //protected ButtonGroup mGroup = new ButtonGroup();
    
    public StrokeMenuButton(float[] pValues, String [] pMenuNames, boolean pGenerateIcons, boolean pHasCustom)
    {
        Float[] values = new Float[pValues.length];
        // I really hope that java 1.5 auto-boxing will handle this type of thing for us...
        for (int i = 0; i < pValues.length; i++)
            values[i] = new Float(pValues[i]);

        buildMenu(values, pMenuNames, false);
        setFont(tufts.vue.VueConstants.FONT_SMALL);
    }

    public StrokeMenuButton() {}
	
    public void setStroke(float width) {
        setToolTipText("Stroke Width: " + width);
        mStroke = width;
	 	
        // if we are using a LineIcon, update it
        if (getButtonIcon() instanceof LineIcon) {
            LineIcon icon = (LineIcon) getButtonIcon();
            icon.setWeight(mStroke);
        }

        if(false)setText(((int)mStroke) + "px");
    }
    public float getStroke() {
        return mStroke;
    }
    public void setPropertyValue(Object o) {
        setStroke(o == null ? 0f : ((Float)o).floatValue());
    }
    public Object getPropertyValue() {
        return new Float(getStroke());
    }

    /** factory for superclass buildMenu */
    protected Icon makeIcon(Object value) {
        return new LineIcon(sIconWidth, sIconHeight, ((Float)value).floatValue());
    }
    
    /*
      Will need to enhance MenuButton to have an overridable addMenuItem or somethign
      if we still want to support the button group.
      mGroup.add( item);
      As WELL as an item menu factory, so it could return JRadioButtonMenuItem's
      instead (and in that method we could add it to the group for us here).
    */

}

