package com.sanwaf.core;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.sanwaf.core.Shield;
import com.sanwaf.core.Sanwaf;

public class VerboseTest {
  private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
  private final PrintStream originalOut = System.out;

  static Sanwaf sanwaf;
  static Shield shield;

  @Before
  public void setUpStreams() {
      System.setOut(new PrintStream(outContent));
 }

  @After
  public void restoreStreams() {
      System.setOut(originalOut);
  }
  
  @Test
  public void verboseDisabledTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, IOException {
    sanwaf = new Sanwaf(new UnitTestLogger(), "/sanwaf.xml");
    String s = outContent.toString();
    assertTrue(!s.contains("Settings:"));
    assertTrue(!s.contains("RegexAlways=true"));
    assertTrue(!s.contains("Shield Secured List: *Ignored*"));
    assertTrue(!s.contains("Except for (exclusion list):"));
    assertTrue(!s.contains("Patterns:"));
    assertTrue(!s.contains("customPatterns:"));
    assertTrue(!s.contains("Configured/Secured Entries:"));
  }

  @Test
  public void verboseEnabledTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, IOException {
    sanwaf = new Sanwaf(new UnitTestLogger(), "/sanwaf-verbose.xml");
    String s = outContent.toString();
    assertTrue(s.contains("Settings:"));
    assertTrue(!s.contains("RegexAlways=true"));
    assertTrue(s.contains("Patterns:"));
    assertTrue(s.contains("customPatterns:"));
    assertTrue(s.contains("Configured/Secured Entries:"));
  }

  @Test
  public void verboseEnabledRegexTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, IOException {
    sanwaf = new Sanwaf(new UnitTestLogger(), "/sanwaf-verboseRegexAlways.xml");
    String s = outContent.toString();
    assertTrue(s.contains("RegexAlways=true"));
    assertTrue(s.contains("Shield Secured List: *Ignored*"));
    assertTrue(s.contains("Except for (exclusion list):"));
    assertTrue(!s.contains("Configured/Secured Entries:"));
  }
  
}
