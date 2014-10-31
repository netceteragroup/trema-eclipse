package com.netcetera.trema.eclipse.editors;

import org.eclipse.ui.actions.ActionFactory;



/**
 * Class holding constants only.
 */
public final class TremaEditorActionConstants {
  
  // ==================================================================
  //  global actions
  // ==================================================================
  
  /** Comment for <code>REMOVE</code>. */
  public static final String REMOVE = ActionFactory.DELETE.getId();
  
  /** Comment for <code>SELECT_ALL</code>. */
  public static final String SELECT_ALL = ActionFactory.SELECT_ALL.getId();

  // ==================================================================
  //  local actions
  // ==================================================================
  
  /** Comment for <code>EDIT</code>. */
  public static final String EDIT = "edit";

  /** Comment for <code>ADD_TEXT_NODE</code>. */
  public static final String ADD_TEXT_NODE = "addTextNode";

  /** Comment for <code>ADD_VALUE_NODE</code>. */
  public static final String ADD_VALUE_NODE = "addValueNode";
  
  /** Comment for <code>MOVE_UP</code>. */
  public static final String MOVE_UP = "moveUp";

  /** Comment for <code>MOVE_DOWN</code>. */
  public static final String MOVE_DOWN = "moveDown";

  /** Comment for <code>EXPAND</code>. */
  public static final String EXPAND = "expand";

  /** Comment for <code>COLLAPSE</code>. */
  public static final String COLLAPSE = "collapse";
  
  /** Comment for <code>IMPORT</code>. */
  public static final String IMPORT = "import";
  
  /** Comment for <code>EXPORT</code>. */
  public static final String EXPORT = "export";
  
  private TremaEditorActionConstants() {
    
  }
  
}
