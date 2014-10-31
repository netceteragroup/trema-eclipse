package com.netcetera.trema.eclipse.editors.xmleditor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;



/** 
 * The classes of this package are based on the Eclipse PDE sample XML
 * text editor project.
 */
public class ColorManager {
  
  private Map<RGB, Color> fColorTable = new HashMap<RGB, Color>(10);
  
  /**
   * Disposes resources used by this class.
   */
  public void dispose() {
    Iterator<Color> e = fColorTable.values().iterator();
    while (e.hasNext()) {
      e.next().dispose();
    }
  }
  /**
   * Gets a Color object.
   * 
   * @param rgb the rgb to get the color for
   * @return Color the color
   */
  public Color getColor(RGB rgb) {
    Color color = fColorTable.get(rgb);
    if (color == null) {
      color = new Color(Display.getCurrent(), rgb);
      fColorTable.put(rgb, color);
    }
    return color;
  }
}
