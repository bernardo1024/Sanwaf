# SanWaf
Sanwaf, short for Sanitation Web Application Filter, is a filter/interceptor that is added to applications to increase the security posture.  It is a new security control meant to augment traditional WAFs on occasions where WAF rules need to be loosened, or when you whitelist parameters, headers, cookies, or URIs. 

Web Severs receive requests with Headers, Cookies, Parameters being sent from an untrusted client to your server.  A hacker can try to send malicious payloads to compromise your applications.  Sanwaf can be configured to detect attack payloads and will prevent submitted data from impacting your system.  
	
Sanwaf sanitizes, or pre-validates your data prior to application code execution making your applications more secure.

SanWaf is a dependency-free code so it is very easy to add to your Java application



## Compatibility
The following section details the compatibility of SanWaf

JAVA  	- tested with JDK 1.6, 1.7, 1.8, 1.9


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
	

Then in your unhandled exception handler, you can pull the error values with these methods:

	String sanwafTrackId = Sanwaf.getTrackId(request);
	String parmsInErrorJson = Sanwaf.getParmErrors(request);

##Sanwaf Quick Guide
Please see the sanwaf-tempalte.xml file for full details of using sanwaf.


###Custom Datatypes
In order to improve the performance of scanning submitted data as fast as possible, custom data types were built and are designed to fail fast. 
Use these data types whenever possible (instead of simply assigning all to the string data type that uses regex's).
  
	Notation	Description 
		c		- Character
		n 		- Number
		n{} 	- Delimited list of Numbers
		a		- Alphanumeric
		a{}		- Alphanumeric and stated additional characters
		s		- String (uses regex's - most expensive - try to use sparingly)
		k{}		- Must be equal to the of if the Constant values provided
		r{}		- Custom regex expression (reusable per field regex capabilities)
		j{}		- Java Class.method - returns true/false for pass/fail

	(min,max)	- Specify max & min length limits for the submitted data; specify -1 to use max range supported (Integer.MAX_VALUE)
				- Suffix a data type with : (min,max)
					For Example: 
						n(7,7)			- 7 digit number
						n{,}(0,5)		- each delimited number must be between 0 and 5 chars in length
						a(0,10)			- 0-10 characters of alphanumeric text
						r{ssn}(0,10)	- 0-10 characters that also match a regex called ssn

###Configuration
You configure how submitted data (parameters/headers/cookies) get processed in the shields/shield/metadata/secured section of this XML file.
The secured section contains the following groups: parameters, headers, cookies. 
						
###Group Format

Groups (parameters|headers|cookies) contain a list of items to be protected by the WAF.
Each item can contain single or multiple entries separated by three colons (":::"). For example:

	Single:	
			<item>key1=value</item>
			<item>key2=value</item>
	Multiple:
			<item>key1-value:::key2=value</item>
		
	NOTATION:
			<parameters|headers|cookies>
				<item>key=[c|n|n{}|a|a{}|k{}|r{}|j{}][:::[...]...]</item>
				...
			</parameters|headers|cookies>
		
	EXAMPLE:
			<parameters>
				<item>foo=s(10,15)</item>
				<item>bar=n{,}(5,5)</item>
				<item>barfoo=s(10,15):::foobar=n{,}(5,5)</item>
			</parameters>



###Custom Datatypes Guide
	(Character)
		c		DESCRIPTION:	Any single character
				FORMAT: 		key=c
				EXAMPLE: 		parameterName=c
								VALID: 			any single character
	
	(Number) 		
		n		DESCRIPTION:	Any positive or negative numeric value 
								('+' sign NOT allowed; one '-' sign allowed @start of value; no spaces; one '.' allowed)  
				FORMAT:			key=n  
				EXAMPLE:		parameterName=n
								VALID: 			-321.123		INVALID: +12
												   0.000			      12.34.56
								
	(Delimited list of Numbers)
		n{}	    DESCRIPTION:	A character separated list of numbers
				FORMAT:			key=n{<separator char>}
								Note: the (min,max) settings applies per delimted value  
				EXAMPLE:		parameterName=n{,}
								VALID: 			-321.123,0.000,123,45
												  
	(Alphanumeric)
		a		DESCRIPTION:	Valid chars are A-Z, a-z, 0-9. 
				FORMAT: 		key=a
				EXAMPLE: 		parameterName=a
								VALID:			abcdefghijklmnopqrstuvwxyz0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ 
	
	(Alphanumeric and stated additional characters)						
		a{}		DESCRIPTION:	Valid chars are A-Z, a-z, 0-9 *AND* the characters you specify in the curly brackets
				FORMAT: 		key=a{<characters to allow>}
								  For <space>, <tab>, <newline>, <carriage return> use: \s \t \n \r respectively
				EXAMPLE:		parameterName=a{+\s,}
								VALID: 			abcdefghijklm nopqrstuvwxyz+,
	
	(String) 
		s 		DESCRIPTION:	Any string.  
								All regex's in the autoRunPatterns are executed against the string				
				FORMAT: 		key=s
				EXAMPLE:		parameterName=s
								VALID: 			"Hello this string does not contain a XSS payload"

	(Constant)
		k{}		DESCRIPTION: 	Constant, must be equal to one of the values specified
					FORMAT: 	key=k{<comma separated list of strings>}
					EXAMPLE: 	unitTestString=k{FOO,BAR,FAR}
								VALID: 			FOO, BAR, FAR	

	(Custom Regex)
		r{}		DESCRIPTION: 	Custom Regex Expression in this file (for reuse)
								Custom Regex's are specified in the Shield's customPatterns section
								Regex must not include the '/' markers nor any flags.  
								For example, only provide the value for <regex>:
									/<regex>/gimsuy  
					FORMAT: 	key=r{CustomRegexName}
					EXAMPLE: 	unitTestString=R{date}
								VALID: 			**depends on regex specified**	
	
	(Java)
		j{}		DESCRIPTION: 	Java, call java class for processing
								-The key value and the ServletRequest object is passed to the method
								-The method of the Java class must be static, with a string and a ServletRequest parameter that returns a boolean value
								For example:
									public static boolean methodName(String s, ServletRequest request)
										return true for threat found, else false;
								for example: public static boolean sanwafMethod(String s){return true;} 
					FORMAT: 	key=j{fully_qualified_className.methodName()}
					EXAMPLE: 	unitTestJava=j{com.foo.bar.SomeClass.someMethod()}
								VALID: 			**depends on class processing**
	




##Sample code
The following code is used for demonstration purposes.  Not all imports or code is provided.  
Add Sanwaf as a dependency to your code:

	<dependency>
		<groupId>com.sanwaf</groupId>
		<artifactId>sanwaf</artifactId>
		<version>0.1.0</version>
		<scope>compile</scope>
	</dependency>

Sample Filter Code:
	
	package com.sanwaf.sample;
	
	// import Sanwaf
	import com.sanwaf.core.Sanwaf;

	// import to sample logger.
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


### Download
Go to https://github.com/bernardo1024/Sanwaf down download Sanwaf.

