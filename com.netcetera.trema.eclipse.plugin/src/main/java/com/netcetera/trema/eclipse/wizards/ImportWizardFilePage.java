package com.netcetera.trema.eclipse.wizards;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.netcetera.trema.core.ParseException;
import com.netcetera.trema.core.api.IDatabase;
import com.netcetera.trema.core.api.IImportSource;
import com.netcetera.trema.core.importing.CSVFile;
import com.netcetera.trema.core.importing.ChangesAnalyzer;
import com.netcetera.trema.core.importing.XLSFile;
import com.netcetera.trema.eclipse.TremaEclipseUtil;
import com.netcetera.trema.eclipse.TremaPlugin;
import com.netcetera.trema.eclipse.TremaUtilEclipse;
import com.netcetera.trema.eclipse.dialogs.TremaInputDialog;



/** Wizard page offering a CSV file selection. */
public class ImportWizardFilePage extends WizardPage {
  
  /** The dialog settings key for storing the specified file paths. */
  public static final String DS_KEY_FILE_PATHS = "csvImportWizardFilePage.filePaths";

  /** The dialog settings key for storing the specified encodig. */
  public static final String DS_KEY_ENCODING = "csvImportWizardFilePage.encoding";

  /** The dialog settings key for storing the specified separator. */
  public static final String DS_KEY_SEPARATOR = "csvImportWizardFilePage.separator";
  
  private Combo fileCombo = null;
  private Combo encodingCombo = null;
  private Text separatorText = null;
  
  private IImportSource importSource = null;
  private IDatabase db = null;
  private String initialFilePath = null;
  
  /** Modify listener common to some widgets. */
  private ModifyListener modifyListener = new ModifyListener() {
    public void modifyText(ModifyEvent e) {
      dialogChanged();
    }
  };
  
  /**
   * Creates a new instance.
   * @param db the database being imported to
   * @param initialFilePath the path to be initially displayed, may be
   * <code>null</code>
   */
  public ImportWizardFilePage(IDatabase db, String initialFilePath) {
    super("tremaCSVImportWizardFilePage");
    this.db = db;
    this.initialFilePath = initialFilePath;
    setTitle("Import to Trema database");
    setDescription("Select the CSV/XLS file to import");
  }

