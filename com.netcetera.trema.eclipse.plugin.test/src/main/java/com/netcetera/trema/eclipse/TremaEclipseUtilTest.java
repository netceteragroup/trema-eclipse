
package com.netcetera.trema.eclipse;

import org.junit.Assert;
import org.junit.Test;



/**
 * Unit test for the <code>TremaEclipseUtil</code> class.
 */
public class TremaEclipseUtilTest {


  /**
   * Tests stripping extensions.
   */
  @Test
  public void testStripExtension() {
    String expected = "foo";
    String actual = TremaEclipseUtil.stripExtension("foo.txt");
    Assert.assertEquals(expected, actual);

    expected = "/folder/path/foo";
    actual = TremaEclipseUtil.stripExtension("/folder/path/foo.txt");
    Assert.assertEquals(expected, actual);

    expected = "C:/folder/path/foo";
    actual = TremaEclipseUtil.stripExtension("C:/folder/path/foo.txt");
    Assert.assertEquals(expected, actual);

    expected = "foo";
    actual = TremaEclipseUtil.stripExtension("foo");
    Assert.assertEquals(expected, actual);

    expected = "foo";
    actual = TremaEclipseUtil.stripExtension("foo.");
    Assert.assertEquals(expected, actual);

    expected = ".foo";
    actual = TremaEclipseUtil.stripExtension(".foo");
    Assert.assertEquals(expected, actual);

    expected = ".foo";
    actual = TremaEclipseUtil.stripExtension(".foo.txt");
    Assert.assertEquals(expected, actual);

    expected = "folder\\path.path\\foo";
    actual = TremaEclipseUtil.stripExtension("folder\\path.path\\foo.txt");
    Assert.assertEquals(expected, actual);

    expected = "/folder.folder/path/foo";
    actual = TremaEclipseUtil.stripExtension("/folder.folder/path/foo.txt");
    Assert.assertEquals(expected, actual);
  }

  /**
   * Tests TremaEclipseUtil.toStringArray().
   */
  @Test
  public void testToString() {
    Object[] strings = new Object[] {"string1", "string2", "string3"};
    String[] result = TremaEclipseUtil.toStringArray(strings);
    Assert.assertArrayEquals(strings, result);

    //assertArrayEquals(strings, result);

    // special cases
    Assert.assertNull(TremaEclipseUtil.toStringArray(null));
    Assert.assertEquals(0, TremaEclipseUtil.toStringArray(new Object[0]).length);
  }

  /**
   * Tests TremaEclipseUtil.rotate().
   */
  @Test
  public void testRotate() {
    String[] pool = new String[] {"one", "two", "three"};

    for (int i = 0; i < pool.length; i++) {
      Assert.assertArrayEquals(pool, TremaEclipseUtil.rotate(pool, pool[i], pool.length));
    }
    Assert.assertArrayEquals(new String[] {"zero", "one", "two"}, TremaEclipseUtil.rotate(pool, "zero", 3));

    pool = new String[] {"one", "two"};
    Assert.assertArrayEquals(new String[] {"zero", "one", "two"}, TremaEclipseUtil.rotate(pool, "zero", 3));

    pool = new String[] {"one"};
    Assert.assertArrayEquals(new String[] {"zero", "one"}, TremaEclipseUtil.rotate(pool, "zero", 3));

    pool = new String[0];
    Assert.assertArrayEquals(new String[] {"zero"}, TremaEclipseUtil.rotate(pool, "zero", 3));

    pool = null;
    Assert.assertArrayEquals(new String[] {"zero"}, TremaEclipseUtil.rotate(pool, "zero", 3));
  }

}
