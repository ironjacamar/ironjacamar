/*
* JBoss, Home of Professional Open Source
* Copyright 2006, JBoss Inc., and individual contributors as indicated
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
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
package org.jboss.rars.generic.cri;

import javax.resource.spi.ConnectionRequestInfo;
import javax.security.auth.Subject;

/**
 * SubjectCRIRequestID.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1.1.1 $
 */
public class SubjectCRIRequestID
{
   /** The subject */
   private Subject subject;
   
   /** The connection request info */
   private ConnectionRequestInfo cri;
   
   /** The cached hashCode */
   private int cachedHashCode = Integer.MIN_VALUE;
   
   /**
    * Create a new SubjectCRIRequestID.
    * 
    * @param subject the subject
    * @param cri the connection request info
    */
   public SubjectCRIRequestID(Subject subject, ConnectionRequestInfo cri)
   {
      this.subject = subject;
      this.cri = cri;
   }
   
   public int hashCode()
   {
      if (cachedHashCode == Integer.MIN_VALUE)
      {
         int hashCode = 37;
         if (subject != null)
            hashCode = (37 + hashCode) * subject.hashCode();
         if (cri != null)
            hashCode = (37 + hashCode) * cri.hashCode();
      }
      return cachedHashCode;
   }
   
   public boolean equals(Object object)
   {
      if (this == object)
         return true;
      if (object == null || object instanceof SubjectCRIRequestID == false)
         return false;
      
      SubjectCRIRequestID other = (SubjectCRIRequestID) object;
      
      if (this.subject == null && other.subject == null && this.cri == null && other.cri == null)
         return true;
      if (this.subject == null && other.subject != null)
         return false;
      if (this.subject != null && subject.equals(other) == false)
         return false;
      if (this.cri == null && other.cri != null)
         return false;
      if (this.cri != null && cri.equals(other.cri) == false)
         return false;
      return true;
   }
}
