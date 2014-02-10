package org.mulgara.doclet;

// Java 2 standard packages
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Third party packages
import com.sun.javadoc.Tag;           // Doclet API
import com.sun.tools.doclets.Taglet;  // Taglet API

/**
* Custom taglets for the Mulgara project which reformat RCS tags placed into
* javadoc tags by the revision control system.
*
* These tags are valid in all scopes (constructors, fields, methods, overviews
* and packages) but not as inline tags.
*
* Although the interface can't enforce this, it is necessary to implement a
* <code>public static void register(Map<String, Taglet>)</code> method on each
* concrete taglet class extended from this one.  The {#register} method is
* provided to simplify this.
*
* @created 2006-08-25
* @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
* @version $Revision$
* @modified $Date$
* @maintenanceAuthor $Author$
* @copyright &copy;2006 <a href="http://www.mulgara.org/">Mulgara Project</a>
* @licence <a href="http://opensource.org/licenses/osl-3.0.php">Open Software License v3.0</a>
*/
public abstract class RCSTaglet implements Taglet
{
  /**
  * Matcher for RCS keywords.
  *
  * This is lazily initialized by {@link #replaceRcsKeywords}.
  */
  private Matcher matcher;
  
  /**
  * @return the heading to be written into the documentation for human readers
  */
  public abstract String getHeading();

  //
  // Methods implementing the Taglet interface
  //

  public abstract String getName();

  /**
  * @inheritDoc
  *
  * @return <code>true</code>
  */
  public boolean inConstructor()
  {
    return true;
  }

  /**
  * @inheritDoc
  *
  * @return <code>true</code>
  */
  public boolean inField()
  {
    return true;
  }

  /**
  * @inheritDoc
  *
  * @return <code>true</code>
  */
  public boolean inMethod()
  {
    return false;
  }

  /**
  * @inheritDoc
  *
  * @return <code>true</code>
  */
  public boolean inOverview()
  {
    return false;
  }

  /**
  * @inheritDoc
  *
  * @return <code>true</code>
  */
  public boolean inPackage()
  {
    return false;
  }

  /**
  * @inheritDoc
  *
  * @return <code>true</code>
  */
  public boolean inType()
  {
    return true;
  }

  /**
  * @inheritDoc
  *
  * @return <code>false</code>
  */
  public boolean isInlineTag()
  {
    return false;
  }

  public String toString(Tag tag)
  {
    return "<DT><B>"
         + getHeading()
         + "</B></DT><DD>"
         + replaceRcsKeywords(tag.text())
         + "</DD>";
  }

  public String toString(Tag[] tags)
  { 
    StringBuffer buffer = new StringBuffer("<DT><B>");
    buffer.append(getHeading())
          .append("</B></DT><DD>");
    for (int i = 0; i < tags.length; i++) {
      buffer.append(replaceRcsKeywords(tags[i].text()));
      if (i + 1 < tags.length) {
        buffer.append(", ");
      }
    }
    buffer.append("</DD>");
    return buffer.toString();
  }

  protected static void register(Taglet taglet, Map<String, Taglet> tagletMap)
  {
    assert taglet != null;
    assert tagletMap != null;

    if (tagletMap.containsKey(taglet.getName())) {
      tagletMap.remove(taglet.getName());
    }
    assert !tagletMap.containsKey(taglet.getName());

    tagletMap.put(taglet.getName(), taglet);
    assert tagletMap.containsKey(taglet.getName());
  }

  //
  // Internal methods
  //

  /**
  * Leave only the value for RCS keywords.
  *
  * For example, <code>&#42;Revision: 1.1 &#42;</code> becomes <code>1.1</code>.
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

      matcher.appendReplacement(buffer, string.replaceAll("\\\\", "\\\\\\\\").replaceAll("\\$", "\\\\\\$"));
    }
    matcher.appendTail(buffer);
    return buffer.toString();
  }
}
