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

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.vfs.FileChangeEvent;
import org.apache.commons.vfs.FileContent;
import org.apache.commons.vfs.FileListener;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSelectInfo;
import org.apache.commons.vfs.FileSelector;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.VFS;
import org.apache.commons.vfs.impl.DefaultFileMonitor;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Disposable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.logging.AbstractLoggingComponent;
import org.sonatype.plexus.appevents.Event;

@Component( role = ScriptStorage.class )
public class DefaultScriptStorage
    extends AbstractLoggingComponent
    implements ScriptStorage, Initializable, Disposable, FileListener
{

    @Requirement
    private ApplicationConfiguration applicationConfiguration;

    private static final String GROOVY = "groovy";

    private static final String DOT_GROOVY = "." + GROOVY;

    private DefaultFileMonitor fileMonitor;

    private Map<String, String> scripts;

    private File scriptDir;

    public void dispose()
    {
        fileMonitor.stop();
        fileMonitor = null;

        scripts.clear();
        scripts = null;

        scriptDir = null;
    }

    public void fileChanged( FileChangeEvent e )
        throws Exception
    {
        if ( !isScriptFile( e.getFile() ) )
        {
            return;
        }

        updateScript( e.getFile() );
    }

    public void fileCreated( FileChangeEvent e )
        throws Exception
    {
        if ( !isScriptFile( e.getFile() ) )
        {
            return;
        }

        updateScript( e.getFile() );
    }

    public void fileDeleted( FileChangeEvent e )
        throws Exception
    {
        if ( !isScriptFile( e.getFile() ) )
        {
            return;
        }

        synchronized ( scripts )
        {
            scripts.remove( getName( e.getFile().getName() ) );
        }
    }

    public String getScript( Class<? extends Event<?>> eventClass )
    {
        synchronized ( scripts )
        {
            return scripts.get( eventClass.getName() );
        }
    }

    public void initialize()
        throws InitializationException
    {
        scripts = new LinkedHashMap<String, String>();

        FileObject listendir;
        try
        {
            FileSystemManager fsManager = VFS.getManager();
            scriptDir = applicationConfiguration.getWorkingDirectory( "scripts" );
            if ( !scriptDir.exists() )
            {
                scriptDir.mkdirs();

                try
                {
                    new File( scriptDir, "place your .groovy files here.txt" ).createNewFile();
                }
                catch ( IOException e )
                {
                    throw new InitializationException( e.getMessage(), e );
                }
            }

            listendir = fsManager.resolveFile( scriptDir.getAbsolutePath() );
        }
        catch ( FileSystemException e )
        {
            throw new InitializationException( e.getMessage(), e );
        }

        FileSelector selector = new FileSelector()
        {
            public boolean traverseDescendents( FileSelectInfo arg0 )
                throws Exception
            {
                return true;
            }

            public boolean includeFile( FileSelectInfo arg0 )
                throws Exception
            {
                return isScriptFile( arg0.getFile() );
            }
        };

        try
        {
            FileObject[] availableScripts = listendir.findFiles( selector );
            for ( FileObject fileObject : availableScripts )
            {
                updateScript( fileObject );
            }
        }
        catch ( FileSystemException e )
        {
            getLogger().warn( "Unable to perform initial directory scan.", e );
        }

        DefaultFileMonitor fm = new DefaultFileMonitor( this );
        fm.setRecursive( true );
        fm.addFile( listendir );
        fm.start();

        this.fileMonitor = fm;
    }

    private boolean isScriptFile( FileObject file )
    {
        FileName name = file.getName();
        if ( name.getBaseName().endsWith( DOT_GROOVY ) )
        {
            return true;
        }
        return false;
    }

    private void updateScript( FileObject file )
    {
        FileName name = file.getName();
        getLogger().info( "New script file found: " + name );

        String script;
        try
        {
            FileContent content = file.getContent();
            script = IOUtil.toString( content.getInputStream() );
            content.close();
        }
        catch ( IOException e )
        {
            getLogger().warn( "Unable to read script file: " + name, e );
            return;
        }

        synchronized ( scripts )
        {
            scripts.put( getName( name ), script );
        }
    }

    private String getName( FileName name )
    {
        String baseName = name.getBaseName();
        baseName = baseName.substring( 0, baseName.length() - DOT_GROOVY.length() );
        return baseName;
    }

    public Map<String, String> getScripts()
    {
        return Collections.unmodifiableMap( this.scripts );
    }

    public void store( String name, String script )
        throws IOException
    {
        if ( !name.endsWith( DOT_GROOVY ) )
        {
            name = name + DOT_GROOVY;
        }

        File output = new File( scriptDir, name );
        FileUtils.fileWrite( output.getAbsolutePath(), script );

        synchronized ( scripts )
        {
            scripts.put( name, script );
        }
    }
}
