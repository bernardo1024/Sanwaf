# Sanwaf-Server
Sanwaf, short for Sanitation Web Application Firewall, is a filter/interceptor that is added to applications to increase the security posture.  It is a new security control meant to augment traditional WAFs on occasions where WAF rules need to be loosened, or when you allowlist parameters, headers, cookies, or URIs. 

Web Severs receive requests with Headers, Cookies, Parameters being sent from an untrusted client to your server.  A hacker can try to send malicious payloads to compromise your applications.  Sanwaf can be configured to detect attack payloads and will prevent submitted data from impacting your system.  
	
Sanwaf sanitizes, or pre-validates your data prior to application code execution making your applications more secure.

SanWaf is a dependency-free code so it is very easy to add to your Java application



## Compatibility
The following section details the compatibility of SanWaf

JAVA  	- tested with JDK 1.6, 1.7, 1.8, 1.9

Note that Sanwaf is written with no dependencies, so will probably work with any version of java. 


## Building Sanwaf
in the Sanwaf Project type:
  
	mvn clean package install



## Implementation
Create an authentication filter to validate all the incoming request objects. 

	//instanciate Sanwaf - you should create a logger that implements the com.sanwaf.log.Logger Interface
	public static Sanwaf sanwaf = new Sanwaf();

	//in your filter or interceptor, call the isThreatDetected(request) method  
	if(sanwaf.isThreatDetected(req)){
		//up to you how you want to handle this. Typical patterns include:
		// 1. throw Exception that will be caught by some globe exception handler to display proper error page
		// 2. log the user out and redirect the user to a login page
		//for this example, we will throw a SecurityException that will be caught and processed by an unhandled exception handler
		throw new SecurityException("Security Violation.  Put your message here.");
	}

