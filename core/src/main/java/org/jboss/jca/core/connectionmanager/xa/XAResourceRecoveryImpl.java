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
package org.jboss.jca.core.connectionmanager.xa;


import java.security.AccessController;
import java.security.Principal;
import java.security.PrivilegedAction;

import javax.resource.ResourceException;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.security.PasswordCredential;
import javax.security.auth.Subject;
import javax.transaction.xa.XAResource;

import org.jboss.logging.Logger;
import org.jboss.security.SecurityContext;
import org.jboss.security.SecurityContextAssociation;
import org.jboss.security.SecurityContextFactory;
import org.jboss.security.SimplePrincipal;
import org.jboss.security.SubjectFactory;
import org.jboss.tm.XAResourceRecovery;
import org.jboss.tm.XAResourceRecoveryRegistry;

/**
 *
 * A XAResourceRecoveryImpl.
 *
 * @author <a href="stefano.maestri@jboss.com">Stefano Maestri</a>
 *
 */
public class XAResourceRecoveryImpl implements XAResourceRecovery
{

   private final ManagedConnectionFactory mcf;

   private final Boolean padXid;

   private final Boolean isSameRMOverrideValue;

   private final Boolean wrapXAResource;

   private String jndiName;

   private final String recoverUserName;

   private final String recoverPassword;

   private final String recoverSecurityDomain;

   private final SubjectFactory subjectFactory;

   private ManagedConnection recoverMC;

   /** Log instance */
   private static Logger log = Logger.getLogger(XAResourceRecoveryImpl.class);

