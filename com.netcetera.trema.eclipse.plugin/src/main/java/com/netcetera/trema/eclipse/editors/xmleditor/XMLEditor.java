package com.netcetera.trema.eclipse.editors.xmleditor;

import org.eclipse.ui.editors.text.TextEditor;



/** 
 * The classes of this package are based on the Eclipse PDE sample XML
 * text editor project.
 */
public class XMLEditor extends TextEditor {
  
  private ColorManager colorManager;
  
  /**
   * Constructor.
   */
  public XMLEditor() {
    super();
    colorManager = new ColorManager();
    setSourceViewerConfiguration(new XMLConfiguration(colorManager));
    setDocumentProvider(new XMLDocumentProvider());
  }
  /** {@inheritDoc} */
  public void dispose() {
    colorManager.dispose();
    super.dispose();
  }
  
}