**Alternatively**, use can use Sanwaf in-line anywhere in your code:

	  //isThreat methods
	  public boolean isThreat(String value)
	  public boolean isThreat(String value, String shieldName)
	  public boolean isThreat(String value, String shieldName, boolean setErrorAttributes, ServletRequest req)
	  public boolean isThreat(String value, String shieldName, boolean setErrorAttributes, ServletRequest req, String xml)

	  //For example, to test a parameter if it is safe using the configured XML...
	  if(sanwaf.isThreat(request.getParameter("parameter_name")){
	    //handle error condition
	  }
  
	  //For example, to test a parameter if it is safe specifying the XML...
	  if(sanwaf.isThreat(request.getParameter("parameter_name"), "XSS", true, request, "<item><name>parameter_name</name><type>s</type><max>20</max><min>0</min><msg>some custom error message</msg><uri>/some/valid/uri</uri></item>");){
	    //handle error condition
	  }
  
When/If an error is detected, you pull the error info with these methods:

	String sanwafTrackId = sanwaf.getTrackId(request);
	String parmsInErrorJson = sanwaf.getParmErrors(request);
	
To use Sanwaf to read allowlisted headers/cookies/parameters:

	String value = sanwaf.getAllowListedValue("[Header Cookie Parameter]", Sanwaf.AllowListType.[HEADER COOKIE PARAMETER], request);

## Sanwaf Quick Guide
Please see the sanwaf-tempalte.xml file for full details of using sanwaf.

### Sanwaf Structure
	<sanwaf>
		[global settings]
		<shields>
			<shield>
				[shield settings]
				[regex settings]
				[metadata settings]
			</shield>
			
			<child-shield>
				[shield settings]
				[regex settings]
				[metadata settings]
			</child-shield>
		</shields>
	</sanwaf>


	where:
	
	<shield>		- shields provide the mechanism to protect incoming data
				  you must specify 1 shield, but can have many shields configured
				  shields can specify a <child> shield that is used for performance reasons where the maxLen of the shield is encountered
	<child-shield>		- child-shield's enhance a shields protection when the shields maxLen is encountered
				  child-shield's are optional and ignored if the shield maxLen is set to "-1" (unlimited)
				  child-shield's can have their own <child> shields
	[global settings]	- settings that apply to the application being protected
	[shield settings]	- settings for the specific shield
	[regex settings]	- the shields regex settings
	[metadata settings]	- the shields metadata settings (discussed in more detail below)


### Custom Datatypes
In order to improve the performance of scanning submitted data as fast as possible, custom data types were built and are designed to fail fast. 
Use these data types whenever possible (instead of simply assigning all to the string data type that uses regex's).
  
	Notation	Description 
		c	- Character
		n 	- Number
		n{} 	- Delimited list of Numbers
		a	- Alphanumeric
		a{}	- Alphanumeric and stated additional characters
		s	- String (uses regex's - most expensive - try to use sparingly)
		k{}	- Must be equal to the of if the Constant values provided
		r{}	- Custom regex expression (reusable per field regex capabilities)
		j{}	- Java Class.method - returns true/false for pass/fail

### Configuration
You configure how submitted data (parameters/headers/cookies) get processed in the **shields/shield/metadata** section of this XML file.  

Note the **enabled** and **caseSensitive** sections that control if the specific section will be enabled and how they will handle the caseSensitivy of parameters/headers/cookies.
 
Also note the **secured section** contains the following groups: endpoints, parameters, headers, cookies.

	<metadata>

		<enabled>
			<parameters>true/false</parameters>
			<headers>true/false</headers>
			<cookies>true/false</cookies>
		</enabled>

		<caseSensitive>
			<parameters>true/false</parameters>
			<headers>true/false</headers>
			<cookies>true/false</cookies>
		</caseSensitive>

		<secured>
			<endpoints>
				<endpoint>
					<uri></uri>
					<item><name></name><type></type><max></max><min></min><max-value></max-value><min-value></min-value><msg></msg><req></req><related></related><format></format></item>
				</endpoint>
			</endpoints>
			<parameters>
				<item><name></name><type></type><max></max><min></min><msg></msg><uri></uri></item>
			</parameters>
			<headers>
				<item><name></name><type></type><max></max><min></min><msg></msg><uri></uri></item>
			</headers>
			<cookies>
				<item><name></name><type></type><max></max><min></min><msg></msg><uri></uri></item>
			</cookies>
		</secured>

	</metadata>						

where <secured> section are:
	<endpoints></endpoints>		- list of endpoints to secure
					- Endpoints are groupings of parameters so additional validation can occur, such as if a paremeter is required
					- See the Sanwaf-ui & Sanwaf-ui-2-server projects for more information on declaritive data validation
	<parameters></parameters>	- list of parameters to secure
	<headers></headers>		- list of headers to secure
	<cookies></cookies>		- list of cookies to secure
	
### Item Format of the Secured Section

	<item><name></name><type></type><max></max><min></min><msg></msg><uri></uri></item>
	
where
	<name></name>		- parameter/header/cookie name
				- specify multiple 'names' in one item tag by using the ':::' delimiter.  
				- for example:
					- <name>parameter1</name>
					- <name>parameter1:::parameter2:::parameter3</name> 
	<type></type>		- the parameter datatype (see Custom Datatypes above) (defaults to 's' if not specified)
	<max></max>		- the max length allowed for this parameter (defaults to Interger.MAX_VALUE if not specified)
	<min></min>		- the min length allowed for this parameter (defaults to 0 if not specified) 
	<max-value></max-value>	- the max value allowed for numeric parameters
	<min-value></min-value>	- the min value allowed for numeric parameters
	<msg></msg>		- the error message for the parameter(s) (uses the shield or global error message is not specified)
	<uri></uri>		- the uri that must match for the parameter evaluation to occur 
				- to specify multiple uri's for one item, use the ':::' delimiter.  
				- For "endpoints" the uri indicates a grouping of items to be evaulated together
	<req></req>		- Used in endpoints only (see Sanwaf-ui project for details)
				- Indicates if a parameter is required
	<related></related>	- Used in endpoints only (see Sanwaf-ui project for details)
				- Establishes a relationship that must be met between parameters
	<format></format>	- Used in endpoints only (see Sanwaf-ui project for details)
				- Establishes a format that the parameter must meet to be valid
	

#### Example

	<item><name>telephone</name><type>r{telephone}</type><max>12</max><min>1</min><msg>Invalid Telephone number entered, must be in the format 555-555-5555</msg><uri>/put/accounts</uri></item>
	<item><name>fname:::lname</name><type>s</type><max>30</max><min>1</min><msg>must be between 1-30 chars</msg></item>
	<item><name>sex</name><type>k{male,female,other}</type><msg>only male/female/other are allowed</msg></item>
	<item><name>count</name><type>n</type><uri>/uri1:::uri2:::uri3</uri></item>

### Custom Datatypes Guide

	(Character)
	c		DESCRIPTION:	Any single character
			FORMAT: 	c
	
	(Number) 		
	n		DESCRIPTION:	Any positive or negative numeric value 
					('+' sign NOT allowed; one '-' sign allowed @start of value; no spaces; one '.' allowed)  
			FORMAT:		n  
			EXAMPLE:	-321.123, 0.0001 - are valid
					+12, 12.34.56	- are invalid
								
	(Delimited list of Numbers)
	n{}		DESCRIPTION:	A character separated list of numbers
			FORMAT:		n{<separator char>}
					Note: the min & max settings applies per delimted value  
			EXAMPLE: 	using n{,}, -321.123,0.000,123,45 is valid
												  
	(Alphanumeric)
	a		DESCRIPTION:	Valid chars are A-Z, a-z, 0-9. 
			FORMAT: 	a
			EXAMPLE:	abcdefghijklmnopqrstuvwxyz0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ - is valid 
	
	(Alphanumeric and stated additional characters)						
	a{}		DESCRIPTION:	Valid chars are A-Z, a-z, 0-9 *AND* the characters you specify in the curly brackets
			FORMAT: 	a{<characters to allow>}
					- For <space>, <tab>, <newline>, <carriage return> use: \s \t \n \r respectively
			EXAMPLE:	using a{+\s,}, abcdefghijklm nopqrstuvwxyz+, is valid
	
	(String) 
	s 		DESCRIPTION:	Any string.  
					All regex's in the autoRunPatterns are executed against the string				
			FORMAT: 	s
			EXAMPLE:	"Hello this string does not contain a XSS payload"

	(Constant)
	k{}		DESCRIPTION: 	Constant, must be equal to one of the values specified
			FORMAT: 	k{<comma separated list of strings>}
			EXAMPLE: 	using k{FOO,BAR,FAR}, FOO, BAR, FAR are valid

	(Custom Regex)
	r{}		DESCRIPTION: 	Custom Regex Expression in this file (for reuse)
					Custom Regex's are specified in the Shield's customPatterns section
					Regex must not include the '/' markers nor any flags.  
					For example, only provide the value for <regex>:
						/<regex>/gimsuy  
			FORMAT: 	r{CustomRegexName}
	
	(Java)
	j{}		DESCRIPTION: 	Java, call java class for processing
					-The key value and the ServletRequest object is passed to the method
					-The method of the Java class must be static, with a string and a ServletRequest parameter that returns a boolean value
						For example:
							public static boolean methodName(String s, ServletRequest request)
								return true for threat found, else false
			FORMAT: 	j{fully_qualified_className.methodName()}


## Sample code

#### For the sample app, go to https://github.com/bernardo1024/SanwafSample

The following code is used for demonstration purposes.  Not all imports or code is provided.  
Add Sanwaf as a dependency to your code:

	<dependency>
		<groupId>com.sanwaf</groupId>
		<artifactId>sanwaf</artifactId>
		<version>0.1.1</version>
		<scope>compile</scope>
	</dependency>

Sample Filter Code:
	
	package com.sanwaf.sample;
	
	// import Sanwaf
	import com.sanwaf.core.Sanwaf;

	// import sample logger.
	// Note: LoggerSystemOut is provided for demo purposes only. do not use in a production environment.
	//       Create a class that implements the com.sanwaf.log.Logger interface and use your preferred Logger,
	//		 then instantiate Sanwaf with it
	import com.sanwaf.log.LoggerSystemOut;
	
	public class SampleAuthenticationFilter implements Filter {
		// instantiate Sanwaf (if you dont specify an xml file, sanwaf.xml will be used if in your classpath)
		static SanWaf sanwaf = new SanWaf(new SampleSystemOutLogger(), "/your-sanwaf-config-file.xml");
	
		public void doFilter(ServletRequest req, ServletResponse resp, FilterChain filterChain) throws SecurityException{
			// call Sanwaf to check if requests are valid or not
			if (sanwaf.isThreatDetected(req)) {
				// Up to you how you want to handle this the error condition.
				// Here we are throwing a SecurityException, passing the tracking ID and errors in json format  
				throw new SecurityException(Sanwaf.getTrackId(request) + ", " + Sanwaf.getParmErrors(request));
			}
			filterChain.doFilter(req, resp);
		}
	}


### Create a custom Logger

You will need to create your own logger and pass it to Sanwaf's constructor otherwise the System.out.printl() method will be used which is not performant nor production facing.

Here is a simple example of creating a custom logger.  

	//add the dependency to your pom
	<dependency>
		<groupId>log4j</groupId>
		<artifactId>log4j</artifactId>
		<version>1.2.17</version>
	</dependency>

	//implement the code
	import org.apache.log4j.Logger;

	public class TestLogger implements com.sanwaf.log.Logger {
		static Logger log = Logger.getLogger(TestLogger.class);

		@Override
		public void error(String msg) {
			log.error(msg);
		}

		@Override
		public void info(String msg) {
			if(log.isInfoEnabled()) {
				log.info(msg);
			}
		}
	}

The log4j.properties is not specified in this readme, so consult the documentation https://logging.apache.org/log4j/2.x/ 


## License

Copyright 2019 Bernardo Sanchez

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at [apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
