package com.netcetera.trema.eclipse.actions;

import java.util.Iterator;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.window.Window;

import com.netcetera.trema.core.api.ITextNode;
import com.netcetera.trema.core.api.IValueNode;
import com.netcetera.trema.eclipse.TremaUtilEclipse;
import com.netcetera.trema.eclipse.dialogs.TextNodeDialog;
import com.netcetera.trema.eclipse.dialogs.ValueNodeDialog;
import com.netcetera.trema.eclipse.validators.KeyValidator;



/**
 * Action opening an edit dialog for editing text or value nodes.
 */
public class EditAction extends TremaEditorAction {
  
  /**
   * Constructor.
   * 
   * @param treeViewer the associated tree viewer
   * @param text the text
   * @param imageDescriptor the image descriptor
   */
  public EditAction(TreeViewer treeViewer, String text, ImageDescriptor imageDescriptor) {
    super(treeViewer, text, imageDescriptor);
 // associate with command
    setActionDefinitionId("com.netcetera.trema.eclipse.commands.editTextNode");
  }
  
  /** {@inheritDoc} */
  @Override
  public void run() {
    if (!isEnabled()) {
      return;
    }
    
    TreeViewer treeViewer = super.getTreeViewer();
    
    IStructuredSelection selection = super.getSelection();
    int result = TremaUtilEclipse.analyzeSelection(selection);
    
    if (TremaUtilEclipse.hasJustValueNodes(result)) {
      if (selection.size() > 1) {
        // multi edit
        ValueNodeDialog valueNodeDialog = new ValueNodeDialog(treeViewer.getControl().getShell(), 
            "Edit Value Nodes", (IValueNode) selection.getFirstElement(),
            null, ValueNodeDialog.TYPE_EDIT_MULTI);
        if (valueNodeDialog.open() != Window.OK) {
          return;
        }
        Iterator<?> iterator = selection.iterator();
        while (iterator.hasNext()) {
          IValueNode valueNode = (IValueNode) iterator.next();
          valueNode.setStatus(valueNodeDialog.getStatus());
        }
      } else {
        // single edit
        IValueNode valueNode = (IValueNode) selection.getFirstElement();
        
        ValueNodeDialog valueNodeDialog = new ValueNodeDialog(treeViewer.getControl().getShell(), 
            "Edit Value Node For Key \"" + (valueNode.getParent()).getKey() + "\"",
            valueNode, null, ValueNodeDialog.TYPE_EDIT_SINGLE);
        if (valueNodeDialog.open() != Window.OK) {
          return;
        }
        
        valueNode.setStatus(valueNodeDialog.getStatus());
        valueNode.setValue(valueNodeDialog.getValue());
      }
      
    } else if (TremaUtilEclipse.hasJustTextNodes(result)) {
      if (selection.size() > 1) {
        // multi edit
        TextNodeDialog textNodeDialog = new TextNodeDialog(treeViewer.getControl().getShell(), 
            "Edit Text Nodes", (ITextNode) selection.getFirstElement(), null,
            TextNodeDialog.TYPE_EDIT_MULTI);
        if (textNodeDialog.open() != Window.OK) {
          return;
        }
        Iterator<?> iterator = selection.iterator();
        while (iterator.hasNext()) {
          ITextNode textNode = (ITextNode) iterator.next();
          textNode.setContext(textNodeDialog.getContext());
        }
      } else {
        // single edit
        ITextNode textNode = (ITextNode) selection.getFirstElement();
        
        TextNodeDialog textNodeDialog = new TextNodeDialog(treeViewer.getControl().getShell(), "Edit Text Node",
            textNode, new KeyValidator(textNode.getParent(), textNode.getKey()),
            TextNodeDialog.TYPE_EDIT_SINGLE);
        
        if (textNodeDialog.open() != Window.OK) {
          return;
        }
        textNode.setKey(textNodeDialog.getKey());
        textNode.setContext(textNodeDialog.getContext());
      }
    }
  }
  
  /** {@inheritDoc} */
  public void updateEnablement(IStructuredSelection selection, int analysisResult) {
    setEnabled(TremaUtilEclipse.hasJustTextNodes(analysisResult) || TremaUtilEclipse.hasJustValueNodes(analysisResult));
  }
  
}
