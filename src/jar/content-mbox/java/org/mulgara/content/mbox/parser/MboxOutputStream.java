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

package org.mulgara.content.mbox.parser;

import java.io.*;

/*
 * MboxOutputStream.java
 * Copyright (C) 1999 dog <dog@dog.net.uk>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * You also have permission to link it with the Sun Microsystems, Inc.
 * JavaMail(tm) extension and run that combination.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */

/**
 * A filter stream that can escape mbox From_ lines in message content.
 * This will only work reliably for messages with <1024 bytes in each line.
 * It will strip out any CRs in the stream.
 * <P/>
 *
 * This code is derived from the 'knife' mail and user news client
 * at http://www.dog.net.uk/knife.
 * <P/>
 *
 * @created 2001-8-21
 *
 * @author dog@dog.net.uk
 * @author Andrew Newman
 * @author Ben Warren
 *
 * @version $Revision: 1.8 $
 *
 * @modified $Date: 2005/01/05 04:57:41 $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy; 2001
 *   <A href="http://www.PIsoftware.com/">Plugged In Software Pty Ltd</A>
 *
 * @licence <A href="{@docRoot}/../../LICENCE_LGPL.txt">Licence description</A>
 */
class MboxOutputStream
  extends FilterOutputStream
{
  private static byte KET = 62;

  /**
  * The buffer where the current line is stored.
  */
  protected byte buf[];

  /**
  * The number of valid bytes in the buffer.
  */
  protected int count = 0;

  public MboxOutputStream(OutputStream out)
  {
    super(out);
    buf = new byte[1024];
  }

  /** Flush the internal buffer */
  protected void validateAndFlushBuffer()
   throws IOException
  {
   if (count > 0)
   {
     for (int i=0; i<count-5; i++)
     {
       if (buf[i]=='F' && buf[i+1]=='r' && buf[i+2]=='o' && buf[i+3]=='m' && buf[i+4]==' ')
       {
         byte[] b2 = new byte[buf.length+1];
         System.arraycopy(buf, 0, b2, 0, buf.length);
         b2[i] = KET;
         System.arraycopy(buf, i, b2, i+1, buf.length-i);
         buf = b2;
         count++;
        break;
       }
       else if (buf[i]!=KET && buf[i]!='\n')
       {
         break;
       }
     }
     out.write(buf, 0, count);
     count = 0;
   }
  }

  /**
  * Writes the specified byte to this output stream.
  */
  public synchronized void write(int b)
   throws IOException
  {
    if (b=='\r')
     return;
    if (b=='\n' || count>buf.length)
     validateAndFlushBuffer();
    buf[count++] = (byte)b;
  }

  /**
  * Writes <code>len</code> bytes from the specified byte array
  * starting at offset <code>off</code> to this output stream.
  */
  public synchronized void write(byte b[], int off, int len)
   throws IOException
  {
    // strip any CRs in the byte array
    for (int i=off; i<off+len; i++)
    {
      if (b[i]=='\r')
      {
        byte[] b2 = new byte[b.length];
        System.arraycopy(b, off, b2, off, len);
        System.arraycopy(b, i+1, b2, i, len-(i-off)-1);
        b = b2;
        len--;
        i--;
      }
    }
    // validate and flush a line at a time
    for (int i=off; i<off+len; i++)
    {
      if (b[i]=='\n' || i-off>buf.length)
      {
        int cl = (i-off>buf.length) ? buf.length : i-off;
        System.arraycopy(b, off, buf, count, cl);
        count += cl;
        validateAndFlushBuffer();
        len = len-(i-off);
        byte[] b2 = new byte[b.length];
        System.arraycopy(b, i, b2, off, len);
        b = b2;
        i = off;
      }
    }
    System.arraycopy(b, off, buf, count, len);
    count += len;
  }

  /**
  * Flushes this output stream.
  */
  public synchronized void flush()
   throws IOException
  {
    validateAndFlushBuffer();
    out.flush();
  }
}
