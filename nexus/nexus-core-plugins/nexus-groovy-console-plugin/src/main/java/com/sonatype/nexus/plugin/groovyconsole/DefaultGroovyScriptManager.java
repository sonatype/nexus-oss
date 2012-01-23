/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package com.sonatype.nexus.plugin.groovyconsole;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.logging.AbstractLoggingComponent;
import org.sonatype.plexus.appevents.Event;
import com.sonatype.nexus.plugin.groovyconsole.rest.dto.GroovyScriptDTO;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.util.AntBuilder;

@Component( role = GroovyScriptManager.class, instantiationStrategy = "singleton" )
public class DefaultGroovyScriptManager
    extends AbstractLoggingComponent
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
