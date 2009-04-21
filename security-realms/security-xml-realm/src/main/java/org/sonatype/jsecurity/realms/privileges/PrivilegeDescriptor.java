package org.sonatype.jsecurity.realms.privileges;

import java.util.List;

import org.sonatype.jsecurity.model.CPrivilege;
import org.sonatype.jsecurity.realms.validator.ValidationContext;
import org.sonatype.jsecurity.realms.validator.ValidationResponse;

public interface PrivilegeDescriptor
{
    String getType();
    String getName();
    List<PrivilegePropertyDescriptor> getPropertyDescriptors();
    String buildPermission( CPrivilege privilege );
    ValidationResponse validatePrivilege( CPrivilege privilege, ValidationContext ctx, boolean update );
}
