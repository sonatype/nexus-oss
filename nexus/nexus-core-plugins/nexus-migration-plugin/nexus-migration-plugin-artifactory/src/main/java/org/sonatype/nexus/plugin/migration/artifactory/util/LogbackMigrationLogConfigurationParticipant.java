/**
 * Copyright (c) 2008-2011 Sonatype, Inc. All rights reserved.
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
package org.sonatype.nexus.plugin.migration.artifactory.util;

import java.io.IOException;
import java.io.InputStream;


import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.log.LogConfigurationParticipant;

@Component( role = LogConfigurationParticipant.class, hint="logback-migration" )
public class LogbackMigrationLogConfigurationParticipant
    implements LogConfigurationParticipant
{

    @Override
    public String getName()
    {
        return "logback-migration.xml";
    }

    @Override
    public InputStream getConfiguration()
    {
        try
        {
            return this.getClass().getResource( "/META-INF/log/logback-migration.xml" ).openStream();
        }
        catch ( IOException e )
        {
            throw new IllegalStateException( "Could not access logback-migration.xml", e );
        }
    }

}
