package com.netcetera.trema.eclipse.actions;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.window.Window;

import com.netcetera.trema.core.XMLValueNode;
import com.netcetera.trema.core.api.ITextNode;
import com.netcetera.trema.eclipse.TremaUtilEclipse;
import com.netcetera.trema.eclipse.dialogs.ValueNodeDialog;
import com.netcetera.trema.eclipse.validators.LanguageValidator;



/**
 * Action opening a dialog for adding a new value node to one or more
 * text nodes.
 */
public class AddValueNodeAction extends TremaEditorAction {

  /**
   * Constructor.
   * 
   * @param treeViewer the treeViewer
   * @param text the text
   * @param imageDescriptor the imageDescriptor
   */
  public AddValueNodeAction(TreeViewer treeViewer, String text, ImageDescriptor imageDescriptor) {
    super(treeViewer, text, imageDescriptor);
    // associate with command
    setActionDefinitionId("com.netcetera.trema.eclipse.commands.addValueNode");
  }
  
  /** {@inheritDoc} */
  @SuppressWarnings("unchecked")
  public void run() {
    if (!isEnabled()) {
      return;
    }
    
    
    IStructuredSelection selection = super.getSelection();
    int result = TremaUtilEclipse.analyzeSelection(selection);
    
    TreeViewer viewer = super.getTreeViewer();
    if (TremaUtilEclipse.hasJustTextNodes(result)) {
      ITextNode[] textNodes = (ITextNode[]) selection.toList().toArray(new ITextNode[selection.size()]);
      for (int i = 0; i < textNodes.length; i++) {
        viewer.expandToLevel(textNodes[i], 1); // make the children visible
      }
      
      String title = "Add Value Node";
      if (textNodes.length == 1) {
        title += " For Key \"" + textNodes[0].getKey() + "\"";
      }
      
      ValueNodeDialog valueNodeDialog = new ValueNodeDialog(viewer.getControl().getShell(), title,
                                                            null, new LanguageValidator(textNodes),
                                                            ValueNodeDialog.TYPE_ADD);
      if (valueNodeDialog.open() != Window.OK) {
        return;
      }
      
      for (int i = 0; i < textNodes.length; i++) {
        textNodes[i].addValueNode(new XMLValueNode(valueNodeDialog.getLanguage(), valueNodeDialog.getStatus(),
                                                   valueNodeDialog.getValue()));
      }
    }
  }
  
  /** {@inheritDoc} */
  public void updateEnablement(IStructuredSelection selection, int analysisResult) {
    setEnabled(TremaUtilEclipse.hasJustTextNodes(analysisResult));
  }

}
