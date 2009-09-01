/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008-2009, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package javax.resource.cci;

/** The interface <code>javax.resource.cci.ResourceAdapterMetaData</code> 
 *  provides information about capabilities of a resource adapter 
 *  implementation. Note that this interface does not provide information 
 *  about an EIS instance that is connected through the resource adapter.
 *
 *  <p>A CCI client uses a <code>ConnectionFactory.getMetaData</code> to 
 *  get metadata information about the resource adapter. The 
 *  <code>getMetaData</code> method does not require that an active 
 *  connection to an EIS instance should have been established.
 *
 *  <p>The ResourceAdapterMetaData can be extended to provide more 
 *  information specific to a resource adapter implementation.
 *  
 *  @author  Rahul Sharma
 *  @version 0.8
 *  @since   0.8
 *  @see     javax.resource.cci.ConnectionFactory
**/

public interface ResourceAdapterMetaData 
{

   /** Gets the version of the resource adapter.
    *
    *  @return   String representing version of the resource adapter
    **/
   public String getAdapterVersion();
   
   /** Gets the name of the vendor that has provided the resource 
    *  adapter.
    *
    *  @return   String representing name of the vendor that has 
    *            provided the resource adapter
    **/
   public String getAdapterVendorName();
  
   /** Gets a tool displayable name of the resource adapter.
    *
    *  @return   String representing the name of the resource adapter
    **/
   public String getAdapterName();
        
   /** Gets a tool displayable short desription of the resource
    *  adapter.
    *
    *  @return   String describing the resource adapter
    **/
   public String getAdapterShortDescription();

   /** Returns a string representation of the version of the 
    *  connector architecture specification that is supported by
    *  the resource adapter.
    *
    *  @return   String representing the supported version of 
    *            the connector architecture
    **/
   public String getSpecVersion();

   /** Returns an array of fully-qualified names of InteractionSpec
    *  types supported by the CCI implementation for this resource
    *  adapter. Note that the fully-qualified class name is for 
    *  the implementation class of an InteractionSpec. This method 
    *  may be used by tools vendor to find information on the 
    *  supported InteractionSpec types. The method should return 
    *  an array of length 0 if the CCI implementation does not 
    *  define specific InteractionSpec types.
    *
    *  @return   Array of fully-qualified class names of
    *            InteractionSpec classes supported by this
    *            resource adapter's CCI implementation
    *  @see      javax.resource.cci.InteractionSpec
    **/
   public String[] getInteractionSpecsSupported();

   /** Returns true if the implementation class for the Interaction 
    *  interface implements public boolean execute(InteractionSpec 
    *  ispec, Record input, Record output) method; otherwise the 
    *  method returns false.
    *
    *  @return   boolean depending on method support
    *  @see      javax.resource.cci.Interaction
    **/
   public boolean supportsExecuteWithInputAndOutputRecord();

   /** Returns true if the implementation class for the Interaction
    *  interface implements public Record execute(InteractionSpec
    *  ispec, Record input) method; otherwise the method returns 
    *  false.
    *
    *  @return   boolean depending on method support
    *  @see      javax.resource.cci.Interaction
    **/
   public boolean supportsExecuteWithInputRecordOnly();

   /** Returns true if the resource adapter implements the LocalTransaction
    *  interface and supports local transaction demarcation on the 
    *  underlying EIS instance through the LocalTransaction interface.
    *
    *  @return  true if resource adapter supports resource manager
    *           local transaction demarcation through LocalTransaction
    *           interface; false otherwise
    *  @see     javax.resource.cci.LocalTransaction
    **/
   public boolean supportsLocalTransactionDemarcation();
}
