/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2015, Red Hat Inc, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the Eclipse Public License 1.0 as
 * published by the Free Software Foundation.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the Eclipse
 * Public License for more details.
 *
 * You should have received a copy of the Eclipse Public License 
 * along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.ironjacamar.core.connectionmanager;

import javax.resource.spi.ConnectionRequestInfo;
import javax.security.auth.Subject;

/**
 * Credential
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a> 
 */
public final class Credential
{
   /** The subject */
   private final Subject subject;
   
   /** The connection request information */
   private final ConnectionRequestInfo cri;
   
   /** The cached hashCode */
   private int hashCode;

   /**
    * Constructor
    * @param subject subject instance
    * @param cri connection request info
    */
   public Credential(Subject subject, ConnectionRequestInfo cri)
   {
      this.subject = subject;
      this.cri = cri;
      this.hashCode = Integer.MAX_VALUE;
   }

   /**
    * Get the subject
    * @return The value
    */
   public Subject getSubject()
   {
      return subject;
   }

   /**
    * Get the connection request info
    * @return The value
    */
   public ConnectionRequestInfo getConnectionRequestInfo()
   {
      return cri;
   }
   
   /**
    * {@inheritDoc}
    */
   @Override
   public int hashCode()
   {
      if (hashCode == Integer.MAX_VALUE)
         hashCode = SecurityActions.hashCode(subject) ^ (cri != null ? cri.hashCode() : 1);  
      
      return hashCode;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;  
      
      if (obj == null || !(obj instanceof Credential))
         return false;  
      
      Credential other = (Credential)obj;
      
      boolean result = SecurityActions.equals(subject, other.subject);
      if (result)
      {
         if (cri != null)
            result = cri.equals(other.cri);
         else
            result = other.cri == null;
      }
      
      return result;
   }
}
