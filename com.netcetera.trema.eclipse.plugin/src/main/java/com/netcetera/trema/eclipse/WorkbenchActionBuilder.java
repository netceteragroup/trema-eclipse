package com.netcetera.trema.eclipse;



import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarContributionItem;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.actions.ContributionItemFactory;
import org.eclipse.ui.application.IActionBarConfigurer;


/**
 * Adds actions to a workbench window.
 */
public final class WorkbenchActionBuilder {
  private IWorkbenchWindow window;
  
  /** 
   * A convince variable and method so that the actionConfigurer
   * does not need to get passed into registerGlobalAction every time
   * it is called.
   */
  private IActionBarConfigurer actionBarConfigurer = null;
  
  // generic actions
  private IWorkbenchAction closeAction = null;
  private IWorkbenchAction closeAllAction = null;
  private IWorkbenchAction closeAllSavedAction = null;
  private IWorkbenchAction saveAction = null;
  private IWorkbenchAction saveAllAction = null;
  private IWorkbenchAction saveAsAction = null;
  private IWorkbenchAction newAction = null;
  
  // generic retarget actions
  private IWorkbenchAction undoAction = null;
  private IWorkbenchAction redoAction = null;
  private IWorkbenchAction cutAction = null;
  private IWorkbenchAction copyAction = null;
  private IWorkbenchAction pasteAction = null;
  private IWorkbenchAction selectAllAction = null;
  private IWorkbenchAction findAction = null;
  private IWorkbenchAction revertAction = null;
  private IWorkbenchAction quitAction = null;
  
  /**
   * Constructs a new action builder which contributes actions
   * to the given window.
   * @param window the window
   */
  public WorkbenchActionBuilder(IWorkbenchWindow window) {
    this.window = window;
  }
  
  /**
   * Gets the window this action builder is contributing to.
   * @return  the window this action builder is contributing to.
   */
  private IWorkbenchWindow getWindow() {
    return window;
  }
  
  /**
   * Builds the actions and contributes them to the given window.
   * @param actionBarConfigurer the action bar configurer
   */
  public void makeAndPopulateActions(IActionBarConfigurer actionBarConfigurer) {
    makeActions(actionBarConfigurer);
    populateMenuBar(actionBarConfigurer);
    populateCoolBar(actionBarConfigurer);
  }
  
  /**
   * Fills the coolbar with the workbench actions.
   * @param configurer
   */
  private void populateCoolBar(IActionBarConfigurer configurer) {
    ICoolBarManager coolBarManager = configurer.getCoolBarManager();
    
    coolBarManager.add(new GroupMarker("group.file"));
    
    // file Group
    IToolBarManager fileToolBar = new ToolBarManager(coolBarManager.getStyle());
    fileToolBar.add(new Separator(IWorkbenchActionConstants.NEW_GROUP));
    fileToolBar.add(new GroupMarker(IWorkbenchActionConstants.SAVE_GROUP));
    fileToolBar.add(saveAction);
    
    fileToolBar.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
    
    // add to the cool bar manager
    coolBarManager.add(new ToolBarContributionItem(fileToolBar, IWorkbenchActionConstants.TOOLBAR_FILE));
    coolBarManager.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
    coolBarManager.add(new GroupMarker(IWorkbenchActionConstants.GROUP_EDITOR));
  }
  
  /**
   * Fills the menu bar with the workbench actions.
   * @param configurer the action bar configurer
   */
  public void populateMenuBar(IActionBarConfigurer configurer) {
    IMenuManager menubar = configurer.getMenuManager();
    menubar.add(createFileMenu());
    menubar.add(createEditMenu());
    menubar.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
  }
  
  /**
   * Creates and returns the file menu.
   */
  private MenuManager createFileMenu() {
    MenuManager menu = new MenuManager("&File", IWorkbenchActionConstants.M_FILE);
    menu.add(new GroupMarker(IWorkbenchActionConstants.FILE_START));
    menu.add(newAction);
    menu.add(new GroupMarker(IWorkbenchActionConstants.NEW_EXT));
    menu.add(closeAction);
    menu.add(closeAllAction);
    menu.add(new GroupMarker(IWorkbenchActionConstants.CLOSE_EXT));
    menu.add(new Separator());
    menu.add(saveAction);
    menu.add(saveAsAction);
    menu.add(saveAllAction);
    
    menu.add(revertAction);
    menu.add(ContributionItemFactory.REOPEN_EDITORS.create(getWindow()));
    menu.add(new GroupMarker(IWorkbenchActionConstants.MRU));
    menu.add(new Separator());
    menu.add(quitAction);
    menu.add(new GroupMarker(IWorkbenchActionConstants.FILE_END));
    return menu;
  }
  
