package com.netcetera.trema.eclipse.actions;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.IWorkbenchCommandConstants;



/**
 * Action to select all currently visible elements in the table tree
 * viewer.
 */
public class SelectAllAction extends TremaEditorAction {

  /**
   * Constructor.
   * 
   * @param treeViewer the treeViewer
   * @param text the text
   * @param imageDescriptor the imageDescriptor
   */
  public SelectAllAction(TreeViewer treeViewer, String text, ImageDescriptor imageDescriptor) {
    super(treeViewer, text, imageDescriptor);
    setActionDefinitionId(IWorkbenchCommandConstants.EDIT_SELECT_ALL);
  }
  
  /** {@inheritDoc} */
  @Override
  public void run() {
    if (!isEnabled()) {
      return;
    }
    
    super.getTreeViewer().getTree().selectAll();
    // this is needed to trigger a JFace SelectionChanged event
    super.getTreeViewer().setSelection(super.getTreeViewer().getSelection());
  }
  
  /** {@inheritDoc} */
  public void updateEnablement(IStructuredSelection selection, int analysisResult) {
    setEnabled(true);
  }
  
}
