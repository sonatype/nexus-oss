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
package org.sonatype.plexus.rest.resource.error;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import java.util.Date;

/**
 * An item describing the error.
 * 
 * @version $Revision$ $Date$
 */
public class ErrorMessage implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field id.
     */
    private String id;

    /**
     * Field msg.
     */
    private String msg;


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Get the id field.
     * 
     * @return String
     */
    public String getId()
    {
        return this.id;
    } //-- String getId() 

    /**
     * Get the msg field.
     * 
     * @return String
     */
    public String getMsg()
    {
        return this.msg;
    } //-- String getMsg() 

    /**
     * Set the id field.
     * 
     * @param id
     */
    public void setId(String id)
    {
        this.id = id;
    } //-- void setId(String) 

    /**
     * Set the msg field.
     * 
     * @param msg
     */
    public void setMsg(String msg)
    {
        this.msg = msg;
    } //-- void setMsg(String) 


//    private String modelEncoding = "UTF-8";
//
//    /**
//     * Set an encoding used for reading/writing the model.
//     *
//     * @param modelEncoding the encoding used when reading/writing the model.
//     */
//    public void setModelEncoding( String modelEncoding )
//    {
//        this.modelEncoding = modelEncoding;
//    }
//
//    /**
//     * @return the current encoding used when reading/writing this model.
//     */
//    public String getModelEncoding()
//    {
//        return modelEncoding;
//    }
}
