package com.sanwaf.core;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.IllegalFormatException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import com.sanwaf.log.Logger;
import com.sanwaf.log.LoggerSystemOut;

public final class Sanwaf {
  private static final String STANDALONE_XML_FILENAME = "sanwaf.xml";
  private static final String REQ_ATT_TRACK_ID = "~sanwaf-id";
  private static final String REQ_ATT_ERRORS = "~sanwaf-errors";

  private String xmlFilename = null;
  protected Logger logger;

  boolean enabled = false;
  boolean verbose = false;
  boolean onErrorAddTrackId = true;
  boolean onErrorAddParmErrors = true;
  protected static String securedAppVersion = "unknown";
  protected List<Shield> shields = new ArrayList<>();
  Map<String, String> globalErrorMessages = new HashMap<>();

  public enum AllowListType {
    HEADER, COOKIE, PARAMETER
  }

  /**
   * Default Sanwaf constructor.
   * 
   * <pre>
   * Creates a in instance of Sanwaf initializing it with:
   *  -default System.out.println Logger (com.sanwaf.log.LoggerSystemOut)
   *   should not be used in a production environment
   *  -default Sanwaf XML configuration file (sanwaf.xml on classpath)
   * </pre>
   * 
   * @return void
   */
  public Sanwaf() throws IOException {
    this(new LoggerSystemOut(), "/" + STANDALONE_XML_FILENAME);
    logger.info("NOTE: Sanwaf is NOT configured with a valid Logger and is using the LoggerSystemOut class which uses System.out.println(...).  To correct this, implement the com.sanwaf.log.Logger Interface and provide in the Sanwaf constructor");
  }

  /**
   * Sanwaf constructor.
   * 
   * <pre>
   * Creates a new Sanwaf instance initializing it with the logger provided; 
   * Uses the default Sanwaf XML configuration file (sanwaf.xml on classpath)
   * </pre>
   * 
   * @param logger
   *          A logger of your choice that implements the com.sanwaf.log.Logger
   *          interface
   * 
   * @return void
   */
  public Sanwaf(Logger logger) throws IOException {
    this(logger, "/" + STANDALONE_XML_FILENAME);
  }

  /**
   * Sanwaf constructor where you specify the logger and properties file to use
   * 
   * <pre>
   * Creates a new instance of Sanwaf using the logger & Sanwaf XML configuration provided.
   * </pre>
   * 
   * @param logger
   *          A logger of your choice that implements the com.sanwaf.log.Logger
   *          interface
   * @param filename
   *          Fully qualified path to a valid Sanwaf XML file
   * @return void
   */
  public Sanwaf(Logger logger, String filename) throws IOException {
    this.logger = logger;
    this.xmlFilename = filename;
    loadProperties();
  }

  /**
   * Test if a threat is detected in a given request
   * 
   * <pre>
   * Threats detected are derived from all shields configurations
   * 
   * If an error is detected, attributes will be added to request for processing latter.  
   *  Attributes added are dependent on the properties settings of:
   *        <provideTrackId>true/false</provideTrackId>
   *        <provideErrors>true/false</provideErrors>
   * 
   * Use the following methods in this class to retrieve the values:
   * 	public static String getTrackingId(HttpServletRequest req)
   * 	public static String getErrors(HttpServletRequest req)
   * </pre>
   * 
   * @param req
   *          ServletRequest the ServletRequest object you want to scan for
   *          threats
   * @return boolean true/false if a threat was detected
   */
  public boolean isThreatDetected(ServletRequest req) {
    if (!enabled || !(req instanceof HttpServletRequest)) {
      return false;
    }
    for (Shield sh : shields) {
      if (sh.threatDetected(req)) {
        addErrorAttributes(req, getSortOfRandomNumber(), getErrorList(req));
        return true;
      }
    }
    return false;
  }

  /**
   * Test if a threat is detected in a value
   *
   * <pre>
   * Threats detected are derived from all shields configurations
   * 
   * No error attributes are set.
   * </pre>
   * 
   * @param value
   *          the string you want to scan for threats
   * @return boolean true/false if a threat was detected
   */
  public boolean isThreat(String value) {
    return checkForThreats(value, null);
  }

  /**
   * Test if a threat is detected in a value using a given shield
   *
   * <pre>
   * Threats detected are derived from the provided shield's configuration
   * 
   * The shields autoRunPatterns will be executed against the value
   * 
   * No error attributes are set.
   * </pre>
   * 
   * @param value
   *          the string you want to scan for threats
   * @param shieldName
   *          the shields name that you want to execute the autoRunPatterns from 
   * @return boolean true/false if a threat was detected
   */
  public boolean isThreat(String value, String shieldName) {
    return checkForThreats(value, shieldName);
  }

