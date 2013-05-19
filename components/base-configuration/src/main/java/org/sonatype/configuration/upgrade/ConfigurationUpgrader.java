/**
 * Copyright (c) 2007-2012 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.configuration.upgrade;

import java.io.File;
import java.io.IOException;

import org.sonatype.configuration.Configuration;

/**
 * A component involved only if old security configuration is found. It will fetch the old configuration, transform it
 * to current Configuration model and return it. Nothing else.
 * 
 * @author cstamas
 */
public interface ConfigurationUpgrader<E extends Configuration>
{
    /**
     * Tries to load an old configuration from file and will try to upgrade it to current model.
     * 
     * @param file
     * @return
     * @throws IOException
     * @throws ConfigurationIsCorruptedException
     * @throws UnsupportedConfigurationVersionException
     */
    public E loadOldConfiguration( File file )
        throws IOException, ConfigurationIsCorruptedException, UnsupportedConfigurationVersionException;
}
