/**
 * 
 */
package org.mulgara.util.io;

import junit.framework.*;        // JUnit


import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

/**
 * @author pag
 *
 */
public abstract class LBufferedFileTest extends TestCase {

  File f = new File("testFile");

  RandomAccessFile braf;

  RandomAccessFile raf;

  LBufferedFile file;

  /**
   * Empty test suite to prevent the test runner from trying to instantiate
   * an abstract class.
   * @return An empty test suite.
   */
  public static Test suite() {
    return new TestSuite();
  }

  /**
   * @throws java.lang.Exception
   */
  public void setUp() throws Exception {
    raf = new RandomAccessFile(f, "rw");
    byte[] data = new byte[19];
    for (byte i = 0; i < data.length; i++) data[i] = i;

    raf.write(data);
    System.setProperty(LMappedBufferedFileRO.PAGE_SIZE_PROP, "4");
    braf = new RandomAccessFile(f, "r");
    file = newFile(braf);
  }

  /**
   * @throws java.lang.Exception
   */
  public void tearDown() throws Exception {
    raf.close();
    f.delete();
  }

  abstract LBufferedFile newFile(RandomAccessFile r) throws IOException;
  
  abstract boolean readOnly();
  
  /**
   * Test method for {@link org.mulgara.util.io.LBufferedFile#read(long, int)}.
   */
  public void testRead() throws Exception {
    // basic read of a whole block
    ByteBuffer bb = file.read(0, 4);
    assertArrEquals(new byte[] { 0, 1, 2, 3 }, bb);

    // across boundaries
    bb = file.read(2, 4);
    assertArrEquals(new byte[] { 2, 3, 4, 5 }, bb);
    
    // read from the partial block
    bb = file.read(16, 3);
    assertArrEquals(new byte[] { 16, 17, 18 }, bb);

    // across boundaries into the partial block
    bb = file.read(14, 5);
    assertArrEquals(new byte[] { 14, 15, 16, 17, 18 }, bb);
    
    // append to the file, and read the new data
    raf.write(new byte[] { 19, 20, 21, 22 });

    bb = file.read(20, 2);
    assertArrEquals(new byte[] { 20, 21 }, bb);

    // read the previous partial block
    bb = file.read(15, 4);
    assertArrEquals(new byte[] { 15, 16, 17, 18 }, bb);

    // read across boundaries between the original block and the new block
    bb = file.read(13, 6);
    assertArrEquals(new byte[] { 13, 14, 15, 16, 17, 18 }, bb);

    // append to the file, making the final block full
    raf.write(new byte[] { 23 });

    bb = file.read(20, 4);
    assertArrEquals(new byte[] { 20, 21, 22, 23 }, bb);

    // append to the file again, making the final block partial
    raf.write(new byte[] { 24, 25 });

    bb = file.read(22, 4);
    assertArrEquals(new byte[] { 22, 23, 24, 25 }, bb);

  }

  /**
   * Test method for {@link org.mulgara.util.io.LBufferedFile#allocate(long, int)}.
   */
  public void testAllocateLongInt() {
    if (!readOnly()) {
      // test
    }
  }

  /**
   * Test method for {@link org.mulgara.util.io.LBufferedFile#write(java.nio.ByteBuffer)}.
   */
  public void testWrite() {
    if (!readOnly()) {
      // test
    }
  }

  /**
   * Test method for {@link org.mulgara.util.io.LBufferedFile#seek(long)}.
   */
  public void testSeek() {
    if (!readOnly()) {
      // test
    }
  }

  static void assertArrEquals(byte[] expected, ByteBuffer actual) {
    if (expected.length != actual.limit()) throw new AssertionError("Arrays differ in length. Expected: " + expected.length + ", Actual: " + actual.limit());
    for (int i = 0; i < expected.length; i++) {
      byte e = expected[i];
      byte a = actual.get(i);
      assertEquals("Data differs at position " + i + ", expected: " + e + ", got: " + a, e, a);
    }
  }
}
