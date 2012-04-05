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
package org.sonatype.security.web;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.enterprise.inject.Typed;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.shiro.web.filter.mgt.FilterChainManager;

/**
 * The default implementation requires a FilterChainManager, so the configuration can be passed to it.
 * 
 * @author Brian Demers
 *
 */
@Singleton
@Typed( value = ProtectedPathManager.class )
@Named( value = "default" )
public class DefaultProtectedPathManager
    implements ProtectedPathManager, FilterChainManagerAware
{
    private FilterChainManager filterChainManager;
    
    protected Map<String, String> pseudoChains = new LinkedHashMap<String, String>();
    

    public void addProtectedResource( String pathPattern, String filterExpression )
    {
        // Only save the pathPattern and filterExpression in the pseudoChains, does not put real filters into the real
        // chain.
        // We can not get the real filters because this method is invoked when the application is starting, when ShiroSecurityFilter
        // might not be located.
     
        if( this.filterChainManager != null )
        {
            this.filterChainManager.createChain( pathPattern, filterExpression );
        }
        else
        {
            this.pseudoChains.put( pathPattern, filterExpression );
        }
    }

    public void setFilterChainManager( FilterChainManager filterChainManager )
    {
        this.filterChainManager = filterChainManager;
        
        // lazy load: see https://issues.sonatype.org/browse/NEXUS-3111
        // which to me seems like a glassfish bug...
        for ( Entry<String, String> entry : this.pseudoChains.entrySet() )
        {
            this.filterChainManager.createChain( entry.getKey(), entry.getValue() );
        }
    }

}
