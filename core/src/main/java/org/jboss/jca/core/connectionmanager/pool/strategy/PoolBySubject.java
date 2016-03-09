/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2008-2009, Red Hat Inc, and individual contributors
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

package org.jboss.jca.core.connectionmanager.pool.strategy;

import org.jboss.jca.core.CoreLogger;
import org.jboss.jca.core.api.connectionmanager.pool.PoolConfiguration;
import org.jboss.jca.core.connectionmanager.ConnectionManager;
import org.jboss.jca.core.connectionmanager.pool.AbstractPrefillPool;
import org.jboss.jca.core.spi.security.SubjectFactory;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Set;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.security.PasswordCredential;
import javax.security.auth.Subject;

import org.jboss.logging.Logger;

/**
 * Pool implementation that uses subject.
 * 
 * @author <a href="mailto:gurkanerdogdu@yahoo.com">Gurkan Erdogdu</a>
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class PoolBySubject extends AbstractPrefillPool
{
   /** The logger */
   private static CoreLogger log = Logger.getMessageLogger(CoreLogger.class, PoolBySubject.class.getName());
   
   /**
    * Creates a new instance.
    * @param mcf managed connection factory
    * @param pc pool configuration
    * @param noTxSeparatePools notx seperate pool
    * @param sharable Are the connections sharable
    * @param mcp mcp
    */
   public PoolBySubject(final ManagedConnectionFactory mcf, final PoolConfiguration pc,
                        final boolean noTxSeparatePools, final boolean sharable,
                        final String mcp)
   {
      super(mcf, pc, noTxSeparatePools, sharable, mcp);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   protected Object getKey(Subject subject, ConnectionRequestInfo cri, boolean separateNoTx) throws ResourceException
   {
      return new SubjectKey(subject, separateNoTx);
   }

   /**
    * {@inheritDoc}
    */
   public boolean testConnection()
   {
      try
      {
         ConnectionManager cm = getConnectionManager();
         ManagedConnectionFactory mcf = getManagedConnectionFactory();
       
         Subject subject = createSubject(cm.getSubjectFactory(), cm.getSecurityDomain(), mcf, cm.getJndiName());
  
         if (subject != null)
            return internalTestConnection(null, subject);
      }
      catch (Throwable t)
      {
         log.debugf(t, "Error during testConnection: %s", t.getMessage());
      }

      return false;
   }

   /**
    * {@inheritDoc}
    */
   public boolean testConnection(ConnectionRequestInfo cri, Subject subject)
   {
      return internalTestConnection(null, subject);
   }

   /**
    * Create a subject
    * @param subjectFactory The subject factory
    * @param securityDomain The security domain
    * @param mcf The managed connection factory
    * @param jndiName The jndi-name
    * @return The subject; <code>null</code> in case of an error
    */
   protected Subject createSubject(final SubjectFactory subjectFactory,
                                   final String securityDomain,
                                   final ManagedConnectionFactory mcf,
                                   final String jndiName)
   {
      if (subjectFactory == null)
         throw new IllegalArgumentException("SubjectFactory is null");

      if (securityDomain == null)
         throw new IllegalArgumentException("SecurityDomain is null");

      return AccessController.doPrivileged(new PrivilegedAction<Subject>() 
      {
         public Subject run()
         {
            try
            {
               Subject subject = subjectFactory.createSubject(securityDomain);

               Set<PasswordCredential> pcs = subject.getPrivateCredentials(PasswordCredential.class);
               if (pcs.size() > 0)
               {
                  for (PasswordCredential pc : pcs)
                  {
                     pc.setManagedConnectionFactory(mcf);
                  }
               }

               return subject;
            }
            catch (Throwable t)
            {
               log.exceptionDuringCreateSubject(jndiName, t.getMessage(), t);
            }

            return null;
         }
      });
   }

   /**
    * {@inheritDoc}
    */
   public CoreLogger getLogger()
   {
      return log;
   }
}
