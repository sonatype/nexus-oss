package org.sonatype.nexus.plugins.lvo;

import java.io.IOException;

import org.sonatype.nexus.proxy.NoSuchRepositoryException;

/**
 * The LVO Plugin interface.
 * 
 * @author cstamas
 */
public interface LvoPlugin
{
    /**
     * Returns the latest V (V from GAV) that fits the properties specified by key.
     * 
     * @param key
     * @return the V, null if key exists but we are unable to calculate LV.
     * @throws NoSuchKeyException
     * @throws NoSuchStrategyException
     * @throws NoSuchRepositoryException
     * @throws IOException
     */
    DiscoveryResponse getLatestVersionForKey( String key )
        throws NoSuchKeyException,
            NoSuchStrategyException,
            NoSuchRepositoryException,
            IOException;

    /**
     * Queries for the latest V (V from GAV) that fits the properties specified by key. If the passed in v is equal of
     * the latest v, returns null.
     * 
     * @param key
     * @param v current version associated with key.
     * @return the V if newer found, null if key exists but we are unable to calculate LV or no newer version exists.
     * @throws NoSuchKeyException
     * @throws NoSuchStrategyException
     * @throws NoSuchRepositoryException
     * @throws IOException
     */
    DiscoveryResponse queryLatestVersionForKey( String key, String v )
        throws NoSuchKeyException,
            NoSuchStrategyException,
            NoSuchRepositoryException,
            IOException;
}
