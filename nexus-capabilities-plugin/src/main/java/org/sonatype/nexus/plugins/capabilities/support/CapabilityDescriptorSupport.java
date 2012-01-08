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
package org.sonatype.nexus.plugins.capabilities.support;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.sonatype.nexus.formfields.FormField;
import org.sonatype.nexus.plugins.capabilities.CapabilityDescriptor;
import org.sonatype.nexus.plugins.capabilities.CapabilityIdentity;
import org.sonatype.nexus.plugins.capabilities.CapabilityType;
import org.sonatype.nexus.plugins.capabilities.Validator;

/**
 * Support class for implementing {@link org.sonatype.nexus.plugins.capabilities.CapabilityDescriptor}s.
 *
 * @since 2.0
 */
public abstract class CapabilityDescriptorSupport
    implements CapabilityDescriptor
{

    static final Validator NO_VALIDATOR = null;

    private final CapabilityType type;

    private final String name;

    private final String about;

    private final List<FormField> formFields;

    protected CapabilityDescriptorSupport( final CapabilityType type,
                                           final String name,
                                           final String about,
                                           final FormField... formFields )
    {
        this.type = type;
        this.name = name;
        this.about = about;
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
    public CapabilityType type()
    {
        return type;
    }

    @Override
    public String name()
    {
        return name;
    }

    @Override
    public String about()
    {
        return about;
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
     * Not hidden.
     *
     * @return false
     */
    @Override
    public boolean isHidden()
    {
        return false;
    }

    /**
     * No validator.
     *
     * @return null
     */
    @Override
    public Validator validator()
    {
        return NO_VALIDATOR;
    }

    /**
     * No validator.
     *
     * @return null
     */
    @Override
    public Validator validator( final CapabilityIdentity id )
    {
        return NO_VALIDATOR;
    }

    /**
     * Return 1.
     *
     * @return 1
     */
    @Override
    public int version()
    {
        return 1;
    }

    /**
     * No conversion.
     *
     * @param properties  to be converted
     * @param fromVersion version of capability properties to be converted
     * @return same properties as passed in
     */
    @Override
    public Map<String, String> convert( final Map<String, String> properties, final int fromVersion )
    {
        return properties;
    }

}
