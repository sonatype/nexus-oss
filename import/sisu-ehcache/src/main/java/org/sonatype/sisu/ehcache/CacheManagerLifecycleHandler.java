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
package org.sonatype.sisu.ehcache;

import org.sonatype.appcontext.lifecycle.Stoppable;

/**
 * Simple reusable CacheManager component lifecycle handler.
 * 
 * @author cstamas
 * @since 1.1
 */
public class CacheManagerLifecycleHandler
    implements Stoppable
{
    private final CacheManagerComponent cacheManagerComponent;

    public CacheManagerLifecycleHandler( final CacheManagerComponent cacheManagerComponent )
    {
        if ( cacheManagerComponent == null )
        {
            throw new NullPointerException( "Supplied CacheManagerComponent  cannot be null!" );
        }
        this.cacheManagerComponent = cacheManagerComponent;
    }

    public void handle()
    {
        cacheManagerComponent.shutdown();
    }
}
