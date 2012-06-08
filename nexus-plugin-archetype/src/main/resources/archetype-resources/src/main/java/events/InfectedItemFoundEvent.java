#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.events;

import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.plexus.appevents.AbstractEvent;

public class InfectedItemFoundEvent
    extends AbstractEvent<Repository>
{
    private final StorageFileItem file;

    public InfectedItemFoundEvent( Repository component, StorageFileItem file )
    {
        super( component );

        this.file = file;
    }

    public StorageFileItem getInfectedFile()
    {
        return file;
    }
}
