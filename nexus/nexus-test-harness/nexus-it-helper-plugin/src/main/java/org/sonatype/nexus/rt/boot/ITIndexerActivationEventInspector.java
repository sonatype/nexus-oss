package org.sonatype.nexus.rt.boot;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.plugins.events.PluginActivatedEvent;
import org.sonatype.nexus.proxy.events.EventInspector;
import org.sonatype.plexus.appevents.Event;

/**
 * The sole purpose of this EventInspector is to turn of "blocking commits" (equivalent of "old" behavior) on Indexer,
 * on Maven Indexer more precisely, since async nature of it borks ITs. This event inspector just puts Indexer into
 * "sync" mode (default is async).
 * 
 * @author cstamas
 */
@Component( role = EventInspector.class, hint = "ITIndexerActivationEventInspector" )
public class ITIndexerActivationEventInspector
    implements EventInspector
{
    @Override
    public boolean accepts( Event<?> evt )
    {
        if ( evt instanceof PluginActivatedEvent )
        {
            PluginActivatedEvent pa = (PluginActivatedEvent) evt;

            return "nexus-indexer-lucene-plugin".equals( pa.getPluginDescriptor().getPluginCoordinates().getArtifactId() );
        }

        return false;
    }

    @Override
    public void inspect( Event<?> evt )
    {
        // Note: in ITs we want to make Indexer perform blocking commits.
        // Since MavenIndexer 4.0, it performs async blocking commits by default, meaning that no "helper" from Nexus
        // is able to tell and potentially block (see EventInspectorsUtil#waitForCalmPeriod() as example) execution
        // up to the moment when indexing operation is actually done.
        // By having this switch, we are switching Nexus indexer back into "blocking" mode as it was before 4.0.
        // The proper fix is to make all Indexer related ITs behave "properly", and have some sort of
        // "try-wait-try-failAfterSomeRetries" the search operation itself.
        System.setProperty( "mavenIndexerBlockingCommits", Boolean.TRUE.toString() );
    }
}
