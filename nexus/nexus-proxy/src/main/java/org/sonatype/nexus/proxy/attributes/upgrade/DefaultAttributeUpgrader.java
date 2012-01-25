package org.sonatype.nexus.proxy.attributes.upgrade;

import java.io.File;
import java.io.IOException;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.logging.AbstractLoggingComponent;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.util.SystemPropertiesHelper;

/**
 * Component that handles upgrade of "legacy attribute storage". It does it by detecting it's presence, and firing the
 * rebuild attributes background task if needed. Finally, it leaves "marker" file to mark the fact upgrade did happen,
 * to not kick in on any subsequent reboot.
 * 
 * @since 1.10.0
 */
@Component( role = AttributeUpgrader.class )
public class DefaultAttributeUpgrader
    extends AbstractLoggingComponent
    implements AttributeUpgrader
{
    /**
     * The "switch" for performing upgrade (by bg thread), default is true (upgrade will happen).
     */
    private final boolean UPGRADE = SystemPropertiesHelper.getBoolean( getClass().getName() + ".upgrade", true );

    /**
     * The "switch" to enable "upgrade throttling" if needed (is critical to lessen IO bound problem with attributes).
     * Default is to 100 UPS to for use throttling. The "measure" is "UPS": item Upgrades Per Second. Reasoning is:
     * Central repository is currently 300k of artifacts, this would mean in "nexus world" 6x300k items (pom, jar,
     * maven-metadata and sha1/md5 hashes for those) if all of Central would be proxied by Nexus, which is not
     * plausible, so assume 50% of Central is present in cache (still is OVER-estimation!). Crawling 900k at 100 UPS
     * would take exactly 2.5 hour to upgrade it. Note: this is only the value used for unnattended upgrade! This is the
     * starting value, that is still possible to "tune" (increase, decrease) over JMX!
     */
    private final int UPGRADE_THROTTLE_UPS = SystemPropertiesHelper.getInteger( getClass().getName() + ".throttleUps",
        50 );

    @Requirement
    private ApplicationConfiguration applicationConfiguration;

    @Requirement
    private RepositoryRegistry repositoryRegistry;

    private volatile UpgraderThread upgraderThread;

    protected File getLegacyAttributesDirectory()
    {
        return applicationConfiguration.getWorkingDirectory( "proxy/attributes", false );
    }

    @Override
    public boolean isLegacyAttributesDirectoryPresent()
    {
        return getLegacyAttributesDirectory().isDirectory();
    }

    @Override
    public boolean isUpgradeNeeded()
    {
        return isLegacyAttributesDirectoryPresent() && !isUpgradeFinished();
    }

    @Override
    public boolean isUpgradeRunning()
    {
        return upgraderThread != null && upgraderThread.isAlive();
    }

    @Override
    public boolean isUpgradeFinished()
    {
        return isUpgradeDone( getLegacyAttributesDirectory(), null );
    }

    @Override
    public int getCurrentUps()
    {
        if ( isUpgradeRunning() )
        {
            return upgraderThread.getActualUps();
        }
        else
        {
            return -1;
        }
    }

    @Override
    public int getLimiterUps()
    {
        if ( isUpgradeRunning() )
        {
            return upgraderThread.getLimiterUps();
        }
        else
        {
            return -1;
        }
    }

    @Override
    public void setLimiterUps( int limit )
    {
        if ( isUpgradeRunning() )
        {
            upgraderThread.setLimiterUps( limit );
        }
    }

    @Override
    public synchronized void upgrade()
    {
        if ( !isLegacyAttributesDirectoryPresent() )
        {
            // file not found or not a directory, stay put to not create noise in logs (new or tidied up nexus
            // instance)
            getLogger().debug( "Legacy attribute directory not present, no need for attribute upgrade." );
        }
        else
        {
            if ( isUpgradeDone( getLegacyAttributesDirectory(), null ) )
            {
                // nag the user to remove the directory
                getLogger().info(
                    "Legacy attribute directory present, but is marked already as upgraded. Please delete, move or rename the \""
                        + getLegacyAttributesDirectory().getAbsolutePath() + "\" folder." );
            }
            else
            {
                if ( UPGRADE )
                {
                    getLogger().info(
                        "Legacy attribute directory present, and upgrade is needed. Starting background upgrade." );
                    this.upgraderThread =
                        new UpgraderThread( getLegacyAttributesDirectory(), repositoryRegistry, UPGRADE_THROTTLE_UPS );
                    this.upgraderThread.start();
                }
                else
                {
                    // nag the user about explicit no-upgrade switch
                    getLogger().info(
                        "Legacy attribute directory present, but upgrade prevented by system property. Not upgrading it." );
                }
            }
        }
    }

    // ==

    private static final String MARKER_FILENAME = "README.txt";

    private static final String MARKER_TEXT =
        "Migration of legacy attributes finished.\nPlease delete, remove or rename this directory!";

    protected static boolean isUpgradeDone( final File attributesDirectory, final String repoId )
    {
        try
        {
            if ( StringUtils.isBlank( repoId ) )
            {
                return StringUtils.equals( MARKER_TEXT,
                    FileUtils.fileRead( new File( attributesDirectory, MARKER_FILENAME ) ) );
            }
            else
            {
                return StringUtils.equals( MARKER_TEXT,
                    FileUtils.fileRead( new File( new File( attributesDirectory, repoId ), MARKER_FILENAME ) ) );
            }
        }
        catch ( IOException e )
        {
            return false;
        }
    }

    protected static void markUpgradeDone( final File attributesDirectory, final String repoId )
    {
        try
        {
            if ( StringUtils.isBlank( repoId ) )
            {
                FileUtils.fileWrite( new File( attributesDirectory, MARKER_FILENAME ), MARKER_TEXT );
            }
            else
            {
                final File target = new File( new File( attributesDirectory, repoId ), MARKER_FILENAME );
                // this step is needed if new repo added while upgrade not done: it will NOT have legacy attributes
                // as other reposes, that were present in old/upgraded instance
                target.getParentFile().mkdirs();
                FileUtils.fileWrite( target, MARKER_TEXT );
            }
        }
        catch ( IOException e )
        {
            // hum?
        }
    }
}
