/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package com.sonatype.nexus.plugin.groovyconsole.rest.dto;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias( "groovyScript" )
public class GroovyScriptDTO
{

    private String name;

    private String script;

    public GroovyScriptDTO()
    {
        super();
    }

    public GroovyScriptDTO( String name, String script )
    {
        this();
        this.name = name;
        this.script = script;
    }

    public String getScript()
    {
        return script;
    }

    public String getName()
    {
        return name;
    }

    public void setScript( String script )
    {
        this.script = script;
    }

    public void setName( String name )
    {
        this.name = name;
    }
}
