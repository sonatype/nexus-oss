/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package com.sonatype.nexus.plugin.groovyconsole;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.util.AntBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.sonatype.plexus.appevents.Event;

import com.sonatype.nexus.plugin.groovyconsole.rest.dto.GroovyScriptDTO;

@Component( role = GroovyScriptManager.class, instantiationStrategy = "singleton" )
public class DefaultGroovyScriptManager
    extends AbstractLogEnabled
    implements GroovyScriptManager
{
    @Requirement
    private ScriptStorage storage;

    @Requirement
    private PlexusContainer plexus;

    public void actUponEvent( Event<?> evt )
    {
        Class<? extends Event<?>> c = (Class<? extends Event<?>>) evt.getClass();
        String script = storage.getScript( c );

        if ( script == null )
        {
            return;
        }

        Binding binding = new Binding();
        binding.setVariable( "ant", new AntBuilder() );
        binding.setVariable( "event", evt );
        binding.setVariable( "plexus", plexus );
        binding.setVariable( "logger", getLogger() );

        CompilerConfiguration config = new CompilerConfiguration( CompilerConfiguration.DEFAULT );

        GroovyShell interpreter = new GroovyShell( binding, config );
        Object result = interpreter.evaluate( script );

        getLogger().info( "Script return: " + result );
    }

    public List<GroovyScriptDTO> getScripts()
    {
        Set<Entry<String, String>> scripts = storage.getScripts().entrySet();

        List<GroovyScriptDTO> sc = new ArrayList<GroovyScriptDTO>();
        for ( Entry<String, String> entry : scripts )
        {
            sc.add( new GroovyScriptDTO( entry.getKey(), entry.getValue() ) );
        }

        return sc;
    }

    public void save( GroovyScriptDTO script )
        throws IOException
    {
        storage.store( script.getName(), script.getScript() );
    }

}
