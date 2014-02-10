/*
 * Copyright 2008 Fedora Commons, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mulgara.store.stringpool.xa11;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import org.mulgara.store.nodepool.NodePool;
import org.mulgara.store.stringpool.SPObject;
import org.mulgara.store.stringpool.SPObjectFactory;
import org.mulgara.store.stringpool.SPTypedLiteral;
import org.mulgara.store.xa.AVLNode;
import org.mulgara.util.Constants;
import org.mulgara.util.io.LBufferedFile;

/**
 * Similar to a C-struct for storing and retrieving the data being stored by this string pool.
 *
 * @created Aug 12, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */
class DataStruct {

  static final int OFFSET_MASK = 0xFFFFFFF8;
  static final long PADDING_MASK = 0x0000000000000007L;
  static final int PADDING_MASK_I = 0x00000007;
  
  static final int MAX_PADDING = Constants.SIZEOF_LONG - 1;
  static final byte[] PADDING = new byte[MAX_PADDING];

  static final int IDX_TYPE_CATEGORY_B = 1;  // BYTE offset
  static final int IDX_TYPE_ID_B = 2;        // BYTE offset
  static final int IDX_SUBTYPE_ID_B = 3;     // BYTE offset
  static final int IDX_DATA_SIZE_B = 4;      // BYTE offset
  static final int IDX_DATA_SIZE_I = 1;      // INT offset
  static final int IDX_DATA_B = 8;           // BYTE offset
  static final int IDX_DATA = 1;             // LONG offset
  static final int IDX_GRAPH_NODE = 9;       // LONG offset. Only used in AVL nodes

  /** The payload size of the AVLNode in longs. */
  static final int PAYLOAD_SIZE = IDX_GRAPH_NODE + 1;

  /** The maximum number of bytes of SPObject data that will fit in the AVL node. */
  static final int MAX_DATA_SIZE = (IDX_GRAPH_NODE - IDX_DATA) * Constants.SIZEOF_LONG;

  /** The size of the data before the buffer. */
  static final int HEADER = IDX_DATA_B;

  /** The type category. */
  private byte typeCategoryId;

  /** The type ID. */
  private byte typeId;

  /** The subtype ID. */
  private byte subtypeId;

  /** The size of the data buffer. */
  private int dataSize;

  /** The raw data for the object. */
  private ByteBuffer data;

  /** Indicates that the data buffer is incomplete, and only holds the prefix. */
  private boolean prefixOnly = false;

  /** The gNode for this data. */
  private long gNode;

  /**
   * Creates a new data structure from an object.
   * @param spObject The object to represent.
   * @param gNode The gNode for the object.
   */
  public DataStruct(SPObject spObject, long gNode) {
    assert !BlankNodeAllocator.isBlank(gNode);
    typeCategoryId = (byte)spObject.getTypeCategory().ID;
    // The type fields in an SPObject all fit into a byte
    if (spObject.getTypeCategory() == SPObject.TypeCategory.TYPED_LITERAL) {
      SPTypedLiteral sptl = (SPTypedLiteral)spObject;
      typeId = (byte)sptl.getTypeId();
      subtypeId = (byte)sptl.getSubtypeId();
    } else {
      typeId = SPObjectFactory.INVALID_TYPE_ID;
      subtypeId = 0;
    }
    data = spObject.getData();
    dataSize = data.limit();
    prefixOnly = false;
    this.gNode = gNode;
  }


  /**
   * Creates a new data structure from an object, without knowing the gNode.
   * @param spObject The object to represent.
   */
  public DataStruct(SPObject spObject) {
    this(spObject, NodePool.NONE);
  }


  /**
   * Reads a data structure from a file at a given offset.
   * @param bfile The file to read the structure from.
   * @param gNode The gNode of the data to read.
   */
  public DataStruct(LBufferedFile bfile, long gNode) throws IOException {
    long offset = toOffset(gNode);
    assert (offset & PADDING_MASK) == 0 : "Bad gNode value: " + gNode;
    ByteBuffer header = bfile.read(offset, HEADER);
    if (0 != header.get(0)) throw new IllegalStateException("Bad data found in Data Pool");
    typeCategoryId = header.get(IDX_TYPE_CATEGORY_B);
    typeId = header.get(IDX_TYPE_ID_B);
    subtypeId = header.get(IDX_SUBTYPE_ID_B);
    dataSize = header.getInt(IDX_DATA_SIZE_B);
    data = bfile.read(offset + HEADER, dataSize);
    prefixOnly = false;
    this.gNode = gNode;
  }


