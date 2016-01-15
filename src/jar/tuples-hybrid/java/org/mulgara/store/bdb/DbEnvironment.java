/*
 * Copyright 2010, Paula Gearon
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

package org.mulgara.store.bdb;

import org.apache.log4j.Logger;
import org.mulgara.util.TempDir;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;

/**
 * A central location for creating a singleton BerkeleyDB environment.
 *
 * @created Jul 12, 2010
 * @author Paula Gearon
 * @copyright &copy; 2009 <a href="http://www.duraspace.org/">DuraSpace</a>
 */
public class DbEnvironment {
  
  private final static Logger logger = Logger.getLogger(DbEnvironment.class);

  static private Environment env;
  static private EnvironmentConfig envCfg;

  static Environment getEnv() {
    if (env == null) {
      try {
        envCfg = new EnvironmentConfig();
        envCfg.setAllowCreate(true);
        env = new Environment(TempDir.getTempDir(), envCfg);
      } catch (DatabaseException dbe) {
        logger.error("Error creating BDB environment: " + dbe.getMessage(), dbe);
        throw new RuntimeException("Unable to manage data with BDB", dbe);
      }
    }
    return env;
  }


}
