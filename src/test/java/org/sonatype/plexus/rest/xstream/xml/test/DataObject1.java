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
