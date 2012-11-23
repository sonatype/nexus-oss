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
package org.sonatype.plexus.rest;

import junit.framework.Assert;

import org.codehaus.plexus.PlexusTestCase;
import org.sonatype.plexus.rest.dto.Bbb;
import org.sonatype.plexus.rest.dto.One;
import org.sonatype.plexus.rest.dto.Two;
import org.sonatype.plexus.rest.xstream.HtmlEscapeStringConverter;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.basic.StringConverter;

public class HtmlEscapeTest
extends PlexusTestCase
{
    public void testEscape()
    {
        XStream xstream = new XStream();
        xstream.registerConverter( new HtmlEscapeStringConverter() );
        
        Bbb bbb = new Bbb();
        bbb.setaValue( "aaa-value" );
        bbb.setbValue( "bbb-value" );
        
        Two twoObject = new Two();
        twoObject.setOneValue( "one-value" );
        twoObject.setTwoValue( "interesting image: <img src=\"http://something.com/\" />" );
        twoObject.setBbb( bbb );
        
        String stringedTwo = xstream.toXML( twoObject );
        Two copyOfTwo = (Two) xstream.fromXML( stringedTwo );
        
        Assert.assertEquals(  "interesting image: &lt;img src=&quot;http://something.com/&quot; /&gt;", copyOfTwo.getTwoValue() );
    }
    
    public void testFieldThatIsNotEscaped()
    {
        XStream xstream = new XStream();
        xstream.registerConverter( new HtmlEscapeStringConverter() );
        xstream.registerLocalConverter( One.class, "oneValue", new StringConverter() );
        
        // now make one field allow html characters
        Bbb bbb = new Bbb();
        bbb.setaValue( "aaa-value" );
        bbb.setbValue( "bbb-value" );
        
        Two twoObject = new Two();
        twoObject.setOneValue( "allow html: <img src=\"http://something.com/\" />" );
        twoObject.setTwoValue( "interesting image: <img src=\"http://something.com/\" />" );
        twoObject.setBbb( bbb );

        String stringedTwo = xstream.toXML( twoObject );
        Two copyOfTwo = (Two) xstream.fromXML( stringedTwo );
        
        Assert.assertEquals(  "interesting image: &lt;img src=&quot;http://something.com/&quot; /&gt;", copyOfTwo.getTwoValue() );
        Assert.assertEquals(  "allow html: <img src=\"http://something.com/\" />", copyOfTwo.getOneValue() );
        
    }

}
