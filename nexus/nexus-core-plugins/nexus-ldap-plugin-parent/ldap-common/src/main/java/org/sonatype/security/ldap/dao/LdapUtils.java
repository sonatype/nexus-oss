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
package org.sonatype.security.ldap.dao;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;

public final class LdapUtils
{

    private LdapUtils()
    {
    }

    public static String getLabeledUriValue( Attributes attributes, String attrName, String label,
        String attributeDescription )
        throws LdapDAOException
    {
        Attribute attribute = attributes.get( attrName );
        if ( attribute != null )
        {
            NamingEnumeration attrs;
            try
            {
                attrs = attribute.getAll();
            }
            catch ( NamingException e )
            {
                throw new LdapDAOException( "Failed to retrieve " + attributeDescription + " (attribute: \'"
                    + attrName + "\').", e );
            }

            while ( attrs.hasMoreElements() )
            {
                Object value = attrs.nextElement();

                String val = String.valueOf( value );

                if ( val.endsWith( " " + label ) )
                {
                    return val.substring( 0, val.length() - ( label.length() + 1 ) );
                }
            }
        }

        return null;
    }

    public static String getAttributeValue( Attributes attributes, String attrName, String attributeDescription )
        throws LdapDAOException
    {
        Attribute attribute = attributes.get( attrName );
        if ( attribute != null )
        {
            try
            {
                Object value = attribute.get();

                return String.valueOf( value );
            }
            catch ( NamingException e )
            {
                throw new LdapDAOException( "Failed to retrieve " + attributeDescription + " (attribute: \'"
                    + attrName + "\').", e );
            }
        }

        return null;
    }
    
    public static Set<String> getAttributeValues( Attributes attributes, String attrName, String attributeDescription )
    throws LdapDAOException
    {
        Set<String> results = new HashSet<String>();
        Attribute attribute = attributes.get( attrName );
        if ( attribute != null )
        {
            try
            {   
                for ( Enumeration<?> values = attribute.getAll(); values.hasMoreElements(); )
                {
                    results.add( String.valueOf( values.nextElement() ) );
                }
            }
            catch ( NamingException e )
            {
                throw new LdapDAOException( "Failed to retrieve " + attributeDescription + " (attribute: \'"
                    + attrName + "\').", e );
            }
        }
    
        return results;
    }

    public static String getAttributeValueFromByteArray( Attributes attributes, String attrName,
        String attributeDescription )
        throws LdapDAOException
    {
        if( attrName != null)
        {
            Attribute attribute = attributes.get( attrName );
            if ( attribute != null )
            {
                try
                {
                    byte[] value = (byte[]) attribute.get();
    
                    return new String( value );
                }
                catch ( NamingException e )
                {
                    throw new LdapDAOException( "Failed to retrieve " + attributeDescription + " (attribute: \'"
                        + attrName + "\').", e );
                }
            }
        }

        return null;
    }
}
