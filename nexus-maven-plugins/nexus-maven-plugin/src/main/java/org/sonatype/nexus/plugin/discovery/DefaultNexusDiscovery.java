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
package org.sonatype.nexus.plugin.discovery;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.model.DeploymentRepository;
import org.apache.maven.model.DistributionManagement;
import org.apache.maven.model.Profile;
import org.apache.maven.model.Repository;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Mirror;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.components.interactivity.Prompter;
import org.codehaus.plexus.components.interactivity.PrompterException;
import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.sonatype.nexus.plugin.util.PromptUtil;
import org.sonatype.plexus.components.sec.dispatcher.SecDispatcher;
import org.sonatype.plexus.components.sec.dispatcher.SecDispatcherException;

@Component( role = NexusInstanceDiscoverer.class )
public class DefaultNexusDiscovery
    implements NexusInstanceDiscoverer, LogEnabled
{

    private static final OptionGenerator<NexusConnectionInfo> URL_OPTION_GENERATOR =
        new OptionGenerator<NexusConnectionInfo>()
        {
            @Override
            public void render( final NexusConnectionInfo item, final StringBuilder sb )
            {
                String name = item.getConnectionName();
                String url = item.getNexusUrl();

                if ( name != null )
                {
                    sb.append( name ).append( " (" ).append( url ).append( ")" );
                }
                else
                {
                    sb.append( url );
                }
            }
        };

    private static final OptionGenerator<Server> SERVER_OPTION_GENERATOR = new OptionGenerator<Server>()
    {
        @Override
        public void render( final Server item, final StringBuilder sb )
        {
            String user = item.getUsername();

            sb.append( item.getId() );
            if ( user != null )
            {
                sb.append( " (" ).append( user ).append( ")" );
            }
        }
    };

    private static final String MANUAL_ENTRY_SERVER_ID = "__manual-entry";

    @Requirement
    private NexusTestClientManager testClientManager;

    @Requirement
    private SecDispatcher secDispatcher;

    @Requirement
    private Prompter prompter;

    private Logger logger;

    public DefaultNexusDiscovery()
    {
    }

    public DefaultNexusDiscovery( final NexusTestClientManager clientManager, final SecDispatcher dispatcher,
                                  final Prompter prompter, final Logger logger )
    {
        testClientManager = clientManager;
        secDispatcher = dispatcher;
        this.prompter = prompter;
        enableLogging( logger );
    }

    public NexusConnectionInfo fillAuth( final String nexusUrl, final Settings settings, final MavenProject project,
                                         final String defaultUser, final boolean fullyAutomatic )
        throws NexusDiscoveryException
    {
        Map<String, Server> serversById = new HashMap<String, Server>();
        List<ServerMapping> serverMap = new ArrayList<ServerMapping>();

        if ( settings != null )
        {
            for ( Server server : settings.getServers() )
            {
                serversById.put( server.getId(), server );
            }
        }

        List<NexusConnectionInfo> candidates = new ArrayList<NexusConnectionInfo>();

        collectForDiscovery( settings, project, candidates, serverMap, serversById );

        for ( NexusConnectionInfo info : candidates )
        {
            if ( info.isConnectable()
                && ( info.getNexusUrl().equals( nexusUrl ) || info.getNexusUrl().startsWith( nexusUrl ) || nexusUrl.startsWith( info.getNexusUrl() ) ) )
            {
                if ( info.isConnectable() )
                {
                    Server server = serversById.get( info.getConnectionId() );
                    if ( ( fullyAutomatic || promptForUserAcceptance( info.getNexusUrl(), info.getConnectionName(),
                                                                      info.getUser() ) )
                        && setAndValidateConnectionAuth( info, server ) )
                    {
                        return info;
                    }
                }
            }
        }

        if ( fullyAutomatic )
        {
            return null;
        }
        else
        {
            NexusConnectionInfo info = new NexusConnectionInfo( nexusUrl );
            if ( !PromptUtil.booleanPrompt( prompter, "Are you sure you want to use the Nexus URL: " + nexusUrl + "? [Y/n] ", Boolean.TRUE ) )
            {
                info = new NexusConnectionInfo( urlPrompt() );
            }

            fillAuth( info, serversById, defaultUser );

            return info;
        }
    }

    public NexusConnectionInfo discover( final Settings settings, final MavenProject project, final String defaultUser,
                                         final boolean fullyAutomatic )
        throws NexusDiscoveryException
    {
        Map<String, Server> serversById = new HashMap<String, Server>();
        List<ServerMapping> serverMap = new ArrayList<ServerMapping>();

        if ( settings != null )
        {
            for ( Server server : settings.getServers() )
            {
                serversById.put( server.getId(), server );
            }
        }

        List<NexusConnectionInfo> candidates = new ArrayList<NexusConnectionInfo>();

        collectForDiscovery( settings, project, candidates, serverMap, serversById );

        NexusConnectionInfo result = testCompleteCandidates( candidates, fullyAutomatic );
        if ( result == null && !fullyAutomatic )
        {
            // if no clear candidate is found, guide the user through menus for URL and server/auth selection.
            all: do
            {
                String url = selectUrl( candidates );

                for ( ServerMapping serverMapping : serverMap )
                {
                    if ( url.equals( serverMapping.getUrl() ) || url.startsWith( serverMapping.getUrl() ) )
                    {
                        Server server = serversById.get( serverMapping.getServerId() );
                        if ( server != null && server.getUsername() != null && server.getPassword() != null )
                        {
                            String password = decryptPassword( server.getPassword() );
                            if ( testClientManager.testConnection( url, server.getUsername(), password ) )
                            {
                                result =
                                    new NexusConnectionInfo( url, server.getUsername(), password,
                                                             serverMapping.getName(), serverMapping.getServerId() );
                                break all;
                            }
                        }
                    }
                }

                Server server = selectAuthentication( url, serversById, defaultUser );

                String password = server.getPassword();
                if ( MANUAL_ENTRY_SERVER_ID.equals( server.getId() ) )
                {
                    password = decryptPassword( password );
                }

                if ( testClientManager.testConnection( url, server.getUsername(), password ) )
                {
                    result = new NexusConnectionInfo( url, server.getUsername(), password );
                }
                else
                {
                    result = null;
                }
            }
            while ( result == null );
        }

        return result;
    }

    private boolean setAndValidateConnectionAuth( final NexusConnectionInfo info, final Server server )
        throws NexusDiscoveryException
    {
        if ( server != null )
        {
            String password = decryptPassword( server.getPassword() );
            info.setUser( server.getUsername() );
            info.setPassword( password );
        }

        if ( testClientManager.testConnection( info.getNexusUrl(), info.getUser(), info.getPassword() ) )
        {
            return true;
        }
        else
        {
            logger.info( "Login failed for: " + info.getNexusUrl() + " (user: " + info.getUser() + ")" );

            return false;
        }
    }

    private void fillAuth( final NexusConnectionInfo info, final Map<String, Server> serversById,
                           final String defaultUser )
    {
        do
        {
            Server server = selectAuthentication( info.getNexusUrl(), serversById, defaultUser );

            info.setUser( server.getUsername() );
            info.setPassword( server.getPassword() );
            if ( !MANUAL_ENTRY_SERVER_ID.equals( server.getId() ) )
            {
                info.setConnectionId( server.getId() );
            }
        }
        while ( !testClientManager.testConnection( info.getNexusUrl(), info.getUser(), info.getPassword() ) );
    }

    private String decryptPassword( final String password )
        throws NexusDiscoveryException
    {
        try
        {
            return secDispatcher.decrypt( password );
        }
        catch ( SecDispatcherException e )
        {
            throw new NexusDiscoveryException( "Failed to decrypt server password: " + password, e );
        }
    }

    private boolean promptForAcceptance( final String url, final String name )
    {
        StringBuilder sb = new StringBuilder();

        sb.append( "\n\nA valid Nexus connection was found at: " ).append( url );
        if ( name != null && name.length() > 0 )
        {
            sb.append( " (" ).append( name ).append( ")" );
        }

        sb.append( "\n\nUse this connection? [Y/n] " );
        return PromptUtil.booleanPrompt( prompter, sb, Boolean.TRUE );
    }

    private boolean promptForUserAcceptance( final String url, final String name, final String user )
    {
        StringBuilder sb = new StringBuilder();

        sb.append( "\n\nLogin to Nexus connection: " ).append( url );
        if ( name != null && name.length() > 0 )
        {
            sb.append( " (" ).append( name ).append( ")" );
        }

        sb.append( "\nwith username: " ).append( user ).append( "? [Y/n] " );
        return PromptUtil.booleanPrompt( prompter,  sb, Boolean.TRUE );
    }

    private Server promptForUserAndPassword( final String defaultUser )
    {
        Server svr = new Server();

        svr.setId( MANUAL_ENTRY_SERVER_ID );
        svr.setUsername( stringPrompt( "Enter Username [" + defaultUser + "]: ", defaultUser ) );
        svr.setPassword( stringPrompt( "Enter Password: " ) );

        return svr;
    }

    private String urlPrompt()
    {
        String result = null;
        URL u = null;
        do
        {
            try
            {
                result = prompter.prompt( "Enter Nexus URL: " );
                if ( result != null )
                {
                    result = result.trim();
                }

                u = new URL( result );
            }
            catch ( MalformedURLException e )
            {
                u = null;
                logger.warn( "Invalid URL: " + result );
            }
            catch ( PrompterException e )
            {
                throw new IllegalStateException( "Prompt for input failed: " + e.getMessage(), e );
            }
        }
        while ( u == null );
        return result;
    }

    

    private String stringPrompt( final CharSequence prompt )
    {
        return stringPrompt( prompt, null );
    }

    private String stringPrompt( final CharSequence prompt, final String defaultValue )
    {
        String result = defaultValue;
        do
        {
            try
            {
                result = prompter.prompt( prompt.toString() );
            }
            catch ( PrompterException e )
            {
                throw new IllegalStateException( "Prompt for input failed: " + e.getMessage(), e );
            }

            if ( result != null )
            {
                result = result.trim();
            }
        }
        while ( result == null || result.length() < 1 );

        return result;
    }

    private Server selectAuthentication( final String url, final Map<String, Server> serversById,
                                         final String defaultUser )
    {
        if ( serversById.isEmpty() )
        {
            return promptForUserAndPassword( defaultUser );
        }
        else
        {
            List<Server> servers = new ArrayList<Server>( serversById.values() );
            StringBuilder sb = new StringBuilder();
            SERVER_OPTION_GENERATOR.generate( servers, sb );

            sb.append( "\nX. Enter username / password manually." );

            sb.append( "\n\nSelect a login to use for Nexus connection '" ).append( url ).append( "': " );

            do
            {
                String result;
                try
                {
                    result = prompter.prompt( sb.toString() );
                    if ( result != null )
                    {
                        result = result.trim();
                    }

                    if ( "X".equalsIgnoreCase( result ) )
                    {
                        return promptForUserAndPassword( defaultUser );
                    }
                }
                catch ( PrompterException e )
                {
                    throw new IllegalStateException( "Prompt for input failed: " + e.getMessage(), e );
                }

                try
                {
                    int idx = Integer.parseInt( result );

                    return servers.get( idx - 1 );
                }
                catch ( NumberFormatException e )
                {
                    logger.warn( "Invalid option: '" + result + "'" );
                }
            }
            while ( true );
        }
    }

    private String selectUrl( final List<NexusConnectionInfo> candidates )
    {
        if ( candidates.isEmpty() )
        {
            return urlPrompt();
        }

        String url = null;
        List<String> urlEntries = new ArrayList<String>();
        for ( NexusConnectionInfo c : candidates )
        {
            urlEntries.add( c.getNexusUrl() );
        }

        StringBuilder sb = new StringBuilder();
        URL_OPTION_GENERATOR.generate( candidates, sb );

        sb.append( "\n\nX. Enter the Nexus URL manually." );
        sb.append( "\n\nSelection: " );

        do
        {
            String result;
            try
            {
                result = prompter.prompt( sb.toString() );
                if ( result != null )
                {
                    result = result.trim();
                }

                if ( "X".equalsIgnoreCase( result ) )
                {
                    return urlPrompt();
                }
            }
            catch ( PrompterException e )
            {
                throw new IllegalStateException( "Prompt for input failed: " + e.getMessage(), e );
            }

            try
            {
                int idx = Integer.parseInt( result );

                url = urlEntries.get( idx - 1 );
            }
            catch ( NumberFormatException e )
            {
                logger.warn( "Invalid option: '" + result + "'" );
            }
        }
        while ( url == null );

        return url;
    }

    private NexusConnectionInfo testCompleteCandidates( final List<NexusConnectionInfo> completeCandidates,
                                                        final boolean fullyAutomatic )
        throws NexusDiscoveryException
    {
        for ( NexusConnectionInfo candidate : completeCandidates )
        {
            if ( !candidate.isConnectable() )
            {
                continue;
            }

            String password = decryptPassword( candidate.getPassword() );
            if ( ( fullyAutomatic || promptForAcceptance( candidate.getNexusUrl(), candidate.getConnectionName() ) )
                && testClientManager.testConnection( candidate.getNexusUrl(), candidate.getUser(), password ) )
            {
                candidate.setPassword( password );
                return candidate;
            }
        }

        return null;
    }

    private void collectForDiscovery( final Settings settings, final MavenProject project,
                                      final List<NexusConnectionInfo> candidates, final List<ServerMapping> serverMap,
                                      final Map<String, Server> serversById )
        throws NexusDiscoveryException
    {
        if ( project != null && project.getDistributionManagement() != null && project.getArtifact() != null )
        {
            DistributionManagement distMgmt = project.getDistributionManagement();
            DeploymentRepository repo = distMgmt.getRepository();
            if ( project.getArtifact().isSnapshot() && distMgmt.getSnapshotRepository() != null )
            {
                repo = distMgmt.getSnapshotRepository();
            }

            if ( repo != null )
            {
                String id = repo.getId();
                String url = repo.getUrl();
                addCandidate( id, url, repo.getName(), serversById, candidates, serverMap );
            }
        }

        if ( settings != null && settings.getMirrors() != null )
        {
            for ( Mirror mirror : settings.getMirrors() )
            {
                addCandidate( mirror.getId(), mirror.getUrl(), mirror.getName(), serversById, candidates, serverMap );
            }
        }

        if ( project != null )
        {
            if ( project.getModel().getRepositories() != null )
            {
                for ( Repository repo : project.getModel().getRepositories() )
                {
                    addCandidate( repo.getId(), repo.getUrl(), repo.getName(), serversById, candidates, serverMap );
                }
            }

            if ( project.getModel().getProfiles() != null )
            {
                for ( Profile profile : project.getModel().getProfiles() )
                {
                    for ( Repository repo : profile.getRepositories() )
                    {
                        addCandidate( repo.getId(), repo.getUrl(), repo.getName(), serversById, candidates, serverMap );
                    }
                }
            }
        }

        if ( settings != null && settings.getProfiles() != null )
        {
            for ( org.apache.maven.settings.Profile profile : settings.getProfiles() )
            {
                if ( profile != null && profile.getRepositories() != null )
                {
                    for ( org.apache.maven.settings.Repository repo : profile.getRepositories() )
                    {
                        addCandidate( repo.getId(), repo.getUrl(), repo.getName(), serversById, candidates, serverMap );
                    }
                }
            }
        }
    }

    private void addCandidate( final String id, String url, final String name, final Map<String, Server> serversById,
                               final List<NexusConnectionInfo> candidates, final List<ServerMapping> serverMap )
        throws NexusDiscoveryException
    {
        if ( url.indexOf( "/service" ) > -1 )
        {
            url = url.substring( 0, url.indexOf( "/service" ) );
        }
        else if ( url.indexOf( "/content" ) > -1 )
        {
            url = url.substring( 0, url.indexOf( "/content" ) );
        }

        if ( id != null && url != null )
        {
            serverMap.add( new ServerMapping( id, url, name ) );
        }

        Server server = serversById.get( id );
        if ( server != null && server.getUsername() != null && server.getPassword() != null )
        {
            String password = decryptPassword( server.getPassword() );
            candidates.add( new NexusConnectionInfo( url, server.getUsername(), password, name, id ) );
        }
        else
        {
            candidates.add( new NexusConnectionInfo( url ).setConnectionName( name ).setConnectionId( id ) );
        }
    }

    public void enableLogging( final Logger logger )
    {
        this.logger = logger;
    }

    public NexusTestClientManager getTestClientManager()
    {
        return testClientManager;
    }

    public void setTestClientManager( final NexusTestClientManager testClientManager )
    {
        this.testClientManager = testClientManager;
    }

    public Logger getLogger()
    {
        return logger;
    }

    public void setLogger( final Logger logger )
    {
        enableLogging( logger );
    }

    public Prompter getPrompter()
    {
        return prompter;
    }

    public void setPrompter( final Prompter prompter )
    {
        this.prompter = prompter;
    }

    private static abstract class OptionGenerator<T>
    {
        public void generate( final Collection<T> items, final StringBuilder sb )
        {
            int count = 0;
            for ( T item : items )
            {
                sb.append( "\n" ).append( ++count ).append( ". " );
                render( item, sb );
            }
        }

        public abstract void render( T item, StringBuilder sb );
    }

    private static final class ServerMapping
    {
        private final String serverId;

        private final String url;

        private final String name;

        ServerMapping( final String serverId, final String url, final String name )
        {
            this.serverId = serverId;
            this.url = url;
            this.name = name;
        }

        String getServerId()
        {
            return serverId;
        }

        String getUrl()
        {
            return url;
        }

        public String getName()
        {
            return name;
        }
    }

	public SecDispatcher getSecDispatcher() 
	{
		return secDispatcher;
	}

	public void setSecDispatcher( SecDispatcher secDispatcher )
	{
		this.secDispatcher = secDispatcher;
	}
}
