package org.sonatype.nexus.gwt.client.services;

import org.sonatype.gwt.client.handler.EntityResponseHandler;
import org.sonatype.gwt.client.resource.Variant;

/**
 * Nexus Feeds service.
 * 
 * @author cstamas
 */
public interface FeedsService
{
    /**
     * List available feeds.
     * 
     * @param handler
     */
    void listFeeds( EntityResponseHandler handler );

    /**
     * Gets the feed in Nexus default variant.
     * 
     * @param path
     * @param handler
     */
    void readFeed( String path, EntityResponseHandler handler );

    /**
     * Gets the feed in custom variant. Feeds probably have no JSON representation but RSS or ATOM.
     * 
     * @param path
     * @param variant
     * @param handler
     */
    void readFeed( String path, Variant variant, EntityResponseHandler handler );
}
