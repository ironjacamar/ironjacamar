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


import javax.resource.ResourceException;

/** The interface <code>ConnectionMetaData</code> provides information 
 *  about an EIS instance connected through a Connection instance. A
 *  component calls the method <code>Connection.getMetaData</code> to
 *  get a <code>ConnectionMetaData</code> instance. 
 *
 *  @version     0.8
 *  @author      Rahul Sharma
 *  @see         javax.resource.cci.Connection
 *  @see         javax.resource.cci.ResultSetInfo
**/

public interface ConnectionMetaData 
{

   /** Returns product name of the underlying EIS instance connected
    *  through the Connection that produced this metadata.
    *
    *  @return   Product name of the EIS instance
    *  @throws   ResourceException  Failed to get the information for
    *                               the EIS instance
    **/
   public String getEISProductName() throws ResourceException;

   /** Returns product version of the underlying EIS instance.
    *
    *  @return   Product version of an EIS instance. 
    *  @throws   ResourceException  Failed to get the information for
    *                               the EIS instance
    **/
   public String getEISProductVersion() throws ResourceException;

   /** Returns the user name for an active connection as known to 
    *  the underlying EIS instance. The name corresponds the resource
    *  principal under whose security context a connection to the
    *  EIS instance has been established.
    *
    *  @return   String representing the user name
    *  @throws   ResourceException  Failed to get the information for
    *                               the EIS instance           
    **/
   public String getUserName() throws ResourceException;
}
