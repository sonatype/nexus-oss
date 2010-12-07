package org.sonatype.nexus.proxy.repository.validator;

import org.sonatype.nexus.proxy.item.StorageItem;

/**
 * A component concentrating all the responses of existing FileTypeValidators and checking that is has not one INVALID
 * response and at least one VALID response.
 * 
 * @author cstamas
 */
public interface FileTypeValidatorHub
{
    boolean isExpectedFileType( StorageItem item );
}
