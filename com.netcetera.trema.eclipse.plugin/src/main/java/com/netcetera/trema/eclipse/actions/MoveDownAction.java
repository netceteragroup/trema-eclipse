package com.netcetera.trema.eclipse.actions;

import java.util.Iterator;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Tree;

import com.netcetera.trema.core.api.IDatabase;
import com.netcetera.trema.core.api.ITextNode;
import com.netcetera.trema.eclipse.TremaUtilEclipse;
import com.netcetera.trema.eclipse.editors.DatabaseContainer;



/**
 * Action to move down one or more text nodes in a database.
 */
public class MoveDownAction extends TremaEditorAction {
  
  /**
   * Constructor.
   * 
   * @param treeViewer the treeViewer
   * @param text the text
   * @param imageDescriptor the imageDescriptor
   */
  public MoveDownAction(TreeViewer treeViewer, String text, ImageDescriptor imageDescriptor) {
    super(treeViewer, text, imageDescriptor);
    // associate with command
    setActionDefinitionId("com.netcetera.trema.eclipse.commands.moveDown");
  }
  
  /** {@inheritDoc} */
  @SuppressWarnings("unchecked")
  @Override
  public void run() {
    if (!isEnabled()) {
      return;
    }
    
    IStructuredSelection selection = super.getSelection();
    int result = TremaUtilEclipse.analyzeSelection(selection);
    
    if (TremaUtilEclipse.hasJustTextNodes(result)) {
      // temporarily set the table tree redraw property to false to
      // reduce flashing when making more than one move
      Tree tree = super.getTreeViewer().getTree();
      tree.setRedraw(false);
      try {
        IDatabase db = ((ITextNode) selection.getFirstElement()).getParent();
        db.moveDownTextNodes((ITextNode[]) selection.toList().toArray(new ITextNode[selection.size()]));
      } finally {
        tree.setRedraw(true);
      }
    }
  }
  
  /** {@inheritDoc} */
  public void updateEnablement(IStructuredSelection selection, int analysisResult) {
    setEnabled(false);
    
    if (TremaUtilEclipse.hasJustTextNodes(analysisResult)) {
      // get the last element to determine whether to enable the move down action
      ITextNode lastElement = null;
      Iterator<?> iterator = selection.iterator();
      while (iterator.hasNext()) {
        lastElement = (ITextNode) iterator.next();
      }
      DatabaseContainer dbContainer = (DatabaseContainer) super.getTreeViewer().getInput();
      IDatabase db = dbContainer.getDatabase();
      setEnabled(db.indexOf(lastElement) < db.getSize() - 1);
    }
  }
  
}
