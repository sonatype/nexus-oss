/**
 * Sonatype Nexus™ [Open Source Version].
 * Copyright © 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdpartyurl}.
 *
 * This program is licensed to you under Version 3 only of the GNU General
 * Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.rest.schedules;

import org.sonatype.nexus.rest.model.ScheduledServiceAdvancedResource;
import org.sonatype.nexus.rest.model.ScheduledServiceBaseResource;
import org.sonatype.nexus.rest.model.ScheduledServiceDailyResource;
import org.sonatype.nexus.rest.model.ScheduledServiceHourlyResource;
import org.sonatype.nexus.rest.model.ScheduledServiceMonthlyResource;
import org.sonatype.nexus.rest.model.ScheduledServiceOnceResource;
import org.sonatype.nexus.rest.model.ScheduledServiceWeeklyResource;
import org.sonatype.plexus.rest.xstream.LookAheadStreamReader;

import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.reflection.AbstractReflectionConverter;
import com.thoughtworks.xstream.converters.reflection.ReflectionProvider;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.mapper.Mapper;

public class ScheduledServiceBaseResourceConverter
    extends AbstractReflectionConverter
{
    public ScheduledServiceBaseResourceConverter( Mapper mapper, ReflectionProvider reflectionProvider )
    {
        super( mapper, reflectionProvider );
    }

    public boolean canConvert( Class type )
    {
        return ScheduledServiceBaseResource.class.equals( type );
    }
    
    protected Object instantiateNewInstance( HierarchicalStreamReader reader, UnmarshallingContext context )
    {
        if ( LookAheadStreamReader.class.isAssignableFrom( reader.getClass() )
                        || LookAheadStreamReader.class.isAssignableFrom( reader.underlyingReader().getClass() ) )
        {
            String schedule = null;

            if ( LookAheadStreamReader.class.isAssignableFrom( reader.getClass() ) )
            {
                schedule = ( (LookAheadStreamReader) reader ).getFieldValue( "schedule" );
            }
            else
            {
                schedule = ( (LookAheadStreamReader) reader.underlyingReader() ).getFieldValue( "schedule" );
            }

            if ( schedule == null )
            {
                return super.instantiateNewInstance( reader, context );
            }
            else if ( AbstractScheduledServicePlexusResource.SCHEDULE_TYPE_MANUAL.equals( schedule ))
            {
                return new ScheduledServiceBaseResource();
            }
            else if ( AbstractScheduledServicePlexusResource.SCHEDULE_TYPE_ONCE.equals( schedule ))
            {
                return new ScheduledServiceOnceResource();
            }
            else if ( AbstractScheduledServicePlexusResource.SCHEDULE_TYPE_HOURLY.equals( schedule ))
            {
                return new ScheduledServiceHourlyResource();
            }
            else if ( AbstractScheduledServicePlexusResource.SCHEDULE_TYPE_DAILY.equals( schedule ))
            {
                return new ScheduledServiceDailyResource();
            }
            else if ( AbstractScheduledServicePlexusResource.SCHEDULE_TYPE_WEEKLY.equals( schedule ))
            {
                return new ScheduledServiceWeeklyResource();
            }
            else if ( AbstractScheduledServicePlexusResource.SCHEDULE_TYPE_MONTHLY.equals( schedule ))
            {
                return new ScheduledServiceMonthlyResource();
            }
            else if ( AbstractScheduledServicePlexusResource.SCHEDULE_TYPE_ADVANCED.equals( schedule ))
            {
                return new ScheduledServiceAdvancedResource();
            }
            else
            {
                return new ScheduledServiceBaseResource();
            }
        }
        else
        {
            return super.instantiateNewInstance( reader, context );
        }
    }
}
