/*
 * Copyright 2010 Revelytix.
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
package org.mulgara.query.operation;

import java.net.URI;

import org.mulgara.connection.Connection;
import org.mulgara.connection.Connection.SessionOp;
import org.mulgara.query.QueryException;

/**
 * A common superclass for commands that perform an operation on a server session.
 */
public abstract class SessionCommand extends ServerCommand {

	/**
	 * Construct a command to operate on the given server graph URI.
	 * @param serverGraphUri The principal graph URI involved in the operation on the server.
	 */
	public SessionCommand(URI serverGraphUri) {
    super(serverGraphUri);
  }

  /* (non-Javadoc)
   * @see org.mulgara.query.operation.Command#execute(org.mulgara.connection.Connection)
   */
  final public Object execute(Connection conn) throws QueryException {
		return conn.execute(getExecutable());
	}

	/**
	 * Gets the operation that will be performed on the server session.
	 * @return The session operation that implements this command.
	 */
	protected abstract SessionOp<?, ? extends QueryException> getExecutable();
}
