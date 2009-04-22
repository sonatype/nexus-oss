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
package org.sonatype.jsecurity;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.codehaus.plexus.PlexusTestCase;
import org.sonatype.security.model.Configuration;
import org.sonatype.security.model.io.xpp3.SecurityConfigurationXpp3Reader;

public abstract class AbstractSecurityTestCase
    extends PlexusTestCase
{
    protected Configuration getConfigurationFromStream( InputStream is )
        throws Exception
    {
        SecurityConfigurationXpp3Reader reader = new SecurityConfigurationXpp3Reader();
    
        Reader fr = new InputStreamReader( is );
    
        return reader.read( fr );
    }
}
