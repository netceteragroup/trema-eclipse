package com.netcetera.trema.eclipse.wizards;

import java.util.ArrayList;
import java.util.Set;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;

import com.netcetera.trema.common.TremaCoreUtil;
import com.netcetera.trema.core.Status;
import com.netcetera.trema.core.api.IDatabase;
import com.netcetera.trema.core.api.ITextNode;
import com.netcetera.trema.eclipse.TremaEclipseUtil;
import com.netcetera.trema.eclipse.TremaPlugin;
import com.netcetera.trema.eclipse.TremaUtilEclipse;
import com.netcetera.trema.eclipse.dialogs.TremaInputDialog;



/**
 * The options page for the Trema export wizard.
 */
public class ExportWizardOptionsPage extends WizardPage {
  
  /** The dialog settings key for storing whether the export type. */
  static final String DS_KEY_EXPORTTYPE = "csvPropExportWizardOptionsPage.exporttype";
  /** The dialog settings key for storing whether the export type was XLS. */
  static final String XLS = "xls";
  /** The dialog settings key for storing whether the export type was CSV. */
  static final String CSV = "csv";
  /** The dialog settings key for storing whether the export type was Java properties. */
  static final String PROP = "prop";
  
  /** The dialog settings key for storing the exported languages. */
  public static final String DS_KEY_LANGUAGES = "csvPropExportWizardOptionsPage.languages";

  /** The dialog settings key for storing the specified folder paths. */
  public static final String DS_KEY_FOLDER_PATHS = "csvPropExportWizardOptionsPage.folderPaths";
  
  /** The dialog settings key for storing the exported status. */
  public static final String DS_KEY_STATUS = "csvPropExportWizardOptionsPage.status";
  
  /** The dialog settings key for storing the specified CSV encoding. */
  public static final String DS_KEY_ENCODING = "csvPropExportWizardOptionsPage.encoding";
  
  /** The dialog settings key for storing the specified CSV separator. */
  public static final String DS_KEY_SEPARATOR = "csvPropExportWizardOptionsPage.separator";

  /** The dialog settings key for storing the messageFormat checkbox value. */
  public static final String DS_KEY_MESSAGE_FORMAT = "csvPropExportWizardOptionsPage.messageFormatFilter";

  private IDatabase db = null;
  private ITextNode[] selectedTextNodes = null;
  private String initialFolderPath = null;
  private String initialBaseName = null;
  
  private Button csvButton = null;
  private Button xlsButton = null;
  private Button propertiesButton = null;
  private Button wholeDbButton = null;
  private Button currentSelectionButton = null;
  private CheckboxTableViewer languageViewer = null;
  private Button[] statusButtons = null;
  private Group destinationGroup = null;
  private Combo folderPathCombo = null;
  private Text baseNameText = null;
  private Label baseNameHelpLabel = null;
  private Group csvOptionsGroup = null;
  private Label encodingLabel = null;
  private Combo encodingCombo = null;
  private Label separatorLabel = null;
  private Text separatorText = null;

  private Group propertiesOptionsGroup = null;
  private Button messageFormatButton = null;
  
  /** Selection listener common to some widgets. */
  private SelectionListener selectionListener = new SelectionAdapter() {
    public void widgetSelected(SelectionEvent e) {
      dialogChanged();
    }
  };
  
  /** Selection listener for the export type buttons. */
  private SelectionListener typeSelectionListener = new SelectionAdapter() {
    public void widgetSelected(SelectionEvent e) {
      typeChanged();
    }
  };
  
  /** Modify listener common to some widgets. */
  private ModifyListener modifyListener = new ModifyListener() {
    public void modifyText(ModifyEvent e) {
      dialogChanged();
    }
  };
  
  /**
   * 
   * Constructor.
   * 
   * @param db the database to export
   * @param selectedTextNodes the currently selected text nodes, may be
   * <code>null</code>
   * @param initialFolderPath the folder path that should initially be displayed,
   * may be <code>null</code>
   * @param initialBaseName the base name that should initially be displayed,
   * may be <code>null</code>
   */
  public ExportWizardOptionsPage(IDatabase db, ITextNode[] selectedTextNodes, String initialFolderPath,
                                              String initialBaseName) {
    super("tremaCSVPropertiesExportWizardOptionPage");
    this.db = db;
    this.selectedTextNodes = selectedTextNodes;
    this.initialFolderPath = initialFolderPath;
    this.initialBaseName = initialBaseName;
    setTitle("Export Trema database");
    setDescription("Choose the export options");
  }

