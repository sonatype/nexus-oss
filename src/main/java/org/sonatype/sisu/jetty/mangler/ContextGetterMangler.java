/*
 * Copyright (c) 2012 Sonatype, Inc. All rights reserved.
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
package org.sonatype.sisu.jetty.mangler;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;

/**
 * Gets the ContextHandler on given path.
 * 
 * @author cstamas
 */
public class ContextGetterMangler
    extends AbstractContextMangler
    implements ServerMangler<ContextHandler>
{
    public ContextGetterMangler( final String contextPath )
    {
        super( contextPath );
    }

    public ContextHandler mangle( final Server server )
    {
        return getContext( server );
    }
}
