/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2011, Red Hat Inc, and individual contributors
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
package org.jboss.jca.core.tx.jbossts;

import org.jboss.jca.core.CoreLogger;
import org.jboss.jca.core.recovery.ValidatingManagedConnectionFactoryRecoveryPlugin;
import org.jboss.jca.core.spi.recovery.RecoveryPlugin;
import org.jboss.jca.core.spi.security.SubjectFactory;
import org.jboss.jca.core.spi.transaction.TransactionIntegration;
import org.jboss.jca.core.spi.transaction.XAResourceStatistics;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashSet;
import java.util.Set;

import javax.resource.ResourceException;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.ValidatingManagedConnectionFactory;
import javax.resource.spi.security.PasswordCredential;
import javax.security.auth.Subject;
import javax.transaction.xa.XAResource;

import org.jboss.logging.Logger;

/**
 * An XAResourceRecovery implementation.
 *
 * @author <a href="stefano.maestri@ironjacamar.org">Stefano Maestri</a>
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class XAResourceRecoveryImpl implements org.jboss.jca.core.spi.transaction.recovery.XAResourceRecovery,
                                               org.jboss.tm.XAResourceRecovery
{
   /** Log instance */
   private static CoreLogger log = Logger.getMessageLogger(CoreLogger.class, XAResourceRecoveryImpl.class.getName());

   private final TransactionIntegration ti;

   private final ManagedConnectionFactory mcf;

   private final Boolean padXid;

   private final Boolean isSameRMOverrideValue;

   private final Boolean wrapXAResource;

   private final String recoverUserName;

   private final String recoverPassword;

   private final String recoverSecurityDomain;

   private final SubjectFactory subjectFactory;

   private final RecoveryPlugin plugin;

   private final XAResourceStatistics xastat;

   private ManagedConnection recoverMC;

   private String jndiName;

   /**
    * Create a new XAResourceRecoveryImpl.
    *
    * @param ti ti
    * @param mcf mcf
    * @param padXid padXid
    * @param isSameRMOverrideValue isSameRMOverrideValue
    * @param wrapXAResource wrapXAResource
    * @param recoverUserName recoverUserName
    * @param recoverPassword recoverPassword
    * @param recoverSecurityDomain recoverSecurityDomain
    * @param subjectFactory subjectFactory
    * @param plugin recovery plugin
    * @param xastat The XAResource statistics implementation
    */
   public XAResourceRecoveryImpl(TransactionIntegration ti,
                                 ManagedConnectionFactory mcf,
                                 Boolean padXid, Boolean isSameRMOverrideValue, Boolean wrapXAResource,
                                 String recoverUserName, String recoverPassword, String recoverSecurityDomain,
                                 SubjectFactory subjectFactory,
                                 RecoveryPlugin plugin,
                                 XAResourceStatistics xastat)
   {
      if (ti == null)
         throw new IllegalArgumentException("TransactionIntegration is null");

      if (mcf == null)
         throw new IllegalArgumentException("MCF is null");

      if (plugin == null)
         throw new IllegalArgumentException("Plugin is null");

      this.ti = ti;
      this.mcf = mcf;
      this.padXid = padXid;
      this.isSameRMOverrideValue = isSameRMOverrideValue;
      this.wrapXAResource = wrapXAResource;
      this.recoverUserName = recoverUserName;
      this.recoverPassword = recoverPassword;
      this.recoverSecurityDomain = recoverSecurityDomain;
      this.subjectFactory = subjectFactory;

      if (plugin instanceof ValidatingManagedConnectionFactoryRecoveryPlugin &&
          mcf instanceof ValidatingManagedConnectionFactory)
      {
         this.plugin = null;
      }
      else
      {
         this.plugin = plugin;
      }

      this.xastat = xastat;

      this.recoverMC = null;
      this.jndiName = null;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void initialize() throws Exception
   {
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void shutdown() throws Exception
   {
      if (recoverMC != null)
         close(recoverMC);
   }

   /**
    * Set the jndiName.
    *
    * @param jndiName The jndiName to set.
    */
   public void setJndiName(String jndiName)
   {
      this.jndiName = jndiName;
   }

   /**
    * Provides XAResource(s) to the transaction system for recovery purposes.
    *
    * @return An array of XAResource objects for use in transaction recovery
    * In most cases the implementation will need to return only a single XAResource in the array.
    * For more sophisticated cases, such as where multiple different connection types are supported,
    * it may be necessary to return more than one.
    *
    * The Resource should be instantiated in such a way as to carry the necessary permissions to
    * allow transaction recovery. For some deployments it may therefore be necessary or desirable to
    * provide resource(s) based on e.g. database connection parameters such as username other than those
    * used for the regular application connections to the same resource manager.
    */
   @Override
   public XAResource[] getXAResources()
   {
      try
      {
         Subject subject = getSubject();

         // Check if we got a valid Subject instance; requirement for recovery
         if (subject != null)
         {
            ManagedConnection mc = open(subject);
            XAResource xaResource = null;

            Object connection = null;
            try
            {
               connection = openConnection(mc, subject);
               xaResource = mc.getXAResource();
            }
            catch (ResourceException reconnect)
            {
               closeConnection(connection);
               connection = null;
               close(mc);
               mc = open(subject);
               xaResource = mc.getXAResource();
            }
            finally
            {
               boolean forceDestroy = closeConnection(connection);
               connection = null;

               if (forceDestroy)
               {
                  close(mc);
                  mc = open(subject);
                  xaResource = mc.getXAResource();
               }
            }
            
            if (wrapXAResource && !(xaResource instanceof org.jboss.jca.core.spi.transaction.xa.XAResourceWrapper))
            {
               String eisProductName = null;
               String eisProductVersion = null;

               try
               {
                  if (mc.getMetaData() != null)
                  {
                     eisProductName = mc.getMetaData().getEISProductName();
                     eisProductVersion = mc.getMetaData().getEISProductVersion();
                  }
               }
               catch (ResourceException re)
               {
                  // Ignore
               }

               if (eisProductName == null)
                  eisProductName = jndiName;

               if (eisProductVersion == null)
                  eisProductVersion = jndiName;

               xaResource = ti.createXAResourceWrapper(xaResource,
                                                       padXid,
                                                       isSameRMOverrideValue,
                                                       eisProductName,
                                                       eisProductVersion,
                                                       jndiName, false,
                                                       xastat);
            }

            log.debugf("Recovery XAResource=%s for %s", xaResource, jndiName);

            return new XAResource[]{xaResource};
         }
         else
         {
            log.nullSubjectCrashRecovery(jndiName);
         }
      }
      catch (ResourceException re)
      {
         log.exceptionDuringCrashRecovery(jndiName, re.getMessage(), re);
      }

      return new XAResource[0];
   }

   /**
    * This method provide the Subject used for the XA Resource Recovery
    * integration with the XAResourceRecoveryRegistry.
    *
    * This isn't done through the SecurityAssociation functionality of JBossSX
    * as the Subject returned here should only be used for recovery.
    *
    * @return The recovery subject; <code>null</code> if no Subject could be created
    */
   private Subject getSubject()
   {
      return AccessController.doPrivileged(new PrivilegedAction<Subject>()
      {
         /**
          * run method
          */
         public Subject run()
         {
            if (recoverUserName != null && recoverPassword != null)
            {
               log.debugf("Recovery user name=%s", recoverUserName);

               // User name and password use-case
               Subject subject = SecurityActions.createSubject(recoverUserName, recoverPassword, mcf);

               log.debugf("Recovery Subject=%s", subject);

               return subject;
            }
            else
            {
               // Security-domain use-case
               try
               {
                  // Select the domain
                  String domain = recoverSecurityDomain;

                  if (domain != null && subjectFactory != null)
                  {
                     Subject subject = SecurityActions.createSubject(subjectFactory, domain);
                     
                     Set<PasswordCredential> pcs = SecurityActions.getPasswordCredentials(subject);
                     if (pcs.size() > 0)
                     {
                        for (PasswordCredential pc : pcs)
                        {
                           pc.setManagedConnectionFactory(mcf);
                        }
                     }

                     log.debugf("Recovery Subject=%s", subject);

                     return subject;
                  }
                  else
                  {
                     log.noCrashRecoverySecurityDomain(jndiName);
                  }
               }
               catch (Throwable t)
               {
                  log.exceptionDuringCrashRecoverySubject(jndiName, t.getMessage(), t);
               }

               return null;
            }
         }
      });
   }

   /**
    * Open a managed connection
    * @param s The subject
    * @return The managed connection
    * @exception ResourceException Thrown in case of an error
    */
   @SuppressWarnings("unchecked")
   private ManagedConnection open(Subject s) throws ResourceException
   {
      log.debugf("Open managed connection (%s)", s);

      if (recoverMC == null)
         recoverMC = mcf.createManagedConnection(s, null);

      if (plugin == null)
      {
         try
         {
            ValidatingManagedConnectionFactory vmcf = (ValidatingManagedConnectionFactory)mcf;

            Set connectionSet = new HashSet(1);
            connectionSet.add(recoverMC);

            Set invalid = vmcf.getInvalidConnections(connectionSet);

            if (invalid != null && invalid.size() > 0)
            {
               log.debugf("Invalid managed connection: %s", recoverMC);

               close(recoverMC);
               recoverMC = mcf.createManagedConnection(s, null);
            }
         }
         catch (ResourceException re)
         {
            log.debugf("Exception during invalid check", re);

            close(recoverMC);
            recoverMC = mcf.createManagedConnection(s, null);
         }
      }

      return recoverMC;
   }

   /**
    * Close a managed connection
    * @param mc The managed connection
    */
   private void close(ManagedConnection mc)
   {
      log.debugf("Closing managed connection for recovery (%s)", mc);

      if (mc != null)
      {
         try
         {
            mc.cleanup();
         }
         catch (ResourceException ire)
         {
            log.debugf("Error during recovery cleanup", ire);
         }
      }

      if (mc != null)
      {
         try
         {
            mc.destroy();
         }
         catch (ResourceException ire)
         {
            log.debugf("Error during recovery destroy", ire);
         }
      }

      // The managed connection for recovery is now gone
      recoverMC = null;
   }

   /**
    * Open a connection
    * @param mc The managed connection
    * @param s The subject
    * @return The connection handle
    * @exception ResourceException Thrown in case of an error
    */
   private Object openConnection(ManagedConnection mc, Subject s) throws ResourceException
   {
      if (plugin == null)
         return null;

      log.debugf("Open connection (%s, %s)", mc, s);

      return mc.getConnection(s, null);
   }

   /**
    * Close a connection
    * @param c The connection
    * @return Should the managed connection be forced closed
    */
   private boolean closeConnection(Object c)
   {
      if (plugin == null)
         return false;

      log.debugf("Closing connection for recovery check (%s)", c);

      boolean forceClose = false;

      if (c != null)
      {
         try
         {
            forceClose = !plugin.isValid(c);
         }
         catch (ResourceException re)
         {
            log.debugf("Error during recovery plugin isValid()", re);
            forceClose = true;
         }

         try
         {
            plugin.close(c);
         }
         catch (ResourceException re)
         {
            log.debugf("Error during recovery plugin close()", re);
            forceClose = true;
         }
      }

      log.debugf("Force close=%s", forceClose);

      return forceClose;
   }
}
