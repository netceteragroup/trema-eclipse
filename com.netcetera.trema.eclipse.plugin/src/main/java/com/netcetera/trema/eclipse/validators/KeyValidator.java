package com.netcetera.trema.eclipse.validators;

import org.eclipse.jface.dialogs.IInputValidator;

import com.netcetera.trema.core.api.IDatabase;
import com.netcetera.trema.eclipse.TremaUtilEclipse;



/** 
 * A validator for text keys. Calls
 * {@link TremaUtilEclipse#validateKey(String, String, IDatabase)}.
 */
public class KeyValidator implements IInputValidator {
  
  private IDatabase db = null;
  private String oldKey = null;
  
  /**
   * Creates a new instance.
   * @param db the parent database, may be <code>null</code>
   * @param oldKey the old key, may be <code>null</code>
   */
  public KeyValidator(IDatabase db, String oldKey) {
    this.db = db;
    this.oldKey = oldKey;
  }

  /** {@inheritDoc} */
  public String isValid(String key) {
    return TremaUtilEclipse.validateKey(key, oldKey, db);
  }
  
}
