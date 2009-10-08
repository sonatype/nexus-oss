package org.sonatype.nexus.events;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.util.ExceptionUtils;
import org.sonatype.nexus.proxy.events.Veto;
import org.sonatype.nexus.proxy.events.VetoFormatter;
import org.sonatype.nexus.proxy.events.VetoFormatterRequest;
import org.sonatype.nexus.util.StringDigester;

@Component( role = VetoFormatter.class )
public class DefaultVetoFormatter
    implements VetoFormatter
{
    public String format( VetoFormatterRequest request )
    {
        StringBuffer sb = new StringBuffer();
        
        if ( request != null
            && request.getEvent() != null 
            && request.getEvent().isVetoed() )
        {
            sb.append( "Event " + request.getEvent().toString() + " has been vetoed by one or more components." );
            
            if ( request.isDetailed() )
            {
                sb.append( StringDigester.LINE_SEPERATOR );
                
                for ( Veto veto : request.getEvent().getVetos() )
                {
                    sb.append( "vetoer: " + veto.getVetoer().toString() );
                    sb.append( "cause:" );
                    sb.append( StringDigester.LINE_SEPERATOR );
                    sb.append( ExceptionUtils.getFullStackTrace( veto.getReason() ) );
                    sb.append( StringDigester.LINE_SEPERATOR );
                }
            }
        }
        
        return sb.toString();
    }
}
