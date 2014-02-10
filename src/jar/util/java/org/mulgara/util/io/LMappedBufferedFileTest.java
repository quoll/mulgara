/**
 * 
 */
package org.mulgara.util.io;

import java.io.IOException;
import java.io.RandomAccessFile;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author pag
 *
 */
public class LMappedBufferedFileTest extends LBufferedFileTest {

  public static Test suite() {
    return new TestSuite(LMappedBufferedFileTest.class);
  }

  LBufferedFile newFile(RandomAccessFile r) throws IOException {
    return new LMappedBufferedFileRO(r);
  }
  
  boolean readOnly() {
    return true;
  }
}
