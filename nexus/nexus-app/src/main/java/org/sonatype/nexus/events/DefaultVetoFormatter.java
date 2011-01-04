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
