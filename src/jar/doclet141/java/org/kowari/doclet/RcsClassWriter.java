package org.mulgara.doclet;

// Java 2 standard packages
import java.io.IOException;
import java.text.*;
import java.util.*;
import java.util.regex.*;

// third party packages
import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.Doc;
import com.sun.javadoc.Tag;
import com.sun.tools.doclets.ClassTree;
import com.sun.tools.doclets.DirectoryManager;
import com.sun.tools.doclets.DocletAbortException;
import com.sun.tools.doclets.standard.ClassWriter;
import com.sun.tools.doclets.standard.ConfigurationStandard;

/**
* An extended {@link ClassWriter} that adds new javadoc tags for RCS symbols.
*
* @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
*/
public class RcsClassWriter extends ClassWriter
{
  /**
  * Configuration object.
  *
  * It's almost certain that one of the superclasses is already holding a
  * copy of this, but I haven't been able to guess which one.
  */
  private ConfigurationStandard configuration;

  /**
  * Matcher for RCS keywords.
  *
  * This is lazily initialized by {@link #replaceRcsKeywords}.
  */
  private Matcher matcher;

  /**
  * Constructor.
  */
  public RcsClassWriter(ConfigurationStandard configurationStandard,
                        String path, String filename, ClassDoc classdoc,
                        ClassDoc prev, ClassDoc next, ClassTree classtree,
                        boolean nopackage)
    throws IOException
  {
    super(configurationStandard, path, filename, classdoc, prev, next,
          classtree, nopackage);
    configuration = configurationStandard;
  }

  /** 
  * Generate a class page.
  *
  * @param prev the previous class to generated, or null if no previous.
  * @param classdoc the class to generate.
  * @param next the next class to be generated, or null if no next.
  */
  public static void generate(ConfigurationStandard configurationStandard,
                              ClassDoc classdoc, ClassDoc prev, ClassDoc next,
                              ClassTree classtree, boolean nopackage)
  {
    RcsClassWriter clsgen;
    String path =
      DirectoryManager.getDirectoryPath(classdoc.containingPackage());
    String filename = classdoc.name() + ".html";
    try {
      clsgen = new RcsClassWriter(configurationStandard, path, filename,
                                  classdoc, prev, next, classtree, nopackage);
      clsgen.generateClassFile();
      clsgen.close();
    }
    catch (IOException exc) {
      configurationStandard.standardmessage.error(
        "doclet.exception_encountered",
        exc.toString(),
        filename
      );
      throw new DocletAbortException();
    }
  }

  /**
  * Overrides {@link HtmlStandardWriter#generateTagInfo} to include Plugged
  * In's tags.
  */
  public void generateTagInfo(Doc doc)
  {
    Tag[] sinces = doc.tags("since");
    Tag[] sees = doc.seeTags();
    Tag[] authors;
    Tag[] versions;
    Tag[] copyrights;
    Tag[] createds;
    Tag[] modifieds;
    Tag[] licences;
    if (configuration.showauthor) {
      authors = doc.tags("author");
    }
    else {
     authors = new Tag[0];
    }
    if (configuration.showversion) {
      versions = doc.tags("version");
    }
    else {
      versions = new Tag[0];
    }
    if (configuration.nosince) {
      sinces = new Tag[0];
    }
    copyrights = doc.tags("copyright");
    createds   = doc.tags("created");
    licences   = doc.tags("licence");
    modifieds  = doc.tags("modified");
    if (sinces.length > 0
      || sees.length > 0
      || authors.length > 0
      || versions.length > 0 
      || copyrights.length > 0 
      || createds.length > 0 
      || licences.length > 0 
      || modifieds.length > 0 
      || (doc.isClass() && ((ClassDoc)doc).isSerializable()))
    {
      dl();
      printSinceTag(doc);
      if (versions.length > 0) {
        // There is going to be only one Version tag.
        dt();
        boldText("doclet.Version");
        dd();
        printInlineComment(versions[0]);
        ddEnd();
      }
      if (authors.length > 0) {
        dt();
        boldText("doclet.Author");
        dd();
        for (int i = 0; i < authors.length; ++i) {
          if (i > 0) {
            print(", ");
          } 
          printInlineComment(authors[i]);
        }
        ddEnd();
      }
      if (createds.length > 0) {
        // There is going to be only one Created tag.
        dt();
        write("<b>Created:</b>");
        dd();
        printInlineComment(createds[0]);
        ddEnd();
      }
      if (modifieds.length > 0) {
        dt();
        write("<b>Modified:</b>");
        dd();
        for (int i = 0; i < modifieds.length; ++i) {
          if (i > 0) {
            print(", ");
          } 
          printInlineComment(modifieds[i]);
        }
        ddEnd();
      }
      if (copyrights.length > 0) {
        dt();
        write("<b>Copyright:</b>");
        dd();
        for (int i = 0; i < copyrights.length; ++i) {
          if (i > 0) {
            print(", ");
          } 
          printInlineComment(copyrights[i]);
        }
        ddEnd();
      }
      if (licences.length > 0) {
        // There is going to be only one Licence tag.
        dt();
        write("<b>Licence:</b>");
        dd();
        printInlineComment(licences[0]);
        ddEnd();
      }
      //printSeeTags(doc);  // This method vanished in 1.4 beta 3
      dlEnd();
    }
  }

  /**
  * Override to include RCS keyword substitution.
  */
  public void printInlineComment(Tag tag)
  {
    String text = tag.text();
    text = replaceRcsKeywords(text);
    text = replaceDocRootDir(text);
    print(text);
  }

  /**
  * Leave only the value for RCS keywords.
  *
  * For example, <code>$Revision: 1.1 $</code> becomes <code>1.0</code>.
  */
  public String replaceRcsKeywords(String text)
  {
    if (matcher == null) {
      matcher = Pattern.compile(
        "\\$(Author|Date|Header|Id|Locker|Log|Name|RCSFile|Revision|Source|State): (.+?) \\$"
      ).matcher(text);
    }
    else {
      matcher.reset(text);
    }

    StringBuffer buffer = new StringBuffer();
    while (matcher.find()) {
      String string = matcher.group(2);

      // For the Date: keyword, have a shot at reformatting string
      if ("Date".equals(matcher.group(1))) {
        try {
          DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
          Date date = dateFormat.parse(string);
          string = date.toString();
        }
        catch (ParseException e) {}  // if we can't parse, return unchanged
      }

      matcher.appendReplacement(buffer, string);
    }
    matcher.appendTail(buffer);
    return buffer.toString();
  }
}
