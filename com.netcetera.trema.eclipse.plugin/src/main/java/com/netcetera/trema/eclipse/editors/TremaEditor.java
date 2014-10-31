package com.netcetera.trema.eclipse.editors;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.commands.ActionHandler;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.ICellEditorListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.TreeViewerEditor;
import org.eclipse.jface.viewers.TreeViewerFocusCellManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.IEditorActionBarContributor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.ide.IGotoMarker;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.ui.texteditor.IStatusField;
import org.eclipse.ui.texteditor.MarkerUtilities;

import com.netcetera.trema.common.TremaUtil;
import com.netcetera.trema.core.ParseException;
import com.netcetera.trema.core.ParseWarning;
import com.netcetera.trema.core.Status;
import com.netcetera.trema.core.XMLDatabase;
import com.netcetera.trema.core.api.IDatabase;
import com.netcetera.trema.eclipse.TremaPlugin;
import com.netcetera.trema.eclipse.TremaUtilEclipse;
import com.netcetera.trema.eclipse.actions.AddTextNodeAction;
import com.netcetera.trema.eclipse.actions.AddValueNodeAction;
import com.netcetera.trema.eclipse.actions.EditAction;
import com.netcetera.trema.eclipse.actions.ExpandCollapseAction;
import com.netcetera.trema.eclipse.actions.ExportAction;
import com.netcetera.trema.eclipse.actions.ImportAction;
import com.netcetera.trema.eclipse.actions.MoveDownAction;
import com.netcetera.trema.eclipse.actions.MoveUpAction;
import com.netcetera.trema.eclipse.actions.RemoveAction;
import com.netcetera.trema.eclipse.actions.SelectAllAction;
import com.netcetera.trema.eclipse.actions.TremaEditorAction;
import com.netcetera.trema.eclipse.editors.xmleditor.XMLEditor;
import com.netcetera.trema.eclipse.validators.LanguageValidator;


/**
 * The Trema multipage editor. The first page shows a table tree view of a Trema
 * database, the second one consists of a nested text editor to display the raw
 * XML source. When the trema database cannot be successfully built from the
 * source file, then the general behaviour is to switch to the source view, so
 * that the error can be corrected there
 * 
 */
public class TremaEditor extends MultiPageEditorPart implements IGotoMarker {

  /** Property for the key column. */
  public static final String PROP_FIRST_COLUMN = "firstColumn";

  /** Property for the value column. */
  public static final String PROP_SECOND_COLUMN = "secondColumn";

  /** Property for the status column. */
  public static final String PROP_THIRD_COLUMN = "thirdColumn";

  /** Status field key for the "size" status field. */
  public static final String STATUS_FIELD_SIZE_KEY = "statusFieldSize";

  /**
   * The database model. This is the only time the database object is
   * instanciated.
   */
  private IDatabase db = new XMLDatabase();

  /** Nested text editor for the source page. */
  private TextEditor textEditor = null;
  
  /** The content provider and database listener. */
  private TremaContentProvider contentProvider = null;

  /** The cell editor for the second column. */
  private TextCellEditor secondColumnCellEditor = null;

  /** The validator for the master language. */
  private LanguageValidator masterLanguageValidator = null;

  /** Table tree columns. */
  private TreeColumn firstColumn = null;
  private TreeColumn secondColumn = null;
  private TreeColumn thirdColumn = null;

  /**
   * Index of the table tree viewer page, will be reassigned by 'addPage'.
   */
  private int tableTreeViewerPageIndex = 0;

  /** Index of the source page, will be reassigned by 'addPage'. */
  private int sourcePageIndex = 0;

  /** Flag indicating modification in the table tree page. */
  private boolean tableTreeModified = false;

  private TreeViewerFocusCellManager focusCellManager;

  /**
   * Flag indicating if the pages are out of sync. This flag has got nothing to
   * do with the dirty state of the editor, e.g. although the source page may
   * have been saved (i.e. not dirty), the table tree page might still be not
   * up-to-date. This flag is used during page changes.
   */
  private boolean pagesOutOfSync = true;

  /**
   * Temporary storage to keep track of the expanded elements in the table tree
   * viewer on page changes.
   */
  private Object[] expandedElements = null;

  private TreeViewer treeViewer;

  /** Central status field repository. */
  private Map<String, IStatusField> statusFieldMap = new HashMap<String, IStatusField>();

