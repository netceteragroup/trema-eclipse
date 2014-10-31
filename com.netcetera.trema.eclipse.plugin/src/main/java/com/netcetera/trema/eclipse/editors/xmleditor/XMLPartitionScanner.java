package com.netcetera.trema.eclipse.editors.xmleditor;

import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.rules.Token;



/** 
 * The classes of this package are based on the Eclipse PDE sample XML
 * text editor project.
 */
public class XMLPartitionScanner extends RuleBasedPartitionScanner {
  /** <code>XML_DEFAULT</code>. */
  public static final String XML_DEFAULT = "__xml_default";
  /** <code>XML_COMMENT</code>. */
  public static final String XML_COMMENT = "__xml_comment";
  /** <code>XML_TAG</code>. */
  public static final String XML_TAG = "__xml_tag";
  
  /**
   * Contructor.
   */
  public XMLPartitionScanner() {
    
    IToken xmlComment = new Token(XML_COMMENT);
    IToken tag = new Token(XML_TAG);
    
    IPredicateRule[] rules = new IPredicateRule[2];
    
    rules[0] = new MultiLineRule("<!--", "-->", xmlComment);
    rules[1] = new TagRule(tag);
    
    setPredicateRules(rules);
  }
}
