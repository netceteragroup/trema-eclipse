package com.netcetera.trema.eclipse.wizards;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.dialogs.ListSelectionDialog;

import com.netcetera.trema.core.Status;
import com.netcetera.trema.core.api.IDatabase;
import com.netcetera.trema.core.api.IExportFilter;
import com.netcetera.trema.core.api.IExporter;
import com.netcetera.trema.core.api.ITextNode;
import com.netcetera.trema.core.exporting.CSVExporter;
import com.netcetera.trema.core.exporting.ExportException;
import com.netcetera.trema.core.exporting.FileOutputStreamFactory;
import com.netcetera.trema.core.exporting.MessageFormatEscapingFilter;
import com.netcetera.trema.core.exporting.PropertiesExporter;
import com.netcetera.trema.core.exporting.XLSExporter;
import com.netcetera.trema.eclipse.TremaEclipseUtil;
import com.netcetera.trema.eclipse.TremaPlugin;
import com.netcetera.trema.eclipse.TremaUtilEclipse;
import com.netcetera.trema.eclipse.exporting.MonitoringTremaCSVPrinter;



/**
 * Wizard to export a database to a CSV/XLS file or a Java properties file.
 * This wizard consist of one single page.
 */
public class ExportWizard extends Wizard {
  
  /**
   * Auxiliary class to encapsulate an export language and a
   * corresponding file name.
   */
  public class LanguageAndFileName {
    
    private String language = null;
    private String fileName = null;
    
    /**
     * Constructor.
     * 
     * @param language the language
     * @param fileName the filename
     */
    public LanguageAndFileName(String language, String fileName) {
      super();
      this.language = language;
      this.fileName = fileName;
    }
    
    /**
     * Gets the filename.
     * 
     * @return the filename
     */
    public String getFileName() {
      return fileName;
    }
    
    /**
     * Gets the language.
     * 
     * @return the language
     */
    public String getLanguage() {
      return language;
    }
    
    /** {@inheritDoc} */
    @Override
    public String toString() {
      // this String will be displayed in the dialog
      return getFileName() + " (" + getLanguage() + ")";
    }
    
  }
  
  private IDatabase db = null;
  private ITextNode[] selectedTextNodes = null;
  private String initialFolderPath = null;
  private String initialBaseName = null;
  private ExportWizardOptionsPage optionsPage = null;

  /**
   * Constructs a new wizard instance.
   * @param db the database being exported
   * @param selectedTextNodes the currently selected text nodes, may be
   * <code>null</code>
   * @param initialFolderPath the folder path that should initially be
   * displayed on the first page, may be <code>null</code>
   * @param initialBaseName the base name that should initially be
   * displayed on the first page, may be <code>null</code>
   */
  public ExportWizard(IDatabase db, ITextNode[] selectedTextNodes,
                                   String initialFolderPath, String initialBaseName) {
    this.db = db;
    this.selectedTextNodes = selectedTextNodes;
    this.initialFolderPath = initialFolderPath;
    this.initialBaseName = initialBaseName;
    setWindowTitle("Trema CSV/Properties Export Wizard");
    setNeedsProgressMonitor(true);
  }
  
  /** {@inheritDoc} */
  @Override
  public void addPages() {
    optionsPage = new ExportWizardOptionsPage(db, selectedTextNodes, initialFolderPath, initialBaseName);
    addPage(optionsPage);
    setDefaultPageImageDescriptor(
TremaPlugin.getDefault().getImageDescriptor(
        "icons/export_wiz.gif"));
  }  
  
