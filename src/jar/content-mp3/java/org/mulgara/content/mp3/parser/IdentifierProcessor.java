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

package org.mulgara.content.mp3.parser;

// Java packages
import java.util.HashMap;
import java.net.URI;
import java.net.URISyntaxException;

// Log4j
import org.apache.log4j.*;

// MP3 Library
import org.blinkenlights.id3.v2.*;

// JRDF
import org.jrdf.graph.*;

// Internal
import org.mulgara.content.mp3.parser.exception.*;

/**
 * Resolves an identifier into an RDF type.
 *
 * @created 2004-07-29
 *
 * @author Mark Ludlow
 *
 * @version $Revision: 1.10 $
 *
 * @modified $Date: 2005/01/13 11:34:53 $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @company Tucana Technologies
 *
 * @copyright &copy; 2004
 *   <A href="http://www.tucanatech.com/">Tucana Technologies</A>
 *
 * @licence <A href="{@docRoot}/LICENCE">License description</A>
 */
public final class IdentifierProcessor {

  /** The category to log to. */
  @SuppressWarnings("unused")
  private static final Logger log = Logger.getLogger(IdentifierProcessor.class);

  /** The identifiers we expect to come across in ID3 tags */
  public static final String AENC = AENCID3V2Frame.class.toString();
  public static final String APIC = APICID3V2Frame.class.toString();
  public static final String COMM = COMMID3V2Frame.class.toString();
  public static final String COMR = COMRID3V2Frame.class.toString();
  public static final String ENCR = ENCRID3V2Frame.class.toString();
  public static final String EQUA = EQUAID3V2Frame.class.toString();
  public static final String ETCO = ETCOID3V2Frame.class.toString();
  public static final String GEOB = GEOBID3V2Frame.class.toString();
  public static final String GRID = GRIDID3V2Frame.class.toString();
  public static final String IPLS = IPLSID3V2Frame.class.toString();
  public static final String LINK = LINKID3V2Frame.class.toString();
  public static final String MCDI = MCDIID3V2Frame.class.toString();
  public static final String MLLT = MLLTID3V2Frame.class.toString();
  public static final String OWNE = OWNEID3V2Frame.class.toString();
  public static final String PCNT = PCNTID3V2Frame.class.toString();
  public static final String POPM = POPMID3V2Frame.class.toString();
  public static final String POSS = POSSID3V2Frame.class.toString();
  public static final String PRIV = PRIVID3V2Frame.class.toString();
  public static final String RBUF = RBUFID3V2Frame.class.toString();
  public static final String RVAD = RVADID3V2Frame.class.toString();
  public static final String RVRB = RVRBID3V2Frame.class.toString();
  public static final String SYLT = SYLTID3V2Frame.class.toString();
  public static final String SYTC = SYTCID3V2Frame.class.toString();
  public static final String TALB = TALBTextInformationID3V2Frame.class.toString();
  public static final String TBPM = TBPMTextInformationID3V2Frame.class.toString();
  public static final String TCOM = TCOMTextInformationID3V2Frame.class.toString();
  public static final String TCON = TCONTextInformationID3V2Frame.class.toString();
  public static final String TCOP = TCOPTextInformationID3V2Frame.class.toString();
  public static final String TDAT = TDATTextInformationID3V2Frame.class.toString();
  public static final String TDLY = TDLYTextInformationID3V2Frame.class.toString();
  public static final String TENC = TENCTextInformationID3V2Frame.class.toString();
  public static final String TEXT = TEXTTextInformationID3V2Frame.class.toString();
  public static final String TFLT = TFLTTextInformationID3V2Frame.class.toString();
  public static final String TIME = TIMETextInformationID3V2Frame.class.toString();
  public static final String TIT1 = TIT1TextInformationID3V2Frame.class.toString();
  public static final String TIT2 = TIT2TextInformationID3V2Frame.class.toString();
  public static final String TIT3 = TIT3TextInformationID3V2Frame.class.toString();
  public static final String TKEY = TKEYTextInformationID3V2Frame.class.toString();
  public static final String TLAN = TLANTextInformationID3V2Frame.class.toString();
  public static final String TLEN = TLENTextInformationID3V2Frame.class.toString();
  public static final String TMED = TMEDTextInformationID3V2Frame.class.toString();
  public static final String TOAL = TOALTextInformationID3V2Frame.class.toString();
  public static final String TOFN = TOFNTextInformationID3V2Frame.class.toString();
  public static final String TOLY = TOLYTextInformationID3V2Frame.class.toString();
  public static final String TOPE = TOPETextInformationID3V2Frame.class.toString();
  public static final String TORY = TORYTextInformationID3V2Frame.class.toString();
  public static final String TOWN = TOWNTextInformationID3V2Frame.class.toString();
  public static final String TPE1 = TPE1TextInformationID3V2Frame.class.toString();
  public static final String TPE2 = TPE2TextInformationID3V2Frame.class.toString();
  public static final String TPE3 = TPE3TextInformationID3V2Frame.class.toString();
  public static final String TPE4 = TPE4TextInformationID3V2Frame.class.toString();
  public static final String TPOS = TPOSTextInformationID3V2Frame.class.toString();
  public static final String TPUB = TPUBTextInformationID3V2Frame.class.toString();
  public static final String TRCK = TRCKTextInformationID3V2Frame.class.toString();
  public static final String TRDA = TRDATextInformationID3V2Frame.class.toString();
  public static final String TRSN = TRSNTextInformationID3V2Frame.class.toString();
  public static final String TRSO = TRSOTextInformationID3V2Frame.class.toString();
  public static final String TSIZ = TSIZTextInformationID3V2Frame.class.toString();
  public static final String TSRC = TSRCTextInformationID3V2Frame.class.toString();
  public static final String TSSE = TSSETextInformationID3V2Frame.class.toString();
  public static final String TXXX = TXXXTextInformationID3V2Frame.class.toString();
  public static final String TYER = TYERTextInformationID3V2Frame.class.toString();
  public static final String UFID = UFIDID3V2Frame.class.toString();
  public static final String USER = USERID3V2Frame.class.toString();
  public static final String USLT = USLTID3V2Frame.class.toString();
  public static final String WCOM = WCOMUrlLinkID3V2Frame.class.toString();
  public static final String WCOP = WCOPUrlLinkID3V2Frame.class.toString();
  public static final String WOAF = WOAFUrlLinkID3V2Frame.class.toString();
  public static final String WOAR = WOARUrlLinkID3V2Frame.class.toString();
  public static final String WOAS = WOASUrlLinkID3V2Frame.class.toString();
  public static final String WORS = WORSUrlLinkID3V2Frame.class.toString();
  public static final String WPAY = WPAYUrlLinkID3V2Frame.class.toString();
  public static final String WPUB = WPUBUrlLinkID3V2Frame.class.toString();
  public static final String WXXX = WXXXUrlLinkID3V2Frame.class.toString();
  public static final String MP3_TYPE = "MP3_TYPE";
  public static final String MP3_URI = "URI";
  public static final String ID3_NAMESPACE = "http://mulgara.org/mulgara/id3#";

