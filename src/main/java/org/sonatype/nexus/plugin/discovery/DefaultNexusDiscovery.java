package org.sonatype.nexus.plugin.discovery;

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
import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.sonatype.plexus.components.sec.dispatcher.SecDispatcher;
import org.sonatype.plexus.components.sec.dispatcher.SecDispatcherException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@Component( role = NexusInstanceDiscoverer.class )
public final class DefaultNexusDiscovery
    implements NexusInstanceDiscoverer, LogEnabled
{

    private static final OptionGenerator<Map.Entry<String, String>> URL_OPTION_GENERATOR =
        new OptionGenerator<Map.Entry<String, String>>()
        {
            @Override
            public void render( final Entry<String, String> item, final StringBuilder sb )
            {
                String name = item.getValue();
                String url = item.getKey();

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

    @Requirement( hint = "maven" )
    private SecDispatcher securityDispatcher;

    private BufferedReader userInput = new BufferedReader( new InputStreamReader( System.in ) );

    private PrintStream userOutput = System.out;

    private Logger logger;

    public DefaultNexusDiscovery()
    {
    }

    public DefaultNexusDiscovery( final NexusTestClientManager clientManager, final SecDispatcher dispatcher,
                                  final Logger logger )
    {
        testClientManager = clientManager;
        securityDispatcher = dispatcher;
        enableLogging( logger );
    }

    public NexusConnectionInfo discover( final Settings settings, final MavenProject project,
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

        List<NexusConnectionInfo> completeCandidates = new ArrayList<NexusConnectionInfo>();
        LinkedHashMap<String, String> potentialUrls = new LinkedHashMap<String, String>();

        collectForDiscovery( settings, project, completeCandidates, potentialUrls, serverMap, serversById );

        NexusConnectionInfo result = testCompleteCandidates( completeCandidates, fullyAutomatic );
        if ( result == null )
        {
            // if no clear candidate is found, guide the user through menus for URL and server/auth selection.
            all: do
            {
                String url = selectUrl( potentialUrls );

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
                                                             serverMapping.getName() );
                                break all;
                            }
                        }
                    }
                }

                Server server = selectAuthentication( serversById );

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

    private boolean promptForAcceptance( final String url, final String name )
    {
        StringBuilder sb = new StringBuilder();

        sb.append( "\n\nA valid Nexus connection was found at: " ).append( url );
        if ( name != null && name.length() > 0 )
        {
            sb.append( " (" ).append( name ).append( ")" );
        }

        sb.append( "\n\nUse this connection? [Y/n] " );

        Boolean result = null;
        do
        {
            userOutput.print( sb.toString() );
            String txt = null;
            try
            {
                txt = userInput.readLine();
                if ( txt != null )
                {
                    if ( txt.trim().length() > 0 )
                    {
                        txt = txt.trim().toLowerCase();
                        if ( "y".equals( txt ) || "yes".equals( txt ) )
                        {
                            result = true;
                        }
                        else if ( "n".equals( txt ) || "no".equals( txt ) )
                        {
                            result = false;
                        }
                    }

                }
            }
            catch ( IOException e )
            {
                throw new IllegalStateException( "Cannot read from user input: " + e.getMessage(), e );
            }

            if ( result == null )
            {
                userOutput.println( "\nInvalid input: " + txt );
            }
        }
        while ( result == null );

        return result;
    }

    private String decryptPassword( final String password )
        throws NexusDiscoveryException
    {
        try
        {
            return securityDispatcher.decrypt( password );
        }
        catch ( SecDispatcherException e )
        {
            throw new NexusDiscoveryException( "Failed to decrypt server password: " + password, e );
        }
    }

    private Server selectAuthentication( final Map<String, Server> serversById )
    {
        if ( serversById.isEmpty() )
        {
            return promptForUserAndPassword();
        }
        else
        {
            List<Server> servers = new ArrayList<Server>( serversById.values() );
            StringBuilder sb = new StringBuilder();
            SERVER_OPTION_GENERATOR.generate( servers, sb );

            sb.append( "\nX. Enter username / password manually." );

            sb.append( "\n\nSelection: " );

            do
            {
                userOutput.print( sb.toString() );
                String result;
                try
                {
                    result = userInput.readLine();
                    if ( result != null )
                    {
                        result = result.trim();
                    }

                    if ( "X".equalsIgnoreCase( result ) )
                    {
                        return promptForUserAndPassword();
                    }
                }
                catch ( IOException e )
                {
                    throw new IllegalStateException( "Cannot read from user input: " + e.getMessage(), e );
                }

                try
                {
                    int idx = Integer.parseInt( result );

                    return servers.get( idx - 1 );
                }
                catch ( NumberFormatException e )
                {
                    userOutput.println( "Invalid option: '" + result + "'" );
                }
            }
            while ( true );
        }
    }

    private Server promptForUserAndPassword()
    {
        try
        {
            Server svr = new Server();
            svr.setId( MANUAL_ENTRY_SERVER_ID );
            do
            {
                userOutput.print( "Enter Username: " );
                String result = userInput.readLine();
                if ( result != null )
                {
                    result = result.trim();
                }

                svr.setUsername( result );
            }
            while ( svr.getUsername() == null || svr.getUsername().length() < 1 );

            do
            {
                userOutput.print( "Enter Password: " );
                String result = userInput.readLine();
                if ( result != null )
                {
                    result = result.trim();
                }

                svr.setPassword( result );
            }
            while ( svr.getPassword() == null || svr.getPassword().length() < 1 );

            return svr;
        }
        catch ( IOException e )
        {
            throw new IllegalStateException( "Cannot read from user input: " + e.getMessage(), e );
        }
    }

    private void collectForDiscovery( final Settings settings, final MavenProject project,
                                      final List<NexusConnectionInfo> completeCandidates,
                                      final LinkedHashMap<String, String> potentialUrls,
                                      final List<ServerMapping> serverMap, final Map<String, Server> serversById )
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
                catalog( id, url, repo.getName(), serversById, completeCandidates, potentialUrls, serverMap );
            }
        }

        if ( settings.getMirrors() != null )
        {
            for ( Mirror mirror : settings.getMirrors() )
            {
                catalog( mirror.getId(), mirror.getUrl(), mirror.getName(), serversById, completeCandidates,
                         potentialUrls, serverMap );
            }
        }

        if ( project != null )
        {
            if ( project.getModel().getRepositories() != null )
            {
                for ( Repository repo : project.getModel().getRepositories() )
                {
                    catalog( repo.getId(), repo.getUrl(), repo.getName(), serversById, completeCandidates,
                             potentialUrls, serverMap );
                }
            }

            if ( project.getModel().getProfiles() != null )
            {
                for ( Profile profile : project.getModel().getProfiles() )
                {
                    for ( Repository repo : profile.getRepositories() )
                    {
                        catalog( repo.getId(), repo.getUrl(), repo.getName(), serversById, completeCandidates,
                                 potentialUrls, serverMap );
                    }
                }
            }
        }

        if ( settings.getProfiles() != null )
        {
            for ( org.apache.maven.settings.Profile profile : settings.getProfiles() )
            {
                if ( profile != null && profile.getRepositories() != null )
                {
                    for ( org.apache.maven.settings.Repository repo : profile.getRepositories() )
                    {
                        catalog( repo.getId(), repo.getUrl(), repo.getName(), serversById, completeCandidates,
                                 potentialUrls, serverMap );
                    }
                }
            }
        }
    }

    private String selectUrl( final LinkedHashMap<String, String> potentialUrls )
    {
        if ( potentialUrls.isEmpty() )
        {
            return promptForUrl();
        }

        String url = null;
        List<String> urlEntries = new ArrayList<String>();
        for ( String u : potentialUrls.keySet() )
        {
            urlEntries.add( u );
        }

        StringBuilder sb = new StringBuilder();
        URL_OPTION_GENERATOR.generate( potentialUrls.entrySet(), sb );

        sb.append( "\n\nX. Enter the Nexus URL manually." );
        sb.append( "\n\nSelection: " );

        do
        {
            userOutput.print( sb.toString() );
            String result;
            try
            {
                result = userInput.readLine();
                if ( result != null )
                {
                    result = result.trim();
                }

                if ( "X".equalsIgnoreCase( result ) )
                {
                    return promptForUrl();
                }
            }
            catch ( IOException e )
            {
                throw new IllegalStateException( "Cannot read from user input: " + e.getMessage(), e );
            }

            try
            {
                int idx = Integer.parseInt( result );

                url = urlEntries.get( idx - 1 );
            }
            catch ( NumberFormatException e )
            {
                userOutput.println( "Invalid option: '" + result + "'" );
            }
        }
        while ( url == null );

        return url;
    }

    private String promptForUrl()
    {
        String result = null;
        URL u = null;
        do
        {
            userOutput.print( "Enter URL: " );
            try
            {
                result = userInput.readLine();
                if ( result != null )
                {
                    result = result.trim();
                }

                u = new URL( result );
            }
            catch ( MalformedURLException e )
            {
                u = null;
                userOutput.println( "Invalid URL: " + result );
            }
            catch ( IOException e )
            {
                throw new IllegalStateException( "Cannot read from user input: " + e.getMessage(), e );
            }
        }
        while ( u == null );
        return result;
    }

    private NexusConnectionInfo testCompleteCandidates( final List<NexusConnectionInfo> completeCandidates,
                                                        final boolean fullyAutomatic )
        throws NexusDiscoveryException
    {
        for ( NexusConnectionInfo candidate : completeCandidates )
        {
            String password = decryptPassword( candidate.getPassword() );
            if ( testClientManager.testConnection( candidate.getNexusUrl(), candidate.getUser(), password )
                && ( fullyAutomatic || promptForAcceptance( candidate.getNexusUrl(), candidate.getConnectionName() ) ) )
            {
                candidate.setPassword( password );
                return candidate;
            }
        }

        return null;
    }

    private void catalog( final String id, String url, final String name, final Map<String, Server> serversById,
                          final List<NexusConnectionInfo> completeCandidates,
                          final LinkedHashMap<String, String> potentialUrls, final List<ServerMapping> serverMap )
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
            NexusConnectionInfo info = new NexusConnectionInfo( url, server.getUsername(), server.getPassword(), name );
            completeCandidates.add( info );
        }
        else
        {
            potentialUrls.put( url, name );
        }
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

    public BufferedReader getUserInput()
    {
        return userInput;
    }

    public void setUserInput( final BufferedReader userInput )
    {
        this.userInput = userInput;
    }

    public PrintStream getUserOutput()
    {
        return userOutput;
    }

    public void setUserOutput( final PrintStream userOutput )
    {
        this.userOutput = userOutput;
    }
}