  /** {@inheritDoc} */
  @Override
  public boolean performFinish() {
    final String masterLanguage = db.getMasterLanguage();
    final ITextNode[] textNodes = optionsPage.getTextNodes();
    String[] languages = optionsPage.getLanguages();
    final Status[] status = optionsPage.getStatus();
    final IPath folderPath = optionsPage.getFolderPath();
    final String baseName = optionsPage.getBaseName();
    final String csvEncoding = optionsPage.getEncoding();
    final char csvSeparator = optionsPage.getSeparator();
    final boolean useMessageFormatFilter = optionsPage.isMessageFormatButtonChecked();
    
    boolean xlsFileExport = optionsPage.isXLSExportType();
    boolean csvFileExport = optionsPage.isCSVExportType();
    boolean propertiesFileExport = optionsPage.isPropertiesExportType();
    
    String fileExtension = null;
    if (csvFileExport) {
      fileExtension = ".csv";
    } else if (propertiesFileExport) {
      fileExtension = ".properties";
    } else if (xlsFileExport) {
      fileExtension = ".xls";
    }
    // the languages to be exported
    final List<LanguageAndFileName> exportList = new ArrayList<LanguageAndFileName>(); 
    // conflicting languages wehre the files already exist
    List<LanguageAndFileName> conflictList = new ArrayList<LanguageAndFileName>(); 
    for (int i = 0; i < languages.length; i++) {
      IPath path = folderPath.append(baseName + "_" + languages[i] + fileExtension);
      LanguageAndFileName languageAndFileName = new LanguageAndFileName(languages[i], path.toOSString());
      exportList.add(languageAndFileName);
      if (path.toFile().exists()) {
        conflictList.add(languageAndFileName);
      }
    }
    
    // check if the specified folder exists
    File folder = folderPath.toFile();
    if (!folder.exists()) {
      String question = "The directory " + folder.getAbsolutePath()
                        + " does not exist. Would you like it to be created?";
      if (!MessageDialog.openQuestion(getShell(), "Trema Export", question)) {
        return false;
      }
      if (!folder.mkdirs()) {
        MessageDialog.openError(getShell(), "Trema Export", "Could not create directory " + folder.getAbsolutePath());
        return false;
      }
    } else {
      // open a selection dialog displaying the conflicting languages
      if (conflictList.size() > 0) {
        ListSelectionDialog dialog = new ListSelectionDialog(getShell(), conflictList,
             new ArrayContentProvider(), new LabelProvider(),
             "Please select the files to be overwritten:");
        dialog.setTitle("Trema Export");
        dialog.setInitialElementSelections(conflictList); // preselect all elements
        if (Window.OK != dialog.open()) {
          return false; // cancel was pressed
        }
        // remove the languages that were not selected in the dialog
        exportList.removeAll(conflictList);
        Object[] res =  dialog.getResult();
        for (Object r : res) {
          if (r instanceof LanguageAndFileName) {
            exportList.add((LanguageAndFileName) r);
          }
        }
      }
    }
    
    if (exportList.size() == 0) {
      return false;
    }
    
    // create the runnable opration
    IRunnableWithProgress operation = null;
    if (csvFileExport) {
      operation = new WorkspaceModifyOperation(null) {
        @Override
        public void execute(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
          try {
            doCSVExport(masterLanguage, textNodes, exportList, status, csvEncoding, csvSeparator, monitor);
          } catch (CoreException e) {
            throw new InvocationTargetException(e);
          } finally {
            monitor.done();
          }
        }
      };
    } else if (propertiesFileExport) {
      operation = new WorkspaceModifyOperation(null) {
        @Override
        public void execute(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
          try {
            doPropertiesExport(textNodes, exportList, status, monitor, useMessageFormatFilter);
          } catch (CoreException e) {
            throw new InvocationTargetException(e);
          } finally {
            monitor.done();
          }
        }
      };
    } else if (xlsFileExport) {
      operation = new WorkspaceModifyOperation(null) {
        @Override
        public void execute(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
          try {
            doXLSExport(masterLanguage, textNodes, exportList, status, monitor);
          } catch (CoreException e) {
            throw new InvocationTargetException(e);
          } finally {
            monitor.done();
          }
        }
      };
    }
    
    try {
      getContainer().run(true, true, operation);
    } catch (InterruptedException e) {
      return false;
    } catch (InvocationTargetException e) {
      Throwable realException = e.getTargetException();
      MessageDialog.openError(getShell(), "Error", realException.getMessage());
      return false;
    }
    
    // on success, store some dialog settings
    IDialogSettings dialogSettings = TremaPlugin.getDefault().getDialogSettings();
    dialogSettings.put(ExportWizardOptionsPage.DS_KEY_LANGUAGES, languages);
    dialogSettings.put(ExportWizardOptionsPage.DS_KEY_STATUS, Status.getNames(status));
    dialogSettings.put(ExportWizardOptionsPage.DS_KEY_FOLDER_PATHS,
        TremaEclipseUtil.rotate(dialogSettings.getArray(ExportWizardOptionsPage.DS_KEY_FOLDER_PATHS),
                               folderPath.toOSString(), 5));
    if (csvFileExport) {
      dialogSettings.put(ExportWizardOptionsPage.DS_KEY_EXPORTTYPE, ExportWizardOptionsPage.CSV);
      dialogSettings.put(ExportWizardOptionsPage.DS_KEY_ENCODING, csvEncoding);
      dialogSettings.put(ExportWizardOptionsPage.DS_KEY_SEPARATOR, String.valueOf(csvSeparator));
    } else if (propertiesFileExport) {
      dialogSettings.put(ExportWizardOptionsPage.DS_KEY_EXPORTTYPE, ExportWizardOptionsPage.PROP);
      dialogSettings.put(ExportWizardOptionsPage.DS_KEY_MESSAGE_FORMAT, useMessageFormatFilter);
    } else if (xlsFileExport) {
      dialogSettings.put(ExportWizardOptionsPage.DS_KEY_EXPORTTYPE, ExportWizardOptionsPage.XLS);
    }
    return true;
  }
  
