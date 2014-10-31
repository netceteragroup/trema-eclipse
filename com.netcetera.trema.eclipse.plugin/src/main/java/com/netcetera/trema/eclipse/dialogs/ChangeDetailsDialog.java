package com.netcetera.trema.eclipse.dialogs;

import java.util.List;

import org.eclipse.compare.internal.ResizableDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.netcetera.trema.common.TremaUtil;
import com.netcetera.trema.core.Status;
import com.netcetera.trema.core.importing.Change;
import com.netcetera.trema.eclipse.TremaPlugin;
import com.netcetera.trema.eclipse.TremaUtilEclipse;
import com.netcetera.trema.eclipse.wizards.ImportWizardChangesPage;



/**
 * A dialog for displaying the details of a change.
 */
// TODO tzueblin Dec 1, 2008: check if there is a better alternative to use
@SuppressWarnings("restriction")
public class ChangeDetailsDialog extends ResizableDialog {
  
  private String title = null;
  private CheckboxTableViewer viewer = null;
  private List<Change> changeList = null;
  private Change currentChange = null;
  
  private Button acceptButton = null;
  private Button previousButton = null;
  private Button nextButton = null;
  private Text changeText = null;
  private Text keyText = null;
  private Text contextText = null;
  private Text importedStatusText = null;
  private Text dbStatusText = null;
  private Text importedValueText = null;
  private Text dbValueText = null;
  private Text importedMasterValueText = null;
  private Text dbMasterValueText = null;
  
  private Button compareValuesButton = null;
  private Button compareMasterValuesButton = null;
  private Button acceptImportedValueButton = null;
  private Button acceptDbValueButton = null;
  private Button acceptImportedStatusButton = null;
  private Button acceptDbStatusButton = null;
  private Button acceptImportedMasterValueButton = null;
  private Button acceptDbMasterValueButton = null;
  
  private boolean showMasterValues = false;

  /**
   * Creates a new dialog instance.
   * @param parentShell the parent shell
   * @param title the title of this dialog
   * @param viewer the parent change viewer
   * @param changeList the underlying change list (elements must be of
   * type <code>Change</code>
   * @param currentChange the current change to be displayed
   * @param showMasterValues true if master values should be displayed
   */
  public ChangeDetailsDialog(Shell parentShell, String title, CheckboxTableViewer viewer,
                             List<Change> changeList, Change currentChange, boolean showMasterValues) {
    super(parentShell, null);
    this.title = title;
    this.viewer = viewer;
    this.changeList = changeList;
    this.currentChange = currentChange;
    this.showMasterValues = showMasterValues;
  }
  
  /**
   * Sets the current change and updates the contents of this dialog.
   * @param change the change to set
   */
  private void setChange(Change change) {
    this.currentChange = change;
    updateContents();
  }
  
  /**
   * Updates the contents of this dialog according to the current
   * change.
   */
  private void updateContents() {
    // accept button
    acceptButton.setSelection(currentChange.isAccept());
    acceptButton.setEnabled(currentChange.isAcceptable());
    
    // previous and next buttons
    int currentIndex = changeList.indexOf(currentChange);
    previousButton.setEnabled(currentIndex > 0);
    nextButton.setEnabled(currentIndex < changeList.size() - 1);
    
    // key and context texts
    keyText.setText(currentChange.getKey());
    contextText.setText(TremaUtil.emptyStringIfNull(currentChange.getContext()));
    changeText.setText(Change.getDescription(currentChange));
    
    // imported status and database status
    importedStatusText.setText(currentChange.getImportedStatus().getName());
    acceptImportedStatusButton.setEnabled(currentChange.isAcceptable());
    
    Status dbStatus = currentChange.getDbStatus();
    if (dbStatus == null) {
      dbStatusText.setText("");
      acceptDbStatusButton.setEnabled(false);
    } else {
      dbStatusText.setText(dbStatus.getName()); 
      acceptDbStatusButton.setEnabled(currentChange.isAcceptable());
    }
    
    // imported value and database value
    importedValueText.setText(currentChange.getImportedValue());
    acceptImportedValueButton.setEnabled(currentChange.isAcceptable());
    
    String dbValue = currentChange.getDbValue();
    if (dbValue == null) {
      dbValueText.setText("");
      acceptDbValueButton.setEnabled(false);
    } else {
      dbValueText.setText(currentChange.getDbValue());
      acceptDbValueButton.setEnabled(currentChange.isAcceptable());
    }
    
    // imported master value and database master value
    if (showMasterValues && currentChange.hasMasterLanguage()) {
      importedMasterValueText.setText(currentChange.getImportedMasterValue());
      acceptImportedMasterValueButton.setEnabled(currentChange.isAcceptable());
      
      String dbMasterValue = currentChange.getDbMasterValue(); 
      if (dbMasterValue == null) {
        dbMasterValueText.setText("");
        acceptDbMasterValueButton.setEnabled(false);
      } else {
        dbMasterValueText.setText(dbMasterValue);
        acceptDbMasterValueButton.setEnabled(currentChange.isAcceptable());
      }
    }
  }
  