  /** {@inheritDoc} */
  public void createControl(Composite parent) {
    initializeDialogUnits(parent);
    
    Composite mainComposite = new Composite(parent, SWT.NONE);
    GridLayout gridLayout = new GridLayout();
    gridLayout.makeColumnsEqualWidth = true;
    gridLayout.numColumns = 2;
    mainComposite.setLayout(gridLayout);    
    setControl(mainComposite);

    createFileTypeAndExportGroup(mainComposite);
    createLanguageAndStatusGroup(mainComposite);
    createDestinationGroup(mainComposite);
    createCSVOptionsGroup(mainComposite);
    createPropertiesOptionsGroup(mainComposite);

    initContents();
  }

  /**
   * Creates the file type and the export (whole database or current
   * selection) option groups.
   * @param parent the parent composite
   */
  private void createFileTypeAndExportGroup(Composite parent) {
    // file type group
    Group fileTypeGroup = new Group(parent, SWT.NONE);
    fileTypeGroup.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
    fileTypeGroup.setText("Export &File Type");
    fileTypeGroup.setLayout(new GridLayout());

    xlsButton = new Button(fileTypeGroup, SWT.RADIO);
    xlsButton.setText("Trema &XLS file");
    xlsButton.addSelectionListener(typeSelectionListener);
    csvButton = new Button(fileTypeGroup, SWT.RADIO);
    csvButton.setText("Trema &CSV file");
    csvButton.addSelectionListener(typeSelectionListener);
    propertiesButton = new Button(fileTypeGroup, SWT.RADIO);
    propertiesButton.setText("Java &properties file");
    propertiesButton.addSelectionListener(typeSelectionListener);
    
    // export group
    Group exportGroup = new Group(parent, SWT.NONE);
    exportGroup.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
    exportGroup.setText("&Export");
    exportGroup.setLayout(new GridLayout());

    wholeDbButton = new Button(exportGroup, SWT.RADIO);
    wholeDbButton.addSelectionListener(selectionListener);
    currentSelectionButton = new Button(exportGroup, SWT.RADIO);
    wholeDbButton.addSelectionListener(selectionListener);
  }

  /**
   * Creates the language and status option groups.
   * @param parent the parent composite
   */
  private void createLanguageAndStatusGroup(Composite parent) {
    // language group
    Group languageGroup = new Group(parent, SWT.NO_RADIO_GROUP);
    languageGroup.setLayout(new GridLayout());
    languageGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
    languageGroup.setText("&Languages To Export");

    languageViewer = CheckboxTableViewer.newCheckList(languageGroup, SWT.BORDER | SWT.V_SCROLL);
    Table languageTable = languageViewer.getTable();
    languageTable.setLayoutData(new GridData(GridData.FILL_BOTH));
    languageViewer.setContentProvider(new ArrayContentProvider());
    languageViewer.addCheckStateListener(new ICheckStateListener() {
      public void checkStateChanged(CheckStateChangedEvent event) {
        dialogChanged();
      }
    });
    languageViewer.addDoubleClickListener(new IDoubleClickListener() {
      public void doubleClick(DoubleClickEvent event) {
        if (event.getSource() == languageViewer) {
          IStructuredSelection selection = (IStructuredSelection) languageViewer.getSelection();
          Object firstElement = selection.getFirstElement();
          if (firstElement != null) {
            languageViewer.setChecked(firstElement, !languageViewer.getChecked(firstElement));
            dialogChanged(); // no CheckStateChangedEvent is triggered by 'setChecked'
          }
        }
      }
    });
    
    // status groups
    Group statusGroup = new Group(parent, SWT.NONE);
    statusGroup.setLayout(new GridLayout());
    statusGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
    statusGroup.setText("S&tatus To Export");

    String[] statusNames = Status.getAvailableStatusNames();
    statusButtons = new Button[statusNames.length];
    for (int i = 0; i < statusButtons.length; i++) {
      statusButtons[i] = new Button(statusGroup, SWT.CHECK);
      statusButtons[i].setText(statusNames[i]);
      statusButtons[i].addSelectionListener(selectionListener);
    }
  }
  
