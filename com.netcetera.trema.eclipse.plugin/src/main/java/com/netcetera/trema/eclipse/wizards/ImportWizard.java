package com.netcetera.trema.eclipse.wizards;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.Wizard;

import com.netcetera.trema.core.api.IDatabase;
import com.netcetera.trema.core.importing.Change;
import com.netcetera.trema.core.importing.ChangesAnalyzer;
import com.netcetera.trema.eclipse.TremaEclipseUtil;
import com.netcetera.trema.eclipse.TremaPlugin;
import com.netcetera.trema.eclipse.dialogs.LogDialog;



/** 
 * The Trema CSV import wizard. This wizard consist of 2 pages. 
 */
public class ImportWizard extends Wizard {
  
  private IDatabase db = null;
  private String initialFilePath = null;
  private ImportWizardFilePage filePage = null;
  private ImportWizardChangesPage changesPage = null;
  private String importLog = null;
  
  /**
   * Constructs a new wizard instance.
   * 
   * @param db the database being imported to
   * @param initialFilePath the path to be initially displayed on the first
   * page, may be <code>null</code>
   */
  public ImportWizard(IDatabase db, String initialFilePath) {
    this.db = db;
    this.initialFilePath = initialFilePath;

    setWindowTitle("Trema CSV/XLS Import Wizard");
    setNeedsProgressMonitor(true);
  }
  
  /** {@inheritDoc} */
  @Override
  public void addPages() {
    filePage = new ImportWizardFilePage(db, initialFilePath);
    addPage(filePage);
    changesPage = new ImportWizardChangesPage(db, filePage);
    addPage(changesPage);
    setDefaultPageImageDescriptor(
        TremaPlugin.getDefault().getImageDescriptor(
        "icons/import_wiz.gif"));
  }  
  
  /** {@inheritDoc} */
  @Override
  public boolean performFinish() {
    final List<Change> conflictingChanges = changesPage.getConflictingChanges();
    final List<Change> nonConflictingChanges = changesPage.getNonConflictingChanges();

    // create the runnable operation
    IRunnableWithProgress operation =  new IRunnableWithProgress() {
      public void run(IProgressMonitor monitor) throws InvocationTargetException {
        try {
          doApplyChanges(conflictingChanges, nonConflictingChanges, monitor);
        } catch (Exception e) {
          throw new InvocationTargetException(e);
        } finally {
          monitor.done();
        }
      }
    };
    
    try {
      getContainer().run(false, false, operation);
    } catch (InterruptedException e) {
      return false;
    } catch (InvocationTargetException e) {
      Throwable realException = e.getTargetException();
      MessageDialog.openError(getShell(), "Error", realException.getMessage());
      return false;
    }
    
    // on success, show summary dialog
    if (importLog != null) {
      new LogDialog(getShell(), "Import Log", "&Import log:", importLog).open();
    }
    
    // store some dialog settings
    IDialogSettings dialogSettings = TremaPlugin.getDefault().getDialogSettings();
    dialogSettings.put(ImportWizardFilePage.DS_KEY_FILE_PATHS,
        TremaEclipseUtil.rotate(dialogSettings.getArray(ImportWizardFilePage.DS_KEY_FILE_PATHS),
                               filePage.getFilePath(), 5));
    dialogSettings.put(ImportWizardFilePage.DS_KEY_ENCODING, filePage.getEncoding());
    dialogSettings.put(ImportWizardFilePage.DS_KEY_SEPARATOR, String.valueOf(filePage.getSeparator()));
    return true;
  }
  
  /**
   * Worker method to apply changes. In addition, the instance variable
   * <code>importLog</code> will be set.
   * 
   * @param conflictingChanges the conflicting changes to apply
   * @param nonConflictingChanges the non-conflicting changes to apply
   * @param monitor the progress monitor to use
   */
  private void doApplyChanges(List<Change> conflictingChanges, List<Change> nonConflictingChanges, 
      IProgressMonitor monitor) {
    monitor.beginTask("Applying changes...", conflictingChanges.size() + nonConflictingChanges.size());
    
    // lists used for logging...
    List<String> appliedConflicting = new ArrayList<String>(conflictingChanges.size());
    List<String> deniedConflicting = new ArrayList<String>(conflictingChanges.size());
    List<String> appliedNonConflicting = new ArrayList<String>(nonConflictingChanges.size());
    List<String> deniedNonConflicting = new ArrayList<String>(nonConflictingChanges.size());
    
    Iterator<Change> i = conflictingChanges.iterator();
    while (i.hasNext()) {
      Change change = i.next();
      if (ChangesAnalyzer.isApplicable(change)) {
        appliedConflicting.add(change.getKey());        
      } else {
        deniedConflicting.add(change.getKey());
      }
      ChangesAnalyzer.applyChange(db, change);
      monitor.worked(1);
    }
    
    i = nonConflictingChanges.iterator();
    while (i.hasNext()) {
      Change change = i.next();
      if (ChangesAnalyzer.isApplicable(change)) {
        appliedNonConflicting.add(change.getKey());        
      } else {
        deniedNonConflicting.add(change.getKey());
      }
      ChangesAnalyzer.applyChange(db, change);
      monitor.worked(1);
    }
    
    // create import log
    StringBuffer buffer = new StringBuffer();
    buffer.append(new Date()).append("\n").append("Import log for: ").append(filePage.getFilePath()).append("\n\n");
    buffer.append("Accepted conflicting changes (").append(appliedConflicting.size()).append("):\n");
    buffer.append(TremaEclipseUtil.listToString(appliedConflicting, "\n")).append("\n\n");
    buffer.append("Denied conflicting changes (").append(deniedConflicting.size()).append("):\n");
    buffer.append(TremaEclipseUtil.listToString(deniedConflicting, "\n")).append("\n\n");
    buffer.append("Accepted non-conflicting changes (").append(appliedNonConflicting.size()).append("):\n");
    buffer.append(TremaEclipseUtil.listToString(appliedNonConflicting, "\n")).append("\n\n");
    buffer.append("Denied non-conflicting changes (").append(deniedNonConflicting.size()).append("):\n");
    buffer.append(TremaEclipseUtil.listToString(deniedNonConflicting, "\n")).append("\n\n");
    
    importLog = buffer.toString();   
  }
  
}
