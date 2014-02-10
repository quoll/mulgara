/*
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 *
 * The Original Code is the Kowari Metadata Store.
 *
 * The Initial Developer of the Original Code is Plugged In Software Pty
 * Ltd (http://www.pisoftware.com, mailto:info@pisoftware.com). Portions
 * created by Plugged In Software Pty Ltd are Copyright (C) 2001,2002
 * Plugged In Software Pty Ltd. All Rights Reserved.
 *
 * Contributor(s): N/A.
 *
 * [NOTE: The text of this Exhibit A may differ slightly from the text
 * of the notices in the Source Code files of the Original Code. You
 * should use the text of this Exhibit A rather than the text found in the
 * Original Code Source Code for Your Modifications.]
 *
 */

package org.mulgara.itql;

// Java APIs
import java.sql.*;
import java.text.DateFormat;
import java.util.*;

// Mail api
import javax.mail.*;
import javax.mail.internet.*;

// Third party packages
import org.apache.log4j.*;
import org.apache.soap.*;

// JRDF
import org.jrdf.graph.URIReference;

// Locally written
import org.mulgara.query.Answer;
import org.mulgara.query.QueryException;
import org.mulgara.query.TuplesException;
import org.mulgara.query.Variable;
import org.mulgara.query.rdf.LiteralImpl;
import org.mulgara.util.StringUtil;