  /** Mapping of identifiers to their RDF equivalent */
  private HashMap<String,URIReference> idMap;

  /**
   * Constructor.
   */
  public IdentifierProcessor() {

    // Set up the hash map
    idMap = new HashMap<String,URIReference>();
  }

  /**
   * Creates the mappings of identifiers to their RDF equivalent for a specific
   * graph.
   *
   * @param graph The graph to create a the mappings in
   *
   * @throws IdentifierException
   */
  public void createMappings(Graph graph) throws IdentifierException {

    if (idMap.isEmpty()) {

      // If the map is empty then initialise it

      // Get the element factory for the graph
      GraphElementFactory factory = graph.getElementFactory();

      try {

        // Store the mappings of identifiers to properties
        idMap.put(AENC,
            factory.createResource(new URI(ID3_NAMESPACE + "audioEncoding")));
        idMap.put(APIC,
            factory.createResource(new URI(ID3_NAMESPACE + "attachedPicture")));
//        idMap.put(ASPI,
//            factory.createResource(new URI(ID3_NAMESPACE +
//            "audioSeekPointIndex")));
        idMap.put(COMM,
            factory.createResource(new URI(ID3_NAMESPACE + "comment")));
        idMap.put(COMR,
            factory.createResource(new URI(ID3_NAMESPACE + "commercialFrame")));
//        idMap.put(CRM,
//            factory.createResource(new URI(ID3_NAMESPACE + "crm")));
        idMap.put(ENCR,
            factory.createResource(new URI(ID3_NAMESPACE + "encryption")));
//        idMap.put(EQU2,
//            factory.createResource(new URI(ID3_NAMESPACE + "equalisation2")));
        idMap.put(EQUA,
            factory.createResource(new URI(ID3_NAMESPACE + "equalisation")));
        idMap.put(ETCO,
            factory.createResource(new URI(ID3_NAMESPACE + "timingCodes")));
        idMap.put(GEOB,
            factory.createResource(new URI(ID3_NAMESPACE + "encapsulatedObject")));
        idMap.put(GRID,
            factory.createResource(new URI(ID3_NAMESPACE +
            "groupIdentification")));
        idMap.put(IPLS,
            factory.createResource(new URI(ID3_NAMESPACE + "ipls")));
        idMap.put(LINK,
            factory.createResource(new URI(ID3_NAMESPACE + "linkedInfo")));
        idMap.put(MCDI,
            factory.createResource(new URI(
            "http://musicbrainz.org/mm/mm-2.1#trmid")));
        idMap.put(MLLT,
            factory.createResource(new URI(ID3_NAMESPACE + "lookupTable")));
        idMap.put(OWNE,
            factory.createResource(new URI(ID3_NAMESPACE + "ownershipFrame")));
        idMap.put(PCNT,
            factory.createResource(new URI(ID3_NAMESPACE + "playCounter")));
//        idMap.put(PIC,
//            factory.createResource(new URI(ID3_NAMESPACE + "pic")));
        idMap.put(POPM,
            factory.createResource(new URI(ID3_NAMESPACE + "popularimeter")));
        idMap.put(POSS,
            factory.createResource(new URI(ID3_NAMESPACE + "synchFrame")));
        idMap.put(PRIV,
            factory.createResource(new URI(ID3_NAMESPACE + "privateFrame")));
        idMap.put(RBUF,
            factory.createResource(new URI(ID3_NAMESPACE + "recommendedBuffer")));
//        idMap.put(RVA2,
//            factory.createResource(new URI(ID3_NAMESPACE + "relativeVolumeAdj2")));
        idMap.put(RVAD,
            factory.createResource(new URI(ID3_NAMESPACE + "relativeVolumeAdj")));
        idMap.put(RVRB,
            factory.createResource(new URI(ID3_NAMESPACE + "reverb")));
//        idMap.put(SEEK,
//            factory.createResource(new URI(ID3_NAMESPACE + "seekFrame")));
//        idMap.put(SIGN,
//            factory.createResource(new URI(ID3_NAMESPACE + "signatureFrame")));
        idMap.put(SYLT,
            factory.createResource(new URI(ID3_NAMESPACE + "synchLyricText")));
        idMap.put(SYTC,
            factory.createResource(new URI(ID3_NAMESPACE + "synchTempoCodes")));
        idMap.put(TALB,
            factory.createResource(new URI(ID3_NAMESPACE + "title")));
        idMap.put(TBPM,
            factory.createResource(new URI(ID3_NAMESPACE + "bpm")));
        idMap.put(TCOM,
            factory.createResource(new URI(
            "http://musicbrainz.org/mm/mm-2.1#sortName")));
        idMap.put(TCON,
            factory.createResource(new URI(
            "http://musicbrainz.org/mm/mm-2.1#releaseType")));
        idMap.put(TCOP,
            factory.createResource(new URI(ID3_NAMESPACE + "copyrightMsg")));
        idMap.put(TDAT,
            factory.createResource(new URI(ID3_NAMESPACE + "tdat")));
//        idMap.put(TDEN,
//            factory.createResource(new URI(ID3_NAMESPACE + "encodingTime")));
        idMap.put(TDLY,
            factory.createResource(new URI(ID3_NAMESPACE + "playlistDelay")));
//        idMap.put(TDOR,
//            factory.createResource(new URI(ID3_NAMESPACE +
//            "originalReleaseTime")));
//        idMap.put(TDRC,
//            factory.createResource(new URI(ID3_NAMESPACE + "recordingTime")));
//        idMap.put(TDRL,
//            factory.createResource(new URI(ID3_NAMESPACE + "releaseTime")));
//        idMap.put(TDTG,
//            factory.createResource(new URI(ID3_NAMESPACE + "taggingTime")));
        idMap.put(TENC,
            factory.createResource(new URI(ID3_NAMESPACE + "encodedBy")));
        idMap.put(TEXT,
            factory.createResource(new URI(ID3_NAMESPACE + "lyricist")));
        idMap.put(TFLT,
            factory.createResource(new URI(ID3_NAMESPACE + "fileType")));
        idMap.put(TIME,
            factory.createResource(new URI(
            "http://musicbrainz.org/mm/mm-2.1#duration")));
//        idMap.put(TIPL,
//            factory.createResource(new URI(ID3_NAMESPACE + "involvedPeople")));
        idMap.put(TIT1,
            factory.createResource(new URI(ID3_NAMESPACE + "contentGroupDesc")));
        idMap.put(TIT2,
            factory.createResource(new URI(ID3_NAMESPACE + "titleDesc")));
        idMap.put(TIT3,
            factory.createResource(new URI(ID3_NAMESPACE + "subtitleDesc")));
        idMap.put(TKEY,
            factory.createResource(new URI(ID3_NAMESPACE + "initialKey")));
        idMap.put(TLAN,
            factory.createResource(new URI(ID3_NAMESPACE + "language")));
        idMap.put(TLEN,
            factory.createResource(new URI(
            "http://musicbrainz.org/mm/mm-2.1#duration")));
//        idMap.put(TMCL,
//            factory.createResource(new URI(ID3_NAMESPACE + "musicianCredits")));
        idMap.put(TMED,
            factory.createResource(new URI(ID3_NAMESPACE + "mediaType")));
//        idMap.put(TMOO,
//            factory.createResource(new URI(ID3_NAMESPACE + "mood")));
        idMap.put(TOAL,
            factory.createResource(new URI(ID3_NAMESPACE + "originalTitle")));
        idMap.put(TOFN,
            factory.createResource(new URI(ID3_NAMESPACE + "originalFilename")));
        idMap.put(TOLY,
            factory.createResource(new URI(ID3_NAMESPACE + "originalLyricist")));
        idMap.put(TOPE,
            factory.createResource(new URI(ID3_NAMESPACE + "originalPerformer")));
        idMap.put(TORY,
            factory.createResource(new URI(ID3_NAMESPACE + "tory")));
        idMap.put(TOWN,
            factory.createResource(new URI(ID3_NAMESPACE + "licensee")));
        idMap.put(TPE1,
            factory.createResource(new URI(ID3_NAMESPACE + "leadPerformer")));
        idMap.put(TPE2,
            factory.createResource(new URI(ID3_NAMESPACE + "accompaniment")));
        idMap.put(TPE3,
            factory.createResource(new URI(ID3_NAMESPACE + "conductor")));
        idMap.put(TPE4,
            factory.createResource(new URI(ID3_NAMESPACE + "interpreter")));
        idMap.put(TPOS,
            factory.createResource(new URI(ID3_NAMESPACE + "partOfSet")));
//        idMap.put(TPRO,
//            factory.createResource(new URI(ID3_NAMESPACE + "producedNotice")));
        idMap.put(TPUB,
            factory.createResource(new URI(ID3_NAMESPACE + "publisher")));
        idMap.put(TRCK,
            factory.createResource(new URI(
            "http://musicbrainz.org/mm/mm-2.1#trackNum")));
        idMap.put(TRDA,
            factory.createResource(new URI(ID3_NAMESPACE + "trda")));
        idMap.put(TRSN,
            factory.createResource(new URI(ID3_NAMESPACE + "IRSName")));
        idMap.put(TRSO,
            factory.createResource(new URI(ID3_NAMESPACE + "IRSOwner")));
        idMap.put(TSIZ,
            factory.createResource(new URI(ID3_NAMESPACE + "tsiz")));
//        idMap.put(TSOA,
//            factory.createResource(new URI(ID3_NAMESPACE + "albumSortOrder")));
//        idMap.put(TSOP,
//            factory.createResource(new URI(ID3_NAMESPACE + "performerSortOrder")));
//        idMap.put(TSOT,
//            factory.createResource(new URI(ID3_NAMESPACE + "titleSortOrder")));
        idMap.put(TSRC,
            factory.createResource(new URI("http://mulgara.org#ISRC")));
        idMap.put(TSSE,
            factory.createResource(new URI(ID3_NAMESPACE + "encodingSettings")));
//        idMap.put(TSST,
//            factory.createResource(new URI(ID3_NAMESPACE + "setSubtitle")));
        idMap.put(TXXX,
            factory.createResource(new URI(ID3_NAMESPACE + "txxx")));
        idMap.put(TYER,
            factory.createResource(new URI(ID3_NAMESPACE + "releaseYear")));
        idMap.put(UFID,
            factory.createResource(new URI(ID3_NAMESPACE + "uniqueFileID")));
        idMap.put(USER,
            factory.createResource(new URI(ID3_NAMESPACE + "useTerms")));
        idMap.put(USLT,
            factory.createResource(new URI(ID3_NAMESPACE + "unsynchLyrics")));
        idMap.put(WCOM,
            factory.createResource(new URI(ID3_NAMESPACE + "commercialInfo")));
        idMap.put(WCOP,
            factory.createResource(new URI(ID3_NAMESPACE + "copyrightInfo")));
        idMap.put(WOAF,
            factory.createResource(new URI(ID3_NAMESPACE + "fileWebpage")));
        idMap.put(WOAR,
            factory.createResource(new URI(ID3_NAMESPACE + "artistWebpage")));
        idMap.put(WOAS,
            factory.createResource(new URI(ID3_NAMESPACE + "audioSourceWebpage")));
        idMap.put(WORS,
            factory.createResource(new URI(ID3_NAMESPACE + "IRSWebpage")));
        idMap.put(WPAY,
            factory.createResource(new URI(ID3_NAMESPACE + "payment")));
        idMap.put(WPUB,
            factory.createResource(new URI(ID3_NAMESPACE + "publisherWebpage")));
        idMap.put(WXXX,
            factory.createResource(new URI(ID3_NAMESPACE + "wxxx")));

        idMap.put(MP3_TYPE,
            factory.createResource(new URI(ID3_NAMESPACE + "MP3")));
        idMap.put(MP3_URI,
            factory.createResource(new URI(ID3_NAMESPACE + "uri")));

        idMap.put("unknown",
            factory.createResource(new URI(ID3_NAMESPACE + "unknown")));
      }
      catch (URISyntaxException uriSyntaxException) {

        // The only way this exception can occur is if we have hard coded a
        // predicate URI wrongly in which case it requires changing before the class
        // will be valid.
        throw new IdentifierException(
            "Unable to construct predicate mappings due to " +
            "incorrect format for predicates.",
            uriSyntaxException);
      }
      catch (GraphElementFactoryException graphElementFactoryException) {

        throw new IdentifierException(
            "Unable to construct predicate mappings due to " +
            "a failure to construct graph elements.",
            graphElementFactoryException);

      }
    }
  }