  /**
   * Creates the destination option group.
   * @param parent the parent composite
   */
  private void createDestinationGroup(Composite parent) {
    // destination group
    destinationGroup = new Group(parent, SWT.NO_RADIO_GROUP);
    GridLayout destinationLayout = new GridLayout();
    destinationLayout.numColumns = 4;
    destinationGroup.setLayout(destinationLayout);
    GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
    gridData.horizontalSpan = 2;
    destinationGroup.setLayoutData(gridData);
    destinationGroup.setText("Export &Destination");

    Label folderLabel = new Label(destinationGroup, SWT.NONE);
    folderLabel.setLayoutData(new GridData());
    folderLabel.setText("F&older:");
    folderPathCombo = new Combo(destinationGroup, SWT.DROP_DOWN);
    gridData = new GridData(GridData.FILL_HORIZONTAL);
    gridData.horizontalSpan = 2;
    folderPathCombo.setLayoutData(gridData);
    folderPathCombo.addModifyListener(modifyListener);
    Button browseButton = new Button(destinationGroup, SWT.NONE);
    browseButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
    browseButton.setText("&Browse...");
    browseButton.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        browseButtonPressed();
      }
    });
    Label baseNameLabel = new Label(destinationGroup, SWT.NONE);
    baseNameLabel.setText("Base &Name:");
    baseNameText = new Text(destinationGroup, SWT.BORDER);
    gridData = new GridData();
    gridData.widthHint = convertWidthInCharsToPixels(TremaInputDialog.DEFAULT_WIDTH_IN_CHARS * 2);
    baseNameText.setLayoutData(gridData);
    baseNameText.addModifyListener(modifyListener);
    baseNameHelpLabel = new Label(destinationGroup, SWT.NONE);
    gridData = new GridData(GridData.FILL_HORIZONTAL);
    gridData.widthHint = convertWidthInCharsToPixels(50);
    baseNameHelpLabel.setLayoutData(gridData);
  }
  
  /**
   * Creates the CSV options group.
   * @param parent the parent composite
   */
  private void createCSVOptionsGroup(Composite parent) {
    // CSV options group
    csvOptionsGroup = new Group(parent, SWT.NO_RADIO_GROUP);
    GridLayout csvOptionsLayout = new GridLayout();
    csvOptionsLayout.numColumns = 2;
    csvOptionsGroup.setLayout(csvOptionsLayout);
    csvOptionsGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    csvOptionsGroup.setText("CSV O&ptions");

    encodingLabel = new Label(csvOptionsGroup, SWT.NONE);
    encodingLabel.setText("&Encoding:");
    encodingCombo = new Combo(csvOptionsGroup, SWT.DROP_DOWN);
    GridData gridData = new GridData();
    gridData.widthHint = convertWidthInCharsToPixels(TremaInputDialog.DEFAULT_WIDTH_IN_CHARS);
    encodingCombo.setLayoutData(gridData);
    encodingCombo.addModifyListener(modifyListener);
    separatorLabel = new Label(csvOptionsGroup, SWT.NONE);
    separatorLabel.setText("Sepa&rator:");
    separatorText = new Text(csvOptionsGroup, SWT.BORDER);
    gridData = new GridData();
    gridData.widthHint = convertWidthInCharsToPixels(3);
    separatorText.setLayoutData(gridData);
    separatorText.addModifyListener(modifyListener);
  }

  /**
   * Creates the Properties Export options group.
   * @param parent the parent composite
   */
  private void createPropertiesOptionsGroup(Composite parent) {
    // CSV options group
    propertiesOptionsGroup = new Group(parent, SWT.NONE);
    propertiesOptionsGroup.setLayout(new GridLayout());
    propertiesOptionsGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
    propertiesOptionsGroup.setText("Properties O&ptions");
    
    messageFormatButton = new Button(propertiesOptionsGroup, SWT.CHECK);
    messageFormatButton.setText("Escape single-quotes for MessageFormat usage");
    messageFormatButton.addSelectionListener(selectionListener);
  }

  /** Initializes the input fields. */
  private void initContents() {
    IDialogSettings dialogSettings = TremaPlugin.getDefault().getDialogSettings();
    
    // export type
    String exportType = dialogSettings.get(DS_KEY_EXPORTTYPE);
    if (CSV.equals(exportType)) {
      csvButton.setSelection(true);
    } else if (PROP.equals(exportType)) {
      propertiesButton.setSelection(true);
    } else {
      xlsButton.setSelection(true);
    }
    
    // language list
    Set<String> languageSet = TremaCoreUtil.getLanguages(db.getTextNodes());
    String[] languages = languageSet.toArray(new String[languageSet.size()]);
    languageViewer.setInput(languages);
    
    String[] storedLanguages = dialogSettings.getArray(DS_KEY_LANGUAGES);
    if (storedLanguages != null) {
      languageViewer.setCheckedElements(storedLanguages);
    } else if (isPropertiesExportType()) {
      // preselect all languages for a properties file export
      languageViewer.setCheckedElements(languages);
    }
    
    // whole database or current selection
    wholeDbButton.setText("&Whole database (" + db.getSize() + " record(s))");
    wholeDbButton.setSelection(true);
    if (selectedTextNodes == null || selectedTextNodes.length == 0) {
      currentSelectionButton.setText("Current &selection");
      currentSelectionButton.setEnabled(false);
    } else {
      currentSelectionButton.setText("Current &selection (" + selectedTextNodes.length + " record(s))");
      currentSelectionButton.setEnabled(true);
    }
    
    // folder path
    String[] storedFolderPaths = dialogSettings.getArray(DS_KEY_FOLDER_PATHS);
    if (storedFolderPaths != null) {
      folderPathCombo.setItems(storedFolderPaths);
    }
    if (initialFolderPath != null) {
      folderPathCombo.setText(initialFolderPath);
    }
    
    // base name
    if (initialBaseName != null) {
      baseNameText.setText(initialBaseName);
    }
    
    // CSV encoding and CSV separator
    TremaUtilEclipse.populateWithEncodings(dialogSettings.get(DS_KEY_ENCODING), encodingCombo);
    String separator = dialogSettings.get(DS_KEY_SEPARATOR);
    separatorText.setText(separator == null ? ";" : separator);
    
    // status
    String[] statusNames = dialogSettings.getArray(DS_KEY_STATUS);
    if (statusNames != null) {
      for (int i = 0; i < statusNames.length; i++) {
        statusButtons[Status.valueOf(statusNames[i]).getPosition()].setSelection(true);
      }
    } else {
      for (int i = 0; i < statusButtons.length; i++) {
        statusButtons[i].setSelection(true);
      }
    }
    
    Boolean messageFormatChecked = dialogSettings.getBoolean(DS_KEY_MESSAGE_FORMAT);
    messageFormatButton.setSelection(messageFormatChecked);
    
    typeChanged();
    csvButton.setFocus();
  }
  
  /**
   * Sets the enablement of the CSV options group.
   * @param enabled true if the CSV options group should be enabled
   */
  private void setCSVOptionsEnabled(boolean enabled) {
    csvOptionsGroup.setEnabled(enabled);
    encodingLabel.setEnabled(enabled);
    encodingCombo.setEnabled(enabled);
    separatorLabel.setEnabled(enabled);
    separatorText.setEnabled(enabled);
  }
  
  /**
   * @param enabled enables the group and its content or not
   */
  private void setPropertiesOptionsEnabled(boolean enabled) {
    propertiesOptionsGroup.setEnabled(enabled);
    messageFormatButton.setEnabled(enabled);
  }
  
  /**
   * This method gets called whenever the export type changes. It
   * updates some labels and then calls <code>dialogChanged()</code>.
   */
  private void typeChanged() {
    setCSVOptionsEnabled(isCSVExportType());
    setPropertiesOptionsEnabled(isPropertiesExportType());
    if (isCSVExportType()) {
      baseNameHelpLabel.setText("(\"_<language>.csv\" will be appended)");
      // try to convert the workspace relative path to an absolute file system path
      // IPath path = workspace.getRoot().getLocation().append(folderPathText.getText());
      // folderPathText.setText(path.toFile().getPath());
    } else if (isXLSExportType()) {
      baseNameHelpLabel.setText("(\"_<language>.xls\" will be appended)");
    } else if (isPropertiesExportType()) {
      baseNameHelpLabel.setText("(\"_<language>.properties\" will be appended)");
    }
    dialogChanged();
  }
  
  /** 
   * Validates the input fields and updates the page status
   * accordingly.
   */
  private void dialogChanged() {
    // check for presence of some required inputs
    if (languageViewer.getCheckedElements().length == 0) {
      updateStatus("Please select at least one language to export");
      return;
    }
    if (getStatus().length == 0) {
      updateStatus("Please select at least one status to export");
      return;
    }
    if (getFolderPath().isEmpty()) {
      updateStatus("Please specify a destination folder.");
      return;
    }
    if (getBaseName().length() == 0) {
      updateStatus("Please specify a base name.");
      return;
    }
    
    // validate the path and file name
    IPath fullPath = getFolderPath().append(getBaseName()).makeAbsolute();
    if (!fullPath.isValidPath(fullPath.toOSString())) {
      updateStatus("The specified path is invalid.");
      return;
    }
    String errorMessage = TremaUtilEclipse.validateFileName(getBaseName(), null);
    if (errorMessage != null) {
      updateStatus(errorMessage);
      return;
    }
    
    // validate the CSV options
    if (isCSVExportType()) {
      String encoding = getEncoding();
      if (encoding.length() == 0) {
        updateStatus("Please enter an encoding.");
        return;
      }
      errorMessage = TremaUtilEclipse.validateEncoding(encoding);
      if (errorMessage != null) {
        updateStatus(errorMessage);
        return;
      }
      String separator = separatorText.getText();
      if (separator.trim().length() == 0) {
        updateStatus("Please enter a non-whitespace separator character.");
        return;
      }
      if (separator.length() != 1) {
        updateStatus("The separator needs to be a single character");
        return;
      }
    }
    
    updateStatus(null);
  }
  
  /**
   * Displays a folder selection dialog.
   */
  private void browseButtonPressed() {
    DirectoryDialog dialog = new DirectoryDialog(getShell(), SWT.NONE);
    dialog.setText("Trema CSV/XLS File Export");
    dialog.setMessage("Select the destination folder:");
    dialog.setFilterPath(getFolderPath().toOSString());
    
    String selectedFolder = dialog.open();
    if (selectedFolder != null) {
      folderPathCombo.setText(selectedFolder);
    }
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
   * Returns a flag indication if the export type is set to Trema CSV file.
   * 
   * @return true if the export type is set to Trema CSV file.
   */
  public boolean isCSVExportType() {
    return csvButton.getSelection();
  }

  /**
   * Returns a flag indication if the export type is set to Trema XLS file.
   * 
   * @return true if the export type is set to Trema XLS file.
   */
  public boolean isXLSExportType() {
    return xlsButton.getSelection();
  }
  
  /**
   * Returns a flag indication if the export type is set to Java properties file.
   * 
   * @return true if the export type is set to Java properties file.
   */
  public boolean isPropertiesExportType() {
    return propertiesButton.getSelection();
  }
  
  /**
   * Gets the text nodes to be exported.
   * @return the text nodes to be exported.
   */
  public ITextNode[] getTextNodes() {
    if (currentSelectionButton.getSelection()) {
      return selectedTextNodes;
    }
    return db.getTextNodes();
  }
  
  /**
   * Gets tha languages to be exported.
   * @return the languages to be exported.
   */
  public String[] getLanguages() {
    return TremaEclipseUtil.toStringArray(languageViewer.getCheckedElements());
  }
  
  /**
   * Gets the status to be exported.
   * @return the status to be exported.
   */
  public Status[] getStatus() {
    java.util.List<Status> statusList = new ArrayList<Status>();
    for (int i = 0; i < statusButtons.length; i++) {
      if (statusButtons[i].getSelection()) {
        statusList.add(Status.valueOf(statusButtons[i].getText()));
      }
    }
    return statusList.toArray(new Status[statusList.size()]);
  }
  
  /**
   * Gets the folder path typed into this wizard page.
   * @return the folder path typed into this wizard page.
   */
  public IPath getFolderPath() {
    return new Path(folderPathCombo.getText());
  }
  
  /**
   * Gets the base name typed into this wizard page.
   * @return the base name typed into this wizard page.
   */
  public String getBaseName() {
    return baseNameText.getText();
  }

  /**
   * Gets the CSV encoding typed into this wizard page.
   * @return the CSV encoding typed into this wizard page.
   */
  public String getEncoding() {
    return encodingCombo.getText();
  }
  
  /**
   * Gets the CSV separator typed into this wizard page.
   * @return the CSV separator typed into this wizard page.
   */
  public char getSeparator() {
    String separator = separatorText.getText();
    if (separator.length() != 1) {
      return 0;
    }
    return separator.charAt(0);
  }
  
  /**
   * Return true if the MessageFormat checkbox is selected.
   * @return true if the MessageFormat checkbox is selected.
   */
  public boolean isMessageFormatButtonChecked() {
    return messageFormatButton.getSelection();
  }
 
}
