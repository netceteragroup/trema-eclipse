package com.netcetera.trema.eclipse.editors.xmleditor;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.ITextViewer;



/** 
 * The classes of this package are based on the Eclipse PDE sample XML
 * text editor project.
 */
public class XMLDoubleClickStrategy implements ITextDoubleClickStrategy {
  private ITextViewer fText;
  
  /** {@inheritDoc} */
  public void doubleClicked(ITextViewer part) {
    int pos = part.getSelectedRange().x;
    
    if (pos < 0) {
      return;
    }
    
    fText = part;
    
    if (!selectComment(pos)) {
      selectWord(pos);
    }
  }
  /**
   * Selects a comment.
   * 
   * @param caretPos the caret Pos
   * @return boolean
   */
  protected boolean selectComment(int caretPos) {
    IDocument doc = fText.getDocument();
    int startPos;
    int endPos;
    
    try {
      int pos = caretPos;
      char c = ' ';
      
      while (pos >= 0) {
        c = doc.getChar(pos);
        if (c == '\\') {
          pos -= 2;
          continue;
        }
        if (c == Character.LINE_SEPARATOR || c == '\"') {
          break;
        }
        --pos;
      }
      
      if (c != '\"') {
        return false;
      }
      
      startPos = pos;
      
      pos = caretPos;
      int length = doc.getLength();
      c = ' ';
      
      while (pos < length) {
        c = doc.getChar(pos);
        if (c == Character.LINE_SEPARATOR || c == '\"') {
          break;
        }
        ++pos;
      }
      if (c != '\"') {
        return false;
      }
      endPos = pos;
      
      int offset = startPos + 1;
      int len = endPos - offset;
      fText.setSelectedRange(offset, len);
      return true;
    } catch (BadLocationException x) {
      //ignore
    }
    
    return false;
  }
  /**
   * SelectWord.
   * 
   * @param caretPos the caretPos
   * @return boolean
   */
  protected boolean selectWord(int caretPos) {
    
    IDocument doc = fText.getDocument();
    int startPos;
    int endPos;
    
    try {
      
      int pos = caretPos;
      char c;
      
      while (pos >= 0) {
        c = doc.getChar(pos);
        if (!Character.isJavaIdentifierPart(c)) {
          break;
        }
        --pos;
      }
      
      startPos = pos;
      
      pos = caretPos;
      int length = doc.getLength();
      
      while (pos < length) {
        c = doc.getChar(pos);
        if (!Character.isJavaIdentifierPart(c)) {
          break;
        }
        ++pos;
      }
      
      endPos = pos;
      selectRange(startPos, endPos);
      return true;
      
    } catch (BadLocationException x) {
      //ignore
    }
    
    return false;
  }
  
  private void selectRange(int startPos, int stopPos) {
    int offset = startPos + 1;
    int length = stopPos - offset;
    fText.setSelectedRange(offset, length);
  }
}