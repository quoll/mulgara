package org.mulgara.doclet;

// Java 2 standard packages
import java.util.Map;

// Third party packages
import com.sun.tools.doclets.Taglet;  // Taglet API

/**
* Custom taglet for the Mulgara project implementing the <code>@modified</code>
* javadoc tag.
*
* @created 2006-08-25
* @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
* @version $Revision$
* @modified $Date$
* @maintenanceAuthor $Author$
* @copyright &copy;2006 <a href="http://www.mulgara.org/">Mulgara Project</a>
* @licence <a href="http://opensource.org/licenses/osl-3.0.php">Open Software License v3.0</a>
*/
public class ModifiedTaglet extends RCSTaglet
{
  //
  // Methods implementing the RCSTaglet interface
  //

  public String getHeading()
  {
    return "Last modified on:";
  }

  public String getName()
  {
    return "modified";
  }

  public static void register(Map<String, Taglet> tagletMap)
  {
    register(new ModifiedTaglet(), tagletMap);
  }
}
