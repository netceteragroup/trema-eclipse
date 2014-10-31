package com.netcetera.trema.eclipse.actions;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;

import com.netcetera.trema.core.api.IDatabase;
import com.netcetera.trema.core.api.ITextNode;
import com.netcetera.trema.eclipse.editors.DatabaseContainer;



/**
 * Action to expand or collapse a table tree viewer element.
 */
public class ExpandCollapseAction extends TremaEditorAction {
  
  private boolean expand = false;
  
  
  /**
   * Constructor.
   * 
   * @param treeViewer the associated tree viewer
   * @param text the text
   * @param imageDescriptor the image descriptor
   * @param expand flag indicating the type of action
   */
  public ExpandCollapseAction(TreeViewer treeViewer, String text, 
                              ImageDescriptor imageDescriptor, boolean expand) {
    super(treeViewer, text, imageDescriptor);
    this.expand = expand;
    if (expand){
      setActionDefinitionId("com.netcetera.trema.eclipse.commands.expandTree");
    }else{
      setActionDefinitionId("com.netcetera.trema.eclipse.commands.collapseTree");
    }
    
  }
  
  /** {@inheritDoc} */
  @Override
  public void run() {
    if (!isEnabled()) {
      return;
    }
    
    TreeViewer treeViewer = super.getTreeViewer();
    treeViewer.getTree().setRedraw(false);
    try{
      if (expand){
        treeViewer.expandToLevel(TreeViewer.ALL_LEVELS);
      } else{
        IDatabase db = ((DatabaseContainer)treeViewer.getInput()).getDatabase();
        for (ITextNode node : db.getTextNodes()){
          treeViewer.setExpandedState(node, false);
        }
      }
    } finally {
      treeViewer.getTree().setRedraw(true);
    }
    
    // temporarily set the table tree redraw property to false to
    // reduce flashing when processing more than one element
//    Tree tableTree = treeViewer.getTree();
//    tableTree.setRedraw(false);
//    
//    try {
//      IStructuredSelection selection = super.getSelection();
//      Iterator<?> iterator = selection.iterator();
//      
//      while (iterator.hasNext()) {
//        Object element = iterator.next();
//        if (treeViewer.isExpandable(element)) {
//          treeViewer.setExpandedState(element, expand);
//        }
//      }
//    } finally {
//      tableTree.setRedraw(true);
//    }
  }
  
  /** {@inheritDoc} */
  public void updateEnablement(IStructuredSelection selection, int analysisResult) {
    setEnabled(true);
  }
  
}
