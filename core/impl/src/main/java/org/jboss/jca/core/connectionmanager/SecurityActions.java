/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2014, Red Hat Inc, and individual contributors
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

package org.jboss.jca.core.connectionmanager;

import org.jboss.jca.core.spi.security.SubjectFactory;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Set;

import jakarta.resource.spi.security.PasswordCredential;
import javax.security.auth.Subject;

/**
 * Privileged Blocks
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
class SecurityActions
{ 
   /**
    * Constructor
    */
   private SecurityActions()
   {
   }

   /**
    * Get a Subject instance
    * @param subjectFactory The subject factory
    * @param domain The domain
    * @return The instance
    */
   static Subject createSubject(final SubjectFactory subjectFactory, final String domain)
   {
      if (System.getSecurityManager() == null)
         return subjectFactory.createSubject(domain);

      return AccessController.doPrivileged(new PrivilegedAction<Subject>() 
      {
         public Subject run()
         {
            return subjectFactory.createSubject(domain);
         }
      });
   }

   /**
    * Get the PasswordCredential from the Subject
    * @param subject The subject
    * @return The instances
    */
   static Set<PasswordCredential> getPasswordCredentials(final Subject subject)
   {
      if (System.getSecurityManager() == null)
         return subject.getPrivateCredentials(PasswordCredential.class);

      return AccessController.doPrivileged(new PrivilegedAction<Set<PasswordCredential>>() 
      {
         public Set<PasswordCredential> run()
         {
            return subject.getPrivateCredentials(PasswordCredential.class);
         }
      });
   }
}
