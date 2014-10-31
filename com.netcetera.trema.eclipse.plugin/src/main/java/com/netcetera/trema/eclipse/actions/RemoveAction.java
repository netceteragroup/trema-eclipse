package com.netcetera.trema.eclipse.actions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Tree;

import com.netcetera.trema.core.api.IDatabase;
import com.netcetera.trema.core.api.ITextNode;
import com.netcetera.trema.core.api.IValueNode;
import com.netcetera.trema.eclipse.TremaUtilEclipse;



/** 
 * Removes one or more text nodes from the database or one or more
 * values from a text node.
 */ 
public class RemoveAction extends TremaEditorAction {
  
  /**
   * Constructor.
   * 
   * @param treeViewer the treeViewer
   * @param text the text
   * @param imageDescriptor the imageDescriptor
   */
  public RemoveAction(TreeViewer treeViewer, String text, ImageDescriptor imageDescriptor) {
    super(treeViewer, text, imageDescriptor);
    setActionDefinitionId("com.netcetera.trema.eclipse.commands.removeNode");
  }
  
  /** {@inheritDoc} */
  @Override
  public void run() {
    if (!isEnabled()) {
      return;
    }
    
    IStructuredSelection selection = super.getSelection();
    
    // temporarily set the table tree redraw property to false to
    // reduce flashing when making more than one removal
    Tree tableTree = super.getTreeViewer().getTree();
    tableTree.setRedraw(false);
    
    try {
      // collect the text nodes and remove them eventually all at once
      List<ITextNode> textNodesToRemove = new ArrayList<ITextNode>();
      
      Iterator<?> i = selection.iterator();
      while (i.hasNext()) {
        Object toRemove = i.next();
        if (toRemove instanceof IValueNode) {
          IValueNode valueNode = (IValueNode) toRemove;
          ITextNode textNode = valueNode.getParent();
          if (textNode != null) {
            textNode.removeValueNode(valueNode);
          }
        } else if (toRemove instanceof ITextNode) {
          textNodesToRemove.add((ITextNode) toRemove);
        }
      }
      
      if (textNodesToRemove.size() > 0) {
        IDatabase db = textNodesToRemove.get(0).getParent();
        db.removeTextNodes(textNodesToRemove.toArray(new ITextNode[textNodesToRemove.size()]));
      }
    } finally {
      tableTree.setRedraw(true);
    }
  }
  
  /** {@inheritDoc} */
  public void updateEnablement(IStructuredSelection selection, int analysisResult) {
    setEnabled(TremaUtilEclipse.hasJustTextOrValueNodes(analysisResult));
  }
  
}
