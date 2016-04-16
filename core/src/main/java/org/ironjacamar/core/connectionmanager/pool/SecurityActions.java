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

package org.ironjacamar.core.connectionmanager.pool;

import org.ironjacamar.core.spi.security.SubjectFactory;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Set;

import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.security.PasswordCredential;
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
    * Get a system property
    * @param name The property name
    * @return The property value
    */
   static String getSystemProperty(final String name)
   {
      return AccessController.doPrivileged(new PrivilegedAction<String>() 
      {
         public String run()
         {
            return System.getProperty(name);
         }
      });
   }

   /**
    * Get the classloader.
    * @param c The class
    * @return The classloader
    */
   static ClassLoader getClassLoader(final Class<?> c)
   {
      if (System.getSecurityManager() == null)
         return c.getClassLoader();

      return AccessController.doPrivileged(new PrivilegedAction<ClassLoader>()
      {
         public ClassLoader run()
         {
            return c.getClassLoader();
         }
      });
   }

   /**
    * Get the context classloader.
    * @return The classloader
    */
   public static ClassLoader getThreadContextClassLoader()
   {
      if (System.getSecurityManager() == null)
      {
         return Thread.currentThread().getContextClassLoader();
      }
      else
      {
         return AccessController.doPrivileged(new PrivilegedAction<ClassLoader>()
         {
            public ClassLoader run()
            {
               return Thread.currentThread().getContextClassLoader();
            }
         });
      }
   }

   /**
    * Set the context classloader.
    * @param cl classloader
    */
   public static void setThreadContextClassLoader(final ClassLoader cl)
   {
      if (System.getSecurityManager() == null)
      {
         Thread.currentThread().setContextClassLoader(cl);
      }
      else
      {
         AccessController.doPrivileged(new PrivilegedAction<Object>()
         {
            public Object run()
            {
               Thread.currentThread().setContextClassLoader(cl);

               return null;
            }
         });
      }
   }

   /**
    * Get a Subject instance
    * @param subjectFactory The subject factory
    * @param domain The domain
    * @param mcf The ManagedConnectionFactory
    * @return The instance
    */
   static Subject createSubject(final SubjectFactory subjectFactory,
                                final String domain,
                                final ManagedConnectionFactory mcf)
   {
      if (System.getSecurityManager() == null)
      {
         Subject subject = subjectFactory.createSubject(domain);
         Set<PasswordCredential> s = getPasswordCredentials(subject);
         if (s != null && !s.isEmpty())
         {
            for (PasswordCredential pc : s)
            {
               pc.setManagedConnectionFactory(mcf);
            }
         }
         return subject;
      }

      return AccessController.doPrivileged(new PrivilegedAction<Subject>() 
      {
         public Subject run()
         {
            Subject subject = subjectFactory.createSubject(domain);
            Set<PasswordCredential> s = getPasswordCredentials(subject);
            if (s != null && !s.isEmpty())
            {
               for (PasswordCredential pc : s)
               {
                  pc.setManagedConnectionFactory(mcf);
               }
            }
            return subject;
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

   /**
    * Get the hash code for a Subject
    * @param subject The Subject
    * @return The hash code
    */
   static int hashCode(final Subject subject)
   {
      if (System.getSecurityManager() == null)
         return subject != null ? subject.hashCode() : 0;

      Integer hashCode = AccessController.doPrivileged(new PrivilegedAction<Integer>() 
      {
         public Integer run()
         {
            return subject != null ? subject.hashCode() : 0;
         }
      });

      return hashCode.intValue();
   }

   /**
    * Verify if two Subject's are equal
    * @param s1 The first Subject
    * @param s2 The second Subject
    * @return True if equal; otherwise false
    */
   static boolean equals(final Subject s1, final Subject s2)
   {
      if (System.getSecurityManager() == null)
         return s1 != null ? s1.equals(s2) : s2 == null;

      Boolean equals = AccessController.doPrivileged(new PrivilegedAction<Boolean>() 
      {
         public Boolean run()
         {
            return s1 != null ? s1.equals(s2) : s2 == null;
         }
      });

      return equals.booleanValue();
   }

   /**
    * Get stack trace
    * @param t The thread
    * @return The trace
    */
   static StackTraceElement[] getStackTrace(final Thread t)
   {
      if (System.getSecurityManager() == null)
         return t.getStackTrace();

      return AccessController.doPrivileged(new PrivilegedAction<StackTraceElement[]>() 
      {
         public StackTraceElement[] run()
         {
            return t.getStackTrace();
         }
      });
   }
}
