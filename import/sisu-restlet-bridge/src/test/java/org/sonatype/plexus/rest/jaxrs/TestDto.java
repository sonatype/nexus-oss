/*
 * Copyright (c) 2007-2012 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.plexus.rest.jaxrs;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TestDto
{
    private String aString;

    private Date aDate;

    private List<String> aStringList;

    private List<TestDto> children;

    public String getAString()
    {
        return aString;
    }

    public void setAString( String string )
    {
        aString = string;
    }

    public Date getADate()
    {
        return aDate;
    }

    public void setADate( Date date )
    {
        aDate = date;
    }

    public List<String> getAStringList()
    {
        if ( aStringList == null )
        {
            aStringList = new ArrayList<String>();
        }

        return aStringList;
    }

    public void setAStringList( List<String> stringList )
    {
        aStringList = stringList;
    }

    public List<TestDto> getChildren()
    {
        if ( children == null )
        {
            children = new ArrayList<TestDto>();
        }

        return children;
    }

    public void setChildren( List<TestDto> children )
    {
        this.children = children;
    }
}