  /**
   * Sets the previous change as the current change and updates the
   * contents of this dialog.
   */
  private void setPreviousChange() {
    int currentIndex = changeList.indexOf(currentChange);
    if (currentIndex > 0) {
      Change previousChange = changeList.get(currentIndex - 1);
      setChange(previousChange);
      viewer.setSelection(new StructuredSelection(previousChange));
    }
  }
  
  /**
   * Sets the next change as the current change and updates the
   * contents of this dialog.
   */
  private void setNextChange() {
    int currentIndex = changeList.indexOf(currentChange);
    if (currentIndex < changeList.size() - 1) {
      Change nextChange = changeList.get(currentIndex + 1);
      setChange(nextChange);
      viewer.setSelection(new StructuredSelection(nextChange));
    }
  }
  
  /** {@inheritDoc} */
  @Override
  protected void configureShell(Shell shell) {
    super.configureShell(shell);
    if (title != null) {
      shell.setText(title);
    }
  }
  
  /** {@inheritDoc} */
  @Override
  protected Control createDialogArea(Composite parent) {
    Composite area = (Composite) super.createDialogArea(parent);
    
    Composite mainComposite = new Composite(area, SWT.NONE);
    GridLayout gridLayout = new GridLayout();
    gridLayout.marginWidth = 0;
    gridLayout.marginHeight = 0;
    mainComposite.setLayout(gridLayout);
    mainComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
    
    createHeaderRow(mainComposite);
    createSummaryGroup(mainComposite);
    createDetailsGroup(mainComposite);
    
    applyDialogFont(area);
    
    updateContents();
    
    return area;
  }
  
