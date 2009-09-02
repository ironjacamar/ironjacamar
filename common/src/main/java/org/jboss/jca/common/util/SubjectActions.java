/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.jca.common.util;

import java.security.AccessController;
import java.security.PrivilegedAction;

import javax.security.auth.Subject;

/**
 * SubjectActions.
 * @author <a href="mailto:gurkanerdogdu@yahoo.com">Gurkan Erdogdu</a> 
 * @version $Rev$ $Date$
 *
 */
public class SubjectActions implements PrivilegedAction<Object>
{  
   /**First subject instance*/
   private Subject subject;
   
   /**Other subject instance*/
   private Subject other;
   
   /**
    * Creates a new subject action.
    * @param subject first instance
    * @param other othe instance
    */
   SubjectActions(Subject subject, Subject other)
   {
      this.subject = subject;
      this.other = other;
   }
   
   /**
    * {@inheritDoc}
    */
   public Object run()
   {
      Object value = null;
      
      if (other == null)
      {
         value = Integer.valueOf(subject.hashCode());  
      }
      else
      {
         value = Boolean.valueOf(subject.equals(other));  
      }
      
      return value;
   }
   
   /**
    * HashCode.
    * @param subject subject instance
    * @return hash code
    */
   public static int hashCode(Subject subject)
   {
      SubjectActions action = new SubjectActions(subject, null);
      return ((Integer) AccessController.doPrivileged(action)).intValue();
   }
   
   /**
    * Equals.
    * @param subject subject instance
    * @param other other instance
    * @return equality
    */
   public static boolean equals(Subject subject, Subject other)
   {
      SubjectActions action = new SubjectActions(subject, other);
      return ((Boolean) AccessController.doPrivileged(action)).booleanValue();
   }
}