   /**
    * Create a new XAResourceRecoveryImpl.
    *
    * @param mcf mcf
    * @param padXid padXid
    * @param isSameRMOverrideValue isSameRMOverrideValue
    * @param wrapXAResource wrapXAResource
    * @param recoverUserName recoverUserName
    * @param recoverPassword recoverPassword
    * @param recoverSecurityDomain recoverSecurityDomain
    * @param subjectFactory subjectFactory
    */
   public XAResourceRecoveryImpl(ManagedConnectionFactory mcf, Boolean padXid, Boolean isSameRMOverrideValue,
      Boolean wrapXAResource, String recoverUserName,
      String recoverPassword, String recoverSecurityDomain, SubjectFactory subjectFactory)
   {
      super();
      this.mcf = mcf;
      this.padXid = padXid;
      this.isSameRMOverrideValue = isSameRMOverrideValue;
      this.wrapXAResource = wrapXAResource;
      this.recoverUserName = recoverUserName;
      this.recoverPassword = recoverPassword;
      this.recoverSecurityDomain = recoverSecurityDomain;
      this.subjectFactory = subjectFactory;

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

            try
            {
               xaResource = mc.getXAResource();
            }
            catch (ResourceException reconnect)
            {
               close(mc);
               mc = open(subject);
               xaResource = mc.getXAResource();
            }

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
               eisProductName = getJndiName();

            if (eisProductVersion == null)
               eisProductVersion = getJndiName();

            try
            {
               if (wrapXAResource)
               {

                  xaResource = new XAResourceWrapperImpl(xaResource,
                                                            padXid,
                                                            isSameRMOverrideValue,
                                                            eisProductName,
                                                            eisProductVersion,
                                                            jndiName);
               }
            }
            catch (Throwable t)
            {
               // Ignore
            }

            if (log.isDebugEnabled())
               log.debug("Recovery XAResource=" + xaResource + " for " + jndiName);

            return new XAResource[]{xaResource};
         }
         else
         {
            if (log.isDebugEnabled())
               log.debug("Subject for recovery was null");
         }
      }
      catch (ResourceException re)
      {
         if (log.isDebugEnabled())
            log.debug("Error during recovery", re);
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
               // User name and password use-case
               Subject subject = new Subject();

               // Principals
               Principal p = new SimplePrincipal(recoverUserName);
               subject.getPrincipals().add(p);

               // PrivateCredentials
               PasswordCredential pc = new PasswordCredential(recoverUserName, recoverPassword.toCharArray());
               pc.setManagedConnectionFactory(mcf);
               subject.getPrivateCredentials().add(pc);

               // PublicCredentials
               // None

               if (log.isDebugEnabled())
                  log.debug("Recovery Subject=" + subject);

               return subject;
            }
            else
            {
               // Security-domain use-case
               try
               {
                  // Create a security context on the association
                  SecurityContext securityContext = SecurityContextFactory
                     .createSecurityContext(recoverSecurityDomain);
                  SecurityContextAssociation.setSecurityContext(securityContext);

                  // Unauthenticated
                  Subject unauthenticated = new Subject();

                  // Leave the subject empty as we don't have any information to do the
                  // authentication with - and we only need it to be able to get the
                  // real subject from the SubjectFactory

                  // Set the authenticated subject
                  securityContext.getSubjectInfo().setAuthenticatedSubject(unauthenticated);

                  // Select the domain
                  String domain = recoverSecurityDomain;

                  if (domain != null)
                  {
                     // Use the unauthenticated subject to get the real recovery subject instance
                     Subject subject = subjectFactory.createSubject(domain);

                     if (log.isDebugEnabled())
                        log.debug("Recovery Subject=" + subject);

                     return subject;
                  }
                  else
                  {
                     if (log.isDebugEnabled())
                        log.debug("RecoverySecurityDomain was empty");
                  }
               }
               catch (Throwable t)
               {
                  log.debug("Exception during getSubject()" + t.getMessage(), t);
               }

               return null;
            }
         }
      });
   }

   /**
    *
    * registeer this impl to passed XAResourceRecoveryRegistry
    *
    * @param registry the registry
    * @param cfJndiName the connection factory jndi name
    */
   public void registerXaRecovery(XAResourceRecoveryRegistry registry, String cfJndiName)
   {
      this.jndiName = cfJndiName;
      registry.addXAResourceRecovery(this);

   }

   /**
    * Open a managed connection
    * @param s The subject
    * @return The managed connection
    * @exception ResourceException Thrown in case of an error
    */
   private ManagedConnection open(Subject s) throws ResourceException
   {
      if (recoverMC == null)
      {
         recoverMC = mcf.createManagedConnection(s, null);
      }

      return recoverMC;
   }

   /**
   * Close a managed connection
   * @param mc The managed connection
   */
   private void close(ManagedConnection mc)
   {
      if (mc != null)
      {
         try
         {
            mc.cleanup();
         }
         catch (ResourceException ire)
         {
            if (log.isDebugEnabled())
               log.debug("Error during recovery cleanup", ire);
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
            if (log.isDebugEnabled())
               log.debug("Error during recovery destroy", ire);
         }
      }

      mc = null;
   }

   /**
    * Get the recoverMC.
    *
    * @return the recoverMC.
    */
   public final ManagedConnection getRecoverMC()
   {
      return recoverMC;
   }

   /**
    * Set the recoverMC.
    *
    * @param recoverMC The recoverMC to set.
    */
   public final void setRecoverMC(ManagedConnection recoverMC)
   {
      this.recoverMC = recoverMC;
   }

   /**
    * Get the mcf.
    *
    * @return the mcf.
    */
   public final ManagedConnectionFactory getMcf()
   {
      return mcf;
   }

   /**
    * Get the padXid.
    *
    * @return the padXid.
    */
   public final Boolean isPadXid()
   {
      return padXid;
   }

   /**
    * Get the isSameRMOverrideValue.
    *
    * @return the isSameRMOverrideValue.
    */
   public final Boolean isSameRMOverrideValue()
   {
      return isSameRMOverrideValue;
   }

   /**
    * Get the wrapXAResource.
    *
    * @return the wrapXAResource.
    */
   public final Boolean isWrapXAResource()
   {
      return wrapXAResource;
   }

   /**
    * Get the jndiName.
    *
    * @return the jndiName.
    */
   public final String getJndiName()
   {
      return jndiName;
   }

   /**
    * Get the recoverUserName.
    *
    * @return the recoverUserName.
    */
   public final String getRecoverUserName()
   {
      return recoverUserName;
   }

   /**
    * Get the recoverPassword.
    *
    * @return the recoverPassword.
    */
   public final String getRecoverPassword()
   {
      return recoverPassword;
   }

   /**
    * Get the recoverSecurityDomain.
    *
    * @return the recoverSecurityDomain.
    */
   public final String getRecoverSecurityDomain()
   {
      return recoverSecurityDomain;
   }

   /**
    * Get the subjectFactory.
    *
    * @return the subjectFactory.
    */
   public final SubjectFactory getSubjectFactory()
   {
      return subjectFactory;
   }

   /**
    * Set the jndiName.
    *
    * @param jndiName The jndiName to set.
    */
   public final void setJndiName(String jndiName)
   {
      this.jndiName = jndiName;
   }
}
