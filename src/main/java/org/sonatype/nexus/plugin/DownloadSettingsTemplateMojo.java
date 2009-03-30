package org.sonatype.nexus.plugin;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.SimpleLayout;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.codehaus.plexus.components.interactivity.Prompter;
import org.codehaus.plexus.components.interactivity.PrompterException;
import org.codehaus.plexus.util.IOUtil;
import org.jdom.Document;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.sonatype.nexus.restlight.common.SimpleRESTClientException;
import org.sonatype.nexus.restlight.m2settings.M2SettingsClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * Download a settings.xml template into the local maven instance, either into $maven.home/conf or into ~/.m2.
 * 
 * @goal download
 * @requiresProject false
 * @aggregator
 */
// TODO: Remove aggregator annotation once we have a better solution, but we should only run this once per build.
public class DownloadSettingsTemplateMojo
    extends AbstractMojo
{
    
    /**
     * @component roleHint="jline"
     */
    private Prompter prompter;

    /**
     * @parameter expression="${url}"
     */
    private String url;
    
    /**
     * @parameter expression="${serverAuthId}"
     */
    private String serverAuthId;

    /**
     * @parameter expression="${username}" default-value="${user.name}"
     */
    private String username;

    /**
     * @parameter expression="${password}"
     */
    private String password;

    /**
     * @parameter expression="${destination}" default-value="user"
     */
    private String destination;
    
    /**
     * @parameter expression="${doBackup}" default-value="true"
     */
    private boolean doBackup;

    /**
     * @parameter expression="${backupFormat}" default-value="yyyyMMdd_HHmmss"
     */
    private String backupFormat;
    
    /**
     * @parameter expression="${verboseDebug}" default-value="false"
     */
    private boolean verboseDebug;
    
    /**
     * @parameter expression="${target}"
     */
    private File target;
    
    /**
     * @parameter expression="${encoding}"
     */
    private String encoding;

    /**
     * @parameter default-value="${settings}"
     * @readonly
     */
    private Settings settings;
    
    /**
     * @parameter default-value="${maven.home}/conf"
     * @readonly
     */
    private File mavenHomeConf;

    /**
     * @parameter default-value="${user.home}/.m2"
     * @readonly
     */
    private File mavenUserConf;

    public void execute()
        throws MojoExecutionException
    {
        fillMissing();
        
        initLog4j();
        
        Document settingsDoc = downloadSettings();
        
        save( settingsDoc );
        
    }

    private void initLog4j()
    {
        if ( getLog().isDebugEnabled() )
        {
            if ( verboseDebug )
            {
                LogManager.getRootLogger().setLevel( Level.DEBUG );
            }
            else
            {
                LogManager.getRootLogger().setLevel( Level.INFO );
            }
        }
        else
        {
            LogManager.getRootLogger().setLevel( Level.WARN );
        }
        
        if ( !LogManager.getRootLogger().getAllAppenders().hasMoreElements() )
        {
            LogManager.getRootLogger().addAppender( new ConsoleAppender( new SimpleLayout() ) );
        }
    }

    private void save( Document settingsDoc )
        throws MojoExecutionException
    {
        File f = target;
        if ( f == null )
        {
            SettingsDestination dest = destination == null ? null : SettingsDestination.valueOf( destination );
            if ( dest == null )
            {
                getLog().warn(
                               "Destination parameter is invalid; using: " + SettingsDestination.user
                                   + ". Please specify either '" + SettingsDestination.global.toString() + "' or '" + SettingsDestination.user.toString() + "'." );
                
                dest = SettingsDestination.user;
            }
            
            if ( SettingsDestination.global == dest )
            {
                getLog().debug( "Saving settings to global maven config directory: " + mavenHomeConf );
                
                f = new File( mavenHomeConf, "settings.xml" );
            }
            else
            {
                getLog().debug( "Saving settings to user config directory: " + mavenUserConf );
                
                f = new File( mavenUserConf, "settings.xml" );
            }
        }
        
        f = f.getAbsoluteFile();
        getLog().debug( "Settings will be saved to: " + f.getAbsolutePath() );
        
        if ( doBackup && f.exists() )
        {
            String backupString = new SimpleDateFormat( backupFormat ).format( new Date() );
            
            File b = new File( f.getParentFile(), "settings.xml." + backupString );
            
            getLog().debug( "Backing up old settings to: " + b.getAbsolutePath() );
            if ( !f.renameTo( b ) )
            {
                throw new MojoExecutionException( "Cannot rename existing settings to backup file.\nExisting file: "
                    + f.getAbsolutePath() + "\nBackup file: " + b.getAbsolutePath() );
            }
            
            getLog().info( "Existing settings backed up to: " + b.getAbsolutePath() );
        }
        
        Writer w = null;
        try
        {
            if ( encoding != null )
            {
                w = new OutputStreamWriter( new FileOutputStream( f ), encoding );
            }
            else
            {
                w = new FileWriter( f );
            }
            
            new XMLOutputter( Format.getPrettyFormat() ).output( settingsDoc, w );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Failed to save settings. Reason: " + e.getMessage(), e );
        }
        finally
        {
            IOUtil.close( w );
        }
        
        getLog().info( "Settings saved to: " + f.getAbsolutePath() );
    }

    private Document downloadSettings()
        throws MojoExecutionException
    {
        String baseUrl;

        int svcIdx = url.indexOf( "/service" );
        if ( svcIdx < 0 )
        {
            throw new MojoExecutionException( "Cannot find Nexus base-URL from: " + url );
        }
        else
        {
            baseUrl = url.substring( 0, svcIdx );
        }
        
        M2SettingsClient client;
        try
        {
            client = new M2SettingsClient( baseUrl, username, password );
        }
        catch ( SimpleRESTClientException e )
        {
            throw new MojoExecutionException( "Failed to start REST client: " + e.getMessage(), e );
        }
        
        try
        {
            return client.getSettingsTemplateAbsolute( url );
        }
        catch ( SimpleRESTClientException e )
        {
            throw new MojoExecutionException( "Failed to retrieve Maven settings.xml from: " + url + "\n(Reason: " + e.getMessage() + ")" , e );
        }
    }

    private void fillMissing()
        throws MojoExecutionException
    {
        while ( url == null || url.trim().length() < 1 )
        {
            try
            {
                url = prompter.prompt( "Nexus URL: " );
            }
            catch ( PrompterException e )
            {
                throw new MojoExecutionException( "Failed to read from CLI prompt: " + e.getMessage(), e );
            }
        }

        if ( serverAuthId != null )
        {
            Server server = settings == null ? null : settings.getServer( serverAuthId );
            if ( server != null )
            {
                username = server.getUsername();
                password = server.getPassword();
            }
            else
            {
                getLog().debug( "Server entry not found for: '" + serverAuthId + "'." );
            }
        }

        while ( password == null || password.trim().length() < 1 )
        {
            try
            {
                password = prompter.promptForPassword( "Password: " );
            }
            catch ( PrompterException e )
            {
                throw new MojoExecutionException( "Failed to read from CLI prompt: " + e.getMessage(), e );
            }
        }

    }

    public Prompter getPrompter()
    {
        return prompter;
    }

    public void setPrompter( Prompter prompter )
    {
        this.prompter = prompter;
    }

    public String getUrl()
    {
        return url;
    }

    public void setUrl( String url )
    {
        this.url = url;
    }

    public String getServerAuthId()
    {
        return serverAuthId;
    }

    public void setServerAuthId( String serverAuthId )
    {
        this.serverAuthId = serverAuthId;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername( String username )
    {
        this.username = username;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword( String password )
    {
        this.password = password;
    }

    public String getDestination()
    {
        return destination;
    }

    public void setDestination( String destination )
    {
        this.destination = destination;
    }

    public boolean isDoBackup()
    {
        return doBackup;
    }

    public void setDoBackup( boolean doBackup )
    {
        this.doBackup = doBackup;
    }

    public String getBackupFormat()
    {
        return backupFormat;
    }

    public void setBackupFormat( String backupFormat )
    {
        this.backupFormat = backupFormat;
    }

    public boolean isVerboseDebug()
    {
        return verboseDebug;
    }

    public void setVerboseDebug( boolean verboseDebug )
    {
        this.verboseDebug = verboseDebug;
    }

    public File getTarget()
    {
        return target;
    }

    public void setTarget( File target )
    {
        this.target = target;
    }

    public String getEncoding()
    {
        return encoding;
    }

    public void setEncoding( String encoding )
    {
        this.encoding = encoding;
    }

    public Settings getSettings()
    {
        return settings;
    }

    public void setSettings( Settings settings )
    {
        this.settings = settings;
    }

    public File getMavenHomeConf()
    {
        return mavenHomeConf;
    }

    public void setMavenHomeConf( File mavenHomeConf )
    {
        this.mavenHomeConf = mavenHomeConf;
    }

    public File getMavenUserConf()
    {
        return mavenUserConf;
    }

    public void setMavenUserConf( File mavenUserConf )
    {
        this.mavenUserConf = mavenUserConf;
    }

}
