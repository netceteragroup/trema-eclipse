package com.netcetera.trema.eclipse.wizards;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;

import com.netcetera.trema.eclipse.TremaEclipseUtil;
import com.netcetera.trema.eclipse.TremaPlugin;
import com.netcetera.trema.eclipse.TremaUtilEclipse;
import com.netcetera.trema.eclipse.dialogs.TremaInputDialog;



/** Main page for the Trema "new" wizard. */
public class NewWizardOptionsPage extends WizardPage {
  
  /** The dialog settings key for storing the specified encoding. */
  public static final String DS_KEY_ENCODING = "newWizardOptionsPage.encoding";
  
  /** The dialog settings key for storing the specified XML schema location. */
  public static final String DS_KEY_SCHEMA = "newWizardOptionsPage.schemaLocation";
  
  private Text folderText = null;
  private Text fileText = null;
  private Text masterLanguageText = null;
  private Combo encodingCombo = null;
  private Text schemaLocationText = null;
  private IStructuredSelection selection = null;
  private IDialogSettings dialogSettings = null;
  
  /**
   * Creates a new wizard option page instance. The selection is used
   * for setting the initial contents.
   * @param selection the selection
   */
  public NewWizardOptionsPage(IStructuredSelection selection) {
    super("tremaNewWizardOptionsPage");
    setTitle("New Trema XML database file");
    setDescription("Create a new Trema XML database file");
    this.selection = selection;
    dialogSettings = TremaPlugin.getDefault().getDialogSettings();
  }
  
