package org.mulgara.util.io;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

public class LLHashMapUnitTest extends TestCase {

  File f;
  LLHashMap map;

  protected void setUp() throws Exception {
    super.setUp();
    f = new File("/tmp/i2i");
    clean(f);
    map = new LLHashMap(f);
    map.clear();
  }

  protected void tearDown() throws Exception {
    super.tearDown();
    map.clear();
    map.close();
    clean(f);
  }

  public void testLLHashMapFile() throws IOException {
    File f = new File("/tmp/int2int");
    clean(f);
    LLHashMap map = new LLHashMap(f);
    map.put(1, 11);
    map.put(2, 12);
    map.put(3, 13);
    assertEquals(11, map.get(1));
    assertEquals(12, map.get(2));
    assertEquals(13, map.get(3));
    assertEquals(-1L, map.get(4));
    assertEquals(null, map.get(Long.valueOf(4)));
    map.close();
    assertEquals(37 * 16, f.length());
    clean(f);
  }

  public void testLLHashMapFileFloatLong() throws IOException {
    File f = new File("/tmp/int2intl");
    clean(f);
    LLHashMap map = new LLHashMap(f, 0.5f, 50L);
    map.put(1, 11);
    map.put(2, 12);
    map.put(3, 13);
    assertEquals(13, map.get(3));
    assertEquals(12, map.get(2));
    assertEquals(11, map.get(1));
    assertEquals(-1L, map.get(4));
    assertEquals(null, map.get(Long.valueOf(4)));
    map.close();
    assertEquals(131 * 16, f.length());
    clean(f);
  }

  public void testClear() {
    assertEquals(0, map.size());
    map.put(1, 11);
    map.put(2, 12);
    map.put(3, 13);
    assertEquals(3, map.size());
    assertEquals(13, map.get(3));
    assertEquals(12, map.get(2));
    assertEquals(11, map.get(1));
    assertEquals(-1L, map.get(4));
    assertEquals(null, map.get(Long.valueOf(4)));
    map.clear();
    assertEquals(0, map.size());
    assertEquals(-1L, map.get(3));
    assertEquals(-1L, map.get(2));
    assertEquals(-1L, map.get(1));
    assertEquals(-1L, map.get(4));
    assertEquals(null, map.get(Long.valueOf(4)));
    map.put(1, 11);
    map.put(2, 12);
    map.put(3, 13);
    assertEquals(3, map.size());
    assertEquals(13, map.get(3));
    assertEquals(12, map.get(2));
    assertEquals(11, map.get(1));
    assertEquals(-1L, map.get(4));
    assertEquals(null, map.get(Long.valueOf(4)));
  }

  public void testContainsKeyObject() {
    map.put(1, 11);
    map.put(2, 12);
    map.put(3, 13);
    assertTrue(map.containsKey(Long.valueOf(1)));
    assertTrue(map.containsKey(Long.valueOf(2)));
    assertTrue(map.containsKey(Long.valueOf(3)));
    assertFalse(map.containsKey(Long.valueOf(4)));
    assertFalse(map.containsKey(Long.valueOf(0)));
  }

  public void testContainsKeyLong() {
    map.put(1, 11);
    map.put(2, 12);
    map.put(3, 13);
    assertTrue(map.containsKey(1));
    assertTrue(map.containsKey(2));
    assertTrue(map.containsKey(3));
    assertFalse(map.containsKey(4));
    assertFalse(map.containsKey(0));
  }

  public void testContainsValueObject() {
    assertFalse(map.containsValue(Long.valueOf(11)));
    assertFalse(map.containsValue(Long.valueOf(0)));
    map.put(1, 11);
    map.put(2, 12);
    map.put(3, 13);
    assertTrue(map.containsValue(Long.valueOf(11)));
    assertFalse(map.containsValue(Long.valueOf(0)));
  }

  public void testContainsValueLong() {
    assertFalse(map.containsValue(11));
    assertFalse(map.containsValue(0));
    map.put(1, 11);
    map.put(2, 12);
    map.put(3, 13);
    assertTrue(map.containsValue(11));
    assertFalse(map.containsValue(0));
  }

  public void testEntrySet() {
//    fail("Not yet implemented");
  }

  public void testGetObject() {
//    fail("Not yet implemented");
  }

