package com.sanwaf.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import com.sanwaf.core.Shield;
import com.sanwaf.core.Sanwaf;

public class UtilTest {
  static Sanwaf sanwaf;
  static Shield shield;

  @BeforeClass
  public static void setUpClass() {
    try {
      sanwaf = new Sanwaf();
      shield = UnitTestUtil.getShield(sanwaf, "xss");
    } catch (IOException ioe) {
      assertTrue(false);
    }
  }

  @Test
  public void testConstructorIsPrivate() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
    Constructor<Util> constructor = Util.class.getDeclaredConstructor();
    assertTrue(Modifier.isPrivate(constructor.getModifiers()));
    constructor.setAccessible(true);
    constructor.newInstance();
  }

  @Test
  public void appendParameterItemMapToSBTest() {
    StringBuilder sb = new StringBuilder();
    Map<String, Parameter> m = new HashMap<>();
    Util.appendPItemMapToSB(m, sb, "label");
    assertEquals(true, sb.indexOf("(none found)") > 0);

    Util.appendPItemMapToSB(null, sb, "label");
    assertEquals(true, sb.indexOf("(none found)") > 0);

    sb = new StringBuilder();
    m = null;
    Util.appendPItemMapToSB(m, sb, "label");
    assertEquals(true, sb.indexOf("(none found)") > 0);
  }

  @Test
  public void splitTest() {
    List<String> list = Util.split(null);
    assertEquals(true, list.isEmpty());

    list = Util.split("");
    assertEquals(true, list.isEmpty());

    list = Util.split("1:::2::::::3");
    assertEquals(3, list.size());
  }

  @Test
  public void jsonEncodeTest() {
    String s = Util.jsonEncode(null);
    assertEquals(true, s.equals(""));
  }

  @Test
  public void parseIntTest() {
    int i = Util.parseInt("12345", -123);
    assertEquals(true, i == 12345);
    i = Util.parseInt("123abc", -123);
    assertEquals(true, i == -123);
  }

  @Test
  public void isNotAlphanumericTest() {
    char c = 0x29;
    assertEquals(true, Util.isNotAlphanumeric(String.valueOf(c)));
    c = 0x7b;
    assertEquals(true, Util.isNotAlphanumeric(String.valueOf(c)));
    c = 0x3b;
    assertEquals(true, Util.isNotAlphanumeric(String.valueOf(c)));
    c = 0x3c;
    assertEquals(true, Util.isNotAlphanumeric(String.valueOf(c)));
    c = 0x5b;
    assertEquals(true, Util.isNotAlphanumeric(String.valueOf(c)));
    c = 0x5c;
    assertEquals(true, Util.isNotAlphanumeric(String.valueOf(c)));

    c = 0x31;
    assertEquals(false, Util.isNotAlphanumeric(String.valueOf(c)));
    c = 0x39;
    assertEquals(false, Util.isNotAlphanumeric(String.valueOf(c)));
    c = 0x41;
    assertEquals(false, Util.isNotAlphanumeric(String.valueOf(c)));
    c = 0x59;
    assertEquals(false, Util.isNotAlphanumeric(String.valueOf(c)));
    c = 0x61;
    assertEquals(false, Util.isNotAlphanumeric(String.valueOf(c)));
    c = 0x79;
    assertEquals(false, Util.isNotAlphanumeric(String.valueOf(c)));
  }

  @Test
  public void isCharAlphanumericTest() {
    char c = 0x29;
    assertEquals(true, Util.isCharNotAlphanumeric(c));
    c = 0x7b;
    assertEquals(true, Util.isCharNotAlphanumeric(c));
    c = 0x3b;
    assertEquals(true, Util.isCharNotAlphanumeric(c));
    c = 0x3c;
    assertEquals(true, Util.isCharNotAlphanumeric(c));
    c = 0x5b;
    assertEquals(true, Util.isCharNotAlphanumeric(c));
    c = 0x5c;
    assertEquals(true, Util.isCharNotAlphanumeric(c));

    c = 0x31;
    assertEquals(false, Util.isCharNotAlphanumeric(c));
    c = 0x39;
    assertEquals(false, Util.isCharNotAlphanumeric(c));
    c = 0x41;
    assertEquals(false, Util.isCharNotAlphanumeric(c));
    c = 0x59;
    assertEquals(false, Util.isCharNotAlphanumeric(c));
    c = 0x61;
    assertEquals(false, Util.isCharNotAlphanumeric(c));
    c = 0x79;
    assertEquals(false, Util.isCharNotAlphanumeric(c));
  }

  @Test
  public void stripEOSnumbersTest() {
    String s = Util.stripEosNumbers("s");
    assert (s.equals("s"));
    s = Util.stripEosNumbers("s");
    assert (s.equals("s"));
    s = Util.stripEosNumbers("abc123");
    assert (s.equals("abc"));
  }

  @Test
  public void parseMethodNameTest() {
    assert (Util.parseMethod("foo.method()").equals("method"));
    assert (Util.parseMethod("foomethod()").equals("foomethod()"));
  }

  @Test
  public void refineNameTest() {
    assert (Util.refineName("*foo.method()", shield.parameters.index) == null);
    assert (Util.refineName("foo*abc", shield.parameters.index) == null);
  }

  @Test
  public void stripXmlCommentsTest() {
    assert (Util.stripXmlComments("").equals(""));
    assert (Util.stripXmlComments(null).equals(""));
  }
}