  /**
   * Reads a data structure from an AVL Node.
   * @param node The AVL node to read from.
   */
  public DataStruct(AVLNode node) {
    typeCategoryId = getTypeCategoryId(node);
    typeId = getTypeId(node);
    subtypeId = getSubtypeId(node);

    // get the data buffer, or its prefix if the buffer is too large
    dataSize = getDataSize(node);
    data = getDataPrefix(node, dataSize);
    prefixOnly = dataSize > MAX_DATA_SIZE;

    gNode = getGNode(node);
  }


  /**
   * Sets the gNode for an object that does not have the gNode set yet.
   * @param gNode The new gNode value.
   * @throws IllegalStateException If the object already has a gNode value.
   */
  public void setGNode(long gNode) {
    if (this.gNode != NodePool.NONE) throw new IllegalStateException("Not allowed to update a GNode on an existing object.");
    this.gNode = gNode;
  }


  /**
   * Return an instance the object represented by this structure.
   * @return A new instance of an SPObject for this structure,
   *         or <code>null</code> if this is a blank node.
   */
  public SPObject getSPObject() {
    if (prefixOnly) throw new IllegalStateException("Only have the prefix for this object");
    if (typeCategoryId == SPObject.TypeCategory.TCID_FREE) return null;
    SPObject.TypeCategory typeCategory = SPObject.TypeCategory.forId(typeCategoryId);
    return XA11StringPoolImpl.SPO_FACTORY.newSPObject(typeCategory, typeId, subtypeId, data);
  }


  /**
   * Write this object to a file. This will update the gNode for the object.
   * @param fc The file channed of the file to write to.
   * @return The number of bytes written.
   * @throws IOException Caused by errors writing to the file.
   */
  public int writeTo(FileChannel fc) throws IOException {
    if (prefixOnly) throw new IllegalStateException("Only have the prefix for this object");
    assert gNode == NodePool.NONE || toOffset(gNode) == fc.position() : "Unexpected gNode value: " + gNode + ". Offset = " + fc.position();
    // don't update the gNode

    // work out the size
    int unpaddedSize = HEADER + dataSize;
    int roundedSize = round(unpaddedSize);
    ByteBuffer buffer = ByteBuffer.allocate(roundedSize);

    // put the data in
    buffer.put(IDX_TYPE_CATEGORY_B, typeCategoryId);
    buffer.put(IDX_TYPE_ID_B, typeId);
    buffer.put(IDX_SUBTYPE_ID_B, subtypeId);
    buffer.putInt(IDX_DATA_SIZE_B, dataSize);
    data.rewind();
    assert dataSize == data.limit();
    buffer.position(IDX_DATA_B);
    buffer.put(data);

    // send it to the file
    buffer.rewind();
    fc.write(buffer);

    return roundedSize;
  }


  /**
   * Writes this object to the payload in an AVLNode.
   * @param node The node to write to.
   */
  public void writeTo(AVLNode node) {
    node.putPayloadByte(IDX_TYPE_CATEGORY_B, typeCategoryId);
    node.putPayloadByte(IDX_TYPE_ID_B, typeId);
    node.putPayloadByte(IDX_SUBTYPE_ID_B, subtypeId);
    node.putPayloadInt(IDX_DATA_SIZE_I, dataSize);
    // store the head of the buffer
    data.rewind();
    // if prefix only then already limitted to the MAX_DATA_SIZE
    if (!prefixOnly) data.limit(Math.min(dataSize, MAX_DATA_SIZE));
    node.getBlock().put((AVLNode.HEADER_SIZE + IDX_DATA) * Constants.SIZEOF_LONG, data);
    // reset the buffer limit if we reduced the limit earlier
    if (!prefixOnly) data.limit(dataSize);
    // The graph node at the end is the difference between AVL nodes and the flat file
    node.putPayloadLong(IDX_GRAPH_NODE, gNode);
  }