/**
 * Collaborator contains methods to store and manipulate annotations for the
 * Collaborator Plug-in. All public methods are exposed as SOAP
 * end-points
 *
 * @created 2002-04-08
 *
 * @author <a href="http://staff.PIsoftware.com/tate/">Tate Jones</a>
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy; 2002-2003 <A href="http://www.PIsoftware.com/">Plugged In
 *      Software Pty Ltd</A>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class Collaborator {

  /** the logging category to log to */
  private final static Logger log = Logger.getLogger(Collaborator.class.getName());

  /** Get line separator. */
  private static final String eol = System.getProperty("line.separator");

  /** Default server name to query */
  private static String SERVER_NAME = "server1";

  /** Default server to query */
  private static String SERVER = "";

  //Determine the localhost name
  static {
    try {
      SERVER = "rmi://" + java.net.InetAddress.getLocalHost().getCanonicalHostName() +
          "/" + SERVER_NAME + "#";
    } catch (java.net.UnknownHostException ex) {
      System.err.print("Unable to determine local host name :" + ex.toString());
    }
  }

  /** The ITQL interpreter Bean. */
  private static ItqlInterpreterBean itqlBean = null;

  /** Default model to use */
  private final static String MODEL = "collaborator";

  /** Namespace for document */
  private final static String NS = "http://mulgara.org/mulgara/Annotation#";

  /** Email address predicate */
  private final static String EMAIL_ADDRESS = NS + "emailAddress";

  /** Nick name predicate */
  private final static String NICK_NAME = NS + "nickName";

  /** Text predicate */
  private final static String TEXT = NS + "text";

  /** documentId predicate */
  private final static String DOCUMENT_ID = NS + "documentId";

  /** annotationId predicate */
  private final static String ANNOTATION_ID_PREFIX = NS + "annotationId";

  /** annotationId predicate */
  private final static String DATE = NS + "lastUpdate";

  /** page number predicate */
  private final static String PAGE_NUMBER = NS + "pageNumber";

  /** x position predicate */
  private final static String X_POSITION = NS + "xPosition";

  /** y position predicate */
  private final static String Y_POSITION = NS + "yPosition";

  /** x anchor predicate */
  private final static String X_ANCHOR = NS + "xAnchor";

  /** y anchor predicate */
  private final static String Y_ANCHOR = NS + "yAnchor";

  /** height predicate */
  private final static String HEIGHT = NS + "height";

  /** width predicate */
  private final static String WIDTH = NS + "width";

  /** Access key prefix */
  private final static String KEY_PREFIX = NS + "key";

  /** Check Mulgara for the collaborator model */
  private final static String CHECK_MODEL =
      "select $model from <" + SERVER + "> " +
      "where $model <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> " +
      "<http://mulgara.org/mulgara#Model> ;";

  /** Create model for collaborator */
  private final static String CREATE_MODEL = "create <" + SERVER + MODEL + ">;";

  /** Delete the collaboration model */
  private final static String DROP_MODEL = "drop <" + SERVER + MODEL + ">;";

  /** Check for registration */
  private final static String CHECK_REGISTRATION =
      "select $key from <" + SERVER + MODEL + "> " + "where $key <" +
      EMAIL_ADDRESS + "> <mailto:~01> ;";

  /** Check for registration */
  private final static String INSERT_REGISTRATION =
      "insert <" + KEY_PREFIX + "~01> <" + EMAIL_ADDRESS +
      "> <mailto:~02> into <" + SERVER + MODEL + ">; " + "insert <" +
      KEY_PREFIX +
      "~01> <" + NICK_NAME + "> '~03' into <" + SERVER + MODEL + ">; ";

  /** Check for access key */
  private final static String CHECK_ACCESS_KEY =
      "select $emailaddress from <" + SERVER + MODEL + "> " + "where <" +
      KEY_PREFIX + "~01> $emailaddress <mailto:~02> ;";

  /** Create annotation */
  private final static String INSERT_ANNOTATION =
      "insert <" + ANNOTATION_ID_PREFIX + "~01> <" + EMAIL_ADDRESS +
      "> <mailto:~02> into <" + SERVER + MODEL + ">; " + "insert <" +
      ANNOTATION_ID_PREFIX + "~01> <" + NICK_NAME + "> '~03' into <" + SERVER +
      MODEL + ">; " + "insert <" + ANNOTATION_ID_PREFIX + "~01> <" + TEXT +
      "> '~04' into <" + SERVER + MODEL + ">; " + "insert <" +
      ANNOTATION_ID_PREFIX + "~01> <" + PAGE_NUMBER + "> '~05' into <" + SERVER +
      MODEL + ">; " + "insert <" + ANNOTATION_ID_PREFIX + "~01> <" + X_POSITION +
      "> '~06' into <" + SERVER + MODEL + ">; " + "insert <" +
      ANNOTATION_ID_PREFIX + "~01> <" + Y_POSITION + "> '~07' into <" + SERVER +
      MODEL + ">; " + "insert <" + ANNOTATION_ID_PREFIX + "~01> <" + X_ANCHOR +
      "> '~08' into <" + SERVER + MODEL + ">; " + "insert <" +
      ANNOTATION_ID_PREFIX + "~01> <" + Y_ANCHOR + "> '~09' into <" + SERVER +
      MODEL + ">; " + "insert <" + ANNOTATION_ID_PREFIX + "~01> <" + HEIGHT +
      "> '~10' into <" + SERVER + MODEL + ">; " + "insert <" +
      ANNOTATION_ID_PREFIX + "~01> <" + WIDTH + "> '~11' into <" + SERVER +
      MODEL + ">; " + "insert <" + ANNOTATION_ID_PREFIX + "~01> <" + DATE +
      "> '~12' into <" + SERVER + MODEL + ">; " + "insert <" +
      ANNOTATION_ID_PREFIX + "~01> <" + DOCUMENT_ID + "> '~13' into <" + SERVER +
      MODEL + ">; ";

  /** Retrieve an annotation */
  private final static String RETRIEVE_ANNOTATION =
      "select $predicate $object from <" + SERVER + MODEL + "> " + "where <" +
      ANNOTATION_ID_PREFIX + "~01> $predicate $object and " + "      <" +
      ANNOTATION_ID_PREFIX + "~01> <" + DOCUMENT_ID + "> '~02';";

  /** Delete an annotation statement */
  private final static String DELETE_ANNOTATION_STATEMENT =
      "delete <" + ANNOTATION_ID_PREFIX + "~01> <~02> ~03 from <" + SERVER +
      MODEL + ">;";

  /** Retrieve all annotations */
  private final static String RETRIEVE_ANNOTATIONS =
      "select $annotationId $emailaddress $nickname $text $pageno " +
      "$x $y $xanchor $yanchor $height $width $lastupdated $documentid " +
      "from <" + SERVER + MODEL + "> " + "where $annotationId <" + DOCUMENT_ID +
      "> '~01' and " + "$annotationId <" + EMAIL_ADDRESS +
      "> $emailaddress and " + "$annotationId <" + NICK_NAME +
      "> $nickname and " + "$annotationId <" + TEXT + "> $text and " +
      "$annotationId <" + PAGE_NUMBER + "> $pageno and " + "$annotationId <" +
      X_POSITION + "> $x and " + "$annotationId <" + Y_POSITION + "> $y and " +
      "$annotationId <" + X_ANCHOR + "> $xanchor and " + "$annotationId <" +
      Y_ANCHOR + "> $yanchor and " + "$annotationId <" + HEIGHT +
      "> $height and " + "$annotationId <" + WIDTH + "> $width and " +
      "$annotationId <" + DATE + "> $lastupdated and " + "$annotationId <" +
      DOCUMENT_ID + "> $documentid ;";

  /** Method identifier used for transaction naming */
  private static final String EXECUTE_COMMAND_TX = "executeCommand";

  /** Method identifier used for transaction naming */
  private static final String ADD_ANNOTATION_TX = "addAnnotation";

  /** Method identifier used for transaction naming */
  private static final String REMOVE_ANNOTATION_TX = "removeAnnotation";
  
  /** For testing purposes only!! */
  public String lastAccessKeyCreated = null;

  /** Date formater */
  DateFormat dateFormat = DateFormat.getInstance();

  /** Track the document updates */
  private final Map<String,Long> documentUpdates = new HashMap<String,Long>();

  /** Track when a user last retrieved annotaions for a document */
  private final Map<String,Long> lastChecked = new HashMap<String,Long>();

  /**
   * The Collaborator will create the ITQL interpreter bean.
   */
  public Collaborator() {

    if (itqlBean == null) {

      itqlBean = new ItqlInterpreterBean();

      //Has the server name been determined
      if (SERVER.length() == 0) log.fatal("Unable to determine localhost name");

      try {
        initializeModel();
      } catch (Exception ex) {
        log.fatal("Unable to initialize collaboration model", ex);
      }
    }
  }


  /**
   * Closes the underlying itql interpreter bean.
   */
  public void close() {
    if (itqlBean != null) {
      itqlBean.close();
      itqlBean = null;
    }
  }

  /**
   * Send an email message via SMTP.
   *
   * @param from The From address.
   * @param to The To address.
   * @param subject The email subject
   * @param content The email content
   */
  private static void sendEmail(String from, String to, String subject,
                                String content) {

    if (log.isDebugEnabled()) {

      log.debug("sending email from :" + from + " to :" + to + eol +
          "subject :" + subject + eol + "Content :" + content);
    }

    //Check for smtp server setting
    Properties props = System.getProperties();

    if ((props.get("mail.smtp.host") == null) ||
        (((String) props.get("mail.smtp.host")).length() == 0)) {

      log.warn("unable to send email from :" + from + " to :" + to + eol +
          "subject :" + subject + eol + "Content :" + content +
          " - SMTP server not configured");

      return;
    }

    try {

      // get the default Session
      javax.mail.Session session =
          javax.mail.Session.getDefaultInstance(props, null);

      // Set up the to address
      Address[] toAddress = InternetAddress.parse(to, false);

      // create a message
      Message message = new MimeMessage(session);

      message.setFrom(new InternetAddress(from));

      message.setSubject(subject);
      message.setHeader("X-Mailer", "Collaborator Registration");
      message.setSentDate(new java.util.Date());
      message.setRecipients(Message.RecipientType.TO, toAddress);
      message.setText(content);

      // Send newMessage
      Transport.send(message);
    } catch (NoSuchProviderException nex) {

      log.error("Cannot send email", nex);
    } catch (AddressException ex) {

      log.error("Cannot send email", ex);
    } catch (MessagingException mex) {

      log.error("Cannot send email", mex);
    }
  }

  /**
   * Retrieve a URL from the server based on a specific client request.
   *
   * The client will open a new web browser and read the contents of the URL.
   * This will method will be used to display messages to user.
   *
   * @param emailAddress user performing the request
   * @param key the current access
   * @param requestType types of requests are:
   *      <dl>
   *      <dt>welcome</dt>    <dd>returns a URL for a welcome page</dd>
   *      <dt>help</dt>       <dd>returns a URL for a help page</dd>
   *      <dt>invalidkey</dt> <dd>returns a URL for an invalid key page</dd>
   *      <dt>keyexpired</dt> <dd>returns a URL for an expired key page</dd>
   *      <dt>invalidemailkey</dt>
   *        <dd>returns a URL for an invalid email and key match</dd>
   *      <dt>reports</dt>    <dd>returns a URL for a reports page</dd>
   *      </dl>
   * @return the URL to be viewed
   * @throws SOAPException  Description of Exception
   */
  public String getURL(String emailAddress, String key, String requestType)
    throws SOAPException {

    return "";
  }

  /**
   * Perform a registation of a new user. A successful registration will result
   * in an email been sent containing an access key to initiate the plug-in for
   * a set period. ?All details are recorded in the Mulgara database.
   *
   * @param emailAddress ?email address to send the access key to
   * @param nickName ? the user name to be registered
   * @return Description of the Returned Value
   * @throws SOAPException excepted errors are :emailAddress and/or nickName
   *      have not been supplied, invalid email address, email address has
   *      already been registered, error sending email
   */
  public synchronized boolean register(String emailAddress, String nickName) throws
      SOAPException {

    boolean registered = false;

    //Check supplied parameters
    if ((emailAddress == null) ||
        (nickName == null) ||
        (emailAddress.length() == 0) ||
        (nickName.length() == 0)) {

      log.warn("Null paramaters supplied for email address or nickname");

      throw new SOAPException(Constants.FAULT_CODE_SERVER, "Email address and nick name must be supplied");
    }

    //TODO : validate email address
    //Check for existing registration
    if (executeCommand(CHECK_REGISTRATION, new String[] {emailAddress}, 0, false)) {

      //new registration required
      // TODO : create a better access key
      String key = String.valueOf(System.currentTimeMillis());

      //For testing purposes only!!! see unit tests
      lastAccessKeyCreated = key;

      //TODO : Calculate expiry date
      registered = executeCommand(INSERT_REGISTRATION, new String[] {key, emailAddress, nickName},
                              "Successfully inserted", true);

      //Send an email with the registration key
      if (registered) {
        sendEmail("foo@localhost", emailAddress,
                       "Collaborator Registration",
                       "Welcome to Mulgara Collaborator. Your access key is " + key);
      }
    } else {

      log.warn("Existing registration already exists for " + emailAddress);

      throw new SOAPException(Constants.FAULT_CODE_SERVER, "Your Email address has already been registered");
    }

    return registered;
  }

  /**
   * Check for a valid access key for the plug-in. Confirms the access key is
   * valid for the supplied email address and the access period has not been
   * exceed. ?
   *
   * @param key evaulation key ?
   * @param emailAddress email address to validate against the access key
   * @return returns true is the evaluation key is valid
   * @throws SOAPException excepted errors are : ?key is invaild, key has expired ?
   */
  public boolean checkAccessKey(String key, String emailAddress) throws SOAPException {

    boolean accessOk = false;

    // Check supplied parameters
    if ((emailAddress == null) ||
        (key == null) ||
        (emailAddress.length() == 0) ||
        (key.length() == 0)) {

      log.warn("Null paramaters supplied for email address and/or key");

      throw new SOAPException(Constants.FAULT_CODE_SERVER, "Email address and key name must be supplied");
    }

    //Check for the access key
    if (executeCommand(CHECK_ACCESS_KEY, new String[] {key, emailAddress}, 1, false)) {

      if (log.isDebugEnabled()) {
        log.debug("Access for key :" + key + " email address :" + emailAddress + " is ok");
      }

      accessOk = true;
    } else {
      log.warn("Invaild access key :" + key + " for email address :" + emailAddress);

      throw new SOAPException(Constants.FAULT_CODE_SERVER,
                            "Invaild access key :" + key + " for email address :" + emailAddress);
    }

    return accessOk;
  }

  /**
   * Adds an annotation to a specified document.
   *
   * A successful addition will result in an unique annotation id returned.
   * An annotation contains the following details.
   *
   * @param emailAddress  emailAddress user performing the request
   * @param key           the current access key
   * @param nickName      nickName the user adding the annotation
   * @param documentId    documentId unique identifier for the document
   * @param text          the annotation text
   * @param pageNo        page number of the annotation
   * @param xAnchor       x position of the annotation
   * @param yAnchor       y position of the annotation
   * @param x             visual x position of the annotation text box
   * @param y             visual y position of the annotation text box
   * @param height        visual height of the annotation text box
   * @param width         visual width of the annotation text box
   * @return returns a non blank annotationId
   * @throws SOAPException excepted errors are:
   *   <ul><li>key is invalid</li>
   *       <li>key has expired</li>
   *       <li>invalid parameters supplied</li></ul>
   */
  public synchronized String addAnnotation(String emailAddress,
                                           String key,
                                           String nickName,
                                           String documentId,
                                           String text,
                                           String pageNo,
                                           String xAnchor,
                                           String yAnchor,
                                           String x,
                                           String y,
                                           String height,
                                           String width) throws SOAPException {

    return addAnnotation(emailAddress, key, nickName, documentId, text,
                         pageNo, xAnchor, yAnchor, x, y, height, width, true);
  }

  /**
   * Remove an annotation from a specified document ?
   *
   * @param emailAddress email address of user performing the request
   * @param key access key for the request email address
   * @param documentId the document containing the annotation
   * @param annotationId the annotation to be removed
   * @return returns true if successful
   * @throws SOAPException Exceptions are : emailAddress and key do not match,
   *      key is invaild, key has expired, invalid parameters supplied,
   *      annotation does not exist
   */
  public synchronized boolean removeAnnotation(
      String emailAddress, String key, String documentId, String annotationId
   ) throws SOAPException {

    return removeAnnotation(emailAddress, key, documentId, annotationId, true);
  }

  /**
   * Edit an exisiting annotation ?
   *
   * @param emailAddress emailAddress user performing the request
   * @param key the current access key
   * @param nickName nickName the user adding the annotation
   * @param documentId documentId unique identifier for the document
   * @param annotationId Description of Parameter
   * @param text the annotation text
   * @param pageNo page number of the annotation
   * @param xAnchor x position of the annotation
   * @param yAnchor y position of the annotation
   * @param x visual x position of the annotation text box
   * @param y visual y position of the annotation text box
   * @param height visual height of the annotation text box
   * @param width visual width of the annotation text box
   * @return returns a non blank annotationId
   * @throws SOAPException excepted errors are : key is invaild, key has
   *      expired, invalid parameters supplied ?
   */
  public synchronized String editAnnotation(String emailAddress, String key,
                                            String nickName, String documentId,
                                            String annotationId, String text,
                                            String pageNo, String xAnchor,
                                            String yAnchor, String x, String y,
                                            String height, String width) throws
      SOAPException {

    boolean added = false;

    // Check the the access key for this user.
    // Will throw an exception if the access key is incorrect
    checkAccessKey(key, emailAddress);

    //Check parameters
    if ( (documentId == null) ||
        (documentId.length() == 0) ||
        (annotationId == null) ||
        (annotationId.length() == 0) ||
        (pageNo == null) ||
        (pageNo.length() == 0) ||
        (xAnchor == null) ||
        (xAnchor.length() == 0) ||
        (yAnchor == null) ||
        (yAnchor.length() == 0) ||
        (x == null) ||
        (x.length() == 0) ||
        (y == null) ||
        (y.length() == 0) ||
        (height == null) ||
        (height.length() == 0) ||
        (width == null) ||
        (width.length() == 0)) {

      log.warn("Invalid paramaters supplied for annotation");

      SOAPException se =
          new SOAPException(Constants.FAULT_CODE_SERVER,
                            "Invalid paramaters supplied for annotation");
      throw se;
    }

    //Correct the text if null
    if (text == null) {

      text = "";
    }

    // Commence a new transaction for the removing and
    // adding of a new annotation.
    beginTransaction("editAnnotation");

    try {

      //Remove the annotation
      if (removeAnnotation(emailAddress, key, documentId, annotationId, false)) {

        //Add the modified annotation
        annotationId =
            addAnnotation(emailAddress, key, nickName, documentId, text, pageNo,
                          xAnchor, yAnchor, x, y, height, width, false);

        //Check the result
        added = (annotationId.length() > 0);
      }
    } catch (SOAPException ex) {

      //Rollback the transaction
      rollback("editAnnotation");

      // re-throw the exception
      throw ex;
    }

    //Check for a new annotation
    if (!added) {

      //Rollback the transaction
      rollback("editAnnotation");

      log.warn("Unable to edit annotation documentId :" + documentId +
               " annotationId :" + annotationId);

      throw new SOAPException(Constants.FAULT_CODE_SERVER,
                            "Unable to edit annotation");
    } else {

      // Commit the changes
      commit("editAnnotation");

      // the flag the update document
      documentHasUpdated(documentId);
    }

    return annotationId;
  }

  /**
   * Retrieve all annotations for a specified docoument
   *
   * @param emailAddress emailAddress user performing the request
   * @param key the current access key
   * @param documentId unique identifier for the document
   * @return Description of the Returned Value
   * @throws SOAPException errors are : emailAddress and key do not match, key
   *      is invalid, key has expired, invalid parameters supplied, invalid
   *      documentId supplied
   */
  public synchronized String retrieveAnnotations(
      String emailAddress, String key, String documentId
  ) throws SOAPException {

    // Check the the access key for this user.
    // Will throw an exception if the access key is incorrect
    checkAccessKey(key, emailAddress);

    //Check parameters
    if ((documentId == null) || (documentId.length() == 0)) {

      log.warn("Invalid paramaters supplied for retrieving annotations");

      throw new SOAPException(Constants.FAULT_CODE_SERVER,
          "Invalid paramaters supplied for retrieving annotations");
    }

    String results = "";

    //Subsitute the parameter into the query
    String query = StringUtil.substituteStrings(RETRIEVE_ANNOTATIONS, documentId);

    if (log.isDebugEnabled()) log.debug("Executing query :" + query);

    //Issue the query
    try {
      results = decode(itqlBean.executeQueryToString(query));
    } catch (Exception ex) {

      log.error("Unable to retrieve annotations for documentId :" + documentId, ex);

      throw new SOAPException(Constants.FAULT_CODE_SERVER, "Unable to retrieve annotations: " + ex.getMessage(), ex);
    }

    // track the document retrieval
    documentHasBeenRetrieved(documentId, emailAddress);

    return results;
  }

  /**
   * Check for annotation updates after a specified date and time.
   *
   * @param emailAddress emailAddress user performing the request
   * @param key the current access key
   * @param documentId unique identifier for the document
   * @return return true if updates have been made
   * @throws SOAPException errors are : emailAddress and key do not match, key
   *      is invalid, key has expired, invalid parameters supplied, invalid
   *      documentId supplied
   */
  public boolean checkAnnotationUpdates(
      String emailAddress, String key, String documentId
  ) throws SOAPException {

    // Check the the access key for this user.
    // Will throw an exception if the access key is incorrect
    checkAccessKey(key, emailAddress);

    // Check parameters
    if ((documentId == null) || (documentId.length() == 0)) {
      log.warn("Invalid paramaters supplied for retrieving annotations");

      throw new SOAPException(Constants.FAULT_CODE_SERVER, "Invalid paramaters supplied for retrieving annotations");
    }

    return hasDocumentUpdated(documentId, emailAddress);
  }

  /**
   * Checks for the collaboration model. If the model does not exist then create it.
   *
   * @return return true if successful with initialisation
   * @throws QueryException Unable to extract or create model
   * @throws SQLException Unable to interate over the model details
   * @throws SOAPException EXCEPTION TO DO
   */
  public synchronized boolean initializeModel() throws QueryException,
      SQLException, SOAPException {

    boolean initialized = false;

    try {
      // ignore string output
      itqlBean.executeQueryToString(CHECK_MODEL);
    } catch (Exception ex) {

      ex.printStackTrace();
    }

    String model = null;

    for (Object obj: itqlBean.executeQueryToList(CHECK_MODEL)) {

      if (obj instanceof Answer) {

        Answer answer = (Answer)obj;

        try {
          //reset cursor
          answer.beforeFirst();

          while (answer.next()) {

            model = ((URIReference)answer.getObject(answer.getColumnIndex(new Variable("model")))).getURI().toString();

            if (log.isDebugEnabled()) log.debug("Found model :" + model);

            int pos = model.indexOf("#");

            if (pos >= 0) {

              if (log.isDebugEnabled()) log.debug("Testing model :" + model.substring(pos + 1));
              initialized = (model.substring(pos + 1).equals(MODEL));
            }
          }
        } catch (TuplesException e) {
          throw new QueryException("Couldn't initialize model", e);
        }
      } else {

        if (obj instanceof String) {

          log.error("Unable to check collaboration model for existance :" + obj + " Using query :" + CHECK_MODEL);
        } else {

          log.error("Unable to check collaboration model for existance Using query :" + CHECK_MODEL);
        }
      }
    }

    if (initialized == false) {
      // Graph has not been initialized
      initialized = createModel();
    } else {
      log.debug("Collaborator model already initialized at " + SERVER + MODEL);
    }

    return initialized;
  }

  /**
   * Creates a new collaborator model
   *
   * @return returns true if successful
   * @throws SOAPException Error creating the model on the server
   */
  public synchronized boolean createModel() throws SOAPException {

    log.warn("creating model " + SERVER + MODEL);
    return executeCommand(CREATE_MODEL, "Successfully created graph", false);
  }

  /**
   * Drops a collaboration model
   *
   * @return returns true is sucessful
   * @throws SOAPException EXCEPTION TO DO
   */
  public synchronized boolean dropModel() throws SOAPException {

    log.warn("dropping model " + SERVER + MODEL);
    return executeCommand(DROP_MODEL, "Successfully dropped graph", false);
  }

  /**
   * Test method for SOAP calls
   *
   * @return a success message
   */
  public String test() {

    return "test";
  }

  /**
   * Remove an annotation from a specified document ?
   *
   * @param emailAddress email address of user performing the request
   * @param key access key for the request email address
   * @param documentId the document containing the annotation
   * @param annotationId the annotation to be removed
   * @param trans insert method into a transaction
   * @return returns true if successful
   * @throws SOAPException Exceptions are : emailAddress and key do not match,
   *      key is invaild, key has expired, invalid parameters supplied,
   *      annotation does not exist
   */
  private boolean removeAnnotation(String emailAddress, String key,
                                   String documentId, String annotationId,
                                   boolean trans) throws SOAPException {

    boolean removed = false;

    // Check the the access key for this user.
    // Will throw an exception if the access key is incorrect
    if (trans) checkAccessKey(key, emailAddress);

    //Check parameters
    if ((documentId == null) ||
        (documentId.length() == 0) ||
        (annotationId == null) ||
        (annotationId.length() == 0)) {

      log.warn("Invalid paramaters supplied for annotation removal");

      throw new SOAPException(Constants.FAULT_CODE_SERVER, "Invalid paramaters supplied for annotation removal");
    }

    //Retrieve the annotation
    Answer annotation = retrieveAnnotation(documentId, annotationId);

    if (annotation != null) {

      String predicate = null;
      String object = null;

      //Begin a transaction to remove the statements
      if (trans) beginTransaction(REMOVE_ANNOTATION_TX);

      try {

        //Extract the predicate and object from the resultSet
        annotation.beforeFirst();

        //Iterate over the rows
        while (annotation.next()) {

          //grab the predicate
          predicate = ((URIReference)annotation.getObject(annotation.getColumnIndex(new Variable("predicate")))).getURI().toString();

          //grab the object - could be a literal or a resource
          int objectIndex = annotation.getColumnIndex(new Variable("object"));
          if (annotation.getObject(objectIndex) instanceof LiteralImpl) {

            object = "'" + encode(((LiteralImpl) annotation.getObject(objectIndex)).getLexicalForm()) + "'";
          } else if (annotation.getObject(objectIndex) instanceof URIReference) {

            object = "<" + ((URIReference)annotation.getObject(objectIndex)).getURI() + ">";
          }

          String[] removalParams = new String[] {annotationId, predicate, object};
          
          // delete the annotation statement
          removed = executeCommand(DELETE_ANNOTATION_STATEMENT,removalParams, "Successfully", false);

          if (!removed) {

            log.warn("Unable to remove annotation statement documentId :" +
                     documentId + " annotationId :" + annotationId +
                     " predicate :" +
                     predicate + " object :" + object);
            throw new SOAPException(Constants.FAULT_CODE_SERVER, "Unable to remove annotation");
          } else {

            // the flag the update document
            documentHasUpdated(documentId);
          }
        }

        // Commit the deletions
        if (trans) commit(REMOVE_ANNOTATION_TX);

      } catch (TuplesException ex) {
        // Rollback the transaction
        if (trans) rollback(REMOVE_ANNOTATION_TX);

        log.error("Unable to locate annotation for removal documentId :" +
                  documentId + " annotationId :" + annotationId, ex);

        throw new SOAPException(Constants.FAULT_CODE_SERVER, "Unable to locate annotation for removal");
      }
    } else {

      log.warn("Unable to locate annotation for removal documentId :" +
               documentId + " annotationId :" + annotationId);

      throw new SOAPException(Constants.FAULT_CODE_SERVER, "Unable to locate annotation for removal");
    }

    return removed;
  }

  /**
   * Adds an annotation to a specified document.
   *
   * A successful addition will result in a unique annotation id returned.
   * An annotation contains the following details:
   *
   * @param emailAddress emailAddress user performing the request
   * @param key the current access key
   * @param nickName nickName the user adding the annotation
   * @param documentId documentId unique identifier for the document
   * @param text the annotation text
   * @param pageNo page number of the annotation
   * @param xAnchor x position of the annotation
   * @param yAnchor y position of the annotation
   * @param x visual x position of the annotation text box
   * @param y visual y position of the annotation text box
   * @param height visual height of the annotation text box
   * @param width visual width of the annotation text box
   * @param trans will this method be contained in a transaction
   * @return returns a non blank annotationId
   * @throws SOAPException excepted errors are:
   *   <ul>
   *   <li>key is invalid</li>
   *   <li>key has expired</li>
   *   <li>invalid parameters supplied</li>
   *   </ul>
   */
  private String addAnnotation(String emailAddress, String key,
                               String nickName, String documentId, String text,
                               String pageNo,
                               String xAnchor, String yAnchor, String x,
                               String y, String height,
                               String width, boolean trans
  ) throws SOAPException {

    String annotationId = "";

    // Check the the access key for this user.
    // Will throw an exception if the access key is incorrect
    // Only check if not nested in another transaction
    if (trans) checkAccessKey(key, emailAddress);

    // Check parameters
    if ((documentId == null) ||
        (documentId.length() == 0) ||
        (pageNo == null) ||
        (pageNo.length() == 0) ||
        (xAnchor == null) ||
        (xAnchor.length() == 0) ||
        (yAnchor == null) ||
        (yAnchor.length() == 0) ||
        (x == null) ||
        (x.length() == 0) ||
        (y == null) ||
        (y.length() == 0) ||
        (height == null) ||
        (height.length() == 0) ||
        (width == null) ||
        (width.length() == 0)) {

      log.warn("Invalid paramaters supplied for annotation");

      throw new SOAPException(Constants.FAULT_CODE_SERVER, "Invalid paramaters supplied for annotation");
    }

    // Correct the text if null
    if (text == null) text = "";

    // Create the supporting attributes
    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
    String lastUpdated = dateFormat.format(cal.getTime());

    // TODO : create a better annotation id
    annotationId = String.valueOf(System.currentTimeMillis());

    if (log.isDebugEnabled()) {
      log.debug("Adding annotation with documentId :" + documentId +
                " email address :" + emailAddress + " nickName :" + nickName +
                " text :" + text + " pageNo :" + pageNo + " xAnchor :" + xAnchor +
                " yAnchor :" + yAnchor + " x :" + x + " y:" + y +
                " height :" + height + " width :" + width +
                " lastUpdated :" + lastUpdated + " annotationId :" + annotationId);
    }

    // Start a transaction for all the inserts
    if (trans) beginTransaction(ADD_ANNOTATION_TX);

    String[] annotationParams = new String[] {
        annotationId, emailAddress, nickName, encode(text), pageNo,
        xAnchor, yAnchor, x, y, height, width, lastUpdated, documentId
    };

    // Clear the annotationId if unsuccessful - handling transactions at this level
    if (!executeCommand(INSERT_ANNOTATION, annotationParams, "Successfully inserted", false)) {

      annotationId = "";

      // Rollback the inserts
      if (trans) rollback(ADD_ANNOTATION_TX);

    } else {

      // Commit the inserts
      if (trans) commit(ADD_ANNOTATION_TX);

      // the flag the update document
      documentHasUpdated(documentId);
    }

    return annotationId;
  }

  /**
   * Execute an iTQL query and checks for a specific response
   *
   * @param command iTQLQuery
   * @param substituteArray Description of Parameter
   * @param rows the expected rows to be returned
   * @param trans Insert command within a transaction
   * @return returns true if the expect result is found in the response
   * @throws SOAPException EXCEPTION TO DO
   */
  private boolean executeCommand(String command, String[] substituteArray,
                                 int rows, boolean trans) throws SOAPException {

    return executeCommand(command, substituteArray, String.valueOf(rows), trans);
  }

  /**
   * Execute an iTQL query and checks for a specific response
   *
   * @param command iTQLQuery
   * @param substituteArray Description of Parameter
   * @param expectedResult the expected result
   * @param trans Insert command within a transaction
   * @return returns true if the expect result is found in the response
   * @throws SOAPException EXCEPTION TO DO
   */
  private boolean executeCommand(
      String command, String[] substituteArray, String expectedResult, boolean trans
  ) throws SOAPException {

    boolean successful = false;

    //Perform any subsitutions
    if (substituteArray != null) {
      command = StringUtil.substituteStrings(command, substituteArray);
    }

    String message = null;

    if (log.isDebugEnabled()) log.debug("Executing command :" + command);

    // Commence a new transaction if required
    if (trans) beginTransaction(EXECUTE_COMMAND_TX);

    // Issue the itql command
    for (Object obj: itqlBean.executeQueryToList(command)) {

      // Is the result a message?
      if (obj instanceof String) {

        message = (String)obj;

        if (log.isDebugEnabled()) log.debug("returned message :" + message);

        // Check the outcome against the expect result
        successful = ((message != null) && (message.indexOf(expectedResult) >= 0));

        // break if failure occurs
        if (!successful) {
          log.error("Collaborator command: " + command + "\nmessage: " + message + "\nexpected: " + expectedResult);
          break;
        }
      }

      // Is the result an answer?
      if (obj instanceof Answer) {

        try {

          // Check the number of rows returned
          long solutionCount = ((Answer)obj).getRowUpperBound();

          if (log.isDebugEnabled()) log.debug("found answer :" + solutionCount);

          message = String.valueOf(solutionCount);

          // Check the outcome against the expect result
          successful = ((message != null) && (message.indexOf(expectedResult) >= 0));

          // break if failure occurs
          if (!successful) break;

        } catch (TuplesException ex) {

          log.error("Error executing command :" + command, ex);
          // Rollback the changes
          if (trans) rollback(EXECUTE_COMMAND_TX);
          return false;
        }
      }
    }

    // Commit the changes whether successful or not
    if (trans) commit(EXECUTE_COMMAND_TX);

    if (log.isDebugEnabled()) {
      log.debug("Command :" + command + "message :" + message + " result :" + successful);
    }

    return successful;
  }

  /**
   * Execute iTQL command and checks for a specific response
   *
   * @param command iTQL command
   * @param expectedResult expected result to be checked
   * @param trans Insert command within a transaction
   * @return returns the resultSet null means no rows were found
   * @throws SOAPException EXCEPTION TO DO
   */
  private boolean executeCommand(String command, String expectedResult,
                                 boolean trans) throws SOAPException {

    return executeCommand(command, null, expectedResult, trans);
  }

  /**
   * Retrieve all statements for a specific annotationId
   *
   * @param documentId unique documentId
   * @param annotationId AnnotationId for a document
   * @return Return a resultSet null for no rows
   */
  private Answer retrieveAnnotation(String documentId, String annotationId) {

    //Subsitute the parameter into the query
    String query =
        StringUtil.substituteStrings(RETRIEVE_ANNOTATION,
                                     new String[] {annotationId, documentId});

    Answer answer = null;

    if (log.isDebugEnabled()) {

      log.debug("Executing query :" + query);
    }

    //Issue the query command
    for (Object obj: itqlBean.executeQueryToList(query)) {

      //Check for an answer
      if (obj instanceof Answer) {

        try {

          //Check the number of rows returned
          long solutionCount = ( (Answer) obj).getRowUpperBound();

          if (log.isDebugEnabled()) {

            log.debug("found answer :" + solutionCount);
          }

          //return the resultset if there are rows
          if (solutionCount > 0) {

            answer = (Answer) obj;
          }
        } catch (TuplesException ex) {

          log.error("Error executing query :" + query, ex);
        }
      }
    }

    return answer;
  }

  /**
   * Description of the Method
   *
   * @param documentId Description of Parameter
   */
  private void documentHasUpdated(String documentId) {

    documentUpdates.put(documentId, new Long(System.currentTimeMillis()));

    if (log.isDebugEnabled()) {

      log.debug("DocumentId : " + documentId + " has been updated ");
    }
  }

  /**
   * Description of the Method
   *
   * @param documentId Description of Parameter
   * @param emailAddress Description of Parameter
   */
  private void documentHasBeenRetrieved(String documentId, String emailAddress) {

    lastChecked.put(documentId + emailAddress,
                    new Long(System.currentTimeMillis()));

    if (log.isDebugEnabled()) {

      log.debug("DocumentId : " + documentId + " has been retrieved by " +
                emailAddress);
    }
  }

  /**
   * Description of the Method
   *
   * @param documentId Description of Parameter
   * @param emailAddress Description of Parameter
   * @return Description of the Returned Value
   */
  private boolean hasDocumentUpdated(String documentId, String emailAddress) {

    boolean updated = false;
    long lastCheckedTime = 0;
    long lastUpdatedTime = System.currentTimeMillis();

    //Find out when the user performed the last retrieval for a document
    if (lastChecked.containsKey(documentId + emailAddress)) {

      lastCheckedTime = lastChecked.get(documentId + emailAddress).longValue();
    }

    //Find out the last updated time
    if (documentUpdates.containsKey(documentId)) {

      // has been updated
      lastUpdatedTime = documentUpdates.get(documentId).longValue();
    } else {

      // No modification has occur for this document.
      // ie. server has been started without any updates
      documentHasUpdated(documentId);
    }

    //Is the last check time less than the last updated time?
    updated = (lastCheckedTime < lastUpdatedTime);

    if (log.isDebugEnabled()) {

      log.debug("DocumentId : " + documentId +
                " has been checked for updates " + "with result :" + updated +
                " last checked :" + lastCheckedTime + " document updated :" +
                lastUpdatedTime);
    }

    return updated;
  }

  /**
   * Commence a new transaction for this session
   *
   * @param name Name of this transaction
   * @throws SOAPException Unable to create a transaction on the server
   */
  private void beginTransaction(String name) throws SOAPException {

    if (log.isDebugEnabled()) log.debug("Begin transaction : " + name);

    try {
      itqlBean.beginTransaction(name);
    } catch (QueryException ex) {
      log.error("Unable to obtain a transaction for the Mulgara Collaborator <" + name + ">", ex);
      throw new SOAPException(Constants.FAULT_CODE_SERVER,
          "Unable to obtain a transaction for the Mulgara Collaborator <" + name + ">");
    }
  }

  /**
   * Commit the current transaction
   *
   * @param name Name of the transaction to commit
   * @throws SOAPException Unable to commit the transaction on the server
   */
  private void commit(String name) throws SOAPException {

    if (log.isDebugEnabled()) log.debug("Commit transaction : " + name);

    try {
      itqlBean.commit(name);
    } catch (QueryException ex) {
      log.error("Unable to obtain a transaction for the Mulgara Collaborator <" + name + ">", ex);
      throw new SOAPException(Constants.FAULT_CODE_SERVER,
          "Unable to commit changes for the Mulgara Collaborator <" + name + ">");
    }
  }

  /**
   * Rollback the current transaction
   *
   * @param name Name of the transaction to roll back
   * @throws SOAPException Unable to roll back the transaction
   */
  private void rollback(String name) throws SOAPException {

    if (log.isDebugEnabled()) log.debug("Rollback transaction : " + name);

    try {
      itqlBean.rollback(name);
    } catch (QueryException ex) {
      log.error("Unable to rollback a transaction for the Mulgara Collaborator <" + name + ">", ex);

      throw new SOAPException(Constants.FAULT_CODE_SERVER,
          "Unable to rollback a transaction for the Mulgara Collaborator <" + name + ">");
    }
  }

  /**
   * Encode a literal string
   *
   * @param value String to encode
   * @return A new encoded string 
   */
  private String encode(String value) {

    return StringUtil.replaceStringWithString(value, "'", "\\'");
  }

  /**
   * Decode a literal string
   *
   * @param value The string to decode
   * @return A new decoded string
   */
  private String decode(String value) {

    return StringUtil.replaceStringWithString(value, "\\'", "'");
  }
}
