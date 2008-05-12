package org.sonatype.nexus.rest.schedules;

import org.sonatype.nexus.rest.model.ScheduledServiceAdvancedResource;
import org.sonatype.nexus.rest.model.ScheduledServiceBaseResource;
import org.sonatype.nexus.rest.model.ScheduledServiceDailyResource;
import org.sonatype.nexus.rest.model.ScheduledServiceMonthlyResource;
import org.sonatype.nexus.rest.model.ScheduledServiceWeeklyResource;
import org.sonatype.plexus.rest.xstream.json.LookAheadStreamReader;

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
            String serviceSchedule = null;

            if ( LookAheadStreamReader.class.isAssignableFrom( reader.getClass() ) )
            {
                serviceSchedule = ( (LookAheadStreamReader) reader ).getFieldValue( "serviceSchedule" );
            }
            else
            {
                serviceSchedule = ( (LookAheadStreamReader) reader.underlyingReader() ).getFieldValue( "serviceSchedule" );
            }

            if ( serviceSchedule == null )
            {
                return super.instantiateNewInstance( reader, context );
            }
            else if ( AbstractScheduledServiceResourceHandler.SCHEDULE_TYPE_DAILY.equals( serviceSchedule ))
            {
                return new ScheduledServiceDailyResource();
            }
            else if ( AbstractScheduledServiceResourceHandler.SCHEDULE_TYPE_WEEKLY.equals( serviceSchedule ))
            {
                return new ScheduledServiceWeeklyResource();
            }
            else if ( AbstractScheduledServiceResourceHandler.SCHEDULE_TYPE_MONTHLY.equals( serviceSchedule ))
            {
                return new ScheduledServiceMonthlyResource();
            }
            else if ( AbstractScheduledServiceResourceHandler.SCHEDULE_TYPE_ADVANCED.equals( serviceSchedule ))
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
