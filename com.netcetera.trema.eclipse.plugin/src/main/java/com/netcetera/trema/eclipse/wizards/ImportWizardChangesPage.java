package com.netcetera.trema.eclipse.wizards;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

import com.netcetera.trema.common.TremaUtil;
import com.netcetera.trema.core.Status;
import com.netcetera.trema.core.api.IDatabase;
import com.netcetera.trema.core.api.IImportSource;
import com.netcetera.trema.core.importing.Change;
import com.netcetera.trema.eclipse.TremaUtilEclipse;
import com.netcetera.trema.eclipse.dialogs.ChangeDetailsDialog;
import com.netcetera.trema.eclipse.importing.MonitoringChangesAnalyzer;



/**
 * Wizard page displaying a summary of the changes, allowing the
 * user to modify and accept them.
 */
@SuppressWarnings("unchecked")
public class ImportWizardChangesPage extends WizardPage {
  
  /** Key column property. */
  public static final String PROP_KEY_COLUMN = "keyColumn";
  
  /** Change description column property. */
  public static final String PROP_CHANGE_COLUMN = "changeColumn";
  
  /** Master value column property. */
  public static final String PROP_MASTER_VALUE_COLUMN = "masterValueColumn";
  
  /** Value column property. */
  public static final String PROP_VALUE_COLUMN = "valueColumn";
  
  /** Status column property. */
  public static final String PROP_STATUS_COLUMN = "statusColumn";
  
  private IDatabase db = null;
  private ImportWizardFilePage filePage = null;
  
  // using arrays to aviod duplicating code
  private static final int CONFLICTING_CHANGES_INDEX = 0;
  private static final int NON_CONFLICTING_CHANGES_INDEX = 1;
  
  private CheckboxTableViewer[] changesViewers = new CheckboxTableViewer[2];
  private List<Change>[] changeLists = new List[2];
  private Button[] detailsButtons = new Button[2];
  private boolean importedMasterLanguagePresent = false;
  
  private Text importSourceLanguageText = null;
  private Label importSourceUpperLabel = null;
  private Label importSourceLowerLabel = null;
  private Text importSourceUpperText = null;
  private Text importSourceLowerText = null;
  private Text dbMasterLanguageText = null;
  private Text dbRecordsText = null;
  private Text[] changesText = new Text[2];  
  
  /**
   * Creates a new instance.
   * @param db the database being imported to
   * @param filePage the wizard page providing the CSV file
   */
  public ImportWizardChangesPage(IDatabase db, ImportWizardFilePage filePage) {
    super("tremaCSVImportWizardChangesPage");
    this.db = db;
    this.filePage = filePage;    
    setTitle("Summary of changes");
    setDescription("Select the changes to accept");
    setPageComplete(false); // don't allow a finish without going through this page
  }
  
  /** {@inheritDoc} */
  public void createControl(Composite parent) {
    initializeDialogUnits(parent);
    
    Composite mainComposite = new Composite(parent, SWT.NULL);
    mainComposite.setLayout(new GridLayout());
    setControl(mainComposite);
    
    createStatisticsGroup(mainComposite);
    
    SashForm sashForm = new SashForm(mainComposite, SWT.VERTICAL);
    GridData gridData = new GridData(GridData.FILL_BOTH);
    gridData.heightHint = convertHeightInCharsToPixels(30);
    sashForm.setLayoutData(gridData);

    createChangesGroup(sashForm, CONFLICTING_CHANGES_INDEX, "&Conflicting Changes");
    createChangesGroup(sashForm, NON_CONFLICTING_CHANGES_INDEX, "&Non-Conflicting Changes");
  }
  
