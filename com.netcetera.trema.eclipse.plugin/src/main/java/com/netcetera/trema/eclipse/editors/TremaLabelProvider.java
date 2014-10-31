package com.netcetera.trema.eclipse.editors;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;

import com.netcetera.trema.common.TremaUtil;
import com.netcetera.trema.core.api.IDatabase;
import com.netcetera.trema.core.api.ITextNode;
import com.netcetera.trema.core.api.IValueNode;
import com.netcetera.trema.eclipse.TremaUtilEclipse;



/**
 * The label provider for the <code>TableTreeViewer</code> in the
 * table tree view of the Trema editor. (Provides the texts displayed in each column in the tree)
 */
public class TremaLabelProvider extends ColumnLabelProvider{
  
  /**
   * Gets the label text for each cell in the tree-table, depending on the object type and the column index.
   * 
   * @param element the element
   * @param columnIndex the current column
   * @return the label
   */
  private String getColumnText(Object element, int columnIndex) {
    // in contrast to the javadoc, returning null causes the table tree viewer problems...
    if (element == null){
      return "";
    } else if (element instanceof IDatabase) {
      switch (columnIndex) {
        case 0:
          return "Trema Database";
        case 1:
          return TremaUtil.emptyStringIfNull(((IDatabase) element).getMasterLanguage());
        default:
          return "";
      }
    } else if (element instanceof ITextNode) {
      ITextNode textNode = (ITextNode) element;
      switch (columnIndex) {
        case 0:
          return TremaUtil.emptyStringIfNull(textNode.getKey());
        case 1:
          return TremaUtil.emptyStringIfNull(textNode.getContext());
        default:
          return "";
      }
    } else if (element instanceof IValueNode) {
      IValueNode valueNode = (IValueNode) element;
      switch (columnIndex) {
        case 0:
          return TremaUtil.emptyStringIfNull(valueNode.getLanguage());
        case 1:
          return TremaUtilEclipse.makeLineEndingsVisible(TremaUtil.emptyStringIfNull(valueNode.getValue()), true);
        case 2:
          return TremaUtil.emptyStringIfNull(valueNode.getStatus().getName());
        default:
          // fall back
      }
    }
    return element.toString();
  }
  
  /** {@inheritDoc} */
  public Color getBackground(Object element) {
    if (element instanceof ITextNode) {
      return new Color(Display.getCurrent(), 232, 242, 254);
    }
    return null;
  }
  
  /** {@inheritDoc} */
  public Color getForeground(Object element) {
    return null;
  }


  /* (non-Javadoc)
   * @see org.eclipse.jface.viewers.ViewerLabelProvider#getTooltipText(java.lang.Object)
   */
  public String getToolTipText(Object element) {
        if (element instanceof ITextNode){
          StringBuffer tooltip = new StringBuffer();
          ITextNode textNode = (ITextNode) element;
          for (IValueNode value : textNode.getValueNodes()){
            tooltip.append(value.getLanguage() + "\t: " + value.getValue()+"\n");
          }
          if (tooltip.length()>0){
            return tooltip.toString();
          }
        }
        return null;
  }

  /** {@inheritDoc} */
  public Point getToolTipShift(Object object) {
          return new Point(5,5);
  }

  /** {@inheritDoc} */
  public int getToolTipDisplayDelayTime(Object object) {
          return 200;
  }

  /** {@inheritDoc} */
  public int getToolTipTimeDisplayed(Object object) {
          return 20000;
  }
    
  /** {@inheritDoc} */
  @Override
  public void update(ViewerCell cell) {
          Object element = cell.getElement();
          // set the text that is displayed  in the table / tree cell
          cell.setText(getColumnText(cell.getElement(), cell.getColumnIndex()));
          Image image = getImage(element);
          cell.setImage(image);
          cell.setBackground(getBackground(element));
          cell.setForeground(getForeground(element));
          cell.setFont(getFont(element));

  }
}
