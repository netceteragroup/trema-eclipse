package com.netcetera.trema.eclipse;

import org.junit.Assert;
import org.junit.Test;


public class TremaUtilEclipseTest {

  @Test
  public void testValidateKey() {
    Assert.assertNull(TremaUtilEclipse.validateKey("key.1-2_3", null, null));
    Assert.assertNull(TremaUtilEclipse.validateKey(".", null, null));
    Assert.assertNotNull(TremaUtilEclipse.validateKey("�", null, null));
    Assert.assertNotNull(TremaUtilEclipse.validateKey("", null, null));
  }

}
