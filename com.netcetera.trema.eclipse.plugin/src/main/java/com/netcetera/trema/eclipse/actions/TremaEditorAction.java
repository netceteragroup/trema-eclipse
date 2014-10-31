package com.netcetera.trema.eclipse.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;



/**
 * Represents an action used in the table tree viewer page of the
 * Trema editor.
 */
public abstract class TremaEditorAction extends Action {
  
  private TreeViewer treeViewer = null;
  
  

  /**
   * Creates a new instance.
   * @param treeViewer the associated tree viewer
   * @param text the text
   * @param imageDescriptor the image descriptor
   */
  public TremaEditorAction(TreeViewer treeViewer, String text, ImageDescriptor imageDescriptor) {
    super(text, imageDescriptor);
    this.treeViewer = treeViewer;
  }
  
  /**
   * Gets the selection of the treeViewer. Can be StructuredSelection.EMPTY
   * 
   * @return the selection
   */
  public IStructuredSelection getSelection() {
    if (treeViewer != null && treeViewer.getSelection() instanceof IStructuredSelection) {
      return (IStructuredSelection) treeViewer.getSelection();
    }
    return StructuredSelection.EMPTY;
  }
  
  
  
  /**
   * Gets the treeViewer.
   * 
   * @return the treeViewer
   */
  public TreeViewer getTreeViewer() {
    return treeViewer;
  }
  /**
   * Updates the enablement of this action depending on a given
   * selection and a selection analysis result.
   * @param selection the selection
   * @param analysisResult the selection analysis result
   * @see com.netcetera.trema.eclipse.TremaUtilEclipse#analyzeSelection
   */
  public abstract void updateEnablement(IStructuredSelection selection, int analysisResult);

}
