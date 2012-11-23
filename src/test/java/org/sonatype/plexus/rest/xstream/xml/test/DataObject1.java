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
package org.sonatype.plexus.rest.xstream.xml.test;

import java.util.ArrayList;
import java.util.List;


public class DataObject1 extends BaseDataObject
{

    String dataObjectField1;

    String dataObjectField2;
    
    List dataList = new ArrayList();

    public String getDataObjectField1()
    {
        return dataObjectField1;
    }

    public void setDataObjectField1( String dataObjectField1 )
    {
        this.dataObjectField1 = dataObjectField1;
    }

    public String getDataObjectField2()
    {
        return dataObjectField2;
    }

    public void setDataObjectField2( String dataObjectField2 )
    {
        this.dataObjectField2 = dataObjectField2;
    }

    public List getDataList()
    {
        return dataList;
    }

    public void setDataList( List dataList )
    {
        this.dataList = dataList;
    }


    
}