  /** {@inheritDoc} */
  public void createControl(Composite parent) {
    initializeDialogUnits(parent);
    
    Composite mainComposite = new Composite(parent, SWT.NULL);
    GridLayout gridLayout = new GridLayout();
    gridLayout.numColumns = 3;
    mainComposite.setLayout(gridLayout);    
    setControl(mainComposite);
    
    // file label, file combo and browse button
    Label fileLabel = new Label(mainComposite, SWT.NONE);
    fileLabel.setText("&Import file:");
    fileCombo = new Combo(mainComposite, SWT.DROP_DOWN);
    fileCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    fileCombo.addModifyListener(modifyListener);
    Button browseButton = new Button(mainComposite, SWT.PUSH);
    browseButton.setText("&Browse...");
    browseButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        browseButtonPressed();
      }
    });
    
    // encoding label and encoding combo
    Label encodingLabel = new Label(mainComposite, SWT.NONE);
    encodingLabel.setText("&Encoding:");
    encodingCombo = new Combo(mainComposite, SWT.DROP_DOWN);
    GridData gridData = new GridData();
    gridData.horizontalSpan = 2;
    gridData.widthHint = convertWidthInCharsToPixels(TremaInputDialog.DEFAULT_WIDTH_IN_CHARS);
    encodingCombo.setLayoutData(gridData);
    encodingCombo.addModifyListener(modifyListener);
    
    // separator label and separator text
    Label separatorLabel = new Label(mainComposite, SWT.NONE);
    separatorLabel.setText("Se&parator:");
    separatorText = new Text(mainComposite, SWT.BORDER);
    gridData = new GridData();
    gridData.widthHint = convertWidthInCharsToPixels(3);
    separatorText.setLayoutData(gridData);
    separatorText.addModifyListener(modifyListener);
    
    initContents();
    setErrorMessage(null); // suppress initial error message
  }
  
  /** Initializes the input fields. */
  private void initContents() {
    IDialogSettings dialogSettings = TremaPlugin.getDefault().getDialogSettings();

    if (initialFilePath != null) {
      fileCombo.setText(initialFilePath);
    }
    String[] storedFilePaths = dialogSettings.getArray(DS_KEY_FILE_PATHS);
    if (storedFilePaths != null) {
      fileCombo.setItems(storedFilePaths);
      fileCombo.setText(storedFilePaths[0]); 
    }
    
    // encoding and separator
    TremaUtilEclipse.populateWithEncodings(dialogSettings.get(DS_KEY_ENCODING), encodingCombo);
    String separator = dialogSettings.get(DS_KEY_SEPARATOR);
    separatorText.setText(separator == null ? ";" : separator);
    
    fileCombo.setFocus();
  }
  
  /** 
   * Validates the input fields and updates the page status
   * accordingly.
   */
  private void dialogChanged() {
    String filePath = getFilePath();
    
    // validate file path
    if (filePath.length() == 0) {
      updateStatus("Please specify a file.");
      return;
    }    
    File file = new File(filePath);
    if (!file.exists()) {
      updateStatus("The specified file does not exist.");
      return;
    }    
    if (file.isDirectory()) {
      updateStatus("The specified file is a directory.");
      return;
    }
    
    if (isCSV()) {
      // handle enablement
      encodingCombo.setEnabled(true);
      separatorText.setEnabled(true);   
      // validate encoding
      String encoding = getEncoding();
      String errorMessage = TremaUtilEclipse.validateEncoding(encoding);
      if (errorMessage != null) {
        updateStatus(errorMessage);
        return;
      }
      
      // validate separator
      String separator = separatorText.getText();
      if (separator.trim().length() == 0) {
        updateStatus("Please enter a non-whitespace separator character.");
        return;
      }
      if (separator.length() != 1) {
        updateStatus("The separator needs to be a single character");
        return;
      }
    } else {
      // only csv files allow choosing of separator and encoding
      encodingCombo.setEnabled(false);
      separatorText.setEnabled(false);  
    }
    
    updateStatus(null);
  }
  
  /**
   * Sets the error message and the "page complete" status.
   * @param message the error message. <code>null</code> means no error
   * message and a complete page.
   */
  private void updateStatus(String message) {
    // avoid flashing
    if (!TremaEclipseUtil.equalsOrNull(getErrorMessage(), message)) {
      setErrorMessage(message);
    }
    setPageComplete(message == null);
  }  
  
  /**
   * Displays a file selection dialog.
   */
  private void browseButtonPressed() {
    FileDialog dialog = new FileDialog(getShell(), SWT.OPEN);
    
    String filePath = fileCombo.getText();
    File file = new File(filePath);
    if (file.isFile()) {
      filePath = file.getParentFile().getAbsolutePath();
    }    
    dialog.setFilterPath(filePath);
    dialog.setFilterExtensions(new String[] {"*.xls", "*.csv", "*.*"});
    
    String selectedFile = dialog.open();
    if (selectedFile != null) {
      fileCombo.setText(selectedFile);
    }
  }
  
  private boolean isXLS() {
    String path = getFilePath();
    if (path != null && path.toLowerCase().endsWith(".xls")) {
      return true;
    }
    return false;
  }
  
  private boolean isCSV() {
    String path = getFilePath();
    if (path != null && path.toLowerCase().endsWith(".csv")) {
      return true;
    }
    return false;
  }
  
  private IImportSource initializeImportSource() throws IOException, ParseException {
    IImportSource s = null;
    if (isXLS()) {
      s = new XLSFile(getFilePath());
    } else {
      s = new CSVFile(getFilePath(), getEncoding(), getSeparator());
    } 
    return s;
  }
  
  
  
  /** {@inheritDoc} */
  @Override
  public IWizardPage getNextPage() {
    // this method is overridden for workaround to emulate a page
    // change event: returning null means "stay on this page";
    // see WizardDialog.nextPressed() and eclipse bug #16803
    try {
      importSource = initializeImportSource();
      
      // catch master languages errors here
      new ChangesAnalyzer(importSource, db); 
      
      return super.getNextPage();
    } catch (ParseException e) {
      StringBuffer message = new StringBuffer("The specified import file has parse errors:\n\n");
      if (e.getLineNumber() >= 1) {
        message.append("line ").append(e.getLineNumber()).append(": ");
      }
      message.append(e.getMessage());
      MessageDialog.openError(getShell(), "Trema Import", message.toString());
      return null;
    } catch (UnsupportedEncodingException e) {
      String message = "The specified encoding is not supported.";
      MessageDialog.openError(getShell(), "Trema Import", message);
      return null;
    } catch (IOException e) {
      String message = "There has been an IO error:\n" + e.getMessage();
      MessageDialog.openError(getShell(), "Trema Import", message);
      return null;
    } catch (IllegalArgumentException e) {
      MessageDialog.openError(getShell(), "Trema Import", e.getMessage());
      return null;
    }
  }
  
  /** {@inheritDoc} */
  @Override
  public boolean canFlipToNextPage() {
    // override this method as a workaround emulating a page change
    // event: getNextPage() should not be called from this method
    return isPageComplete();
  }

  /**
   * Gets the file path typed into this wizard page.
   * @return the file path typed into this wizard page.
   */
  public String getFilePath() {
    return fileCombo.getText();
  }
  
  /**
   * Gets the encoding typed into this wizard page.
   * @return the encoding typed into this wizard page.
   */
  public String getEncoding() {
    return encodingCombo.getText();
  }
  
  /**
   * Gets the separator typed into this wizard page.
   * @return the separator typed into this wizard page.
   */
  public char getSeparator() {
    String separator = separatorText.getText();
    if (separator.length() != 1) {
      return 0;
    }
    return separator.charAt(0);
  }
  
  /**
   * Gets the parsed and internalized import source.
   * @return the parsed and internalized import source.
   */
  public IImportSource getImportSource() {
    return importSource;
  }
  
}
