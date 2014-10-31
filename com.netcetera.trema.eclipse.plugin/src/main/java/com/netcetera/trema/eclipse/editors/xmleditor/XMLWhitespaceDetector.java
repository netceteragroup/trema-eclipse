package com.netcetera.trema.eclipse.editors.xmleditor;

import org.eclipse.jface.text.rules.IWhitespaceDetector;



/** 
 * The classes of this package are based on the Eclipse PDE sample XML
 * text editor project.
 */
public class XMLWhitespaceDetector implements IWhitespaceDetector {
  
  /** {@inheritDoc} */
  public boolean isWhitespace(char c) {
    return (c == ' ' || c == '\t' || c == '\n' || c == '\r');
  }
  
}
