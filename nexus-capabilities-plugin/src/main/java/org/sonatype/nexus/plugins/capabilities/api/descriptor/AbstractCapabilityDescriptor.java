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
package org.sonatype.nexus.plugins.capabilities.api.descriptor;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.sonatype.nexus.formfields.FormField;

/**
 * Support class for implementing {@link CapabilityDescriptor}s.
 *
 * @since 1.10.0
 */
public abstract class AbstractCapabilityDescriptor
    implements CapabilityDescriptor
{

    private final String id;

    private final String name;

    private final List<FormField> formFields;

    protected AbstractCapabilityDescriptor( final String id,
                                            final String name,
                                            final FormField... formFields )
    {
        this.id = id;
        this.name = name;
        if ( formFields == null )
        {
            this.formFields = Collections.emptyList();
        }
        else
        {
            this.formFields = Arrays.asList( formFields );
        }
    }

    @Override
    public String id()
    {
        return id;
    }

    @Override
    public String name()
    {
        return name;
    }

    @Override
    public List<FormField> formFields()
    {
        return formFields;
    }

    /**
     * Always exposed.
     *
     * @return true
     */
    @Override
    public boolean isExposed()
    {
        return true;
    }

    /**
     * Describes the capability via all its properties.
     */
    @Override
    public String describe( final Map<String, String> properties )
    {
        return properties == null ? null : properties.toString();
    }

}
