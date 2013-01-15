/*
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
package org.sonatype.security.realms.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.security.model.Configuration;

public abstract class AbstractConfigurationManager
    implements ConfigurationManager
{
    private final Logger logger = LoggerFactory.getLogger( getClass() );

    protected Logger getLogger()
    {
        return logger;
    }

    //

    private volatile EnhancedConfiguration configuration = null;

    public synchronized void clearCache()
    {
        configuration = null;
    }

    protected synchronized EnhancedConfiguration getConfiguration()
    {
        if ( configuration != null )
        {
            return configuration;
        }

        final Configuration newConfiguration = doGetConfiguration();

        // enhancing it
        this.configuration = new EnhancedConfiguration( newConfiguration );

        return this.configuration;
    }

    protected abstract Configuration doGetConfiguration();
}
