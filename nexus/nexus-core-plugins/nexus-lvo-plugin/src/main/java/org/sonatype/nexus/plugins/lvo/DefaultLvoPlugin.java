package org.sonatype.nexus.plugins.lvo;

import java.io.IOException;
import java.util.Map;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.index.ArtifactInfo;
import org.sonatype.nexus.plugins.lvo.config.LvoPluginConfiguration;
import org.sonatype.nexus.plugins.lvo.config.model.CLvoKey;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;

@Component( role = LvoPlugin.class )
public class DefaultLvoPlugin
    extends AbstractLogEnabled
    implements LvoPlugin
{
    @Requirement
    private LvoPluginConfiguration lvoPluginConfiguration;

    @Requirement( role = DiscoveryStrategy.class )
    private Map<String, DiscoveryStrategy> strategies;

    public DiscoveryResponse getLatestVersionForKey( String key )
        throws NoSuchKeyException,
            NoSuchStrategyException,
            NoSuchRepositoryException,
            IOException
    {
        CLvoKey info = lvoPluginConfiguration.getLvoKey( key );

        String strategyId = info.getStrategy();

        if ( StringUtils.isEmpty( strategyId ) )
        {
            // default it
            strategyId = "index";
        }

        if ( strategies.containsKey( strategyId ) )
        {
            DiscoveryStrategy strategy = strategies.get( strategyId );

            DiscoveryRequest req = new DiscoveryRequest( key, info );

            return strategy.discoverLatestVersion( req );
        }
        else
        {
            throw new NoSuchStrategyException( info.getStrategy() );
        }

    }

    public DiscoveryResponse queryLatestVersionForKey( String key, String v )
        throws NoSuchKeyException,
            NoSuchStrategyException,
            NoSuchRepositoryException,
            IOException
    {
        DiscoveryResponse lv = getLatestVersionForKey( key );

        if ( !lv.isSuccessful() )
        {
            // nothing to compare to
            return lv;
        }

        // compare the two versions

        ArtifactInfo ca = new ArtifactInfo();
        ca.groupId = "dummy";
        ca.artifactId = "dummy";
        ca.version = "[" + v + "]";

        ArtifactInfo la = new ArtifactInfo();
        la.groupId = "dummy";
        la.artifactId = "dummy";
        la.version = "[" + lv.getVersion() + "]";

        if ( ArtifactInfo.VERSION_COMPARATOR.compare( la, ca ) >= 0 )
        {
            lv.getResponse().clear();

            lv.setSuccessful( true );
        }

        return lv;
    }
}
