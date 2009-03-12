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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/** 
 *  Streamable interface enables a resource adapter to extract data from
 *  an input Record or set data into an output Record as a stream of 
 *  bytes. 
 *
 *  <p>The Streamable interface provides a resource adapter's view
 *  of the data that has been set in a Record instance by a component.
 *  
 *  <p>The Streamable interface is not directly used by a component. It
 *  is used by a resource adapter implementation. A component uses Record 
 *  or any derived interfaces to manage records.
 *
 *  @author Rahul Sharma
 *  @since  0.8
 *  @see    javax.resource.cci.Record
**/
public interface Streamable 
{
   /** 
    *  Read data from an InputStream and initialize fields of a 
    *  Streamable object. 
    *
    *  @param  istream   InputStream that represents a resource
    *                    adapter specific internal representation
    *                    of fields of a Streamable object
    *  @exception IOException If an error occurs
    **/
   public void read(InputStream istream) throws IOException;
  
   /** 
    *  Write fields of a Streamable object to an OutputStream
    *  @param  ostream   OutputStream that holds value of a
    *                    Streamable object
    *  @exception IOException If an error occurs
    **/
   public void write(OutputStream ostream) throws IOException;
}

