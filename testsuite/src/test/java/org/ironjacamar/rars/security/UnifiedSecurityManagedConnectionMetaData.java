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
package org.ironjacamar.rars.security;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Set;

import javax.resource.ResourceException;
import javax.resource.spi.ManagedConnectionMetaData;
import javax.resource.spi.security.PasswordCredential;
import javax.security.auth.Subject;

import org.jboss.logging.Logger;

/**
 * UnifiedSecurityManagedConnectionMetaData
 *
 * @version $Revision: $
 */
public class UnifiedSecurityManagedConnectionMetaData implements ManagedConnectionMetaData
{
   /**
    * The logger
    */
   private static Logger log = Logger.getLogger(UnifiedSecurityManagedConnectionMetaData.class.getName());

   private final UnifiedSecurityManagedConnection mc;

   /**
    * constructor
    *
    * @param mc the managed connection
    */
   public UnifiedSecurityManagedConnectionMetaData(UnifiedSecurityManagedConnection mc)
   {
      this.mc = mc;
   }

   /**
    * Returns Product name of the underlying EIS instance connected through the ManagedConnection.
    *
    * @return Product name of the EIS instance
    * @throws ResourceException Thrown if an error occurs
    */
   @Override
   public String getEISProductName() throws ResourceException
   {
      log.trace("getEISProductName()");
      return "UnifiedSecurity resource adapter (CRI)";
   }

   /**
    * Returns Product version of the underlying EIS instance connected through the ManagedConnection.
    *
    * @return Product version of the EIS instance
    * @throws ResourceException Thrown if an error occurs
    */
   @Override
   public String getEISProductVersion() throws ResourceException
   {
      log.trace("getEISProductVersion()");
      return "1.0";
   }

   /**
    * Returns maximum limit on number of active concurrent connections
    *
    * @return Maximum limit for number of active concurrent connections
    * @throws ResourceException Thrown if an error occurs
    */
   @Override
   public int getMaxConnections() throws ResourceException
   {
      log.trace("getMaxConnections()");
      return 0;
   }

   /**
    * Returns name of the user associated with the ManagedConnection instance
    *
    * @return Name of the user
    * @throws ResourceException Thrown if an error occurs
    */
   @Override
   public String getUserName() throws ResourceException
   {
      log.trace("getUserName()");
      if (mc.getCri() != null)
      {
         return ((UnifiedSecurityCri) mc.getCri()).getUserName();
      }
      else if (mc.getSubject() != null)
      {
         Set<PasswordCredential> credentials = this.getPasswordCredentials(mc.getSubject());
         if (credentials != null && !credentials.isEmpty())
         {
            for (PasswordCredential pc : credentials)
            {
               return pc.getUserName();
            }
         }
      }

      return null;

   }

   /**
    * Get the PasswordCredential from the Subject
    *
    * @param subject The subject
    * @return The instances
    */
   private Set<PasswordCredential> getPasswordCredentials(final Subject subject)
   {
      if (System.getSecurityManager() == null)
         return subject.getPrivateCredentials(PasswordCredential.class);

      return AccessController.doPrivileged(
            (PrivilegedAction<Set<PasswordCredential>>) () -> subject.getPrivateCredentials(PasswordCredential.class));
   }

}