  /**
   * Test if a threat is detected in a value using a given shield
   *
   * <pre>
   * Threats detected are derived from the provided shield's configuration
   * 
   * The shields autoRunPatterns will be executed against the value
   * 
   * Error attributes will be set if specified
   * 
   * If an error is detected, attributes will be added to request for processing latter.  
   *  Attributes added are dependent on the properties settings of:
   *        <provideTrackId>true/false</provideTrackId>
   *        <provideErrors>true/false</provideErrors>
   * 
   * Use the following methods in this class to retrieve the values:
   *  public static String getTrackingId(HttpServletRequest req)
   *  public static String getErrors(HttpServletRequest req)
   * </pre>
   * 
   * @param value
   *         the string you want to scan for threats
   * @param shieldName
   *          The shields name that you want to execute the autoRunPatterns from 
   * @param setErrorAttributes
   *          boolean to indicate whether to set the tracking id and error json to the request's attributes 
   * @param req
   *          ServletRequest to add the error attributes to (can be null if setErrorAttributes is false)
   * @return boolean true/false if a threat was detected
   */
  public boolean isThreat(String value, String shieldName, boolean setErrorAttributes, ServletRequest req) {
    boolean foundThreat = checkForThreats(value, shieldName);
    if(foundThreat && setErrorAttributes) {
      addErrorAttributes(req, getSortOfRandomNumber(), getErrorList(value));
    }
    return foundThreat;
  }
  
  /**
   * Test if a threat is detected in a value using XML provided
   *
   * <pre>
   * Threats detected are derived from the XML provided
   * XML must conform to Sanwaf.xml specifications
   * 
   * The specified shield's autoRunPatterns will be executed against the value for datatype String
   * 
   * Error attributes will be set if specified
   * 
   * If an error is detected, attributes will be added to request for processing latter.  
   *  Attributes added are dependent on the properties settings of:
   *        <provideTrackId>true/false</provideTrackId>
   *        <provideErrors>true/false</provideErrors>
   * 
   * Use the following methods in this class to retrieve the values:
   *  public static String getTrackingId(HttpServletRequest req)
   *  public static String getErrors(HttpServletRequest req)
   * </pre>
   * 
   * @param value
   *          the string you want to scan for threats
   * @param shieldName
   *          the shields name that you want to execute the autoRunPatterns from (String data type only)
   *          or use the custom regex's specified (regex data type only) 
   * @param setErrorAttributes
   *          boolean to indicate whether to set the tracking id and error json to the request's attributes 
   * @param req
   *          calling ServletRequest object used to test URIs
   * @param xml
   *          XML String to configure the data type.  See sanwaf.xml shield/metadata/secured section for configuration details
   * @return boolean true/false if a threat was detected
   */
  public boolean isThreat(String value, String shieldName, boolean setErrorAttributes, ServletRequest req, String xml) {
    Item item = Metadata.parseItem(new Xml(xml));
    Shield sh = getShield(shieldName);
    if(sh == null) {
      logger.error("Invalid ShieldName provided to isThreat():" + shieldName);
      return false;
    }
    if(item.inError(req, sh, value)) {
      if(setErrorAttributes) {
        Error error = new Error(sh, item, null, value);
        addErrorAttributes(req, getSortOfRandomNumber(), Arrays.asList(error));
      }
      return true;
    }
    return false;
  }

  private boolean checkForThreats(String value, String shieldName) {
    for (Shield sh : shields) {
      if((shieldName == null || shieldName.contains(sh.name)) && sh.threat(null, null, "", value)) {
          return true;
      }
    }
    return false;
  }
  
  /**
   * Retrieve an allow-listed parameter/header/cookie
   * 
   * <pre>
   *  The header/cookie/parameter value will be returned IFF the its
   *  name is set in any Shield's Metadata block
   *
   *    <metadata>
   *      <secured>
   *        <headers></headers>
   *        <cookies></cookies>
   *        <parameters></parameters>
   *      </secured>
   *    </metadata>
   * </pre>
   * 
   * @param request
   *          HttpServletRequest Object to pull the header/cookie/parameter
   *          value from
   * @param type
   *          Sanwaf.AllowListType enumeration (HEADER, COOKIE, PARAMETER)
   * @param name
   *          the name of the header/cookie/parameter you want to retrieve
   * @return String the value of the requested header/cookie/parameter requested
   *         or null.
   */
  public String getAllowListedValue(String name, AllowListType type, HttpServletRequest req) {
    for (Shield sh : shields) {
      String value = sh.getAllowListedValue(name, type, req);
      if (value != null) {
        return value;
      }
    }
    return null;
  }

  /**
   * Dynamically reload sanwaf
   *
   * @return void
   */
  public final void reLoad() throws IOException {
    loadProperties();
  }

  /**
   * Get the Sanwaf Tracking ID
   * 
   * <pre>
   *  useful for displaying to your users in case they call support. this allows
   *  you to pull the exact exception from the log file
   * 
   * <pre>
   * 
   * @param req
   *          HttpServletRequest the request object where
   *          Sanwaf.isThreatDetected() returned true.
   * @return String returns the Sanwaf Tracking ID
   */
  public static String getTrackingId(HttpServletRequest req) {
    Object o = req.getAttribute(REQ_ATT_TRACK_ID);
    if (o != null) {
      return String.valueOf(o);
    }
    return null;
  }

