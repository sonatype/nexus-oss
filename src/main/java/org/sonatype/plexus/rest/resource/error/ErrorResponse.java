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
 * Class ErrorResponse.
 * 
 * @version $Revision$ $Date$
 */
public class ErrorResponse
implements java.io.Serializable
{


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field errors.
     */
    private java.util.List errors;


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Method addError.
     * 
     * @param errorMessage
     */
    public void addError(ErrorMessage errorMessage)
    {
        if ( !(errorMessage instanceof ErrorMessage) )
        {
            throw new ClassCastException( "ErrorResponse.addErrors(ErrorResponse) parameter must be instanceof " + ErrorMessage.class.getName() );
        }
        getErrors().add( errorMessage );
    } //-- void addError(ErrorMessage) 

    /**
     * Method getErrors.
     * 
     * @return java.util.List
     */
    public java.util.List getErrors()
    {
        if ( this.errors == null )
        {
            this.errors = new java.util.ArrayList();
        }
        
        return this.errors;
    } //-- java.util.List getErrors() 

    /**
     * Method removeError.
     * 
     * @param errorMessage
     */
    public void removeError(ErrorMessage errorMessage)
    {
        if ( !(errorMessage instanceof ErrorMessage) )
        {
            throw new ClassCastException( "ErrorResponse.removeErrors(errorMessage) parameter must be instanceof " + ErrorMessage.class.getName() );
        }
        getErrors().remove( errorMessage );
    } //-- void removeError(NexusError) 

    /**
     * Set the errors field.
     * 
     * @param errors
     */
    public void setErrors(java.util.List errors)
    {
        this.errors = errors;
    } //-- void setErrors(java.util.List) 

//
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
