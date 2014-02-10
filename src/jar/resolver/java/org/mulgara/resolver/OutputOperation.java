/*
 * Copyright 2009 Revelytix.
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
package org.mulgara.resolver;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URI;

import org.mulgara.resolver.spi.DatabaseMetadata;
import org.mulgara.resolver.spi.SystemResolver;

/**
 * Abstract base class for operations that need to write output to the file system.
 * @created Jun 23, 2008
 * @author Alex Hall
 * @copyright &copy; 2008 <a href="http://www.revelytix.com">Revelytix, Inc.</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public abstract class OutputOperation extends TuplesBasedOperation implements Operation {

  protected final OutputStream outputStream;
  protected final URI destinationURI;

  public OutputOperation(OutputStream outputStream, URI destinationURI) {
    if (outputStream == null && destinationURI == null) {
      throw new IllegalArgumentException("At least one of outputStream and destinationURI must be set");
    }
    this.outputStream = outputStream;
    this.destinationURI = destinationURI;
  }

  /**
   * Gets an output stream for this operation, opening one from the destination
   * URI if necessary.
   * @return An output stream to receive the contents from this operation.
   * @throws FileNotFoundException if creating from a destination URI which could not be opened.
   */
  protected OutputStream getOutputStream() throws FileNotFoundException {
    OutputStream os = outputStream;

    // Check if an output stream was supplied and open the local file if it
    // hasn't.
    if (os == null) {
      // Verify that the destination is a local file.
      String scheme = destinationURI.getScheme();
      if (scheme == null) {
        throw new IllegalArgumentException(
            "Relative URIs are not supported as output destination");
      }
      if (!scheme.equals("file")) {
        throw new IllegalArgumentException(
            "Only file URIs are currently supported as output destination");
      }

      // Open the local file.
      os = new FileOutputStream(destinationURI.getPath());
    }

    // Ensure the output is buffered for efficiency.
    os = new BufferedOutputStream(os);

    return os;
  }

  /* (non-Javadoc)
   * @see org.mulgara.resolver.Operation#execute(org.mulgara.resolver.OperationContext, org.mulgara.resolver.spi.SystemResolver, org.mulgara.resolver.spi.DatabaseMetadata)
   */
  abstract public void execute(OperationContext operationContext,
      SystemResolver systemResolver, DatabaseMetadata metadata)
      throws Exception;

  /**
   * @return <code>false</code>
   */
  public boolean isWriteOperation() {
    return false;
  }
}
