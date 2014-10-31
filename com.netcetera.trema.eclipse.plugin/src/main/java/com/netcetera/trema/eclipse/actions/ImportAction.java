package com.netcetera.trema.eclipse.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchWindow;

import com.netcetera.trema.core.api.IDatabase;
import com.netcetera.trema.eclipse.TremaUtilEclipse;
import com.netcetera.trema.eclipse.editors.TremaEditor;
import com.netcetera.trema.eclipse.wizards.ImportWizard;



/**
 * Action to launch the Trema import wizard.
 */
public class ImportAction extends TremaEditorAction {
  
  private TremaEditor tremaEditor = null;
  private IWorkbenchWindow window = null;

  /**
   * Constructor.
   * 
   * @param window the window
   * @param tremaEditor the tremaEditor
   * @param text the text
   * @param imageDescriptor the imageDescriptor
   */
  public ImportAction(IWorkbenchWindow window, TremaEditor tremaEditor, String text,
                      ImageDescriptor imageDescriptor) {
    super(tremaEditor.getTreeViewer(), text, imageDescriptor);
    setActionDefinitionId("com.netcetera.trema.eclipse.commands.importFile");
    this.tremaEditor = tremaEditor;
    this.window = window;
  }
  
  
  
  /** {@inheritDoc} */
  public void run() {
    if (!isEnabled()) {
      return;
    }
    
    IDatabase db = TremaUtilEclipse.getDatabase(super.getTreeViewer());
    IFile dbFile = ((IFileEditorInput) tremaEditor.getTextEditor().getEditorInput()).getFile();
    
    ImportWizard wizard = new ImportWizard(db, dbFile.getParent().getLocation().toOSString());
    WizardDialog dialog = new WizardDialog(window.getShell(), wizard);
    dialog.open();
  }
  
  /** {@inheritDoc} */
  public void updateEnablement(IStructuredSelection selection, int analysisResult) {
    setEnabled(true); // always enabled
  }

}
