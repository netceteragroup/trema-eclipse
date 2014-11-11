package com.netcetera.trema.eclipse.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchWindow;

import com.netcetera.trema.core.api.IDatabase;
import com.netcetera.trema.core.api.ITextNode;
import com.netcetera.trema.eclipse.TremaEclipseUtil;
import com.netcetera.trema.eclipse.TremaUtilEclipse;
import com.netcetera.trema.eclipse.editors.TremaEditor;
import com.netcetera.trema.eclipse.wizards.ExportWizard;



/**
 * Action to launch the Trema export wizard.
 */
public class ExportAction extends TremaEditorAction {

  private IWorkbenchWindow window = null;
  private TremaEditor tremaEditor = null;
  
  /**
   * Constructor.
   * 
   * @param window the window 
   * @param tremaEditor The tremaEditor
   * @param text The text
   * @param imageDescriptor The imageDescriptor
   */
  public ExportAction(IWorkbenchWindow window, TremaEditor tremaEditor, String text,
                      ImageDescriptor imageDescriptor) {
    super(tremaEditor.getTreeViewer(), text, imageDescriptor);
    setActionDefinitionId("com.netcetera.trema.eclipse.commands.exportFile");
    this.tremaEditor = tremaEditor;
    this.window = window;
  }
  
  /** {@inheritDoc} */
  public void run() {
    if (!isEnabled()) {
      return;
    }
    
    
    IDatabase db = TremaUtilEclipse.getDatabase(super.getTreeViewer());
    ITextNode[] selectedTextNodes =
      TremaUtilEclipse.getTextNodes(super.getSelection());
    IFile dbFile = ((IFileEditorInput) tremaEditor.getTextEditor().getEditorInput()).getFile();
    
    String initialFolderPath = dbFile.getParent().getLocation().toOSString();
    String initialBaseName = TremaEclipseUtil.stripExtension(dbFile.getName());
    
    ExportWizard wizard =
      new ExportWizard(db, selectedTextNodes, initialFolderPath, initialBaseName);
    WizardDialog dialog = new WizardDialog(window.getShell(), wizard);
    dialog.open();
  }
  
  /** {@inheritDoc} */
  public void updateEnablement(IStructuredSelection selection, int analysisResult) {
    setEnabled(true); // always enabled
  }

}
