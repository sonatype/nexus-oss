/**
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
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
package org.sonatype.security.configuration.upgrade;

import java.io.File;

import org.sonatype.security.configuration.ConfigurationException;

/**
 * Thrown when the configuration has model version but it is unknown to the security layer.
 * 
 * @author cstamas
 */
public class UnsupportedConfigurationVersionException
    extends ConfigurationException
{
    private static final long serialVersionUID = 1965812260368747123L;

    public UnsupportedConfigurationVersionException( String version, File file )
    {
        super( "Unsupported configuration file in " + file.getAbsolutePath() + " with version: " + version
            + ". Cannot upgrade." );
    }
}