  /**
   * Get Sanwaf Errors
   * 
   * <pre>
   *  Returns all threats found for a give request object in JSON format
   *  used to display errors to the user.
   * </pre>
   * 
   * @param req
   *          HttpServletRequest the request object where
   *          Sanwaf.isThreatDetected() returned true.
   * @return String Returns all threats found in JSON format
   */
  public static String getErrors(HttpServletRequest req) {
    Object o = req.getAttribute(REQ_ATT_ERRORS);
    if (o != null) {
      return String.valueOf(o);
    }
    return null;
  }

  private List<Error> getErrorList(ServletRequest req) {
    List<Error> errors = new ArrayList<>();
    if (!onErrorAddParmErrors) {
      return errors;
    }
    String k = null;
    String[] values = null;
    Enumeration<?> names = req.getParameterNames();

    while (names.hasMoreElements()) {
      k = (String) names.nextElement();
      values = req.getParameterValues(k);
      for (String v : values) {
        getShieldErrors(req, errors, k, v);
      }
    }
    return errors;
  }

  private List<Error> getErrorList(String v) {
    List<Error> errors = new ArrayList<>();
    if (!onErrorAddParmErrors) {
      return errors;
    }
    getShieldErrors(null, errors, null, v);
    return errors;
  }

  private void getShieldErrors(ServletRequest req, List<Error> errors, String key, String value) {
    if(req == null) {
      for (Shield sh : shields) {
        errors.addAll(sh.getErrors(req, key, value, true));
      }
    }
    else {
      for (Shield sh : shields) {
        if (sh.threatDetected(req)) {
          errors.addAll(sh.getErrors(req, key, value));
        }
      }
    }
  }

  List<Error> getError(ServletRequest req, Shield shield, String key, String value) {
    return shield.getErrors(req, key, value);
  }

  private void addErrorAttributes(ServletRequest req, String id, List<Error> errors) {
    if (onErrorAddTrackId) {
      req.setAttribute(REQ_ATT_TRACK_ID, id);
    }
    if (onErrorAddParmErrors) {
      req.setAttribute(REQ_ATT_ERRORS, Error.toJson(errors));
    }
  }

  static String getSortOfRandomNumber() {
      java.security.SecureRandom srandom = new java.security.SecureRandom();
      DecimalFormat fStart = new DecimalFormat("00000");
      DecimalFormat fEnd = new DecimalFormat("0000");
      return fStart.format(srandom.nextInt(99999)) + "-" + fEnd.format(srandom.nextInt(9999)); 
  }
  
  Shield getShield(String name) {
    for (Shield shield : shields) {
      if (shield.name.equalsIgnoreCase(name)) {
        return shield;
      }
    }
    return null;
  }

  // XML LOAD CODE
  private static final String XML_GLOBAL_SETTINGS = "global-settings";
  private static final String XML_ENABLED = "enabled";
  private static final String XML_VERBOSE = "verbose";
  private static final String XML_APP_VER = "app.version";
  private static final String XML_ERR_HANDLING = "errorHandling";
  private static final String XML_ERR_SET_ATT_TRACK_ID = "provideTrackId";
  private static final String XML_SET_ATT_PARM_ERR = "provideErrors";
  private static final String XML_SHIELD = "shield";

  private synchronized void loadProperties() throws IOException {
    long start = System.currentTimeMillis();
    Xml xml;
    try {
      xml = new Xml(Sanwaf.class.getResource(xmlFilename));
    } catch (IOException e) {
      throw new IOException("Sanwaf Failed to load config file " + xmlFilename + ".  \n**Server is NOT protected**\n", e);
    }

    String settingsBlock = xml.get(XML_GLOBAL_SETTINGS);
    Xml settingsBlockXml = new Xml(settingsBlock);
    enabled = Boolean.parseBoolean(settingsBlockXml.get(XML_ENABLED));
    verbose = Boolean.parseBoolean(settingsBlockXml.get(XML_VERBOSE));
    securedAppVersion = settingsBlockXml.get(XML_APP_VER);
    logger.info("Starting Sanwaf: enabled=" + enabled + ", " + XML_VERBOSE + "=" + verbose + ", " + XML_APP_VER + "=" + securedAppVersion);

    String errorBlock = xml.get(XML_ERR_HANDLING);
    Xml errorBlockXml = new Xml(errorBlock);
    onErrorAddTrackId = Boolean.parseBoolean(errorBlockXml.get(XML_ERR_SET_ATT_TRACK_ID));
    onErrorAddParmErrors = Boolean.parseBoolean(errorBlockXml.get(XML_SET_ATT_PARM_ERR));

    Error.setErrorMessages(globalErrorMessages, xml);

    String[] xmls = xml.getAll(XML_SHIELD);
    for (String item : xmls) {
      shields.add(new Shield(this, xml, new Xml(item), logger));
    }
    logger.info("Started in: " + (System.currentTimeMillis() - start) + " ms.");
  }

}
