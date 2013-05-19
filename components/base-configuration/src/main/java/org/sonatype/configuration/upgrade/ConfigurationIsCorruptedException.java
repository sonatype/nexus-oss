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

import org.sonatype.configuration.ConfigurationException;

/**
 * Thrown when the configuration file is corrupt and cannot be loaded neither upgraded. It has wrong syntax or is
 * unreadable.
 * 
 * @author cstamas
 */
public class ConfigurationIsCorruptedException
    extends ConfigurationException
{
    private static final long serialVersionUID = 5592204171297423008L;

    public ConfigurationIsCorruptedException( File file )
    {
        this( file.getAbsolutePath() );
    }

    public ConfigurationIsCorruptedException( String filePath )
    {
        this( filePath, null );
    }

    public ConfigurationIsCorruptedException( String filePath, Throwable t )
    {
        super( "Could not read or parse security configuration file on path " + filePath + "! It may be corrupted.", t );
    }

}
