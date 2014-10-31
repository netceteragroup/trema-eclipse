package com.netcetera.trema.eclipse.editors;

import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.SubActionBars;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.editors.text.TextEditorActionContributor;
import org.eclipse.ui.part.EditorActionBarContributor;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.eclipse.ui.texteditor.StatusLineContributionItem;



/** Contributor class for the Trema editor. */
public class TremaEditorContributor extends EditorActionBarContributor  {
  
  /** The global action ids the Trema editor has handlers for. */
  private static final String[] GLOBAL_TREMA_EDITOR_ACTIONS = {
      TremaEditorActionConstants.REMOVE,
      TremaEditorActionConstants.SELECT_ALL
  };
  
  /** The global action ids the text editor has handlers for. */
  private static final String[] GLOBAL_TEXT_EDITOR_ACTIONS = {
      ITextEditorActionConstants.UNDO, 
      ITextEditorActionConstants.REDO,
      ITextEditorActionConstants.CUT,
      ITextEditorActionConstants.COPY,
      ITextEditorActionConstants.PASTE,
      ITextEditorActionConstants.DELETE,
      ITextEditorActionConstants.SELECT_ALL,
      ITextEditorActionConstants.FIND,
      ITextEditorActionConstants.PRINT,
      ITextEditorActionConstants.REVERT
  };
  
  private TextEditorActionContributor sourcePageContributor = null;
  private SubActionBars sourcePageActionBars = null;
  private SubActionBars tableTreeViewPageActionBars = null;
  private StatusLineContributionItem numberOfRecords = null;
  
  /**
   * Default no-args constructor.
   */
  public TremaEditorContributor() {
    sourcePageContributor = new TextEditorActionContributor();
  }
  
  /** {@inheritDoc} */
  public void init(IActionBars bars, IWorkbenchPage page) {
    super.init(bars);
    
    // add the "number of records" status line item
    numberOfRecords = new StatusLineContributionItem("com.netcetera.trema.eclipse.statusLine.numberOfRecords");
    tableTreeViewPageActionBars = new SubActionBars(bars);
    tableTreeViewPageActionBars.getStatusLineManager().add(numberOfRecords);
    
    // delegate the initialization of the source page action bars
    sourcePageActionBars = new SubActionBars(bars);
    sourcePageContributor.init(sourcePageActionBars);    
  }
  
  /** {@inheritDoc} */
  public void setActiveEditor(IEditorPart targetEditor) {
    TremaEditor tremaEditor = (TremaEditor) targetEditor;
    tremaEditor.registerStatusField(TremaEditor.STATUS_FIELD_SIZE_KEY, numberOfRecords);
    setActivePage(tremaEditor, tremaEditor.getActivePage());
  }
  
  /**
   * Hooks up the global action handlers and the status line action
   * bars for a given page index of a Trema editor.
   * @param tremaEditor the Trema editor
   * @param pageIndex the page index
   */
  public void setActivePage(TremaEditor tremaEditor, int pageIndex) {
    IActionBars rootBars = getActionBars();
    if (pageIndex == tremaEditor.getSourcePageIndex()) {
      TextEditor textEditor = tremaEditor.getTextEditor();
      
      // clear all existing Trema editor handlers
      for (int i = 0; i < GLOBAL_TREMA_EDITOR_ACTIONS.length; i++) {
        rootBars.setGlobalActionHandler(GLOBAL_TREMA_EDITOR_ACTIONS[i], null);
      }
      
      // register the text editor handlers
      for (int i = 0; i < GLOBAL_TEXT_EDITOR_ACTIONS.length; i++) {
        rootBars.setGlobalActionHandler(GLOBAL_TEXT_EDITOR_ACTIONS[i],
                                        textEditor.getAction(GLOBAL_TEXT_EDITOR_ACTIONS[i]));
      }
      
      // let the TextEditorActionContributor do its contribution work
      sourcePageContributor.setActiveEditor(tremaEditor.getTextEditor());
      
      // activate the appropriate action bars
      tableTreeViewPageActionBars.deactivate();
      sourcePageActionBars.activate();
    } else if (pageIndex == tremaEditor.getTableTreeViewerPageIndex()) {
      // clear all existing text editor handlers
      for (int i = 0; i < GLOBAL_TEXT_EDITOR_ACTIONS.length; i++) {
        rootBars.setGlobalActionHandler(GLOBAL_TEXT_EDITOR_ACTIONS[i], null);
      }
      
      // register the Trema editor handlers
      for (int i = 0; i < GLOBAL_TREMA_EDITOR_ACTIONS.length; i++) {
        rootBars.setGlobalActionHandler(GLOBAL_TREMA_EDITOR_ACTIONS[i],
                                        tremaEditor.getAction(GLOBAL_TREMA_EDITOR_ACTIONS[i]));
      }

      // update the status field
      tremaEditor.updateStatusField(TremaEditor.STATUS_FIELD_SIZE_KEY);
      
      // activate the appropriate action bars
      tableTreeViewPageActionBars.activate();
      sourcePageActionBars.deactivate();
    }
    rootBars.updateActionBars();
  }

}