  /** Central Trema editor action repository. */
  private Map<String, TremaEditorAction> actionMap = new HashMap<String, TremaEditorAction>();

  /** {@inheritDoc} */
  @Override
  public void init(IEditorSite site, IEditorInput editorInput) throws PartInitException {
    // if we open an external file or run the editor as RCP application, we get
    // a JavaFileEditorInput
    // fixme: YT 2004-09-17: handle JavaFileEditorInput correctly (see other
    // fix-me tasks)
    if (!(editorInput instanceof IFileEditorInput)
        && !(editorInput instanceof FileStoreEditorInput)) {
      throw new PartInitException("Invalid input: must be FileStoreEditorInput");
    }
    super.init(site, editorInput);
  }

  /** {@inheritDoc} */
  @Override
  protected void createPages() {
    activateContext();
    createTableTreeViewerPage();
    createSourcePage();
    
    MenuManager menuManager = new MenuManager();
    menuManager.setRemoveAllWhenShown(true);
    // create the context menu dynamically to allow for contributions
    menuManager.addMenuListener(new IMenuListener() {
      public void menuAboutToShow(IMenuManager menuManager) {
        fillContextMenu(menuManager);
      }
    });
    
    Control tree= treeViewer.getTree();
    Menu menu = menuManager.createContextMenu(tree);
    tree.setMenu(menu);
    getSite().registerContextMenu(menuManager, treeViewer);


    configureDatabase();
    initTreeViewerInput();
    updateTitle();
    updateColumnTexts();
    // register as selection provider in order for the handler
    // enablement/disablement to work
  }

  private void activateContext() {
    IContextService contextService = (IContextService) getSite().getService(IContextService.class);
    contextService.activateContext("com.netcetera.trema.eclipse.contexts.tremaContext");
  }

  /**
   * Initializes the table tree viewer input. In case of an error while building
   * the database from the source, a switch to the sourceview is done.
   */
  private void initTreeViewerInput() {
    try {
      updateTableTreeFromTextEditor();
      treeViewer.setSelection(new StructuredSelection(db));
    } catch (ParseException e) {
      setActivePage(sourcePageIndex);
    }
  }

  /** Configures the database. */
  private void configureDatabase() {
    db.addListener(contentProvider);
  }

  /** Creates the table tree view page. */
  private void createTableTreeViewerPage() {
    Control page = createTableTreeViewer();

    tableTreeViewerPageIndex = addPage(page);
    setPageText(tableTreeViewerPageIndex, "Table Tree View");
  }

  /**
   * Creates the table tree viewer. This is the only time the table tree viewer
   * object is instantiated.
   */
  private Control createTableTreeViewer() {
    Composite parent= new Composite(getContainer(), SWT.None);    
    parent.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
    parent.setLayout(new GridLayout());
	  
    PatternFilter filter = new TremaTreePatternFilter();
    filter.setIncludeLeadingWildcard(true);
    
    Composite toolbarContainer= new Composite(parent, SWT.BORDER);
    toolbarContainer.setLayout(new FillLayout());
    toolbarContainer.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    
    ToolBarManager toolbarManager = new ToolBarManager(SWT.FLAT | SWT.RIGHT);
    toolbarManager.createControl(toolbarContainer);

    //SWT.MULTI and FocusCellOwnerDrawHighlighter dont work together nicely, see
    //https://bugs.eclipse.org/bugs/show_bug.cgi?id=206692
    //https://bugs.eclipse.org/bugs/show_bug.cgi?id=268135
    //A patched version of FocusCellOwnerDrawHighlighter is used, which can probably be removed, as soon as there is a better solution 
    FilteredTree tree = new FilteredTree(parent, SWT.FULL_SELECTION | SWT.BORDER | SWT.MULTI, filter, true);
    tree.setLayoutData(new GridData(GridData.FILL_BOTH));
    
    treeViewer = tree.getViewer();
    //treeViewer = new TreeViewer(getContainer(), SWT.FULL_SELECTION | SWT.MULTI);
    
    configureTable(treeViewer); 
    addTableTreeViewerListeners(treeViewer);
    setCellEditorsAndCellModifier(treeViewer);
    
    // content provider and label provider
    contentProvider = new TremaContentProvider(this);
    ColumnViewerToolTipSupport.enableFor(treeViewer);
    treeViewer.setContentProvider(contentProvider);
    //treeViewer.setLabelProvider(new TremaLabelProvider()); 
    
    // various properties
    treeViewer.setAutoExpandLevel(2);
    treeViewer.setUseHashlookup(true);
    getSite().setSelectionProvider(treeViewer);
    createActions(treeViewer);
    createToolbarManager(toolbarManager);
    return parent;
  }

