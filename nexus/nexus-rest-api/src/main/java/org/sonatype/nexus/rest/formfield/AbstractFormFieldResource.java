package org.sonatype.nexus.rest.formfield;

import java.util.ArrayList;
import java.util.List;

import org.sonatype.nexus.formfields.FormField;
import org.sonatype.nexus.rest.AbstractNexusPlexusResource;
import org.sonatype.nexus.rest.model.FormFieldResource;

public abstract class AbstractFormFieldResource
    extends AbstractNexusPlexusResource
{
    protected List<? extends FormFieldResource> formFieldToDTO( List<FormField> fields, Class<? extends FormFieldResource> clazz )
    {
        List<FormFieldResource> dtoList = new ArrayList<FormFieldResource>();
        
        for ( FormField field : fields )
        {
            try
            {
                FormFieldResource dto = clazz.newInstance();
                dto.setHelpText( field.getHelpText() );
                dto.setId( field.getId() );
                dto.setLabel( field.getLabel() );
                dto.setRegexValidation( field.getRegexValidation() );
                dto.setRequired( field.isRequired() );
                dto.setType( field.getType() );
                
                dtoList.add( dto );
            }
            catch ( InstantiationException e )
            {
                getLogger().error( "Unable to properly translate DTO", e );
            }
            catch ( IllegalAccessException e )
            {
                getLogger().error( "Unable to properly translate DTO", e );
            }
        }
        
        return dtoList;
    }
}
