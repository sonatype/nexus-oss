/*
 * $Id$
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
