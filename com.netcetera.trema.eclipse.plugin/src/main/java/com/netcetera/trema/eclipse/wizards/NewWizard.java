package com.netcetera.trema.eclipse.wizards;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.dialogs.ContainerGenerator;
import org.eclipse.ui.ide.IDE;

import com.netcetera.trema.eclipse.TremaEclipseUtil;
import com.netcetera.trema.eclipse.TremaPlugin;
import com.netcetera.trema.eclipse.TremaUtilEclipse;



/**
 * Standard workbench "new" wizard to create a new Trema XML file
 * resource in the workspace. This wizard consists of just one page.
 */
public class NewWizard extends Wizard implements INewWizard {
  
  /** The wizard page. */
  private NewWizardOptionsPage wizardPage = null;
  
  /** The current workbench selection. */
  private IStructuredSelection selection;
  
  /** Constructs a new instance. */
  public NewWizard() {
    setWindowTitle("Trema");
    setNeedsProgressMonitor(true);
  }
  
  /** {@inheritDoc} */
  @Override
  public void addPages() {
    wizardPage = new NewWizardOptionsPage(selection);
    addPage(wizardPage);
    setDefaultPageImageDescriptor(
TremaPlugin.getDefault().getImageDescriptor(
        "icons/newfile_wiz.gif"));
  }
  
  /** {@inheritDoc} */
  @Override
  public boolean performFinish() {
    final IPath folderPath = wizardPage.getFolderPath();
    final String fileName = wizardPage.getFileName();
    final String masterLanguage = wizardPage.getMasterLanguage();
    final String encoding = wizardPage.getEncoding();
    final String schemaLocation = wizardPage.getSchemaLocation();
    
    IRunnableWithProgress operation = new WorkspaceModifyOperation(null) {
      @Override
      public void execute(IProgressMonitor monitor) throws InvocationTargetException {
        try {
          doFinish(folderPath, fileName, masterLanguage, encoding, schemaLocation, monitor);
        } catch (CoreException e) {
          throw new InvocationTargetException(e);
        } finally {
          monitor.done();
        }
      }
    };
    
    try {
      getContainer().run(true, false, operation);
    } catch (InterruptedException e) {
      return false;
    } catch (InvocationTargetException e) {
      Throwable realException = e.getTargetException();
      MessageDialog.openError(getShell(), "Error", realException.getMessage());
      return false;
    }
    
    // on success, store some dialog settings
    IDialogSettings dialogSettings = TremaPlugin.getDefault().getDialogSettings();
    dialogSettings.put(NewWizardOptionsPage.DS_KEY_ENCODING, encoding);
    dialogSettings.put(NewWizardOptionsPage.DS_KEY_SCHEMA, schemaLocation);
    return true;
  }

  /**
   * Worker method that creates an empty Trema XML database file with
   * a given file name under a given container. The container will be
   * created if is does not already exist. An exception is
   * @param folderPath the path to the parent folder
   * @param fileName the path to the file
   * @param masterLanguage the master language
   * @param encoding the encoding to be used
   * @param schemaLocation the XML schema location to reference, may be
   * <code>null</code>
   * @param monitor the progress monitor
   * @throws CoreException if the file could not be created
   */
  private void doFinish(IPath folderPath, String fileName, String masterLanguage, String encoding, 
                        String schemaLocation, IProgressMonitor monitor) throws CoreException {
    monitor.beginTask("", 3000); // hack to make the task name be displayed
    monitor.setTaskName("Creating file " + fileName + "...");
    
    IPath absolutePath = folderPath.makeAbsolute();
    
    // possibly create the folder
    ContainerGenerator generator = new ContainerGenerator(absolutePath);
    generator.generateContainer(new SubProgressMonitor(monitor, 1000));
    
    IPath newFilePath = absolutePath.append(fileName);
    
    final IFile newFile = ResourcesPlugin.getWorkspace().getRoot().getFile(newFilePath);   
    
    // get the initial contents and create the file
    InputStream initialContents = null;
    try {
      initialContents = getInitialContents(masterLanguage, encoding, schemaLocation);
      newFile.create(initialContents, false, new SubProgressMonitor(monitor, 1000));
    } catch (UnsupportedEncodingException e) {
      // the page validation should prevent this
      throw new CoreException(TremaUtilEclipse.createErrorStatus(e.getMessage() + " is an unsupported encoding."));
    } finally {
      if (initialContents != null) {
        try {
          initialContents.close();
        } catch (IOException e) {
          throw new CoreException(TremaUtilEclipse.createErrorStatus("Could not write output: " + e.getMessage()));
        }
      }
    }
    
    // open the file
    monitor.setTaskName("Opening file for editing...");
    getShell().getDisplay().asyncExec(new Runnable() {
      public void run() {
        IWorkbenchPage workbenchPage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        try {
          IDE.openEditor(workbenchPage, newFile, true);
        } catch (PartInitException e) {
          // ignore
        }
      }
    });
    monitor.worked(1000);
  }
  
  /**
   * Creates an input stream with the initial contents for an empty
   * Trema XML database.
   * @param masterLanguage the master language
   * @param encoding the encoding to be used
   * @param schemaLocation the XML schema location to be referenced,
   * may be <code>null</code>
   * @return the initial contents for an empty Trema XML database
   * @throws UnsupportedEncodingException if the given encoding is not
   * supported
   */
  private InputStream getInitialContents(String masterLanguage, String encoding, String schemaLocation)
  throws UnsupportedEncodingException {
    StringBuffer contents = new StringBuffer(1024);
    String lineSeparator = TremaEclipseUtil.getDefaultLineSeparator();
    contents.append("<?xml version=\"1.0\" encoding=\"").append(encoding).append("\"?>");
    contents.append(lineSeparator);
    contents.append("<!-- generated on " + new Date() + " -->");
    contents.append(lineSeparator);
    contents.append("<trema masterLang=\"").append(masterLanguage).append("\"");
    if (schemaLocation != null && schemaLocation.length() > 0) {
      contents.append(" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"");
      contents.append(" xsi:noNamespaceSchemaLocation=\"").append(schemaLocation).append("\"");
    }
    contents.append("/>");
    return new ByteArrayInputStream(contents.toString().getBytes(encoding)); 
  }
  
  /** {@inheritDoc} */
  public void init(IWorkbench workbench,  IStructuredSelection selection) {
    this.selection = selection;
  }
  
}