  /**
   * Create the statistics group.
   * @param parent the parent composite
   */
  private void createStatisticsGroup(Composite parent) {
    Group statisticsGroup = new Group(parent, SWT.NONE);
    statisticsGroup.setText("Statist&ics");
    statisticsGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING));
    GridLayout gridLayout = new GridLayout();
    gridLayout.makeColumnsEqualWidth = true;
    gridLayout.numColumns = 3;
    statisticsGroup.setLayout(gridLayout);

    Composite leftComposite = new Composite(statisticsGroup, SWT.NONE);
    leftComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING));
    gridLayout = new GridLayout();
    gridLayout.marginWidth = 0;
    gridLayout.marginHeight = 0;
    gridLayout.numColumns = 2;
    leftComposite.setLayout(gridLayout);

    Label label = new Label(leftComposite, SWT.NONE);
    GridData gridData = new GridData();
    gridData.horizontalSpan = 2;
    label.setLayoutData(gridData);
    TremaUtilEclipse.setBoldFont(label, getControl().getDisplay());
    label.setText("Import source:");

    label = new Label(leftComposite, SWT.NONE);
    label.setText("Language:");    
    importSourceLanguageText = new Text(leftComposite, SWT.READ_ONLY);
    
    importSourceUpperLabel = new Label(leftComposite, SWT.NONE);
    importSourceUpperLabel.setText("Master Language:");
    importSourceUpperText = new Text(leftComposite, SWT.READ_ONLY);
    importSourceLowerLabel = new Label(leftComposite, SWT.NONE);
    importSourceLowerLabel.setText("Records:");
    importSourceLowerText = new Text(leftComposite, SWT.READ_ONLY);

    Composite middleComposite = new Composite(statisticsGroup, SWT.NONE);
    gridLayout = new GridLayout();
    gridLayout.marginWidth = 0;
    gridLayout.marginHeight = 0;
    gridLayout.numColumns = 2;
    middleComposite.setLayout(gridLayout);
    middleComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING));

    label = new Label(middleComposite, SWT.NONE);
    gridData = new GridData();
    gridData.horizontalSpan = 2;
    label.setLayoutData(gridData);
    TremaUtilEclipse.setBoldFont(label, getControl().getDisplay());
    label.setText("Database:");

    label = new Label(middleComposite, SWT.NONE);
    label.setText("Master Language:");

    dbMasterLanguageText = new Text(middleComposite, SWT.READ_ONLY);

    label = new Label(middleComposite, SWT.NONE);
    label.setText("Records:");

    dbRecordsText = new Text(middleComposite, SWT.READ_ONLY);

    Composite rightComposite = new Composite(statisticsGroup, SWT.NONE);
    rightComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING));
    gridLayout = new GridLayout();
    gridLayout.marginWidth = 0;
    gridLayout.marginHeight = 0;
    gridLayout.numColumns = 2;
    rightComposite.setLayout(gridLayout);

    label = new Label(rightComposite, SWT.NONE);
    gridData = new GridData();
    gridData.horizontalSpan = 2;
    label.setLayoutData(gridData);
    TremaUtilEclipse.setBoldFont(label, getControl().getDisplay());
    label.setText("Changes:");

    label = new Label(rightComposite, SWT.NONE);
    label.setText("Conflicting:");

    changesText[CONFLICTING_CHANGES_INDEX] = new Text(rightComposite, SWT.READ_ONLY);

    label = new Label(rightComposite, SWT.NONE);
    label.setText("Non-conflicting:");

    changesText[NON_CONFLICTING_CHANGES_INDEX] = new Text(rightComposite, SWT.READ_ONLY);
  }

  /**
   * Creates a group that displays changes.
   * @param parent the parent composite
   * @param index the index (CONFLICTING_CHANGES_INDEX or
   * NON_CONFLICTING_CHANGES_INDEX)
   * @param groupText the text to be displayed in the group
   */
  private void createChangesGroup(Composite parent, final int index, String groupText) {
    Group conflictingChangesGroup = new Group(parent, SWT.NONE);
    conflictingChangesGroup.setText(groupText);
    GridLayout gridLayout = new GridLayout();
    gridLayout.numColumns = 3;
    conflictingChangesGroup.setLayout(gridLayout);
    GridData gridData = new GridData(GridData.FILL_BOTH);
    conflictingChangesGroup.setLayoutData(gridData);
    
    // changes viewer
    Table table = createTable(conflictingChangesGroup);
    changesViewers[index] = new CheckboxTableViewer(table);
    changesViewers[index].setUseHashlookup(true);
    changesViewers[index].setContentProvider(new ArrayContentProvider());
    changesViewers[index].setLabelProvider(new ChangeLabelProvider());
    changesViewers[index].addDoubleClickListener(new IDoubleClickListener() {
      public void doubleClick(DoubleClickEvent event) {
        detailsButtonPressed(index);
      }
    });
    changesViewers[index].addSelectionChangedListener(new ISelectionChangedListener() {
      public void selectionChanged(SelectionChangedEvent event) {
        updateDetailsButton(index);
      }
    });
    
    // cell editors and cell modifier
    CellEditor[] cellEditors = new CellEditor[table.getColumnCount()];
    cellEditors[0] = null;
    cellEditors[1] = null;
    // cellEditors[2] = new TextCellEditor(table);
    // cellEditors[3] = new TextCellEditor(table);
    // cellEditors[4] = new ComboBoxCellEditor(table,
    // Status.getAvailableStatusNames(), SWT.READ_ONLY);
    cellEditors[2] = null;
    cellEditors[3] = null;
    cellEditors[4] = null;

    changesViewers[index].setCellEditors(cellEditors);
    changesViewers[index].setCellModifier(new ChangeCellModifier(changesViewers[index]));
    
    // column properties
    changesViewers[index].setColumnProperties(new String[] {
        PROP_KEY_COLUMN, PROP_CHANGE_COLUMN, PROP_MASTER_VALUE_COLUMN, PROP_VALUE_COLUMN, PROP_STATUS_COLUMN
    });
    
    // check state listener
    changesViewers[index].addCheckStateListener(new ICheckStateListener() {
      public void checkStateChanged(CheckStateChangedEvent event) {
        // little hack to prevent grayed elements from being selected
        Change change = (Change) event.getElement();
        change.setAccept(event.getChecked());
        updateCheckbox(changesViewers[index], change);
      }
    });
    
    // buttons
    detailsButtons[index] = new Button(conflictingChangesGroup, SWT.PUSH);
    detailsButtons[index].setText(index == CONFLICTING_CHANGES_INDEX ? "&Details..." : "De&tails");
    detailsButtons[index].addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        detailsButtonPressed(index);
      }
    });
    Button selectAllButton = new Button(conflictingChangesGroup, SWT.PUSH);
    selectAllButton.setText(index == CONFLICTING_CHANGES_INDEX ? "&Select All" : "Select &All");
    selectAllButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        selectAllButtonPressed(index);
      }
    });
    Button deselectAllButton = new Button(conflictingChangesGroup, SWT.PUSH);
    deselectAllButton.setText(index == CONFLICTING_CHANGES_INDEX ? "Deselec&t All" : "Dese&lect All");
    deselectAllButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        deselectAllButtonPressed(index);
      }
    });
  }

  /**
   * Select all button event handler.
   * @param index the index (CONFLICTING_CHANGES_INDEX or
   * NON_CONFLICTING_CHANGES_INDEX)
   */
  private void selectAllButtonPressed(int index) {
    for (Iterator<Change> i = changeLists[index].iterator(); i.hasNext();) {
      Change change = i.next();
      change.setAccept(true);
      updateCheckbox(changesViewers[index], change);
    }
  }
  
  /**
   * Deselect all button event handler.
   * @param index the index (CONFLICTING_CHANGES_INDEX or
   * NON_CONFLICTING_CHANGES_INDEX)
   */
  private void deselectAllButtonPressed(int index) {
    for (Iterator<Change> i = changeLists[index].iterator(); i.hasNext();) {
      Change change = i.next();
      change.setAccept(false);
      updateCheckbox(changesViewers[index], change);
    }
  }
  
  /**
   * Creates the table of a viewer displaying changes.
   * @param parent the parent composite
   */
  private Table createTable(Composite parent) {
    Table table = new Table(parent, SWT.CHECK | SWT.BORDER | SWT.FULL_SELECTION | SWT.SINGLE);
    table.setLinesVisible(true);
    table.setHeaderVisible(true);
    GridData gridData = new GridData(GridData.FILL_BOTH);
    gridData.horizontalSpan = 3;
    table.setLayoutData(gridData);
    
    TableColumn keyColumn = new TableColumn(table, SWT.NONE);
    keyColumn.setText("Key");
    
    TableColumn changeColumn = new TableColumn(table, SWT.NONE);
    changeColumn.setText("Change");
    
    TableColumn masterValueColumn = new TableColumn(table, SWT.NONE);
    masterValueColumn.setText("Master Value to accept");
    
    TableColumn valueColumn = new TableColumn(table, SWT.NONE);
    valueColumn.setText("Value to accept");
    
    TableColumn statusColumn = new TableColumn(table, SWT.NONE);
    statusColumn.setText("Status to accept");
    statusColumn.setResizable(false);
    
    setColumnWidths(table, true);
    
    return table;
  }
  
  /**
   * Adjusts the column widths.
   * @param table the table whose columns are to adjust
   * @param showMasterLanguage true if the master language column is to
   * be shown
   */
  private void setColumnWidths(Table table, boolean showMasterLanguage) {
    TableColumn[] tableColumns = table.getColumns();
    // to avoid horizontal scrollbars, the sum of the single column widths must be same in both cases (730)
    if (showMasterLanguage) {
      tableColumns[0].setWidth(120); // key
      tableColumns[1].setWidth(180); // change description
      tableColumns[2].setWidth(170); // master language
      tableColumns[2].setResizable(true);
      tableColumns[3].setWidth(170); // value
      tableColumns[4].setWidth(90);  // status
    } else {
      // since no columns can be removed, simply set the master language column
      // non-resizable and its width to zero
      tableColumns[0].setWidth(150); // key
      tableColumns[1].setWidth(240); // change description
      tableColumns[2].setWidth(0);   // master language
      tableColumns[2].setResizable(false); // don't allow resizing
      tableColumns[3].setWidth(240); // value
      tableColumns[4].setWidth(100); // status
    }
  }

  /**
   * Opens a change details dialog for the currently selected change.
   * @param index the index (CONFLICTING_CHANGES_INDEX or
   * NON_CONFLICTING_CHANGES_INDEX)
   */
  private void detailsButtonPressed(int index) {
    IStructuredSelection selection = (IStructuredSelection) changesViewers[index].getSelection();
    
    if (selection.size() != 1) {
      return;
    }
    
    Object selected = selection.getFirstElement();   
    Dialog dialog = new ChangeDetailsDialog(getShell(), "Change Details", changesViewers[index], changeLists[index],
                                            (Change) selected, importedMasterLanguagePresent);
    dialog.open();
  }
  
  /**
   * Updates the enablement of the details buttons depending on the
   * current selection.
   * @param index the index (CONFLICTING_CHANGES_INDEX or
   * NON_CONFLICTING_CHANGES_INDEX)
   */
  private void updateDetailsButton(int index) {
    IStructuredSelection selection = (IStructuredSelection) changesViewers[index].getSelection();
    detailsButtons[index].setEnabled(selection.size() == 1);
  }
  
  /** {@inheritDoc} */
  @Override
  public void setVisible(boolean visible) {
    if (visible) {
      IImportSource importSource = filePage.getImportSource();
      if (importSource != null) {
        // analyze the changes and populate the viewers
        final MonitoringChangesAnalyzer analyzer = new MonitoringChangesAnalyzer(importSource, db);
        
        IRunnableWithProgress operation = new IRunnableWithProgress() {
          public void run(IProgressMonitor monitor) {
            try {
              analyzer.setMonitor(monitor);
              analyzer.analyze();
            } finally {
              monitor.done();
            }
          }
        };
        
        try {
          getContainer().run(false, false, operation);
          
          // get the analysis results
          importedMasterLanguagePresent = importSource.hasMasterLanguage();
          changeLists[CONFLICTING_CHANGES_INDEX] = new ArrayList<Change>(analyzer.getConflictingChangesAsList());
          changeLists[NON_CONFLICTING_CHANGES_INDEX] = new ArrayList<Change>(analyzer.getNonConflictingChangesAsList());
          
          // update the statistics groups
          importSourceLanguageText.setText(importSource.getLanguage());
          if (importedMasterLanguagePresent) {
            importSourceUpperLabel.setText("Master Language:");
            importSourceUpperText.setText(importSource.getMasterLanguage());
            importSourceLowerLabel.setText("Records:");
            importSourceLowerText.setText(String.valueOf(importSource.getSize()));            
          } else {
            importSourceUpperLabel.setText("Records:");
            importSourceUpperText.setText(String.valueOf(importSource.getSize()));  
            importSourceLowerLabel.setText("");
            importSourceLowerText.setText(""); 
          }
          dbMasterLanguageText.setText(db.getMasterLanguage());
          dbRecordsText.setText(String.valueOf(db.getSize()));
          
          for (int index = 0; index < 2; index++) {
            // set the new inputs for the viewers
            changesViewers[index].setInput(changeLists[index]);
            
            // update the checkboxes
            updateCheckbox(changesViewers[index], changeLists[index]);
            
            // adjust the column widths
            setColumnWidths(changesViewers[index].getTable(), importedMasterLanguagePresent);
            
            // update the change count
            changesText[index].setText(String.valueOf(changeLists[index].size()));
            
            // update the details buttons
            updateDetailsButton(index);
          }
        } catch (InvocationTargetException e) {
          // should never happen
          MessageDialog.openError(getShell(), "Trema Import", "There was an error analyzing the changes.");
        } catch (InterruptedException e) {
          // the opreation is not cancelable, so this should never happen
          MessageDialog.openError(getShell(), "Trema Import", "There was an error analyzing the changes.");
        }
      }
      setPageComplete(true); // this page needs no validation and is always complete if visible
    } else {
      setPageComplete(false); // don't allow a finish without going through this page
    }
    
    super.setVisible(visible);
  }
  
  /**
   * Updates the checkbox state of a given change.
   * @param viewer the viewer to be updated
   * @param change the change
   */
  private void updateCheckbox(CheckboxTableViewer viewer, Change change) {
    viewer.setChecked(change, change.isAccept());
    viewer.setGrayed(change, !change.isAcceptable());
  }
  
  /**
   * Updates the checkbox states of a given change list.
   * @param viewer the viewer to be updated
   * @param changeList the list of changes, elements must be of type
   * <code>Change</code>
   */
  private void updateCheckbox(CheckboxTableViewer viewer, List<Change> changeList) {
    for (Change change : changeList) {
      updateCheckbox(viewer, change);           
    }
  }
  
  /**
   * Gets the conflicting changes.
   * @return the conflicting changes.
   */
  public List<Change> getConflictingChanges() {
    return changeLists[CONFLICTING_CHANGES_INDEX];
  }
  
  /**
   * Gets the non-conflicting changes.
   * @return the non-conflicting changes.
   */
  public List<Change> getNonConflictingChanges() {
    return changeLists[NON_CONFLICTING_CHANGES_INDEX];
  }
  
  /**
   * Label provider for the change viewers.
   */
  private class ChangeLabelProvider extends LabelProvider implements ITableLabelProvider {
    
    /** {@inheritDoc} */
    public Image getColumnImage(Object element, int columnIndex) {
      return null;
    }
    
    /** {@inheritDoc} */
    public String getColumnText(Object element, int columnIndex) {
      if (element instanceof Change) {
        Change change = (Change) element;
        
        switch (columnIndex) {
          case 0:
            return TremaUtil.emptyStringIfNull(change.getKey());
          case 1:
            return TremaUtil.emptyStringIfNull(Change.getDescription(change));
          case 2:
            return TremaUtil.emptyStringIfNull(change.getAcceptMasterValue());
          case 3:
            return TremaUtil.emptyStringIfNull(change.getAcceptValue());
          case 4:
            return TremaUtil.emptyStringIfNull("" + change.getAcceptStatus());
          default:
            return "";
        }
      }
      
      return element.toString();
    }
    
  }
  
  /**
   * Cell modifier for the change viewers.
   */
  private class ChangeCellModifier implements ICellModifier {
    
    private TableViewer viewer = null;
    
    /**
     * Creates a new instance.
     * @param viewer the associated change viewer
     */
    public ChangeCellModifier(TableViewer viewer) {
      this.viewer = viewer;
    }
    
    /** {@inheritDoc} */
    public boolean canModify(Object element, String property) {
      if (element instanceof Change) {
        Change change = (Change) element;
        return change.isAcceptable();
      }
      return false;
    }

    /** {@inheritDoc} */
    public Object getValue(Object element, String property) {
      if (element instanceof Change) {
        Change change = (Change) element;
        if (property == PROP_MASTER_VALUE_COLUMN) {
          return TremaUtil.emptyStringIfNull(change.getAcceptMasterValue());
        } else if (property == PROP_VALUE_COLUMN) {
          return TremaUtil.emptyStringIfNull(change.getAcceptValue());
        } else if (property == PROP_STATUS_COLUMN) {
          Status status = change.getAcceptStatus();
          if (status == null) {
            return Integer.valueOf(0);
          }
          return new Integer(status.getPosition());
        }
      }
      return null;
    }

    /** {@inheritDoc} */
    public void modify(Object element, String property, Object value) {
      // null indicates that the validator rejected the value
      if (value == null) {
        return;
      }
      Object e = element;
      
      if (element instanceof Item) {
        e = ((Item) element).getData();
      }

      if (e instanceof Change) {
        Change change = (Change) e;
        if (property == PROP_MASTER_VALUE_COLUMN) {
          change.setAcceptMasterValue((String) value);
          viewer.update(change, new String[] {PROP_MASTER_VALUE_COLUMN});
        } else if (property == PROP_VALUE_COLUMN) {
          change.setAcceptValue((String) value);
          viewer.update(change, new String[] {PROP_VALUE_COLUMN});
        } else if (property == PROP_STATUS_COLUMN) {
          change.setAcceptStatus(Status.valueOf((Integer) value));
          viewer.update(change, new String[] {PROP_STATUS_COLUMN});
        }
      }
    }
  }

}
