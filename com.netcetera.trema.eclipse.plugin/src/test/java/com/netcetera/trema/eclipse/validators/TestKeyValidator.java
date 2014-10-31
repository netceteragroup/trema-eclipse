package com.netcetera.trema.eclipse.validators;


import org.junit.Assert;
import org.junit.Test;

import com.netcetera.trema.core.XMLDatabase;
import com.netcetera.trema.core.XMLTextNode;
import com.netcetera.trema.core.api.IDatabase;



/**
 * Test class for the KeyValidator.
 */
public class TestKeyValidator {
  
  /**
   * Tests key validation.
   */
  @Test
  public void testRegExp() {
    KeyValidator validator = new KeyValidator(new XMLDatabase(), "");
    
    Assert.assertNull(validator.isValid("key1.2.3-4_dialog"));
    Assert.assertNull(validator.isValid("key"));
    Assert.assertNull(validator.isValid("."));
    Assert.assertNull(validator.isValid("key_1"));
    Assert.assertNull(validator.isValid("KEY-1"));
    Assert.assertNull(validator.isValid("999"));
    Assert.assertNull(validator.isValid("1234-5.6_7"));
    Assert.assertNotNull(validator.isValid(""));
    Assert.assertNotNull(validator.isValid(" "));
    Assert.assertNotNull(validator.isValid("\t  "));
    Assert.assertNotNull(validator.isValid("�"));
    Assert.assertNotNull(validator.isValid("+/"));
  }
  
  /**
   * Tests key validation.
   */
  @Test
  public void testWithDatabaseAndOldValue() {
    IDatabase db = new XMLDatabase();
    db.addTextNode(new XMLTextNode("key.1", "context"));
    db.addTextNode(new XMLTextNode("key.2", "contest"));

    KeyValidator validator = new KeyValidator(db, "");
    Assert.assertNull(validator.isValid("key.3"));
    Assert.assertNull(validator.isValid("key.4.5.6"));
    Assert.assertNotNull(validator.isValid("key.1"));
    Assert.assertNotNull(validator.isValid("key.2"));
    
    validator = new KeyValidator(db, "key.1");
    Assert.assertNull(validator.isValid("key.1"));
    Assert.assertNull(validator.isValid("key.3"));
    Assert.assertNotNull(validator.isValid("key.2"));
  }
  
}
