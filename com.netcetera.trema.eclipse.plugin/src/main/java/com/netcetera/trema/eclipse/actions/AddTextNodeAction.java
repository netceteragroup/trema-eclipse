package com.netcetera.trema.eclipse.actions;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.window.Window;

import com.netcetera.trema.core.Status;
import com.netcetera.trema.core.XMLTextNode;
import com.netcetera.trema.core.XMLValueNode;
import com.netcetera.trema.core.api.IDatabase;
import com.netcetera.trema.core.api.ITextNode;
import com.netcetera.trema.eclipse.TremaUtilEclipse;
import com.netcetera.trema.eclipse.dialogs.TextNodeDialog;
import com.netcetera.trema.eclipse.validators.KeyValidator;



/**
 * Action opening a dialog for adding a new text node to the database.
 */ 
public class AddTextNodeAction extends TremaEditorAction {

  /**
   * Constructor.
   * 
   * @param treeViewer the treeViewer
   * @param text the text 
   * @param imageDescriptor the imageDescriptor
   */
  public AddTextNodeAction(TreeViewer treeViewer, String text, ImageDescriptor imageDescriptor) {
    super(treeViewer, text, imageDescriptor);
    // associate with command
    setActionDefinitionId("com.netcetera.trema.eclipse.commands.addTextNode");
  }
  
  /** {@inheritDoc} */
  public void run() {
    if (!isEnabled()) {
      return;
    }
    TreeViewer viewer = super.getTreeViewer();
    
    IDatabase db = TremaUtilEclipse.getDatabase(viewer);
    if (db == null) {
      return;
    }
    
    viewer.expandToLevel(db, 1); // make the children visible
    TextNodeDialog textNodeDialog = new TextNodeDialog(viewer.getControl().getShell(),
                                                       "Add Text Node", null,
                                                       new KeyValidator(db, null), TextNodeDialog.TYPE_ADD);
    if (textNodeDialog.open() != Window.OK) {
      return;
    }
      
    ITextNode textNode = new XMLTextNode(textNodeDialog.getKey(), textNodeDialog.getContext());
    String[] languages = textNodeDialog.getLanguages();
    for (int i = 0; i < languages.length; i++) {
      textNode.addValueNode(new XMLValueNode(languages[i], Status.INITIAL, ""));
    }
    db.addTextNode(textNode);
  }
  
  /** {@inheritDoc} */
  public void updateEnablement(IStructuredSelection selection, int analysisResult) {
    setEnabled(true);
  }

}
