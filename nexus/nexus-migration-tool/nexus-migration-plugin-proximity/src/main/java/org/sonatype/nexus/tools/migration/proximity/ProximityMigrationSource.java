/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype Inc.                                                                                                                          
 * 
 * This file is part of Nexus.                                                                                                                                  
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 */
package org.sonatype.nexus.tools.migration.proximity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.codehaus.plexus.util.InterpolationFilterReader;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.sonatype.nexus.tools.migration.AbstractMigrationSource;
import org.sonatype.nexus.tools.migration.MigrationMonitor;
import org.sonatype.nexus.tools.migration.MigrationRequest;
import org.sonatype.nexus.tools.migration.MigrationResult;
import org.sonatype.nexus.tools.migration.MigrationSource;

/**
 * Converts Proximity configuration to Nexus.
 * 
 * @author cstamas
 * @plexus.component role-hint="proximity"
 */
public class ProximityMigrationSource
    extends AbstractMigrationSource
    implements MigrationSource
{

    /**
     * @plexus.requirement
     */
    private NexusConfigurationBuilder nexusConfigurationBuilder;

    private List<File> springContexts;

    private Properties externalizedProperties;

    public void migrateConfiguration( MigrationRequest req, MigrationResult res, MigrationMonitor monitor )
        throws IOException
    {
        res.setSuccesful( false );
        int step = 1;
        monitor.migrationStarted( req, 5 );

        try
        {

            // we should be pointed to web.xml of proximity
            if ( "web.xml".equals( req.getFile().getName() ) )
            {
                getLogger().debug( "web.xml found" );
                // get all contexts from context-param named "contextConfigLocation"
                Reader r = new FileReader( req.getFile() );
                Xpp3Dom webXmlDom = Xpp3DomBuilder.build( r );

                Xpp3Dom[] children = webXmlDom.getChildren( "context-param" );
                for ( int i = 0; i < children.length; i++ )
                {
                    if ( children[i].getChild( "param-name" ).getValue() != null
                        && "contextConfigLocation".equals( children[i].getChild( "param-name" ).getValue() ) )
                    {
                        getLogger().debug( "found Spring contextConfigLocation context-param" );
                        String[] contexts = children[i].getChild( "param-value" ).getValue().split( "\n" );
                        springContexts = new ArrayList<File>();
                        for ( int j = 0; j < contexts.length; j++ )
                        {
                            String ctxPath = StringUtils.trim( contexts[j] );
                            // exclude XFire context which is on classpath
                            if ( ctxPath != null && !ctxPath.startsWith( "classpath:" ) )
                            {
                                getLogger().debug( "extracted " + StringUtils.trim( contexts[j] ) );
                                // we have a spring ctx file
                                File ctxFile = new File( req.getFile().getParentFile().getParentFile(), ctxPath );
                                if ( ctxFile.exists() )
                                {
                                    springContexts.add( ctxFile );
                                }
                            }
                        }
                        break;
                    }
                }
            }
            else
            {
                res.getExceptions().add(
                    new IllegalArgumentException( "Point to the web.xml of the Proximity1 web application!" ) );
                return;
            }
            monitor.migrationProgress( req, step++, "Extracted Spring contexts from web.xml." );

            File propsFile = new File( req.getFile().getParentFile().getParentFile(), "/WEB-INF/proximity.properties" );
            if ( propsFile.exists() )
            {
                externalizedProperties = new Properties();
                externalizedProperties.load( new FileInputStream( propsFile ) );
                monitor.migrationProgress( req, step++, "Proximity properties found and loaded." );
            }
            else
            {
                monitor.migrationProgress( req, step++, "Proximity properties not found." );
            }

            // merging spring contexts
            Xpp3Dom springContext = null;
            for ( File file : springContexts )
            {
                getLogger().debug( "Loading context file: " + file.getPath() );

                Reader r = new FileReader( file );
                Xpp3Dom ctx;
                if ( externalizedProperties != null )
                {
                    InterpolationFilterReader ir = new InterpolationFilterReader( r, externalizedProperties );
                    ctx = Xpp3DomBuilder.build( ir );
                }
                else
                {
                    ctx = Xpp3DomBuilder.build( r );
                }
                if ( springContext == null )
                {
                    springContext = ctx;
                }
                else
                {
                    Xpp3Dom.mergeXpp3Dom( springContext, ctx, Boolean.FALSE );
                }
            }
            monitor.migrationProgress( req, step++, "Merged Spring contexts." );

            // 1st pass, collecting interesting beans
            ProximitySpringContext ctx = new ProximitySpringContext();
            Xpp3Dom[] beans = springContext.getChildren( "bean" );
            for ( int i = 0; i < beans.length; i++ )
            {
                if ( beans[i].getAttribute( "class" ) != null )
                {
                    if ( "org.abstracthorizon.proximity.impl.LogicDrivenProximityImpl".equals( beans[i]
                        .getAttribute( "class" ) ) )
                    {
                        // we found proximity bean
                        ctx.setProximityBean( beans[i] );
                        getLogger().debug( "Found Proximity bean" );
                    }
                    else if ( "org.abstracthorizon.proximity.mapping.PathBasedGroupRequestMapper".equals( beans[i]
                        .getAttribute( "class" ) ) )
                    {
                        // we found the path mapper
                        ctx.setGroupRequestMapperBean( beans[i] );
                        getLogger().debug( "Found PathMapper bean" );
                    }
                    else if ( "org.abstracthorizon.proximity.impl.LogicDrivenRepositoryImpl".equals( beans[i]
                        .getAttribute( "class" ) ) )
                    {
                        // we have a repo
                        ctx.getRepositoryBeans().put( beans[i].getAttribute( "id" ), beans[i] );
                        getLogger().debug( "Found Repository bean: " + beans[i].getAttribute( "id" ) );
                    }
                    else if ( "org.abstracthorizon.proximity.storage.local.WritableFileSystemStorage".equals( beans[i]
                        .getAttribute( "class" ) )
                        || "org.abstracthorizon.proximity.storage.local.ReadOnlyFileSystemStorage".equals( beans[i]
                            .getAttribute( "class" ) ) )
                    {
                        ctx.getLocalStorageBeans().put( beans[i].getAttribute( "id" ), beans[i] );
                        getLogger().debug( "Found Repository Local Storage bean: " + beans[i].getAttribute( "id" ) );
                    }
                    else if ( "org.abstracthorizon.proximity.storage.remote.CommonsHttpClientRemotePeer"
                        .equals( beans[i].getAttribute( "class" ) ) )
                    {
                        ctx.getRemoteStorageBeans().put( beans[i].getAttribute( "id" ), beans[i] );
                        getLogger().debug( "Found Repository Remote Storage bean: " + beans[i].getAttribute( "id" ) );
                    }
                    else if ( "org.abstracthorizon.proximity.maven.MavenProxyRepositoryLogic".equals( beans[i]
                        .getAttribute( "class" ) ) )
                    {
                        ctx.getRepositoryLogicBeans().put( beans[i].getAttribute( "id" ), beans[i] );
                        getLogger().debug( "Found Repository Logic bean: " + beans[i].getAttribute( "id" ) );
                    }
                }

            }
            monitor.migrationProgress( req, step++, "Beans extracted from Spring contexts." );

            nexusConfigurationBuilder.buildConfiguration( ctx, req, res );

            monitor.migrationProgress( req, step++, "Nexus configuration constructed." );

            res.setSuccesful( true );
        }
        catch ( Exception e )
        {
            res.getExceptions().add( e );
        }

    }

}
