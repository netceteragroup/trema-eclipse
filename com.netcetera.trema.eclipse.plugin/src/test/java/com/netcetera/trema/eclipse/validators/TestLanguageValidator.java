package com.netcetera.trema.eclipse.validators;

import junit.framework.Assert;

import org.junit.Test;

import com.netcetera.trema.core.Status;
import com.netcetera.trema.core.XMLTextNode;
import com.netcetera.trema.core.XMLValueNode;
import com.netcetera.trema.core.api.ITextNode;



/**
 * Test class for the LanguageValidator.
 */
public class TestLanguageValidator {
  
  /**
   * Tests the LanguageValidator.
   */
  @Test
  public void testRegexp() {
    LanguageValidator validator = new LanguageValidator();
    
    Assert.assertNull(validator.isValid("de"));
    Assert.assertNull(validator.isValid("De"));
    Assert.assertNull(validator.isValid("de_CH"));
    Assert.assertNull(validator.isValid("de-CH"));
    Assert.assertNull(validator.isValid("de_C-H"));
    Assert.assertNotNull(validator.isValid(" de"));
    Assert.assertNotNull(validator.isValid(" \t"));
    Assert.assertNotNull(validator.isValid("de4"));
    Assert.assertNotNull(validator.isValid("de.ch"));
    Assert.assertNotNull(validator.isValid("*"));
    Assert.assertNotNull(validator.isValid(""));
    Assert.assertNotNull(validator.isValid(null));
  }
  
  /**
   * Tests the LanguageValidator.
   */
  public void testWithTextNode() {
    ITextNode textNode = new XMLTextNode("key1", "context1");
    textNode.addValueNode(new XMLValueNode("de", Status.INITIAL, "value1"));
    textNode.addValueNode(new XMLValueNode("en", Status.INITIAL, "value2"));
    LanguageValidator validator = new LanguageValidator(textNode);
    
    Assert.assertNull(validator.isValid("it"));
    Assert.assertNotNull(validator.isValid("de")); // "de" is already present
    Assert.assertNotNull(validator.isValid("en")); // "en" is already present
    Assert.assertNull(validator.isValid("DE"));
  }
  
}
