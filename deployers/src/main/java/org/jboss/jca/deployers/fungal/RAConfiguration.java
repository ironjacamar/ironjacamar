/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008-2010, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.jca.deployers.fungal;

import org.jboss.jca.core.api.bootstrap.CloneableBootstrapContext;
import org.jboss.jca.core.spi.mdr.MetadataRepository;
import org.jboss.jca.core.spi.naming.JndiStrategy;

import java.io.PrintStream;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.transaction.TransactionManager;

import org.jboss.logging.Logger;

/**
 * A configuration for the resource adapter deployer chain
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class RAConfiguration
{
   private static Logger log = Logger.getLogger(RAConfiguration.class);

   private static boolean trace = log.isTraceEnabled();

   /** The transaction manager */
   private TransactionManager transactionManager = null;

   /** Preform bean validation */
   private AtomicBoolean beanValidation = new AtomicBoolean(true);

   /** Preform archive validation */
   private AtomicBoolean archiveValidation = new AtomicBoolean(true);

   /** Archive validation: Fail on Warn */
   private AtomicBoolean archiveValidationFailOnWarn = new AtomicBoolean(false);

   /** Archive validation: Fail on Error */
   private AtomicBoolean archiveValidationFailOnError = new AtomicBoolean(true);

   /** Print stream */
   private PrintStream printStream = null;

   /** Default bootstrap context */
   private CloneableBootstrapContext defaultBootstrapContext = null;

   /** Bootstrap contexts */
   private Map<String, CloneableBootstrapContext> bootstrapContexts = null;

   /** Scope deployment */
   private AtomicBoolean scopeDeployment = new AtomicBoolean(false);

   /** JNDI strategy */
   private JndiStrategy jndiStrategy = null;

   /** Metadata repository */
   private MetadataRepository mdr = null;

   /**
    * Constructor
    */
   public RAConfiguration()
   {
   }

   /**
    * Set the transaction manager
    * @param value The value
    */
   public void setTransactionManager(TransactionManager value)
   {
      transactionManager = value;
   }

   /**
    * Get the transaction manager
    * @return The value
    */
   public TransactionManager getTransactionManager()
   {
      return transactionManager;
   }

   /**
    * Set if bean validation should be performed
    * @param value The value
    */
   public void setBeanValidation(boolean value)
   {
      beanValidation.set(value);
   }

   /**
    * Should bean validation be performed
    * @return True if validation; otherwise false
    */
   public boolean getBeanValidation()
   {
      return beanValidation.get();
   }

   /**
    * Set if archive validation should be performed
    * @param value The value
    */
   public void setArchiveValidation(boolean value)
   {
      archiveValidation.set(value);
   }

   /**
    * Should archive validation be performed
    * @return True if validation; otherwise false
    */
   public boolean getArchiveValidation()
   {
      return archiveValidation.get();
   }

   /**
    * Set if a failed warning archive validation report should fail the deployment
    * @param value The value
    */
   public void setArchiveValidationFailOnWarn(boolean value)
   {
      archiveValidationFailOnWarn.set(value);
   }

   /**
    * Does a failed archive validation warning report fail the deployment
    * @return True if failing; otherwise false
    */
   public boolean getArchiveValidationFailOnWarn()
   {
      return archiveValidationFailOnWarn.get();
   }

   /**
    * Set if a failed error archive validation report should fail the deployment
    * @param value The value
    */
   public void setArchiveValidationFailOnError(boolean value)
   {
      archiveValidationFailOnError.set(value);
   }

   /**
    * Does a failed archive validation error report fail the deployment
    * @return True if failing; otherwise false
    */
   public boolean getArchiveValidationFailOnError()
   {
      return archiveValidationFailOnError.get();
   }

   /**
    * Set the print stream
    * @param value The value
    */
   public void setPrintStream(PrintStream value)
   {
      printStream = value;
   }

   /**
    * Get the print stream
    * @return The handle
    */
   public PrintStream getPrintStream()
   {
      return printStream;
   }

   /**
    * Set the default bootstrap context
    * @param value The value
    */
   public void setDefaultBootstrapContext(CloneableBootstrapContext value)
   {
      defaultBootstrapContext = value;
   }

   /**
    * Get the default bootstrap context
    * @return The handle
    */
   public CloneableBootstrapContext getDefaultBootstrapContext()
   {
      return defaultBootstrapContext;
   }

   /**
    * Set the bootstrap context map
    * @param value The value
    */
   public void setBootstrapContexts(Map<String, CloneableBootstrapContext> value)
   {
      bootstrapContexts = value;
   }

   /**
    * Get the bootstrap context map
    * @return The handle
    */
   public Map<String, CloneableBootstrapContext> getBootstrapContexts()
   {
      return bootstrapContexts;
   }

   /**
    * Set if deployments should be scoped
    * @param value The value
    */
   public void setScopeDeployment(boolean value)
   {
      scopeDeployment.set(value);
   }

   /**
    * Are the deployments scoped
    * @return True if scoped; otherwise false
    */
   public boolean getScopeDeployment()
   {
      return scopeDeployment.get();
   }

   /**
    * Set the JNDI strategy
    * @param value The value
    */
   public void setJndiStrategy(JndiStrategy value)
   {
      jndiStrategy = value;
   }

   /**
    * Get the JNDI strategy
    * @return The handle
    */
   public JndiStrategy getJndiStrategy()
   {
      return jndiStrategy;
   }

   /**
    * Set the metadata repository
    * @param value The value
    */
   public void setMetadataRepository(MetadataRepository value)
   {
      mdr = value;
   }

   /**
    * Get the metadata repository
    * @return The handle
    */
   public MetadataRepository getMetadataRepository()
   {
      return mdr;
   }

   /**
    * Start
    */
   public void start()
   {
      if (defaultBootstrapContext == null)
         throw new IllegalStateException("DefaultBootstrapContext not defined");

      if (printStream == null)
         throw new IllegalStateException("PrintStream not defined");

      if (jndiStrategy == null)
         throw new IllegalStateException("JndiStrategy not defined");

      if (mdr == null)
         throw new IllegalStateException("MetadataRepository not defined");
   }
}
