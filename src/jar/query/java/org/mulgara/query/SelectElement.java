/*
 * Copyright 2008 The Topaz Foundation 
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 * Contributions:
 */
package org.mulgara.query;

import java.io.Serializable;

/**
 * Marker interface to identify elements legally permitted within the
 * select-clause of an itql query.
 *
 * @created 2008-01-14
 * @author <a href="mailto:andrae@netymon.com">Andrae Muys</a>
 * @company <A href="mailto:mail@netymon.com">Netymon Pty Ltd</A>
 * @copyright &copy;2008 <a href="http://www.topazproject.org/">The Topaz Foundation</a>
 * @licence Apache License v2.0
 */
public interface SelectElement extends Serializable, Cloneable { }
