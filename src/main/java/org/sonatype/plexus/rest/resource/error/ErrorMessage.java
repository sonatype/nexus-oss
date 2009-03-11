/*
 * $Id$
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
