/*
 * Copyright (c) 2007-2013 Sonatype, Inc. All rights reserved.
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

    private volatile EnhancedConfiguration configurationCache = null;

    public void clearCache()
    {
        configurationCache = null;
    }

    protected EnhancedConfiguration getConfiguration()
    {
        // Assign configuration to local variable first, as calls to clearCache can null it out at any time
        EnhancedConfiguration configuration = this.configurationCache;
        if ( configuration == null || shouldRebuildConifuguration() )
        {
            synchronized ( this )
            {
                // double-checked locking of volatile is apparently OK with java5+
                // http://www.cs.umd.edu/~pugh/java/memoryModel/DoubleCheckedLocking.html
                configuration = this.configurationCache;
                if ( configuration == null || shouldRebuildConifuguration() )
                {
                    configuration = new EnhancedConfiguration( doGetConfiguration() );
                    this.configurationCache = configuration;
                }
            }
        }
        return configuration;
    }

    /**
     * Returns <code>true</code> if configuration needs to be rebuilt (by calling {@link #doGetConfiguration()}).
     */
    protected boolean shouldRebuildConifuguration()
    {
        return false;
    }

    /**
     * Builds and returns fresh new Configuration instance. Implementation is expected to reset
     * {@link #shouldRebuildConifuguration()} flag back to <code>false</code>.
     */
    protected abstract Configuration doGetConfiguration();
}