  /**
   * Worker method for the CSV file export.
   * @param masterLanguage the master language of the database being
   * exported
   * @param textNodes the text nodes to export
   * @param exportList the list of <code>LanguageAndFileName</code>
   * objects providing the languages to export and the corresponding
   * file names
   * @param status the status to export
   * @param monitor the progress monitor
   * @throws CoreException if the CSV file could not be created
   * @throws InterruptedException if the operation is canceled
   */
  private void doXLSExport(String masterLanguage, ITextNode[] textNodes, 
      List<LanguageAndFileName> exportList, Status[] status, IProgressMonitor monitor)
  throws CoreException, InterruptedException {
    monitor.beginTask("", exportList.size());
    
    for (LanguageAndFileName languageAndFileName : exportList) {
      if (monitor.isCanceled()) {
        throw new InterruptedException();
      }
      String language = languageAndFileName.getLanguage();
      monitor.setTaskName("Exporting " + language + "...");
      try {
        File output = new File(languageAndFileName.getFileName());
        IExporter xlsExporter = new XLSExporter(output);
        xlsExporter.export(textNodes, masterLanguage, language, status);
      } catch (ExportException e) {
        throw new CoreException(TremaUtilEclipse.createErrorStatus("XLS export failed:" + e.getMessage()));
      }
    }
  }

  /**
   * Worker method for the CSV file export.
   * @param masterLanguage the master language of the database being
   * exported
   * @param textNodes the text nodes to export
   * @param exportList the list of <code>LanguageAndFileName</code>
   * objects providing the languages to export and the corresponding
   * file names
   * @param status the status to export
   * @param encoding the encoding to be used
   * @param separator the separator to be used
   * @param monitor the progress monitor
   * @throws CoreException if the CSV file could not be created
   * @throws InterruptedException if the operation is canceled
   */
  private void doCSVExport(String masterLanguage, ITextNode[] textNodes, 
      List<LanguageAndFileName> exportList, Status[] status,
    String encoding, char separator, IProgressMonitor monitor)
  throws CoreException, InterruptedException {
    monitor.beginTask("", exportList.size());
    for (LanguageAndFileName languageAndFileName : exportList) {
      if (monitor.isCanceled()) {
        throw new InterruptedException();
      }
      String language = languageAndFileName.getLanguage();
      monitor.setTaskName("Exporting " + language + "...");
      Writer writer = null;
      try {
        OutputStream outputStream = new FileOutputStream(languageAndFileName.getFileName());
        writer = new OutputStreamWriter(outputStream, encoding);
        MonitoringTremaCSVPrinter printer 
        = new MonitoringTremaCSVPrinter(writer, separator, new SubProgressMonitor(monitor, 1));
        CSVExporter exporter = new CSVExporter(printer);
        exporter.export(textNodes, masterLanguage, language, status);
      } catch (UnsupportedEncodingException e) {
        throw new CoreException(TremaUtilEclipse.createErrorStatus(e.getMessage() + " is an unsupported encoding."));
      } catch (IOException e) {
        throw new CoreException(TremaUtilEclipse.createErrorStatus("Could not write output: " + e.getMessage()));
      } finally {
        if (writer != null) {
          try {
            writer.close();
          } catch (IOException e) {
            throw new CoreException(TremaUtilEclipse.createErrorStatus("Could not write output: " + e.getMessage()));
          }
        }
      }
    }
  }
  
  /**
   * Worker method for the Java properties file export.
   * @param textNodes the text nodes to export
   * @param exportList the list of <code>LanguageAndFileName</code>
   * objects providing the languages to export and the corresponding
   * file names
   * @param status the status to export
   * @param monitor the progress monitor
   * @throws CoreException if the properties file could not be created
   * @throws InterruptedException if the operation is canceled
   */
  private void doPropertiesExport(ITextNode[] textNodes, List<LanguageAndFileName> exportList, 
      Status[] status, IProgressMonitor monitor, boolean useMessageFormatFilter)
  throws CoreException, InterruptedException {
    monitor.beginTask("", exportList.size());
    
    for (LanguageAndFileName languageAndFileName : exportList) {
      if (monitor.isCanceled()) {
        throw new InterruptedException();
      }
      String language = languageAndFileName.getLanguage();
      monitor.setTaskName("Exporting " + language + "...");
      try {
        PropertiesExporter exporter = new PropertiesExporter(new File(
            languageAndFileName.getFileName()), new FileOutputStreamFactory());
        if (useMessageFormatFilter) {
          exporter.setExportFilter(new IExportFilter[]{new MessageFormatEscapingFilter()});
        }
        // note that masterlanguage is not needed for the properties export and can be passed null
        exporter.export(textNodes, null, language, status);
        monitor.worked(1);
      } catch (ExportException e) {
        throw new CoreException(TremaUtilEclipse.createErrorStatus("Could not write output: " + e.getMessage()));
      }
    }
  }

}
