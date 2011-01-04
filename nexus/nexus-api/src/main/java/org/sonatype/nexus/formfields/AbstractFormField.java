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
package org.sonatype.nexus.formfields;

public abstract class AbstractFormField
    implements FormField
{
    private String helpText;
    private String id;
    private String regexValidation;
    private boolean required;
    private String label;
    
    public AbstractFormField( String id, String label, String helpText, boolean required, String regexValidation )
    {
        this( id, label, helpText, required );
        this.regexValidation = regexValidation;
    }
    
    public AbstractFormField( String id, String label, String helpText, boolean required )
    {
        this( id );
        this.label = label;
        this.helpText = helpText;
        this.required = required;
    }
    
    public AbstractFormField( String id )
    {
        this.id = id;
    }
    
    public String getLabel()
    {
        return this.label;
    }
    public String getHelpText()
    {
        return this.helpText;
    }
    public String getId()
    {
        return this.id;
    }
    public String getRegexValidation()
    {
        return this.regexValidation;
    }
    public boolean isRequired()
    {
        return this.required;
    }
    public void setHelpText( String helpText )
    {
        this.helpText = helpText;
    }
    public void setId( String id )
    {
        this.id = id;
    }
    public void setRegexValidation( String regex )
    {
        this.regexValidation = regex;
    }
    public void setRequired( boolean required )
    {
        this.required = required;
    }
    public void setLabel( String label )
    {
        this.label = label;
    }
}
