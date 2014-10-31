package com.netcetera.trema.eclipse.editors.xmleditor;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WhitespaceRule;



/** 
 * The classes of this package are based on the Eclipse PDE sample XML
 * text editor project.
 */
public class XMLScanner extends RuleBasedScanner {
  
  /**
   * Constructor.
   * 
   * @param manager the color manager to be used
   */
  public XMLScanner(ColorManager manager) {
    IToken procInstr =
      new Token(
                new TextAttribute(
                                  manager.getColor(IXMLColorConstants.PROC_INSTR)));
    
    IRule[] rules = new IRule[2];
    //Add rule for processing instructions
    rules[0] = new SingleLineRule("<?", "?>", procInstr);
    // Add generic whitespace rule.
    rules[1] = new WhitespaceRule(new XMLWhitespaceDetector());
    
    setRules(rules);
  }
}
