package com.netcetera.trema.eclipse.editors.xmleditor;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.MultiLineRule;



/** 
 * The classes of this package are based on the Eclipse PDE sample XML
 * text editor project.
 */
public class TagRule extends MultiLineRule {
  
  /**
   * Constructor.
   * 
   * @param token the token
   */
  public TagRule(IToken token) {
    super("<", ">", token);
  }
  /** {@inheritDoc} */
  protected boolean sequenceDetected(
                                     ICharacterScanner scanner,
                                     char[] sequence,
                                     boolean eofAllowed) {
    int c = scanner.read();
    if (sequence[0] == '<') {
      if (c == '?') {
        // processing instruction - abort
        scanner.unread();
        return false;
      }
      if (c == '!') {
        scanner.unread();
        // comment - abort
        return false;
      }
    } else if (sequence[0] == '>') {
      scanner.unread();
    }
    return super.sequenceDetected(scanner, sequence, eofAllowed);
  }
}
