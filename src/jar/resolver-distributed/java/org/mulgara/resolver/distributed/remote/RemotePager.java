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

package org.mulgara.resolver.distributed.remote;

import java.rmi.*;
import java.io.Serializable;

/**
 * The interface for paging an iterable object over RMI.
 * @param <E> The elements of the paged list.
 *
 * @created 2007-04-23
 * @author <a href="mailto:gearon@users.sourceforge.net">Paul Gearon</a>
 * @copyright &copy; 2007 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */
public interface RemotePager<E extends Serializable> extends Remote {

  public int size() throws RemoteException;

  public E[] firstPage() throws RemoteException;

  public E[] nextPage() throws RemoteException;
}