  private void createToolbarManager(ToolBarManager toolbarManager) {
    toolbarManager.add(getAction(TremaEditorActionConstants.EXPAND));
    toolbarManager.add(getAction(TremaEditorActionConstants.COLLAPSE));
    toolbarManager.add(new Separator());
    toolbarManager.add(getAction(TremaEditorActionConstants.EDIT));
    toolbarManager.add(getAction(TremaEditorActionConstants.ADD_VALUE_NODE));
    toolbarManager.add(getAction(TremaEditorActionConstants.MOVE_UP));
    toolbarManager.add(getAction(TremaEditorActionConstants.MOVE_DOWN));
    toolbarManager.add(getAction(TremaEditorActionConstants.REMOVE));
    toolbarManager.add(new Separator());
    toolbarManager.add(getAction(TremaEditorActionConstants.ADD_TEXT_NODE));
    toolbarManager.add(getAction(TremaEditorActionConstants.IMPORT));
    toolbarManager.add(getAction(TremaEditorActionConstants.EXPORT));
    toolbarManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
    toolbarManager.update(true);
  }

  /**
   * Configures the table of the table tree viewer.
   * 
   * @param treeViewer
   */
  private void configureTable(TreeViewer treeViewer) {
    Tree tree = treeViewer.getTree();
    tree.setHeaderVisible(true);
    tree.setLinesVisible(true);


    TreeViewerColumn firstViewerColumn = new TreeViewerColumn(treeViewer, SWT.LEFT);
    firstViewerColumn.setLabelProvider(new TremaLabelProvider());
    firstColumn = firstViewerColumn.getColumn();
    // add the columns and set the column properties
    firstColumn.setText("Key");

    TreeViewerColumn secondViewerColumn = new TreeViewerColumn(treeViewer, SWT.LEFT);
    secondViewerColumn.setLabelProvider(new TremaLabelProvider());
    secondColumn = secondViewerColumn.getColumn();
    secondColumn.setText("Context");

    TreeViewerColumn thirdViewerColumn = new TreeViewerColumn(treeViewer, SWT.LEFT);
    thirdViewerColumn.setLabelProvider(new TremaLabelProvider());
    thirdColumn = thirdViewerColumn.getColumn();
    thirdColumn.setText("");
    thirdColumn.setResizable(false);

    TableLayout tableLayout = new TableLayout();
    tableLayout.addColumnData(new ColumnWeightData(30, true));
    tableLayout.addColumnData(new ColumnWeightData(70, true));
    tableLayout.addColumnData(new ColumnPixelData(90, false));
    tree.setLayout(tableLayout);

    treeViewer.setColumnProperties(new String[]{PROP_FIRST_COLUMN, PROP_SECOND_COLUMN,
        PROP_THIRD_COLUMN});

    // code below defines how cell editing is triggered, eg. by navigating into
    // a table cell with tabs etc.

    focusCellManager = new TreeViewerFocusCellManager(treeViewer,
        new FocusCellOwnerDrawHighlighterForMultiselection(treeViewer));

    ColumnViewerEditorActivationStrategy actSupport = new ColumnViewerEditorActivationStrategy(
        treeViewer) {

      @Override
      protected boolean isEditorActivationEvent(ColumnViewerEditorActivationEvent event) {
        // Editing mode is activated when traversing with Tab (while already
        // editing), left mouse click into the cell, enter key or program
        return event.eventType == ColumnViewerEditorActivationEvent.TRAVERSAL
            || (event.eventType == ColumnViewerEditorActivationEvent.MOUSE_CLICK_SELECTION && ((MouseEvent) event.sourceEvent).button == 1)
            || (event.eventType == ColumnViewerEditorActivationEvent.KEY_PRESSED && event.keyCode == SWT.CR)
            || event.eventType == ColumnViewerEditorActivationEvent.PROGRAMMATIC;
      }
    };

    TreeViewerEditor.create(treeViewer, focusCellManager, actSupport,
        ColumnViewerEditor.TABBING_HORIZONTAL | ColumnViewerEditor.TABBING_MOVE_TO_ROW_NEIGHBOR
            | ColumnViewerEditor.TABBING_VERTICAL | ColumnViewerEditor.KEYBOARD_ACTIVATION);


  }

