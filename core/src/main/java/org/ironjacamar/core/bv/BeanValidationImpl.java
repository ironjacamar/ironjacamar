/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2016, Red Hat Inc, and individual contributors
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

package org.ironjacamar.core.bv;

import org.ironjacamar.core.spi.bv.BeanValidation;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.validation.Configuration;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;

/**
 * Bean validation implementation backed by Hibernate Validator
 *
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class BeanValidationImpl implements BeanValidation
{
   /** Validator factory */
   private static final String VALIDATOR_FACTORY = "java:/ValidatorFactory";

   /** The validator factory */
   private ValidatorFactory validatorFactory;

   private final int jndiPort;

   private final String jndiProtocol;

   private final String jndiHost;

   /**
    * Constructor
    * @param jndiProtocol the jndi protocol
    * @param jndiHost the jndi host
    * @param jndiPort the jndi port
    */
   public BeanValidationImpl(final String jndiProtocol, final String jndiHost, final int jndiPort)
   {
      this.jndiProtocol = jndiProtocol;
      this.jndiHost = jndiHost;
      this.jndiPort = jndiPort;
      validatorFactory = createValidatorFactory();
   }

   /**
    * Get the validator factory
    * @return The factory
    */
   public ValidatorFactory getValidatorFactory()
   {
      return new IronJacamarValidatorFactory(validatorFactory);
   }

   /**
    * Create a validator factory
    * @return The factory
    */
   static ValidatorFactory createValidatorFactory()
   {
      Configuration configuration = Validation.byDefaultProvider().configure();
      Configuration<?> conf = configuration.traversableResolver(new IronJacamarTraversableResolver());

      return conf.buildValidatorFactory();
   }

   /**
    * Start
    * @exception Throwable If an error occurs
    */
   public void start() throws Throwable
   {
      Context context = null;
      try
      {
         Properties properties = new Properties();
         properties.setProperty(Context.PROVIDER_URL, jndiProtocol + "://" + jndiHost + ":" + jndiPort);
         context = new InitialContext(properties);
         context.rebind(VALIDATOR_FACTORY, new SerializableValidatorFactory(validatorFactory));
      }
      finally
      {
         try
         {
            if (context != null)
               context.close();
         }
         catch (NamingException ne)
         {
            // Ignore
         }
      }
   }

   /**
    * Stop
    * @exception Throwable If an error occurs
    */
   public void stop() throws Throwable
   {
      if (validatorFactory != null)
         validatorFactory.close();

      Context context = null;
      try
      {
         Properties properties = new Properties();
         properties.setProperty(Context.PROVIDER_URL, jndiProtocol + "://" + jndiHost + ":" + jndiPort);
         context = new InitialContext(properties);
         context.unbind(VALIDATOR_FACTORY);
      }
      finally
      {
         try
         {
            if (context != null)
               context.close();
         }
         catch (NamingException ne)
         {
            // Ignore
         }
      }
   }
}