  /**
   * Resolves a given identifier into an RDF type.
   *
   * @param identifier The identifier whose type we are determining
   *
   * @return The resolved RDF type
   */
  public PredicateNode resolveIdentifier(String identifier) {

    // Look up the RDF predicate by identifier
    PredicateNode predicate = (PredicateNode) idMap.get(identifier);

    if (predicate == null) {

      // If the predicate was not found, use the unknown predicate
      predicate = (PredicateNode) idMap.get("unknown");
    }

    return predicate;
  }

  /**
   * Resolves a given identifier into literal.
   *
   * @param frame The frame to extract the value from.
   * @param factory The factory to use to create the literal.
   *
   * @return The resolved RDF literal.
   */
  public ObjectNode resolveLiteral(ID3V2Frame frame,
      GraphElementFactory factory) throws GraphElementFactoryException {

    String newValue = "";
    StringBuffer newValueBuffer = new StringBuffer();

    if (frame instanceof AENCID3V2Frame) {
      newValue = ((AENCID3V2Frame) frame).getOwnerIdentifier();
    }
    else if (frame instanceof APICID3V2Frame) {
      newValue = ((APICID3V2Frame) frame).getDescription();
    }
    else if (frame instanceof COMMID3V2Frame) {
      newValue = ((COMMID3V2Frame) frame).getActualText();
    }
    else if (frame instanceof COMRID3V2Frame) {
      COMRID3V2Frame newFrame = (COMRID3V2Frame) frame;
      newValue = "Price=" + newFrame.getPrice() + ", Valid Until=" +
          newFrame.getValidUntilDate() + ", Contact URL=" +
          newFrame.getContactUrl() + ", Received As=" +
          newFrame.getReceivedAsFormat() + ", Name Of Seller=" +
          newFrame.getNameOfSeller() + ", Description=" +
          newFrame.getDescription();
    }
    else if (frame instanceof ENCRID3V2Frame) {
      // Use default
    }
    else if (frame instanceof EQUAID3V2Frame) {
      // Use default
    }
    else if (frame instanceof ETCOID3V2Frame) {
      // Use default
    }
    else if (frame instanceof GEOBID3V2Frame) {
      // Use default
    }
    else if (frame instanceof GRIDID3V2Frame) {
      // Use default
    }
    else if (frame instanceof IPLSID3V2Frame) {
      // Use default
    }
    else if (frame instanceof LINKID3V2Frame) {
      // Use default
    }
    else if (frame instanceof MCDIID3V2Frame) {
      newValue = new String(((MCDIID3V2Frame) frame).getCDTOCData());
    }
    else if (frame instanceof OWNEID3V2Frame) {
      // Use default
    }
    else if (frame instanceof PCNTID3V2Frame) {
      newValue = Integer.toString(((PCNTID3V2Frame) frame).getPlayCount());
    }
    else if (frame instanceof POPMID3V2Frame) {
      // Use default
    }
    else if (frame instanceof POSSID3V2Frame) {
      // Use default
    }
    else if (frame instanceof PRIVID3V2Frame) {
      // Use default
    }
    else if (frame instanceof RBUFID3V2Frame) {
      // Use default
    }
    else if (frame instanceof RVADID3V2Frame) {
      // Use default
    }
    else if (frame instanceof RVRBID3V2Frame) {
      // Use default
    }
    else if (frame instanceof SYLTID3V2Frame) {
      // Use default
    }
    else if (frame instanceof SYTCID3V2Frame) {
      // Use default
    }
    else if (frame instanceof TALBTextInformationID3V2Frame) {
      newValue = ((TALBTextInformationID3V2Frame) frame).getAlbum();
    }
    else if (frame instanceof TBPMTextInformationID3V2Frame) {
      newValue = Integer.toString(((TBPMTextInformationID3V2Frame) frame).
          getBeatsPerMinute());
    }
    else if (frame instanceof TCOMTextInformationID3V2Frame) {
      String[] composers = ((TCOMTextInformationID3V2Frame) frame).getComposers();
      for (int index = 0; index < composers.length; index++) {
        newValueBuffer.append(composers[index]);
        if (index < composers.length - 1) {
          newValueBuffer.append(", ");
        }
      }
      newValue = newValueBuffer.toString();
    }
    else if (frame instanceof TCONTextInformationID3V2Frame) {
      ContentType contentType = ((TCONTextInformationID3V2Frame) frame).
          getContentType();
      newValue = contentType.toString();
    }
    else if (frame instanceof TCOPTextInformationID3V2Frame) {
      newValue = ((TCOPTextInformationID3V2Frame) frame).getCopyrightMessage();
      int year = ((TCOPTextInformationID3V2Frame) frame).getCopyrightYear();
      if (year != 0) {
        newValue += ", " + year;
      }
    }
    else if (frame instanceof TDATTextInformationID3V2Frame) {
      // Use default
    }
    else if (frame instanceof TDLYTextInformationID3V2Frame) {
      newValue = Integer.toString(((TDLYTextInformationID3V2Frame) frame).
          getPlaylistDelay());
    }
    else if (frame instanceof TENCTextInformationID3V2Frame) {
      newValue = ((TENCTextInformationID3V2Frame) frame).getEncodedBy();
    }
    else if (frame instanceof TEXTTextInformationID3V2Frame) {
      String[] lyricists = ((TEXTTextInformationID3V2Frame) frame).getLyricists();
      for (int index = 0; index < lyricists.length; index++) {
        newValueBuffer.append(lyricists[index]);
        if (index < lyricists.length - 1) {
          newValueBuffer.append(", ");
        }
      }
      newValue = newValueBuffer.toString();
    }
    else if (frame instanceof TFLTTextInformationID3V2Frame) {
      newValue = ((TFLTTextInformationID3V2Frame) frame).getFileType();
    }
    else if (frame instanceof TIMETextInformationID3V2Frame) {
      newValue = ((TIMETextInformationID3V2Frame) frame).getHours() + " hours, " +
          ((TIMETextInformationID3V2Frame) frame).getMinutes();
    }
    else if (frame instanceof TIT1TextInformationID3V2Frame) {
      newValue = ((TIT1TextInformationID3V2Frame) frame).
          getContentGroupDescription();
    }
    else if (frame instanceof TIT2TextInformationID3V2Frame) {
      newValue = ((TIT2TextInformationID3V2Frame) frame).getTitle();
    }
    else if (frame instanceof TIT3TextInformationID3V2Frame) {
      newValue = ((TIT3TextInformationID3V2Frame) frame).getSubtitle();
    }
    else if (frame instanceof TKEYTextInformationID3V2Frame) {
      newValue = ((TKEYTextInformationID3V2Frame) frame).getInitialKey();
    }
    else if (frame instanceof TLANTextInformationID3V2Frame) {
      newValue = ((TLANTextInformationID3V2Frame) frame).getLanguages();
    }
    else if (frame instanceof TLENTextInformationID3V2Frame) {
      newValue = Integer.toString(((TLENTextInformationID3V2Frame) frame).
          getTrackLength());
    }
    else if (frame instanceof TMEDTextInformationID3V2Frame) {
      newValue = ((TMEDTextInformationID3V2Frame) frame).getMediaType();
    }
    else if (frame instanceof TOALTextInformationID3V2Frame) {
      newValue = ((TOALTextInformationID3V2Frame) frame).getOriginalAlbumTitle();
    }
    else if (frame instanceof TOFNTextInformationID3V2Frame) {
      newValue = ((TOFNTextInformationID3V2Frame) frame).getOriginalFilename();
    }
    else if (frame instanceof TOLYTextInformationID3V2Frame) {
      String[] lyricists = ((TOLYTextInformationID3V2Frame) frame).getOriginalLyricists();
      for (int index = 0; index < lyricists.length; index++) {
        newValueBuffer.append(lyricists[index]);
        if (index < lyricists.length - 1) {
          newValueBuffer.append(", ");
        }
      }
      newValue = newValueBuffer.toString();
    }
    else if (frame instanceof TOPETextInformationID3V2Frame) {
      String[] performers = ((TOPETextInformationID3V2Frame) frame).getOriginalPerformers();
      for (int index = 0; index < performers.length; index++) {
        newValueBuffer.append(performers[index]);
        if (index < performers.length - 1) {
          newValueBuffer.append(", ");
        }
      }
      newValue = newValueBuffer.toString();
    }
    else if (frame instanceof TORYTextInformationID3V2Frame) {
      newValue = Integer.toString(((TORYTextInformationID3V2Frame) frame).
          getOriginalReleaseYear());
    }
    else if (frame instanceof TOWNTextInformationID3V2Frame) {
      newValue = ((TOWNTextInformationID3V2Frame) frame).getFileOwner();
    }
    else if (frame instanceof TPE1TextInformationID3V2Frame) {
      String[] performers = ((TPE1TextInformationID3V2Frame) frame).getLeadPerformers();
      for (int index = 0; index < performers.length; index++) {
        newValueBuffer.append(performers[index]);
        if (index < performers.length - 1) {
          newValueBuffer.append(", ");
        }
      }
      newValue = newValueBuffer.toString();
    }
    else if (frame instanceof TPE2TextInformationID3V2Frame) {
      newValue = ((TPE2TextInformationID3V2Frame) frame).getBandOrchestraAccompaniment();
    }
    else if (frame instanceof TPE3TextInformationID3V2Frame) {
      newValue = ((TPE3TextInformationID3V2Frame) frame).getConductor();
    }
    else if (frame instanceof TPE4TextInformationID3V2Frame) {
      newValue = ((TPE4TextInformationID3V2Frame) frame).getModifiedBy();
    }
    else if (frame instanceof TPOSTextInformationID3V2Frame) {
      newValue = "Part " + ((TPOSTextInformationID3V2Frame) frame).getPartNumber() +
          "of " + ((TPOSTextInformationID3V2Frame) frame).getTotalParts();
    }
    else if (frame instanceof TPUBTextInformationID3V2Frame) {
      newValue = ((TPUBTextInformationID3V2Frame) frame).getPublisher();
    }
    else if (frame instanceof TRCKTextInformationID3V2Frame) {
      newValue = "Track " + ((TRCKTextInformationID3V2Frame) frame).getTrackNumber();
    }
    else if (frame instanceof TRDATextInformationID3V2Frame) {
      newValue = ((TRDATextInformationID3V2Frame) frame).getRecordingDates();
    }
    else if (frame instanceof TRSNTextInformationID3V2Frame) {
      newValue = ((TRSNTextInformationID3V2Frame) frame).getInternetRadioStationName();
    }
    else if (frame instanceof TRSOTextInformationID3V2Frame) {
      newValue = ((TRSOTextInformationID3V2Frame) frame).getInternetRadioStationOwner();
    }
    else if (frame instanceof TSIZTextInformationID3V2Frame) {
      newValue = Integer.toString(((TSIZTextInformationID3V2Frame) frame).getSizeInBytes());
    }
    else if (frame instanceof TSRCTextInformationID3V2Frame) {
      newValue = ((TSRCTextInformationID3V2Frame) frame).getISRC();
    }
    else if (frame instanceof TSSETextInformationID3V2Frame) {
      newValue = ((TSSETextInformationID3V2Frame) frame).getHardwareSoftwareSettings();
    }
    else if (frame instanceof TXXXTextInformationID3V2Frame) {
      newValue = ((TXXXTextInformationID3V2Frame) frame).getDescription() + ", " +
          ((TXXXTextInformationID3V2Frame) frame).getInformation();
    }
    else if (frame instanceof TYERTextInformationID3V2Frame) {
      newValue = Integer.toString(((TYERTextInformationID3V2Frame) frame).getYear());
    }
    else if (frame instanceof UFIDID3V2Frame) {
      newValue = ((UFIDID3V2Frame) frame).getOwnerIdentifier();
    }
    else if (frame instanceof USERID3V2Frame) {
      newValue = ((USERID3V2Frame) frame).getLanguage() + ", " +
          ((USERID3V2Frame) frame).getTermsOfUse();
    }
    else if (frame instanceof USLTID3V2Frame) {
      newValue = ((USLTID3V2Frame) frame).getContentDescriptor() + ", " +
          ((USLTID3V2Frame) frame).getLanguage() + ", " +
          ((USLTID3V2Frame) frame).getLyrics();
    }
    else if (frame instanceof WCOMUrlLinkID3V2Frame) {
      // Use default
    }
    else if (frame instanceof WCOPUrlLinkID3V2Frame) {
      // Use default
    }
    else if (frame instanceof WOAFUrlLinkID3V2Frame) {
      // Use default
    }
    else if (frame instanceof WOARUrlLinkID3V2Frame) {
      // Use default
    }
    else if (frame instanceof WOASUrlLinkID3V2Frame) {
      // Use default
    }
    else if (frame instanceof WORSUrlLinkID3V2Frame) {
      // Use default
    }
    else if (frame instanceof WPAYUrlLinkID3V2Frame) {
      // Use default
    }
    else if (frame instanceof WPUBUrlLinkID3V2Frame) {
      // Use default
    }
    else if (frame instanceof WXXXUrlLinkID3V2Frame) {
      // Use default
    }
    else {
      newValue = frame.toString();
    }

    // Look up the RDF predicate by identifier
    ObjectNode object = factory.createLiteral(newValue);

    return object;
  }
}