  /** Hooks up event listeners to the table tree viewer. */
  private void addTableTreeViewerListeners(final TreeViewer treeViewer) {
    treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {

      public void selectionChanged(SelectionChangedEvent event) {
        if (event.getSource() == treeViewer) {
          updateColumnTexts();
          updateActionEnablements();
          updateCellEditorValidators();
        }
      }
    });
  }

  /**
   * Sets the cell editors and the cell modifier of the table tree viewer.
   * 
   * @param treeViewer
   */
  private void setCellEditorsAndCellModifier(TreeViewer treeViewer) {
    secondColumnCellEditor = new TextCellEditor(treeViewer.getTree());
    secondColumnCellEditor.addListener(new ICellEditorListener() {

      // listener displaying error messages in the status bar
      public void applyEditorValue() {
        setErrorMessage(null);
      }

      public void cancelEditor() {
        setErrorMessage(null);
      }

      public void editorValueChanged(boolean oldValidState, boolean newValidState) {
        setErrorMessage(secondColumnCellEditor.getErrorMessage());
      }

      private void setErrorMessage(String errorMessage) {
        getEditorSite().getActionBars().getStatusLineManager().setErrorMessage(errorMessage);
      }
    });

    // the validator will be added dynamically if the master language is going
    // to be edited
    masterLanguageValidator = new LanguageValidator();

    ComboBoxCellEditor statusEditor = new ComboBoxCellEditor(treeViewer.getTree(),
        Status.getAvailableStatusNames(), SWT.READ_ONLY);
    treeViewer.setCellEditors(new CellEditor[]{new TextCellEditor(treeViewer.getTree()),
        secondColumnCellEditor, statusEditor});

    treeViewer.setCellModifier(new TremaCellModifier());
  }

  /** Creates the Trema editor actions used by the table tree viewer page. */
  @SuppressWarnings("deprecation")
  private void createActions(TreeViewer treeViewer) {

    // getEditorSite().getKeyBindingService().registerAction(action) needs to be
    // called in order for the enablement / disablement to work in the toolbar

    // edit action
    ImageDescriptor image = TremaPlugin.getDefault().getImageDescriptor("icons/edit.gif");
    TremaEditorAction action = new EditAction(treeViewer, "&Edit...", image);
    registerAction(TremaEditorActionConstants.EDIT, action);
    getEditorSite().getKeyBindingService().registerAction(action);

    // remove action
    image = PlatformUI.getWorkbench()
        .getSharedImages()
        .getImageDescriptor(ISharedImages.IMG_TOOL_DELETE);
    action = new RemoveAction(treeViewer, "&Remove", image);
    registerAction(TremaEditorActionConstants.REMOVE, action);

    // getEditorSite().getKeyBindingService().registerAction(action);
    IHandlerService handlerService = (IHandlerService) getSite().getService(IHandlerService.class);
    handlerService.activateHandler(action.getActionDefinitionId(), new ActionHandler(action));


    // add text node action
    image = TremaPlugin.getDefault().getImageDescriptor("icons/add.gif");
    action = new AddTextNodeAction(treeViewer, "Add &Text Node...", image);
    registerAction(TremaEditorActionConstants.ADD_TEXT_NODE, action);
    getEditorSite().getKeyBindingService().registerAction(action);

    // add value node action
    image = TremaPlugin.getDefault().getImageDescriptor("icons/add.gif");
    action = new AddValueNodeAction(treeViewer, "Add &Value Node...", image);
    registerAction(TremaEditorActionConstants.ADD_VALUE_NODE, action);
    getEditorSite().getKeyBindingService().registerAction(action);

    // move up action
    image = TremaPlugin.getDefault().getImageDescriptor("icons/up.gif");
    action = new MoveUpAction(treeViewer, "Move &Up", image);
    registerAction(TremaEditorActionConstants.MOVE_UP, action);
    getEditorSite().getKeyBindingService().registerAction(action);

    // move down action
    image = TremaPlugin.getDefault().getImageDescriptor("icons/down.gif");
    action = new MoveDownAction(treeViewer, "Move &Down", image);
    registerAction(TremaEditorActionConstants.MOVE_DOWN, action);
    getEditorSite().getKeyBindingService().registerAction(action);

    // select all action
    action = new SelectAllAction(treeViewer, "Select &all", null);
    registerAction(TremaEditorActionConstants.SELECT_ALL, action);

    // expand action
    image = TremaPlugin.getDefault().getImageDescriptor("icons/expand.gif");
    action = new ExpandCollapseAction(treeViewer, "E&xpand", image, true);
    registerAction(TremaEditorActionConstants.EXPAND, action);
    getEditorSite().getKeyBindingService().registerAction(action);

    // collapse action
    image = TremaPlugin.getDefault().getImageDescriptor("icons/collapse.gif");
    action = new ExpandCollapseAction(treeViewer, "&Collapse", image, false);
    registerAction(TremaEditorActionConstants.COLLAPSE, action);
    getEditorSite().getKeyBindingService().registerAction(action);

    // import action
    image = TremaPlugin.getDefault().getImageDescriptor("icons/import.gif");
    action = new ImportAction(getEditorSite().getWorkbenchWindow(), this, "&Import...", image);
    registerAction(TremaEditorActionConstants.IMPORT, action);
    getEditorSite().getKeyBindingService().registerAction(action);

    // export action
    image = TremaPlugin.getDefault().getImageDescriptor("icons/export.gif");
    action = new ExportAction(getEditorSite().getWorkbenchWindow(), this, "&Export...", image);
    registerAction(TremaEditorActionConstants.EXPORT, action);
    getEditorSite().getKeyBindingService().registerAction(action);

  }

  /**
   * Updates the column text headers according to the current selection.
   */
  protected final void updateColumnTexts() {
    int result = TremaUtilEclipse.analyzeSelection((IStructuredSelection) treeViewer.getSelection());

    if (TremaUtilEclipse.hasJustDatabases(result)) {
      firstColumn.setText("");
      secondColumn.setText("Master Language");
      thirdColumn.setText("");
    } else if (TremaUtilEclipse.hasJustTextNodes(result)) {
      firstColumn.setText("Key");
      secondColumn.setText("Context");
      thirdColumn.setText("");
    } else if (TremaUtilEclipse.hasJustValueNodes(result)) {
      firstColumn.setText("Language");
      secondColumn.setText("Value");
      thirdColumn.setText("Status");
    } else if (TremaUtilEclipse.hasJustDatabasesAndTextNodes(result)) {
      firstColumn.setText("Key");
      secondColumn.setText("Master Language / Context");
      thirdColumn.setText("");
    } else if (TremaUtilEclipse.hasJustDatabasesAndValueNodes(result)) {
      firstColumn.setText("Language");
      secondColumn.setText("Master Language / Value");
      thirdColumn.setText("Status");
    } else if (TremaUtilEclipse.hasJustTextAndValueNodes(result)) {
      firstColumn.setText("Key / Language");
      secondColumn.setText("Context / Value");
      thirdColumn.setText("Status");
    } else {
      firstColumn.setText("Key / Language");
      secondColumn.setText("Master Language / Context / Value");
      thirdColumn.setText("Status");
    }
  }

  /**
   * Sets the enablements of the registered Trema editor actions according to
   * the current selection.
   */
  protected void updateActionEnablements() {
    IStructuredSelection selection = (IStructuredSelection) treeViewer.getSelection();
    int analysisResult = TremaUtilEclipse.analyzeSelection(selection);
    Iterator<TremaEditorAction> iterator = actionMap.values().iterator();
    while (iterator.hasNext()) {
      iterator.next().updateEnablement(selection, analysisResult);
    }
    getEditorSite().getActionBars().getToolBarManager().update(true);
  }

  /** Disables all registered Trema editor actions. */
  protected void disableActions() {
    Iterator<TremaEditorAction> iterator = actionMap.values().iterator();
    while (iterator.hasNext()) {
      TremaEditorAction action = iterator.next();
      action.setEnabled(false);
    }
  }

  /**
   * Adds the cell editor validators depending on the current selection.
   */
  protected void updateCellEditorValidators() {
    IStructuredSelection selection = (IStructuredSelection) treeViewer.getSelection();
    int result = TremaUtilEclipse.analyzeSelection(selection);

    // add a LanguageValidator only if editing the master language
    if (TremaUtilEclipse.hasJustDatabases(result)) {
      secondColumnCellEditor.setValidator(masterLanguageValidator);
    } else {
      secondColumnCellEditor.setValidator(null);
    }
  }

  /** Creates the source view page with a nested text editor. */
  private void createSourcePage() {
    try {
      textEditor = new XMLEditor();
      sourcePageIndex = addPage(textEditor, getEditorInput());
      setPageText(sourcePageIndex, "Source");
      IEditorInput editorInput = textEditor.getEditorInput();
      // fixme: YT 2004-09-16: ...getDocument(editorInput) returns null for a
      // JavaFileEditorInput
      textEditor.getDocumentProvider()
          .getDocument(editorInput)
          .addDocumentListener(new IDocumentListener() {

            public void documentAboutToBeChanged(DocumentEvent event) {
              // nothing to do
            }

            public void documentChanged(DocumentEvent event) {
              pagesOutOfSync = true; // set the flag
            }
          });
    } catch (PartInitException e) {
      TremaPlugin.logError(e);
    }
  }

  /**
   * This method should be called by the event listeners if any change is made
   * to the table tree.
   */
  public void tableTreeModified() {
    tableTreeModified(false);
  }

  /**
   * This method should be called by the event listeners if any change is made
   * to the table tree.
   * 
   * @param sizeChanged true if the size of the database was changed
   */
  public void tableTreeModified(boolean sizeChanged) {
    tableTreeModified = true;
    pagesOutOfSync = true;
    // fire a property change event in case only the table tree has
    // changed (changes in the source page already trigger such an
    // event)
    if (!super.isDirty()) {
      firePropertyChange(IEditorPart.PROP_DIRTY);
    }
    updateActionEnablements();
    if (sizeChanged) {
      updateStatusField(STATUS_FIELD_SIZE_KEY);
    }
  }

  /** {@inheritDoc} */
  @Override
  public boolean isDirty() {
    return tableTreeModified || super.isDirty();
  }

  /** Updates the title to be displayed in the editor tab. */
  private void updateTitle() {
    IEditorInput input = getEditorInput();
    setPartName(input.getName());
    setTitleToolTip(input.getToolTipText());
  }

  /**
   * Updates the database model and consequently the table tree page from the
   * source page by re-parsing the XML code.
   * 
   * @throws ParseException if any parse errors occur during parsing
   */
  private void updateTableTreeFromTextEditor() throws ParseException {
    // note that the text file encoding is automatically recognized by
    // the eclipse core for *.xml and *.trm files since they are
    // associated with the "org.eclipse.core.runtime.xml" content type
    String editorInputText = textEditor.getDocumentProvider()
        .getDocument(textEditor.getEditorInput())
        .get();
    try {
      deleteMarkers();
      ((XMLDatabase) db).build(editorInputText, false);
      pagesOutOfSync = false;
      treeViewer.setInput(new DatabaseContainer(db));
      addWarningMarkers(((XMLDatabase) db).getParseWarnings());
    } catch (ParseException e) {
      treeViewer.setInput(null);
      addMarker(e.getMessage(), e.getLineNumber(), IMarker.SEVERITY_ERROR);
      TremaPlugin.logError(e);
      throw e;
    } catch (IOException e) {
      treeViewer.setInput(null);
      // IOException is serious and will trigger plattfrom error handling
      TremaPlugin.logError(e);
      throw new RuntimeException(e);
    }
  }

  /**
   * Updates the source page from the table tree page by generating XML code
   * from the database model.
   */
  private void updateTextEditorFromTableTree() {
    // only update the textEditor if the treeEditor contains valid data
    if (!((XMLDatabase) db).isXmlInternalized()) {
      return;
    }
    String encoding = null;
    try {
      IEditorInput editorInput = textEditor.getEditorInput();
      if (editorInput instanceof IFileEditorInput) {
        encoding = ((IFileEditorInput) textEditor.getEditorInput()).getFile().getCharset();
      } else if (editorInput instanceof FileStoreEditorInput) {
        encoding = "UTF-8";
      }
    } catch (CoreException e) {
      TremaPlugin.logError(e.getMessage());
      encoding = "UTF-8";
    }

    String lineSeparator = TremaUtil.getDefaultLineSeparator(); // use the
                                                                // default
                                                                // platform line
                                                                // separator

    StringWriter stringWriter = new StringWriter();
    try {
      ((XMLDatabase) db).writeXML(stringWriter, encoding, "  ", lineSeparator);
      textEditor.getDocumentProvider()
          .getDocument(textEditor.getEditorInput())
          .set(stringWriter.toString());
      pagesOutOfSync = false;
    } catch (IOException e) {
      TremaPlugin.logError(e.getMessage());
    }
  }

  private void deleteMarkers() {
    try {
      getInputFile().deleteMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE);
    } catch (CoreException e1) {
      throw new RuntimeException(e1);
    }
  }

  /**
   * Adds the ParseWarnings as Markers in the Editor.
   * 
   * @param warnings the warnings to add
   */
  private void addWarningMarkers(ParseWarning[] warnings) {
    for (ParseWarning w : warnings) {
      addMarker(w.getMessage(), w.getLineNumber(), IMarker.SEVERITY_WARNING);
    }
  }

  /**
   * Convenience method to add a marker to the editor.
   * 
   * @param message the message
   * @param linenumber the linemarker to add the marker to
   * @param severity the severity
   */
  protected void addMarker(String message, int linenumber, int severity) {
    Map<Object, Object> map = new HashMap<Object, Object>();
    int displayedLineNumber = linenumber;
    if (displayedLineNumber == 0) {
      // marker on line 0 cannot be seen.
      displayedLineNumber = 1;
    }
    MarkerUtilities.setLineNumber(map, displayedLineNumber);
    MarkerUtilities.setMessage(map, message);
    map.put(IMarker.MESSAGE, message);
    map.put(IMarker.SEVERITY, Integer.valueOf(severity));
    try {
      MarkerUtilities.createMarker(getInputFile(), map, IMarker.PROBLEM);
    } catch (CoreException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * @return the Editor input file
   */
  protected IFile getInputFile() {
    IFileEditorInput ife = (IFileEditorInput) textEditor.getEditorInput();
    if (ife != null) {
      return ife.getFile();
    } else {
      return null;
    }
  }

  @Override
  protected void setActivePage(int pageIndex) {
    if (pageIndex != getActivePage()) {
      super.setActivePage(pageIndex);
    }
  }

  /** {@inheritDoc} */
  @Override
  protected void pageChange(int newPageIndex) {
    if (newPageIndex == tableTreeViewerPageIndex) {
      updateActionEnablements();
      if (pagesOutOfSync || !isDirty()) {
        try {
          // if the source page contains errors, don't change the page
          updateTableTreeFromTextEditor();
          if (expandedElements != null) {
            treeViewer.setExpandedElements(expandedElements);
          }
        } catch (ParseException e) {
          setActivePage(sourcePageIndex);
          return; // if the source page contains parse errorr, don't change the
                  // page
        }
      }
    } else if (newPageIndex == sourcePageIndex) {
      disableActions(); // the table tree viewer actions should not be runnable
                        // from within the source page
      expandedElements = treeViewer.getExpandedElements();
      if (pagesOutOfSync) {
        updateTextEditorFromTableTree();
      }
    }

    super.pageChange(newPageIndex);

    // since our contributor is not an instance of
    // MultiPageEditorActionContributor,
    // the pageChange method of the superclass will not call any contributor
    // method
    IEditorActionBarContributor contributor = getEditorSite().getActionBarContributor();
    if (contributor instanceof TremaEditorContributor) {
      ((TremaEditorContributor) contributor).setActivePage(this, newPageIndex);
    }
  }

  /**
   * Registeres a status field with a given key.
   * 
   * @param key the key of the status field
   * @param statusField the status field
   */
  public void registerStatusField(String key, IStatusField statusField) {
    if (statusField == null) {
      statusFieldMap.remove(key);
    } else {
      statusFieldMap.put(key, statusField);
    }
  }

  /**
   * Updates a status field with a given key.
   * 
   * @param key the key for the status field to update
   */
  public void updateStatusField(String key) {
    if (!statusFieldMap.containsKey(key)) {
      return;
    }
    IStatusField statusField = statusFieldMap.get(key);
    if (key.equals(STATUS_FIELD_SIZE_KEY)) {
      int dbSize = db.getSize();
      statusField.setText(dbSize + ((dbSize == 1) ? " Record" : " Records"));
    }
  }

  /** {@inheritDoc} */
  @Override
  public void doSave(IProgressMonitor monitor) {
    // possibly update the text editor
    if (getActivePage() == tableTreeViewerPageIndex && pagesOutOfSync) {
      updateTextEditorFromTableTree();
    }
    tableTreeModified = false;
    // delegate to the text editor's doSave method
    textEditor.doSave(monitor);
    // if the user saved from the source page, the tree viewer model needs to be
    // updated now
    if (getActivePage() == sourcePageIndex) {
      try {
        // make sure that the tree viewer is in sync with the text editor after
        // saving
        initTreeViewerInput();
      } catch (RuntimeException e) {
        // ignore, it is ok to save, even if the xml/trm file that is invalid.
      }
    }
  }

  /** {@inheritDoc} */
  @Override
  public void doSaveAs() {
    // possibly update the text editor
    if (getActivePage() == tableTreeViewerPageIndex && pagesOutOfSync) {
      updateTextEditorFromTableTree();
    }
    tableTreeModified = false;
    // delegate to the text editor's doSaveAs method
    textEditor.doSaveAs();
    setInput(textEditor.getEditorInput());
    updateTitle();
    // if the user saved from the source page, the tree viewer model needs to be
    // updated now
    if (getActivePage() == sourcePageIndex) {
      try {
        // make sure that the tree viewer is in sync with the text editor after
        // saving
        initTreeViewerInput();
      } catch (RuntimeException e) {
        // ignore, it is ok to save, even if the xml/trm file that is invalid.
      }
    }
  }

  /** {@inheritDoc} */
  @Override
  public boolean isSaveAsAllowed() {
    return true;
  }

  /** {@inheritDoc} */
  public void gotoMarker(IMarker marker) {
    setActivePage(sourcePageIndex);
    IGotoMarker gotoMarker = (IGotoMarker) textEditor.getAdapter(IGotoMarker.class);
    if (gotoMarker != null) {
      gotoMarker.gotoMarker(marker);
    }
  }

  /**
   * Gets the index of the table tree viewer page.
   * 
   * @return the index of the table tree viewer page.
   */
  public int getTableTreeViewerPageIndex() {
    return tableTreeViewerPageIndex;
  }

  /**
   * Gets the index of the source page.
   * 
   * @return the index of the source page.
   */
  public int getSourcePageIndex() {
    return sourcePageIndex;
  }

  /**
   * Registers a trema editor action.
   * 
   * @param id the action id
   * @param action the action
   */
  public void registerAction(String id, TremaEditorAction action) {
    actionMap.put(id, action);
  }

  /**
   * Gets a trema editor action to a given id.
   * 
   * @param id the action id
   * @return the action or <code>null</code> if none found
   */
  public TremaEditorAction getAction(String id) {
    return actionMap.get(id);
  }

  /** {@inheritDoc} */
  @Override
  public int getActivePage() { // to increase the visibility
    return super.getActivePage();
  }

  /**
   * Gets the text editor from the source page.
   * 
   * @return the text editor from the source page.
   */
  public TextEditor getTextEditor() {
    return textEditor;
  }

  /**
   * Gets the table tree viewer from the table tree viewer page.
   * 
   * @return the table tree viewer from the table tree viewer page.
   */
  public TreeViewer getTreeViewer() {
    return treeViewer;
  }

  private void fillContextMenu(IMenuManager menuManager) {
    menuManager.add(getAction(TremaEditorActionConstants.EXPAND));
    menuManager.add(getAction(TremaEditorActionConstants.COLLAPSE));
    menuManager.add(new Separator());
    menuManager.add(getAction(TremaEditorActionConstants.EDIT));
    menuManager.add(getAction(TremaEditorActionConstants.ADD_VALUE_NODE));
    menuManager.add(getAction(TremaEditorActionConstants.MOVE_UP));
    menuManager.add(getAction(TremaEditorActionConstants.MOVE_DOWN));
    menuManager.add(getAction(TremaEditorActionConstants.REMOVE));
    menuManager.add(new Separator());
    menuManager.add(getAction(TremaEditorActionConstants.ADD_TEXT_NODE));
    menuManager.add(getAction(TremaEditorActionConstants.IMPORT));
    menuManager.add(getAction(TremaEditorActionConstants.EXPORT));
    menuManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
  }

}
