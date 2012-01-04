/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions
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
package org.sonatype.nexus.plugins.capabilities.internal.validator;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;

import org.sonatype.nexus.plugins.capabilities.CapabilityDescriptorRegistry;
import org.sonatype.nexus.plugins.capabilities.CapabilityType;
import org.sonatype.nexus.plugins.capabilities.ValidationResult;
import org.sonatype.nexus.plugins.capabilities.Validator;
import org.sonatype.nexus.plugins.capabilities.support.validator.DefaultValidationResult;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.Repository;
import com.google.inject.assistedinject.Assisted;

/**
 * A {@link Validator} that ensures that capability repository property references a repository of specified kind(s).
 *
 * @since 2.0
 */
@Named
public class RepositoryTypeValidator
    extends ValidatorSupport
    implements Validator
{

    private final RepositoryRegistry repositoryRegistry;

    private final String propertyKey;

    private final Class<?> facet;

    @Inject
    RepositoryTypeValidator( final RepositoryRegistry repositoryRegistry,
                             final CapabilityDescriptorRegistry capabilityDescriptorRegistry,
                             final @Assisted CapabilityType type,
                             final @Assisted String propertyKey,
                             final @Assisted Class<?> facet )
    {
        super( capabilityDescriptorRegistry, type );
        this.repositoryRegistry = checkNotNull( repositoryRegistry );
        this.propertyKey = checkNotNull( propertyKey );
        this.facet = checkNotNull( facet );
    }

    @Override
    public ValidationResult validate( final Map<String, String> properties )
    {
        String repositoryId = fixRepositoryValue( properties.get( propertyKey ) );
        if ( repositoryId != null )
        {
            try
            {
                final Repository repository = repositoryRegistry.getRepository( repositoryId );
                if ( !repository.getRepositoryKind().isFacetAvailable( facet ) )
                {
                    return new DefaultValidationResult().add( buildMessage( repository ) );
                }
            }
            catch ( NoSuchRepositoryException ignore )
            {
                // ignore
            }
        }
        return ValidationResult.VALID;
    }

    @Override
    public String explainValid()
    {
        final StringBuilder message = new StringBuilder();
        message.append( propertyName( propertyKey ) ).append( " is a " ).append( facetName() ).append( " repository" );
        return message.toString();
    }

    @Override
    public String explainInvalid()
    {
        final StringBuilder message = new StringBuilder();
        message.append( propertyName( propertyKey ) ).append( " is not a " ).append( facetName() )
            .append( " repository" );
        return message.toString();

    }

    private String buildMessage( final Repository repository )
    {
        final StringBuilder message = new StringBuilder();
        message.append( "Selected " ).append( propertyName( propertyKey ).toLowerCase() )
            .append( " '" ).append( repository.getName() )
            .append( "' must be a " ).append( facetName() ).append( " repository" );
        return message.toString();
    }

    private Object facetName()
    {
        return facet.getSimpleName().toLowerCase().replace( "repository", "" );
    }

}
