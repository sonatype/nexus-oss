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
package org.sonatype.nexus.plugin;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.components.interactivity.PrompterException;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.InterpolationFilterReader;
import org.jdom.Document;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.sonatype.nexus.restlight.common.RESTLightClientException;
import org.sonatype.nexus.restlight.m2settings.M2SettingsClient;

/**
 * <p>
 * Download a settings.xml template into the local maven instance. Settings can be saved to several locations:
 * </p>
 * <ul>
 * <li><b>destination == global</b>: Save to $maven.home/conf</li>
 * <li><b>destination == user</b>: Save to ~/.m2</li>
 * <li><b>target == /path/to/settings.xml</b>: Save to <code>/path/to/settings.xml</code></li>
 * </ul>
 * <p>
 * Additionally, by default any existing settings.xml file in the way will be backed up using a datestamp to ensure
 * previous backup files are not overwritten.
 * </p>
 * 
 * @goal settings-download
 * @requiresProject false
 * @aggregator
 */
// TODO: Remove aggregator annotation once we have a better solution, but we should only run this once per build.
public class DownloadSettingsTemplateMojo
    extends AbstractNexusMojo
{

    /**
     * The ID of the template to download. For instance, a templateId of 'sonatype' will download from
     * '${nexusUrl}/service/local/templates/settings/sonatype/content'.
     * 
     * @parameter expression="${settings.templateId}" default-value="default"
     */
    private String templateId;

    /**
     * The full URL of a settings template available from a particular Nexus Professional instance. If missing, the mojo
     * will prompt for this value.
     * 
     * @parameter expression="${url}"
     */
    private String url;

    /**
     * The standard destination where the downloaded settings.xml template should be saved. The <a
     * href="#target">target</a> parameter will override this value.
     * 
     * @parameter expression="${destination}" default-value="user"
     */
    private String destination;

    /**
     * If true and there is a pre-existing settings.xml file in the way of this download, backup the file to a
     * datestamped filename, where the specific format of the datestamp is given by the <a
     * href="#backupFormat">backupFormat</a> parameter.
     * 
     * @parameter expression="${doBackup}" default-value="true"
     */
    private boolean doBackup;

    /**
     * When backing up an existing settings.xml file, use this date format in conjunction with {@link SimpleDateFormat}
     * to construct a new filename of the form: <code>settings.xml.$(format)</code>. Datestamps are used for backup
     * copies of the settings.xml to avoid overwriting previously backed up settings files. This protects against the
     * case where the download mojo is used multiple times with incorrect settings, where using a single static
     * backup-file name would destroy the original, pre-existing settings.
     * 
     * @parameter expression="${backupFormat}" default-value="yyyyMMdd_HHmmss"
     */
    private String backupFormat;

    /**
     * If set, ignore the standard location given by the <a href="#destination">destination</a> parameter, and use this
     * file location to save the settings template instead. If this file exists, it will be backed up using the same
     * logic as the standard locations (using the <a href="#doBackup">doBackup</a> and <a
     * href="#backupFormat">backupFormat</a> parameters).
     * 
     * @parameter expression="${target}"
     */
    private File target;

    /**
     * Use this parameter to define a non-default encoding for the settings file.
     * 
     * @parameter expression="${encoding}"
     */
    private String encoding;

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

    /**
     * @parameter expression="${settings.defaults}" default-value="${user.home}/.m2/settings-defaults.properties"
     */
    private File settingsDefaults;

    private M2SettingsClient client;

    public void execute()
        throws MojoExecutionException
    {
        fillMissing();

        initLog4j();

        try
        {
            connect();
        }
        catch ( RESTLightClientException e )
        {
            throw new MojoExecutionException( "Failed to start REST client.", e );
        }

        Document settingsDoc = downloadSettings();

        save( settingsDoc );

    }

    private void save( final Document settingsDoc )
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
                        + ". Please specify either '" + SettingsDestination.global.toString() + "' or '"
                        + SettingsDestination.user.toString() + "'." );

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

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try
        {
            new XMLOutputter( Format.getPrettyFormat() ).output( settingsDoc, new OutputStreamWriter( bos ) );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Failed to interpolate settings. Reason: " + e.getMessage(), e );
        }

        final Properties p = new Properties();
        try
        {
            if ( settingsDefaults != null && settingsDefaults.exists() )
            {
                p.load( new FileInputStream( settingsDefaults ) );
            }
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Failed to load settings.defaults: " + settingsDefaults + ". Reason: "
                + e.getMessage(), e );
        }

        InterpolationFilterReader r =
            new InterpolationFilterReader( new InputStreamReader( new ByteArrayInputStream( bos.toByteArray() ) ),
                new SettingsMap( p ), "$[", "]" );

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

            IOUtil.copy( r, w );
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

    @Override
    protected synchronized M2SettingsClient connect()
        throws RESTLightClientException, MojoExecutionException
    {
        String url = formatUrl( getNexusUrl() );

        getLog().info( "Logging into Nexus: " + url );
        getLog().info( "User: " + getUsername() );

        client = new M2SettingsClient( url, getUsername(), getPassword() );
        return client;
    }

    @Override
    public String getNexusUrl()
    {
        String nexusUrl = super.getNexusUrl();
        if ( nexusUrl == null && getUrl() != null )
        {
            int svcIdx = getUrl().indexOf( "/service" );
            if ( svcIdx > -1 )
            {
                nexusUrl = getUrl().substring( 0, svcIdx );
            }

            setNexusUrl( nexusUrl );
        }

        return nexusUrl;
    }

    private Document downloadSettings()
        throws MojoExecutionException
    {
        if ( getUrl() != null )
        {
            try
            {
                getLog().info( "Retrieving Settings from:" + getUrl() );
                return client.getSettingsTemplateAbsolute( getUrl() );
            }
            catch ( RESTLightClientException e )
            {
                throw new MojoExecutionException( "Failed to retrieve Maven settings.xml from URL: " + getUrl()
                    + "\n(Reason: " + e.getMessage() + ")", e );
            }
        }
        else
        {
            try
            {
                getLog().info(
                    "Retrieving Settings from Template \"" + templateId + "\" on server: \"" + this.getNexusUrl()
                        + "\"" );
                return client.getSettingsTemplate( templateId );
            }
            catch ( RESTLightClientException e )
            {
                throw new MojoExecutionException( "Failed to retrieve Maven settings.xml from template: " + templateId
                    + "\n(Reason: " + e.getMessage() + ")", e );
            }
        }
    }

    @Override
    protected void fillMissing()
        throws MojoExecutionException
    {
        while ( templateId == null && ( getUrl() == null || getUrl().trim().length() < 1 ) )
        {
            try
            {
                setUrl( getPrompter().prompt( "Settings Template URL: " ) );
            }
            catch ( PrompterException e )
            {
                throw new MojoExecutionException( "Failed to read from CLI prompt: " + e.getMessage(), e );
            }
        }

        super.fillMissing();
    }

    public String getUrl()
    {
        return url;
    }

    public void setUrl( final String url )
    {
        this.url = url;
    }

    public String getDestination()
    {
        return destination;
    }

    public void setDestination( final String destination )
    {
        this.destination = destination;
    }

    public boolean isDoBackup()
    {
        return doBackup;
    }

    public void setDoBackup( final boolean doBackup )
    {
        this.doBackup = doBackup;
    }

    public String getBackupFormat()
    {
        return backupFormat;
    }

    public void setBackupFormat( final String backupFormat )
    {
        this.backupFormat = backupFormat;
    }

    public File getTarget()
    {
        return target;
    }

    public void setTarget( final File target )
    {
        this.target = target;
    }

    public String getEncoding()
    {
        return encoding;
    }

    public void setEncoding( final String encoding )
    {
        this.encoding = encoding;
    }

    public File getMavenHomeConf()
    {
        return mavenHomeConf;
    }

    public void setMavenHomeConf( final File mavenHomeConf )
    {
        this.mavenHomeConf = mavenHomeConf;
    }

    public File getMavenUserConf()
    {
        return mavenUserConf;
    }

    public void setMavenUserConf( final File mavenUserConf )
    {
        this.mavenUserConf = mavenUserConf;
    }

}