  /**
   * Gets the remaining data of an object into the buffer.
   * @param bfile The file to read the data from.
   */
  public void getRemainingBytes(LBufferedFile bfile) throws IOException {
    // only need to get more if we only have the prefix
    if (!prefixOnly) return;
    // move the limit out to the end
    data.limit(dataSize);
    // read the file starting at the data, plus the header, plus the already read portion
    long location = toOffset(gNode) + HEADER + MAX_DATA_SIZE;
    // read into the buffer, filling at the point where the data had been truncated.
    int remainingBytes = dataSize - MAX_DATA_SIZE;
    assert remainingBytes > 0;
    ByteBuffer bb = bfile.read(location, remainingBytes);
    data.position(MAX_DATA_SIZE);
    data.put(bb);
    data.position(0);
  }


  /** @return the typeCategoryId */
  public byte getTypeCategoryId() { return typeCategoryId; }


  /** @return the typeId */
  public byte getTypeId() { return typeId; }


  /** @return the subtypeId */
  public byte getSubtypeId() { return subtypeId; }


  /** @return the dataSize */
  public int getDataSize() { return dataSize; }


  /** @return the data */
  public ByteBuffer getData() { return data; }


  /** @return the prefixOnly */
  public boolean isPrefixOnly() { return prefixOnly; }


  /** @return the gNode */
  public long getGNode() { return gNode; }


  /** @return structural aspects to this data, but not the buffer */
  public String toString() {
    StringBuilder sb = new StringBuilder("gNode:");
    sb.append(gNode);
    sb.append(", typeCategoryId:").append(typeCategoryId);
    sb.append(", typeId:").append(typeId);
    sb.append(", subtypeId:").append(subtypeId);
    sb.append(", dataSize:").append(dataSize);
    return sb.toString();
  }

  /** @return The type category from an AVL node. */
  static byte getTypeCategoryId(AVLNode node) { return (byte)node.getPayloadByte(IDX_TYPE_CATEGORY_B); }

  /** @return The type ID from an AVL node. */
  static byte getTypeId(AVLNode node) { return (byte)node.getPayloadByte(IDX_TYPE_ID_B); }

  /** @return The sub type ID from an AVL node. */
  static byte getSubtypeId(AVLNode node) { return (byte)node.getPayloadByte(IDX_SUBTYPE_ID_B); }

  /** @return The data size from an AVL node. */
  static int getDataSize(AVLNode node) { return node.getPayloadInt(IDX_DATA_SIZE_I); }

  /** @return The data prefix from an AVL node. */
  static ByteBuffer getDataPrefix(AVLNode node, int size) {
    ByteBuffer dataPrefix = ByteBuffer.allocate(size);
    if (size > MAX_DATA_SIZE) dataPrefix.limit(MAX_DATA_SIZE);
    node.getBlock().get((AVLNode.HEADER_SIZE + IDX_DATA) * Constants.SIZEOF_LONG, dataPrefix);
    dataPrefix.rewind();
    return dataPrefix;
  }

  /** @return The gNode from an AVL node. */
  public static long getGNode(AVLNode node) { return node.getPayloadLong(IDX_GRAPH_NODE); }


  /**
   * Gets the remaining data of an object into the buffer.
   * @param bfile The file to read the data from.
   */
  public static void getRemainingBytes(ByteBuffer data, LBufferedFile bfile, long gNode) throws IOException {
    // read the file starting at the data, plus the header, plus the already read portion
    long location = toOffset(gNode) + HEADER + MAX_DATA_SIZE;
    // read into the buffer, filling at the point where the data had been truncated.
    int remainingBytes = data.limit() - data.position();
    assert remainingBytes > 0;
    ByteBuffer bb = bfile.read(location, remainingBytes);
    data.position(MAX_DATA_SIZE);
    data.put(bb);
    data.position(0);
  }


  /**
   * Converts a gNode to an offset into a file.
   * @param gNode The gNode to convert
   * @return The file offset associated with the gNode
   */
  public static final long toOffset(long gNode) {
    long offset = gNode - NodePool.MIN_NODE;
    if (offset % Constants.SIZEOF_LONG != 0) throw new IllegalArgumentException("Invalid gNode: " + gNode);
    return offset;
  }


  /**
   * Converts a file offset to the gNode stored at that location.
   * @param offset The file offset to convert.
   * @return The gNode associated with the file offset.
   */
  public static final long toGNode(long offset) {
    return offset + NodePool.MIN_NODE;
  }


  /**
   * Rounds this value down to the nearest long boundary.
   * @param offset The offset to round.
   * @return The closest offset >= the argument that is on a long boundary.
   */
  public static final int round(int offset) {
    return (offset + MAX_PADDING) & OFFSET_MASK;
  }

}