  public void testGetLong() {
    assertEquals(-1, map.get(1));
    assertEquals(-1, map.get(11));
    assertEquals(-1, map.get(0));
    map.put(1, 11);
    map.put(2, 12);
    map.put(3, 13);
    assertEquals(11, map.get(1));
    assertEquals(-1, map.get(11));
    assertEquals(-1, map.get(0));
    assertEquals(12, map.get(2));
    assertEquals(13, map.get(3));
  }

  public void testIsEmpty() {
    assertTrue(map.isEmpty());
    map.put(1, 11);
    assertFalse(map.isEmpty());
    map.put(2, 12);
    assertFalse(map.isEmpty());
    map.put(3, 13);
    assertFalse(map.isEmpty());
    map.clear();
    assertTrue(map.isEmpty());
  }

  public void testKeySet() {
//    fail("Not yet implemented");
  }

  public void testPutLongLong() throws Exception {
    map.put(1, 11);
    map.put(2, 12);
    map.put(3, 13);
    map.put(0, 10);
    assertEquals(4, map.size());
    assertEquals(11, map.get(1));
    assertEquals(12, map.get(2));
    assertEquals(13, map.get(3));
    assertEquals(10, map.get(0));
    assertEquals(-1L, map.get(4));
    assertEquals(null, map.get(Long.valueOf(4)));
  }

  public void testRehash() throws Exception {
    File f = new File("/tmp/int2int_small");
    clean(f);
    LLHashMap map = new LLHashMap(f);
    for (long i = 0L; i < 27L; i++) {
      map.put(i, i * i);
    }
    assertEquals(27, map.size());
    for (long i = 0L; i < 27L; i++) {
      assertEquals(i * i, map.get(i));
    }
    map.close();
    assertEquals(37 * 16, f.length());
    clean(f);
  }

  public void testHighLoad() throws Exception {
    File f = new File("/tmp/int2int_big");
    clean(f);
    LLHashMap map = new LLHashMap(f);
    long i = 0;
    try {
      for (i = 0L; i < 10000L; i++) {
        map.put(i, i * i);
      }
    } catch (Exception ex) {
      System.err.println("Exception on: " + i);
      throw ex;
    } catch (Error e) {
      System.err.println("Error on: " + i);
      throw e;
    }
    assertEquals(10000L, map.size());
    for (i = 0L; i < 10000L; i++) {
      assertEquals(i * i, map.get(i));
    }
    map.close();
    assertEquals(16411L * 16, f.length());
    clean(f);
  }

  public void testExpHighLoad() throws Exception {
    File f = new File("/tmp/int2int_big2");
    clean(f);
    LLHashMap map = new LLHashMap(f, 0.75f, 10000);
    long i = 0;
    try {
      for (i = 0L; i < 10000L; i++) {
        map.put(i, i * i);
      }
    } catch (Exception ex) {
      System.err.println("Exception on: " + i);
      throw ex;
    } catch (Error e) {
      System.err.println("Error on: " + i);
      throw e;
    }
    assertEquals(10000L, map.size());
    for (i = 0L; i < 10000L; i++) {
      assertEquals(i * i, map.get(i));
    }
    map.close();
    assertEquals(16411L * 16, f.length());
    clean(f);
  }

  public void testPutLongLong1() {
//    fail("Not yet implemented");
  }

  public void testPutAll() {
//    fail("Not yet implemented");
  }

  public void testRemoveObject() {
//    fail("Not yet implemented");
  }

  public void testRemoveLong() {
//    fail("Not yet implemented");
  }

  public void testSize() {
    assertEquals(0, map.size());
    map.put(1, 11);
    assertEquals(1, map.size());
    map.put(2, 12);
    assertEquals(2, map.size());
    map.put(3, 13);
    assertEquals(3, map.size());
    map.put(3, 14);
    assertEquals(3, map.size());
    map.remove(1);
    assertEquals(2, map.size());
    map.remove(1);
    assertEquals(2, map.size());
    map.remove(2);
    assertEquals(1, map.size());
    map.remove(3);
    assertEquals(0, map.size());
    map.remove(3);
    assertEquals(0, map.size());
  }

  public void testValues() {
//    fail("Not yet implemented");
  }

  static void clean(File file) throws IOException {
    if (file.exists()) {
      file.delete();
      File md = new File(file.getPath() + ".hmd");
      if (md.exists()) md.delete();
    }
  }
}
