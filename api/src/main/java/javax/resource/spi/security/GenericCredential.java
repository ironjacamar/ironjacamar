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

package javax.resource.spi.security;

import javax.resource.spi.SecurityException;

/** The interface <code>javax.resource.spi.security.GenericCredential</code> 
 *  defines a security mechanism independent interface for accessing 
 *  security credential of a resource principal. 
 *
 *  <p>The <code>GenericCredential</code> interface provides a Java 
 *  wrapper over an underlying mechanism specific representation of
 *  a security credential. For example, the <code>GenericCredential</code>
 *  interface can be used to wrap Kerberos credentials.
 *
 *  <p>The connector architecture does not define any standard format 
 *  and requirements for security mechanism specific credentials. For 
 *  example, a security credential wrapped by a GenericCredential 
 *  interface can have a native representation specific to an operating 
 *  system.
 *
 *  <p>The GenericCredential interface enables a resource adapter to 
 *  extract information about a security credential. The resource adapter
 *  can then manage EIS sign-on for a resource principal by either:
 *  <UL>
 *    <LI>using the credentials in an EIS specific manner if the underlying
 *        EIS supports the security mechanism type represented by the 
 *        GenericCredential instance, or,
 *    <LI>using GSS-API if the resource adapter and underlying EIS 
 *        instance support GSS-API.
 *  </UL>
 *
 *  @author  Rahul Sharma
 *  @version 0.7
 *  @since   0.7
 *  @see     javax.security.auth.Subject
 *  @see     java.security.Principal
 *  @deprecated The preferred way to represent generic credential information 
 *  is via the <code>org.ietf.jgss.GSSCredential</code> interface in 
 *  J2SE Version 1.4, which provides similar functionality.
 */

public interface GenericCredential 
{

   /** Returns the name of the resource principal associated 
    *  with a GenericCredential instance.
    *
    *  @return     Name of the principal
    **/
   public String getName();
   
   /** Returns the mechanism type for the GenericCredential instance. 
    *  The mechanism type definition for GenericCredential should be 
    *  consistent with the Object Identifier (OID) based representation
    *  specified in the GSS specification. In the GenericCredential
    *  interface, the mechanism type is returned as a stringified 
    *  representation of the OID specification.
    *
    *  @return    mechanism type
    **/
   public String getMechType();

   /** Gets security data for a specific security mechanism represented
    *  by the GenericCredential. An example is authentication data required
    *  for establishing a secure association with an EIS instance on
    *  behalf of the associated resource principal. 
    *
    *  <p>The getCredentialData method returns the credential 
    *  representation as an array of bytes. Note that the connector 
    *  architecture does not define any standard format for the returned 
    *  credential data.
    *
    *  @return   credential representation as an array of bytes.
    *  @throws   SecurityException   
    *                      Failed operation due to security related
    *                      error condition
    **/
   public byte[] getCredentialData() throws SecurityException;

   /** Tests if this GenericCredential instance refers to the same entity 
    *  as the supplied object.  The two credentials must be acquired over
    *  the same mechanisms and must refer to the same principal.  
    *
    *  Returns true if the two GenericCredentials refer to the same entity;
    *  false otherwise.
    *  @param another The other object
    *  @return True if equal; otherwise false
    **/
   public boolean equals(Object another);

   /** Returns the hash code for this GenericCredential
    * 
    *  @return  hash code for this GenericCredential
    **/
   public int hashCode();
}
