package org.mulgara.doclet;

// Java 2 standard packages
import java.io.IOException;
import java.util.Arrays;

// Third party packages
import com.sun.javadoc.ClassDoc;                    // Sun javadoc
import com.sun.javadoc.RootDoc;
import com.sun.tools.doclets.ClassTree;
import com.sun.tools.doclets.DocletAbortException;
import com.sun.tools.doclets.standard.Standard;     // Sun standard doclet

/**
* Custom doclet for Mulgara project.
*
* @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
*/
public class MulgaraDoclet extends Standard
{
  /**
  * Doclet entry point.
  */
  public static boolean start(RootDoc root) throws IOException
  {
    try {
      MulgaraDoclet doclet = new MulgaraDoclet();
      doclet.configuration();
      doclet.startGeneration(root);
      return true;
    }
    catch (DocletAbortException e) { return false; }
  }

  /**
  * Instantiate {@link RcsClassWriter} for each Class within the ClassDoc[]
  * passed to it and generate Documentation for that.
  */
  protected void generateClassCycle(ClassDoc[] arr,
                                    ClassTree  classtree,
                                    boolean    nopackage)
  {
    Arrays.sort(arr);
    for(int i = 0; i < arr.length; i++) {
      if (configuration.nodeprecated &&
          arr[i].tags("deprecated").length > 0) { continue; }
      
      ClassDoc prev = (i == 0)?  null: arr[i-1];
      ClassDoc curr = arr[i];
      ClassDoc next = (i+1 == arr.length)?  null: arr[i+1];

      RcsClassWriter.generate(configuration, curr, prev, next, classtree,
                              nopackage);
    }
  }
}