  /**
   * Creates the header row.
   * @param parent the parent composite
   */
  private void createHeaderRow(Composite parent) {
    Composite headerComposite = new Composite(parent, SWT.NONE);
    GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
    headerComposite.setLayoutData(gridData);
    GridLayout headerLayout = new GridLayout();
    headerLayout.numColumns = 2;
    headerComposite.setLayout(headerLayout);

    acceptButton = new Button(headerComposite, SWT.CHECK);
    acceptButton.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL));
    acceptButton.setText("&Accept this change");
    acceptButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        acceptButtonPressed();
      }
    });

    Composite navigationComposite = new Composite(headerComposite, SWT.NONE);
    GridLayout navigationLayout = new GridLayout();
    navigationLayout.marginWidth = 0;
    navigationLayout.marginHeight = 0;
    navigationLayout.numColumns = 2;
    navigationComposite.setLayout(navigationLayout);

    nextButton = new Button(navigationComposite, SWT.PUSH);
    nextButton.setToolTipText("Show next change");
    nextButton.setImage(TremaPlugin.getDefault().getImage("icons/down.gif"));
    nextButton.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_END));
    nextButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        setNextChange();
      }
    });

    previousButton = new Button(navigationComposite, SWT.PUSH);
    previousButton.setToolTipText("Show previous change");
    previousButton.setImage(TremaPlugin.getDefault().getImage("icons/up.gif"));
    previousButton.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL));
    previousButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        setPreviousChange();
      }
    });
  }
  
  /**
   * Creates the summary group.
   * @param parent the parent composite
   */
  private void createSummaryGroup(Composite parent) {
    Group summaryGroup = new Group(parent, SWT.NONE);
    summaryGroup.setText("&Summary");
    GridLayout generalGroupLayout = new GridLayout();
    generalGroupLayout.numColumns = 2;
    summaryGroup.setLayout(generalGroupLayout);
    summaryGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    
    Label changeLabel = new Label(summaryGroup, SWT.NONE);
    changeLabel.setText("Chan&ge:");
    
    changeText = new Text(summaryGroup, SWT.READ_ONLY);
    changeText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

    Label keyLabel = new Label(summaryGroup, SWT.NONE);
    keyLabel.setText("&Key:");

    keyText = new Text(summaryGroup, SWT.READ_ONLY);
    keyText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

    Label contextLabel = new Label(summaryGroup, SWT.NONE);
    contextLabel.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
    contextLabel.setText("Co&ntext:");

    contextText = new Text(summaryGroup, SWT.V_SCROLL | SWT.MULTI | SWT.WRAP | SWT.READ_ONLY);
    GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
    gridData.heightHint = convertHeightInCharsToPixels(2);
    contextText.setLayoutData(gridData);
  }

  /**
   * Creates the details group.
   * @param parent the parent composite
   */
  private void createDetailsGroup(Composite parent) {
    // using a nested grid layout to make the columns equal width
    
    Group detailsGroup = new Group(parent, SWT.NONE);
    detailsGroup.setText("&Details");
    detailsGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
    GridLayout gridLayout = new GridLayout();
    gridLayout.numColumns = 2;
    detailsGroup.setLayout(gridLayout);
    
    createHeadingRow(detailsGroup);
    createStatusRow(detailsGroup);
    createValueRow(detailsGroup);
    if (showMasterValues) {
      createMasterValueRow(detailsGroup);
    }
  }
  
  /**
   * Creates the heading row of the details group.
   * @param parent the parent group
   */
  private void createHeadingRow(Group parent) {
    // spacer label
    new Label(parent, SWT.NONE);

    Composite subComposite = new Composite(parent, SWT.NONE);
    GridLayout gridLayout = new GridLayout();
    gridLayout.marginWidth = 0;
    gridLayout.marginHeight = 0;
    gridLayout.numColumns = 2;
    gridLayout.makeColumnsEqualWidth = true;
    subComposite.setLayout(gridLayout);
    subComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

    Composite leftComposite = new Composite(subComposite, SWT.NONE);
    leftComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    gridLayout = new GridLayout();
    gridLayout.marginWidth = 2;
    gridLayout.marginHeight = 0;
    leftComposite.setLayout(gridLayout);
    
    Label importSourceLabel = new Label(leftComposite, SWT.NONE);
    TremaUtilEclipse.setBoldFont(importSourceLabel, getShell().getDisplay());
    importSourceLabel.setText("Import source:");

    Composite rightComposite = new Composite(subComposite, SWT.NONE);
    rightComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    gridLayout = new GridLayout();
    gridLayout.marginWidth = 2;
    gridLayout.marginHeight = 0;
    rightComposite.setLayout(gridLayout);

    Label dbLabel = new Label(rightComposite, SWT.NONE);
    TremaUtilEclipse.setBoldFont(dbLabel, getShell().getDisplay());
    dbLabel.setText("Database:");
  }
  
  /**
   * Creates the status row of the details group.
   * @param parent the parent group
   */
  private void createStatusRow(Group parent) {
    Label statusLabel = new Label(parent, SWT.NONE);
    statusLabel.setText("Sta&tus:");

    Composite subComposite = new Composite(parent, SWT.NONE);
    GridLayout gridLayout = new GridLayout();
    gridLayout.marginWidth = 0;
    gridLayout.marginHeight = 0;
    gridLayout.numColumns = 2;
    gridLayout.makeColumnsEqualWidth = true;
    subComposite.setLayout(gridLayout);
    subComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

    Composite leftComposite = new Composite(subComposite, SWT.NONE);
    leftComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    gridLayout = new GridLayout();
    gridLayout.marginWidth = 0;
    gridLayout.marginHeight = 0;
    gridLayout.numColumns = 2;
    leftComposite.setLayout(gridLayout);
    
    importedStatusText = new Text(leftComposite, SWT.READ_ONLY);
    acceptImportedStatusButton = new Button(leftComposite, SWT.ARROW | SWT.RIGHT | SWT.FLAT);
    acceptImportedStatusButton.setToolTipText("Accept this value");
    acceptImportedStatusButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        setAcceptStatus(Status.valueOf(importedStatusText.getText()));
      }
    });

    Composite rightComposite = new Composite(subComposite, SWT.NONE);
    rightComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    gridLayout = new GridLayout();
    gridLayout.marginWidth = 0;
    gridLayout.marginHeight = 0;
    gridLayout.numColumns = 2;
    rightComposite.setLayout(gridLayout);

    dbStatusText = new Text(rightComposite, SWT.READ_ONLY);
    acceptDbStatusButton = new Button(rightComposite, SWT.ARROW | SWT.RIGHT | SWT.FLAT);
    acceptDbStatusButton.setToolTipText("Accept this value");
    acceptDbStatusButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        setAcceptStatus(Status.valueOf(dbStatusText.getText()));
      }
    });
  }

  /**
   * Creates the value row of the details group.
   * @param parent the parent group
   */
  private void createValueRow(Group parent) {
    Label valuesLabel = new Label(parent, SWT.NONE);
    valuesLabel.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
    valuesLabel.setText("&Values:");
    
    Composite subComposite = new Composite(parent, SWT.NONE);
    GridLayout gridLayout = new GridLayout();
    gridLayout.marginWidth = 0;
    gridLayout.marginHeight = 0;
    gridLayout.numColumns = 2;
    gridLayout.makeColumnsEqualWidth = true;
    subComposite.setLayout(gridLayout);
    GridData gridData = new GridData(GridData.FILL_BOTH);
    gridData.verticalSpan = 2;
    subComposite.setLayoutData(gridData);

    Composite leftComposite = new Composite(subComposite, SWT.NONE);
    leftComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
    gridLayout = new GridLayout();
    gridLayout.marginWidth = 0;
    gridLayout.marginHeight = 0;
    gridLayout.numColumns = 2;
    leftComposite.setLayout(gridLayout);
    
    importedValueText = new Text(leftComposite, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL | SWT.READ_ONLY);
    importedValueText.setBackground(getShell().getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
    gridData = new GridData(GridData.FILL_BOTH);
    gridData.heightHint = convertHeightInCharsToPixels(4);
    importedValueText.setLayoutData(gridData);
    
    acceptImportedValueButton = new Button(leftComposite, SWT.ARROW | SWT.RIGHT | SWT.FLAT);
    acceptImportedValueButton.setToolTipText("Accept this value");
    gridData = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
    acceptImportedValueButton.setLayoutData(gridData);
    acceptImportedValueButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        setAcceptValue(importedValueText.getText());
      }
    });
    
    Composite rightComposite = new Composite(subComposite, SWT.NONE);
    rightComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
    gridLayout = new GridLayout();
    gridLayout.marginWidth = 0;
    gridLayout.marginHeight = 0;
    gridLayout.numColumns = 2;
    rightComposite.setLayout(gridLayout);
    
    dbValueText = new Text(rightComposite, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL | SWT.READ_ONLY);
    dbValueText.setBackground(getShell().getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
    gridData = new GridData(GridData.FILL_BOTH);
    gridData.heightHint = convertHeightInCharsToPixels(4);
    dbValueText.setLayoutData(gridData);

    acceptDbValueButton = new Button(rightComposite, SWT.ARROW | SWT.RIGHT | SWT.FLAT);
    acceptDbValueButton.setToolTipText("Accept this value");
    gridData = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
    acceptDbValueButton.setLayoutData(gridData);
    acceptDbValueButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        setAcceptValue(dbValueText.getText());
      }
    });
    
    compareValuesButton = new Button(parent, SWT.NONE);
    compareValuesButton.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
    compareValuesButton.setText("Com&pare...");
    compareValuesButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        compareValuesButtonPressed();
      }
    });
  }

  /**
   * Creates the master value row of the details group.
   * @param parent the parent group
   */
  private void createMasterValueRow(Group parent) {
    Label masterValuesLabel = new Label(parent, SWT.NONE);
    masterValuesLabel.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
    masterValuesLabel.setText("&Master Values:");
    
    Composite subComposite = new Composite(parent, SWT.NONE);
    GridLayout gridLayout = new GridLayout();
    gridLayout.marginWidth = 0;
    gridLayout.marginHeight = 0;
    gridLayout.numColumns = 2;
    gridLayout.makeColumnsEqualWidth = true;
    subComposite.setLayout(gridLayout);
    GridData gridData = new GridData(GridData.FILL_BOTH);
    gridData.verticalSpan = 2;
    subComposite.setLayoutData(gridData);

    Composite leftComposite = new Composite(subComposite, SWT.NONE);
    leftComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
    gridLayout = new GridLayout();
    gridLayout.marginWidth = 0;
    gridLayout.marginHeight = 0;
    gridLayout.numColumns = 2;
    leftComposite.setLayout(gridLayout);
    
    importedMasterValueText = new Text(leftComposite, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL | SWT.READ_ONLY);
    importedMasterValueText.setBackground(getShell().getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
    gridData = new GridData(GridData.FILL_BOTH);
    gridData.heightHint = convertHeightInCharsToPixels(4);
    importedMasterValueText.setLayoutData(gridData);
    
    acceptImportedMasterValueButton = new Button(leftComposite, SWT.ARROW | SWT.RIGHT | SWT.FLAT);
    acceptImportedMasterValueButton.setToolTipText("Accept this value");
    gridData = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
    acceptImportedMasterValueButton.setLayoutData(gridData);
    acceptImportedMasterValueButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        setAcceptMasterValue(importedMasterValueText.getText());
      }
    });
    
    Composite rightComposite = new Composite(subComposite, SWT.NONE);
    rightComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
    gridLayout = new GridLayout();
    gridLayout.marginWidth = 0;
    gridLayout.marginHeight = 0;
    gridLayout.numColumns = 2;
    rightComposite.setLayout(gridLayout);
    
    dbMasterValueText = new Text(rightComposite, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL | SWT.READ_ONLY);
    dbMasterValueText.setBackground(getShell().getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
    gridData = new GridData(GridData.FILL_BOTH);
    gridData.heightHint = convertHeightInCharsToPixels(4);
    dbMasterValueText.setLayoutData(gridData);
    
    acceptDbMasterValueButton = new Button(rightComposite, SWT.ARROW | SWT.RIGHT | SWT.FLAT);
    acceptDbMasterValueButton.setToolTipText("Accept this value");
    gridData = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
    acceptDbMasterValueButton.setLayoutData(gridData);
    acceptDbMasterValueButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        setAcceptMasterValue(dbMasterValueText.getText());
      }
    });
    
    compareMasterValuesButton = new Button(parent, SWT.NONE);
    compareMasterValuesButton.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
    compareMasterValuesButton.setText("Co&mpare...");
    compareMasterValuesButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        compareMasterValuesButtonPressed();
      }
    });
  }

  /** {@inheritDoc} */
  @Override
  protected void createButtonsForButtonBar(Composite parent) {
    // just a close button
    createButton(parent, IDialogConstants.OK_ID, IDialogConstants.CLOSE_LABEL, true);
  }
  
  /**
   * Toggles the accept state of the current change and updates the
   * viewer.
   */
  private void acceptButtonPressed() {
    currentChange.setAccept(!currentChange.isAccept());
    viewer.setChecked(currentChange, currentChange.isAccept());
  }
  
  /** Opens a compare dialog for the two of the values. */
  private void compareValuesButtonPressed() {
    String leftText = importedValueText.getText();
    String rightText = dbValueText.getText();
    
    new CompareDialog(getShell(), "Compare Values", "Import source", "Database", leftText, rightText).open();
  }
  
  /** Opens a compare dialog for the two of the master values. */
  private void compareMasterValuesButtonPressed() {
    String leftText = importedMasterValueText.getText();
    String rightText = dbMasterValueText.getText();
    
    new CompareDialog(getShell(), "Compare Master Values", "Import source", "Database", leftText, rightText).open();
  }
  
  /** 
   * Sets the accept status of the current change and updates the
   * viewer.
   * @param status the accept status to set
   */
  private void setAcceptStatus(Status status) {
    currentChange.setAcceptStatus(status);
    viewer.update(currentChange, new String[] {ImportWizardChangesPage.PROP_STATUS_COLUMN});
  }
  
  /**
   * Sets the accept value of the current change and updates the
   * viewer.
   * @param value the accept value to set
   */
  private void setAcceptValue(String value) {
    currentChange.setAcceptValue(value);
    viewer.update(currentChange, new String[] {ImportWizardChangesPage.PROP_VALUE_COLUMN});
  }
  
  /**
   * Sets the accept master value of the current change and updates the
   * viewer.
   * @param masterValue the accept master value to set
   */
  private void setAcceptMasterValue(String masterValue) {
    currentChange.setAcceptMasterValue(masterValue);
    viewer.update(currentChange, new String[] {ImportWizardChangesPage.PROP_MASTER_VALUE_COLUMN});
  }
  
}
