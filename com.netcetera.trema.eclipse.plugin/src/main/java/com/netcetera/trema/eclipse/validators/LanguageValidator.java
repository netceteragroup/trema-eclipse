package com.netcetera.trema.eclipse.validators;

import java.util.Set;

import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.viewers.ICellEditorValidator;

import com.netcetera.trema.common.TremaUtil;
import com.netcetera.trema.core.api.ITextNode;
import com.netcetera.trema.eclipse.TremaUtilEclipse;



/** 
 * A valudator for language codes. Calls
 * {@link TremaUtilEclipse#validateLanguage(String, Set)}.
 */
public class LanguageValidator implements IInputValidator, ICellEditorValidator {
  
  private Set<String> languages = null;
  
  /**
   * Constructs a new instance without any invalid languages.
   */
  public LanguageValidator() {
    this(new ITextNode[0]);
  }
  
  /**
   * Constructs a new instance.
   * @param textNode the text node whose languages
   * are invalid
   */
  public LanguageValidator(ITextNode textNode) {
    this(new ITextNode[] {textNode});
  }
  
  /**
   * Constructs a new instance.
   * @param textNodes the set of text nodes whose languages
   * are invalid
   */
  public LanguageValidator(ITextNode[] textNodes) {
    languages = TremaUtil.getLanguages(textNodes);
  }

  /** {@inheritDoc} */
  public String isValid(String language) {
    return TremaUtilEclipse.validateLanguage(language, languages);
  }
  
  /** {@inheritDoc} */
  public String isValid(Object value) {
    return isValid((String) value);
  }
  
}
