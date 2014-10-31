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
public class XMLTagScanner extends RuleBasedScanner {
  
  /**
   * Constructor.
   * 
   * @param manager the color manager 
   */
  public XMLTagScanner(ColorManager manager) {
    IToken string =
      new Token(
                new TextAttribute(manager.getColor(IXMLColorConstants.STRING)));
    
    IRule[] rules = new IRule[3];
    
    // Add rule for double quotes
    rules[0] = new SingleLineRule("\"", "\"", string, '\\');
    // Add a rule for single quotes
    rules[1] = new SingleLineRule("'", "'", string, '\\');
    // Add generic whitespace rule.
    rules[2] = new WhitespaceRule(new XMLWhitespaceDetector());
    
    setRules(rules);
  }
}
