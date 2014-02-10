package org.mulgara.store.jxunit;

import java.text.SimpleDateFormat;
import java.util.Date;

import net.sourceforge.jxunit.JXProperties;
import net.sourceforge.jxunit.JXTestCase;
import net.sourceforge.jxunit.JXTestStep;

/**
 * @created 2006-07-15
 * @author Brian Sletten
 */
public class TimeZoneSubstituteJX implements JXTestStep {
	
	// Names of properties to pull out of the JXProperties class
	
	/** The date to be converted */
	public final static String TIMEDATE = "timedate";
	
	/** The token to substitute */
	public final static String TOKEN = "token";
	
	/** The template document to substitute the token in */
	public final static String TEMPLATE = "template";
	
	/** The actual results from the query */
	public final static String QUERYRESULT = "queryResult";
	
	/** The property to set on success */
	public final static String PROPERTY= "returnProperty";

	@SuppressWarnings("unchecked")  // JXProperties do not us Generics
  public void eval(JXTestCase testCase) throws Throwable {
		JXProperties props = testCase.getProperties();
		boolean success = false;
		
		/* Retrieve each of the properties we are expecting.
		   Throw an exception if any are missing */
		
		String timedate = checkGetValue(props, TIMEDATE);
		String token = checkGetValue(props, TOKEN);
		String template = checkGetValue(props, TEMPLATE);
		String queryResult = checkGetValue(props, QUERYRESULT);
		String returnProperty = checkGetValue(props, PROPERTY);
		
		/* Parse the datetime to substitute and convert it to the local timezone */
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
		Date d = sdf.parse(timedate);
		
		/* Convert it to the expected format */
		sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		String localTime = sdf.format(d);
		
		/* Replace the token and see if it matches the results */
		String replaced = template.replace(token, localTime);
		success = queryResult.equals(replaced);

		if (success) props.put(returnProperty, "true");
	}

	/* Retrieve the specified property and complain if it doesn't exist
	   by throwing an IllegalStateException */
	
	private String checkGetValue( JXProperties props, String propertyName ) {
		String retValue = props.getString(propertyName);
		if (retValue == null) throw new IllegalStateException("Missing expected property: " + propertyName );
		return retValue;
	}
}