  /** {@inheritDoc} */
  public void createControl(Composite parent) {
    initializeDialogUnits(parent);
    
    Composite mainComposite = new Composite(parent, SWT.NONE);
    GridLayout layout = new GridLayout();
    mainComposite.setLayout(layout);
    layout.numColumns = 3;
    layout.verticalSpacing = 9;    
    setControl(mainComposite);
    
    // modify listener common to some widgets
    ModifyListener modifyListener = new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        dialogChanged();
      }
    };
    
    // folder label
    Label folderLabel = new Label(mainComposite, SWT.NONE);
    folderLabel.setText("Fol&der:");
    
    // folder text
    folderText = new Text(mainComposite, SWT.BORDER | SWT.SINGLE);
    folderText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    folderText.addModifyListener(modifyListener);
    
    // browse button
    Button browseButton = new Button(mainComposite, SWT.PUSH);
    browseButton.setText("B&rowse...");
    browseButton.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        browseButtonPressed();
      }
    });
    
    // file label
    Label fileLabel = new Label(mainComposite, SWT.NONE);
    fileLabel.setText("&File name:");
    
    // file text
    fileText = new Text(mainComposite, SWT.BORDER | SWT.SINGLE);
    fileText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    fileText.addModifyListener(modifyListener);
    
    // spacer label
    new Label(mainComposite, SWT.NONE);
    
    // master language label
    Label masterLanguageLabel = new Label(mainComposite, SWT.NONE);
    masterLanguageLabel.setText("&Master language:");
    
    // master language text
    masterLanguageText = new Text(mainComposite, SWT.BORDER | SWT.SINGLE);
    GridData gridData = new GridData();
    gridData.horizontalSpan = 2;
    gridData.widthHint = convertWidthInCharsToPixels(TremaInputDialog.DEFAULT_WIDTH_IN_CHARS);
    masterLanguageText.setLayoutData(gridData);
    masterLanguageText.addModifyListener(modifyListener);
    
    // encoding label
    Label encodingLabel = new Label(mainComposite, SWT.NONE);
    encodingLabel.setText("File &Encoding:");
    
    // encoding combo
    encodingCombo = new Combo(mainComposite, SWT.DROP_DOWN);
    gridData = new GridData();
    gridData.horizontalSpan = 2;
    gridData.widthHint = convertWidthInCharsToPixels(TremaInputDialog.DEFAULT_WIDTH_IN_CHARS);
    encodingCombo.setLayoutData(gridData);
    encodingCombo.addModifyListener(modifyListener);
    
    // options group
    Group optionsGroup = new Group(mainComposite, SWT.NONE);
    GridLayout gridLayout = new GridLayout();
    gridLayout.numColumns = 2;
    optionsGroup.setLayout(gridLayout);
    gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
    gridData.horizontalSpan = 3;
    optionsGroup.setLayoutData(gridData);
    optionsGroup.setText("&Options");

    Label schemaLocationLabel = new Label(optionsGroup, SWT.NONE);
    schemaLocationLabel.setText("Trema XML &schema location:");

    schemaLocationText = new Text(optionsGroup, SWT.BORDER);
    schemaLocationText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    schemaLocationText.addModifyListener(modifyListener);

    initContents();
    if (getFolderPath().isEmpty()) {
      // supress initial error message
      setErrorMessage(null);
    }
  }
  
  /** Initializes the input fields. */
  private void initContents() {
    // test if the current workbench selection is a suitable folder to use
    if (selection != null && !selection.isEmpty()) {
      if (selection.size() == 1) {
        Object firstElement = selection.getFirstElement();
        if (firstElement instanceof IResource) {
          IContainer container;
          if (firstElement instanceof IContainer) {
            container = (IContainer) firstElement;
          } else {
            container = ((IResource) firstElement).getParent();
          }
          // make the path relative (in analogy to the new file wizard)
          folderText.setText(container.getFullPath().makeRelative().toOSString());
        }
      }
    }
    
    String initialEncoding = dialogSettings.get(DS_KEY_ENCODING);
    TremaUtilEclipse.populateWithEncodings(initialEncoding, encodingCombo);
    
    String storedSchemaLocation = dialogSettings.get(DS_KEY_SCHEMA);
    if (storedSchemaLocation != null) {
      schemaLocationText.setText(storedSchemaLocation);
    }
    
    fileText.setText("texts.trm");
    masterLanguageText.setText("de");
    
    folderText.setFocus();
  }
  
  /** Displays a folder selection dialog. */
  private void browseButtonPressed() {
    // use the standard container selection dialog
    ContainerSelectionDialog dialog = new ContainerSelectionDialog(
      getShell(), getFolder(), true,
      "Enter or select the parent folder of the new file:"
    );
    dialog.showClosedProjects(false);
    if (dialog.open() == Window.OK) {
      Object[] result = dialog.getResult();
      if (result.length == 1) {
        // make the path relative (in analogy to the new file wizard)
        folderText.setText(((IPath) result[0]).makeRelative().toOSString());
      }
    }
  }
  
  /** 
   * Validates the input fields and updates the page status
   * accordingly.
   */
  private void dialogChanged() {
    IPath folderPath = getFolderPath();
    String fileName = getFileName();
    String masterLanguage = getMasterLanguage();
    String encoding = getEncoding();
    
    // check folder path for presence
    if (folderPath == null || folderPath.isEmpty()) {
      updateStatus("Please specify a parent folder.");
      return;
    }
    
    // validate file name
    String errorMessage = TremaUtilEclipse.validateFileName(fileName, new String[] {"trm", "xml"});
    if (errorMessage != null) {
      updateStatus(errorMessage);
      return;
    }
    
    // validate full path
    // make the path absolute to allow the user to enter relative paths (in analogy to the new file wizard)
    IPath fullPath = folderPath.append(fileName).makeAbsolute();
    errorMessage = TremaUtilEclipse.validateWorkspacePath(fullPath, true, true);
    if (errorMessage != null) {
      updateStatus(errorMessage);
      return;
    }
    
    // validate master language
    errorMessage = TremaUtilEclipse.validateLanguage(masterLanguage, null);
    if (errorMessage != null) {
      updateStatus(errorMessage);
      return;
    }
    
    // validate encoding
    errorMessage = TremaUtilEclipse.validateEncoding(encoding);
    if (errorMessage != null) {
      updateStatus(errorMessage);
      return;
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
   * Gets the folder path typed into this wizard page.
   * @return the folder path typed into this wizard page.
   */
  public IPath getFolderPath() {
    return new Path(folderText.getText());
  }
  
  /**
   * Tries to find the folder typed into this wizard page.
   * @return the folder typed into this wizard page or
   * <code>null</code> if it could not be found
   */
  private IContainer getFolder() {
    IResource resource = ResourcesPlugin.getWorkspace().getRoot().findMember(getFolderPath());
    if (resource instanceof IContainer) {
      return (IContainer) resource;
    }
    return null;
  }
  
  /**
   * Gets the file name typed into this wizard page.
   * @return the file name typed into this wizard page.
   */
  public String getFileName() {
    return fileText.getText();
  }
  
  /**
   * Gets the master language typed into this wizard page.
   * @return the master language typed into this wizard page.
   */
  public String getMasterLanguage() {
    return masterLanguageText.getText();
  }
  
  /**
   * Gets the encoding typed into this wizard page.
   * @return the encoding typed into this wizard page.
   */
  public String getEncoding() {
    return encodingCombo.getText();
  }
  
  /**
   * Gets the XML schema location typed into this wizard page.
   * @return the XML schema location typed into this wizard page
   */
  public String getSchemaLocation() {
    return schemaLocationText.getText().trim();
  }
  
}
