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
package org.sonatype.logging;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.slf4j.bridge.SLF4JBridgeHandler;

/**
 * Simply installs the JULOverSlf4j handler.
 * 
 * @author cstamas
 * @plexus.component role="org.sonatype.logging.Slf4jHandlerInstaller"
 */
public class Slf4jHandlerInstaller
    implements Initializable
{
    public void initialize()
        throws InitializationException
    {
        SLF4JBridgeHandler.install();
    }
}
