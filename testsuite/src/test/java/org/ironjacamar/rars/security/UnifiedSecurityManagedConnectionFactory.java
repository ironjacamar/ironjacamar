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

import java.io.PrintWriter;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionDefinition;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.ResourceAdapterAssociation;
import javax.resource.spi.ValidatingManagedConnectionFactory;
import javax.resource.spi.security.PasswordCredential;
import javax.security.auth.Subject;

import org.jboss.logging.Logger;

/**
 * UnifiedSecurityManagedConnectionFactory
 *
 * @version $Revision: $
 */
@ConnectionDefinition(connectionFactory = UnifiedSecurityConnectionFactory.class,
      connectionFactoryImpl = UnifiedSecurityConnectionFactoryImpl.class,
      connection = UnifiedSecurityConnection.class,
      connectionImpl = UnifiedSecurityConnectionImpl.class)
public class UnifiedSecurityManagedConnectionFactory implements ManagedConnectionFactory, ResourceAdapterAssociation,
      ValidatingManagedConnectionFactory
{

   /**
    * The serial version UID
    */
   private static final long serialVersionUID = 1L;

   /**
    * The logger
    */
   private static Logger log = Logger.getLogger(UnifiedSecurityManagedConnectionFactory.class.getName());

   /**
    * The resource adapter
    */
   private ResourceAdapter ra;

   /**
    * The logwriter
    */
   private PrintWriter logwriter;

   /**
    * Default constructor
    */
   public UnifiedSecurityManagedConnectionFactory()
   {

   }


   /**
    * Creates a Connection Factory instance.
    *
    * @param cxManager ConnectionManager to be associated with created EIS connection factory instance
    * @return EIS-specific Connection Factory instance or javax.resource.cci.ConnectionFactory instance
    * @throws ResourceException Generic exception
    */
   public Object createConnectionFactory(ConnectionManager cxManager) throws ResourceException
   {
      log.tracef("createConnectionFactory(%s)", cxManager);
      return new UnifiedSecurityConnectionFactoryImpl(this, cxManager);
   }

   /**
    * Creates a Connection Factory instance.
    *
    * @return EIS-specific Connection Factory instance or javax.resource.cci.ConnectionFactory instance
    * @throws ResourceException Generic exception
    */
   public Object createConnectionFactory() throws ResourceException
   {
      throw new ResourceException("This resource adapter doesn't support non-managed environments");
   }

   /**
    * Creates a new physical connection to the underlying EIS resource manager.
    *
    * @param subject       Caller's security information
    * @param cxRequestInfo Additional resource adapter specific connection request information
    * @return ManagedConnection instance
    * @throws ResourceException generic exception
    */
   public ManagedConnection createManagedConnection(Subject subject, ConnectionRequestInfo cxRequestInfo)
         throws ResourceException
   {
      log.tracef("createManagedConnection(%s, %s)", subject, cxRequestInfo);
      return new UnifiedSecurityManagedConnection(this, subject, cxRequestInfo);
   }

   /**
    * Returns a matched connection from the candidate set of connections.
    *
    * @param connectionSet Candidate connection set
    * @param subject       Caller's security information
    * @param cxRequestInfo Additional resource adapter specific connection request information
    * @return ManagedConnection if resource adapter finds an acceptable match otherwise null
    * @throws ResourceException generic exception
    */
   public ManagedConnection matchManagedConnections(Set connectionSet, Subject subject,
         ConnectionRequestInfo cxRequestInfo) throws ResourceException
   {
      log.tracef("matchManagedConnections(%s, %s, %s)", connectionSet, subject, cxRequestInfo);
      ManagedConnection result = null;
      Iterator it = connectionSet.iterator();
      while (result == null && it.hasNext())
      {
         ManagedConnection mc = (ManagedConnection) it.next();
         boolean subjectMatched = false;
         boolean criMatched = false;
         if (mc instanceof UnifiedSecurityManagedConnection)
         {
            if (subject == null && ((UnifiedSecurityManagedConnection) mc).getSubject() == null)
            {
               subjectMatched = true;
            }
            else if (subject != null)
            {

               Set<PasswordCredential> givenCredentials = this.getPasswordCredentials(subject);

               Set<PasswordCredential> credentials = this
                     .getPasswordCredentials(((UnifiedSecurityManagedConnection) mc).getSubject());
               if (credentials != null && !credentials.isEmpty() && givenCredentials != null
                     && !givenCredentials.isEmpty())
               {
                  for (PasswordCredential pc : credentials)
                  {
                     for (PasswordCredential givenPc : givenCredentials)
                     {
                        if (givenPc.getUserName() != null && givenPc.getUserName().equals(pc.getUserName()) &&
                              givenPc.getPassword() != null && givenPc.getPassword().equals(pc.getPassword()))
                        {
                           subjectMatched = true;
                        }
                     }
                  }
               }

            }

            if (cxRequestInfo == null && ((UnifiedSecurityManagedConnection) mc).getCri() == null)
            {
               criMatched = true;
            }
            else if (cxRequestInfo != null)
            {

               if (cxRequestInfo instanceof UnifiedSecurityCri && ((UnifiedSecurityManagedConnection) mc)
                     .getCri() instanceof UnifiedSecurityCri)
               {
                  String givenUserName = ((UnifiedSecurityCri) cxRequestInfo).getUserName();
                  String givenPassword = ((UnifiedSecurityCri) cxRequestInfo).getPassword();

                  String mcUserName = ((UnifiedSecurityCri) ((UnifiedSecurityManagedConnection) mc).getCri())
                        .getUserName();
                  String mcPassword = ((UnifiedSecurityCri) ((UnifiedSecurityManagedConnection) mc).getCri())
                        .getPassword();

                  if (givenUserName != null && givenUserName.equals(mcUserName) &&
                        givenPassword != null && givenPassword.equals(mcPassword))
                  {
                     criMatched = true;
                  }

               }
            }

            if (subjectMatched && criMatched)
            {
               result = mc;
            }

         }

      }
      return result;
   }

   /**
    * Get the log writer for this ManagedConnectionFactory instance.
    *
    * @return PrintWriter
    * @throws ResourceException generic exception
    */
   public PrintWriter getLogWriter() throws ResourceException
   {
      log.trace("getLogWriter()");
      return logwriter;
   }

   /**
    * Set the log writer for this ManagedConnectionFactory instance.
    *
    * @param out PrintWriter - an out stream for error logging and tracing
    * @throws ResourceException generic exception
    */
   public void setLogWriter(PrintWriter out) throws ResourceException
   {
      log.tracef("setLogWriter(%s)", out);
      logwriter = out;
   }

   /**
    * Get the resource adapter
    *
    * @return The handle
    */
   public ResourceAdapter getResourceAdapter()
   {
      log.trace("getResourceAdapter()");
      return ra;
   }

   /**
    * Set the resource adapter
    *
    * @param ra The handle
    */
   public void setResourceAdapter(ResourceAdapter ra)
   {
      log.tracef("setResourceAdapter(%s)", ra);
      this.ra = ra;
   }

   /**
    * Returns a hash code value for the object.
    *
    * @return A hash code value for this object.
    */
   @Override
   public int hashCode()
   {
      return 17;
   }

   /**
    * Indicates whether some other object is equal to this one.
    *
    * @param other The reference object with which to compare.
    * @return true if this object is the same as the obj argument, false otherwise.
    */
   @Override
   public boolean equals(Object other)
   {
      if (other == null)
         return false;
      if (other == this)
         return true;
      if (!(other instanceof UnifiedSecurityManagedConnectionFactory))
         return false;
      return true;
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

   @Override
   public Set getInvalidConnections(Set connectionSet) throws ResourceException
   {
      Set result = new HashSet<>();

      Iterator it = connectionSet.iterator();

      while (it.hasNext())
      {
         UnifiedSecurityManagedConnection mc = (UnifiedSecurityManagedConnection) it.next();
         if (mc.isInvalid())
         {
            result.add(mc);
         }
      }

      return result;
   }

}
