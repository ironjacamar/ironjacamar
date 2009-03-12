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

package javax.resource;

import javax.naming.Reference;

/** The Referenceable interface extends the javax.naming.Referenceable
 *  interface. It enables support for JNDI Reference mechanism for 
 *  the registration of the connection factory in the JNDI name space. 
 *  Note that the implementation and structure of Reference is specific
 *  to an application server.
 *
 *  <p>The implementation class for a connection factory interface is 
 *  required to implement both java.io.Serializable and 
 *  javax.resource.Referenceable interfaces to support JNDI registration.
 *
 *  @version     0.9
 *  @author      Rahul Sharma
 *
**/

public interface Referenceable extends javax.naming.Referenceable 
{
   /** Sets the Reference instance. This method is called by the
    *  deployment code to set the Reference that can be later
    *  returned by the getReference method (as defined in the
    *  javax.naming.Referenceable interface).
    *
    *  @param   reference  A Reference instance
    *  @see     javax.naming.Referenceable#getReference
    *
    **/
   public void setReference(Reference reference);
}