  /**
   * Creates and returns the Edit menu.
   */
  private MenuManager createEditMenu() {
    MenuManager menu = new MenuManager("&Edit", IWorkbenchActionConstants.M_EDIT);
    menu.add(new GroupMarker(IWorkbenchActionConstants.EDIT_START));
    
    menu.add(undoAction);
    menu.add(redoAction);
    menu.add(new GroupMarker(IWorkbenchActionConstants.UNDO_EXT));
    
    menu.add(cutAction);
    menu.add(copyAction);
    menu.add(pasteAction);
    menu.add(new GroupMarker(IWorkbenchActionConstants.CUT_EXT));
    
    menu.add(selectAllAction);
    menu.add(new Separator());
    
    menu.add(findAction);
    menu.add(new GroupMarker(IWorkbenchActionConstants.FIND_EXT));
    
    menu.add(new GroupMarker(IWorkbenchActionConstants.ADD_EXT));
    
    menu.add(new GroupMarker(IWorkbenchActionConstants.EDIT_END));
    menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
    return menu;
  }
  
  /**
   * Disposes any resources and unhooks any listeners that are no longer needed.
   * Called when the window is closed.
   */
  public void dispose() {
    newAction.dispose();
    closeAction.dispose();
    closeAllAction.dispose();
    closeAllSavedAction.dispose();
    saveAction.dispose();
    saveAllAction.dispose();
    saveAsAction.dispose();
    redoAction.dispose();
    cutAction.dispose();
    copyAction.dispose();
    pasteAction.dispose();
    selectAllAction.dispose();
    findAction.dispose();
    revertAction.dispose();
    quitAction.dispose();
    
    // null out actions to make leak debugging easier
    closeAction = null;
    closeAllAction = null;
    closeAllSavedAction = null;
    saveAction = null;
    saveAllAction = null;
    saveAsAction = null;
    undoAction = null;
    redoAction = null;
    cutAction = null;
    copyAction = null;
    pasteAction = null;
    selectAllAction = null;
    findAction = null;
    revertAction = null;
    quitAction = null;
  }
  
  /**
   * Creates actions (and contribution items) for the menu bar, toolbar and status line.
   */
  private void makeActions(IActionBarConfigurer actionBarConfigurer) {
    
    // the actions in jface do not have menu vs. enable, vs. disable vs. color
    // there are actions in here being passed the workbench - problem 
    setCurrentActionBarConfigurer(actionBarConfigurer);
    
    newAction = ActionFactory.NEW.create(getWindow());
    
    saveAction = ActionFactory.SAVE.create(getWindow());
    registerGlobalAction(saveAction);
    
    saveAsAction = ActionFactory.SAVE_AS.create(getWindow());
    registerGlobalAction(saveAsAction);
    
    saveAllAction = ActionFactory.SAVE_ALL.create(getWindow());
    registerGlobalAction(saveAllAction);
    
    undoAction = ActionFactory.UNDO.create(getWindow());
    registerGlobalAction(undoAction);
    
    redoAction = ActionFactory.REDO.create(getWindow());
    registerGlobalAction(redoAction);
    
    cutAction = ActionFactory.CUT.create(getWindow());
    registerGlobalAction(cutAction);
    
    copyAction = ActionFactory.COPY.create(getWindow());
    registerGlobalAction(copyAction);
    
    pasteAction = ActionFactory.PASTE.create(getWindow());
    registerGlobalAction(pasteAction);
    
    selectAllAction = ActionFactory.SELECT_ALL.create(getWindow());
    registerGlobalAction(selectAllAction);
    
    findAction = ActionFactory.FIND.create(getWindow());
    registerGlobalAction(findAction);
    
    closeAction = ActionFactory.CLOSE.create(getWindow());
    registerGlobalAction(closeAction);
    
    closeAllAction = ActionFactory.CLOSE_ALL.create(getWindow());
    registerGlobalAction(closeAllAction);
    
    closeAllSavedAction = ActionFactory.CLOSE_ALL_SAVED.create(getWindow());
    registerGlobalAction(closeAllSavedAction);
    
    revertAction = ActionFactory.REVERT.create(getWindow());
    registerGlobalAction(revertAction);
    
    quitAction = ActionFactory.QUIT.create(getWindow());
    registerGlobalAction(quitAction);
  }
  
  private void setCurrentActionBarConfigurer(IActionBarConfigurer actionBarConfigurer) {
    this.actionBarConfigurer = actionBarConfigurer;
  }
  
  private void registerGlobalAction(IAction action) {
    actionBarConfigurer.registerGlobalAction(action);
  }
  
}
