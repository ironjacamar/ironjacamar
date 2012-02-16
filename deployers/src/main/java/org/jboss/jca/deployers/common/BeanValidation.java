/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.jca.deployers.common;

import org.jboss.jca.deployers.DeployersLogger;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.validation.Configuration;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.groups.Default;

import org.jboss.logging.Logger;

/**
 * 
 * @author <a href="mailto:jeff.zhang@jboss.org">Jeff Zhang</a>
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 * @version $Revision: $
 */
public class BeanValidation
{
   private static DeployersLogger log = Logger.getMessageLogger(DeployersLogger.class, BeanValidation.class.getName());

   private static boolean trace = log.isTraceEnabled();

   private static String factoryName = "java:/ValidatorFactory";

   private ValidatorFactory factory;

   /**
    * Constructor
    */
   public BeanValidation()
   {
      this(null);
   }

   /**
    * Constructor
    * @param vf The validator factory
    */
   public BeanValidation(ValidatorFactory vf)
   {
      this.factory = vf;
   }

   /**
    * Validate the object against the Bean Validation specification (JSR-303).
    * The object must be fully initialized
    * @param object The object that should be validated
    * @param groupsClasses groups targeted for validation
    * @exception ConstraintViolationException Thrown if the object can't be validated
    */
   @SuppressWarnings("unchecked")
   public void validate(Object object, List<Class> groupsClasses) throws ConstraintViolationException
   {
      if (object == null)
      {
         throw new IllegalArgumentException("Object is null");
      }

      if (factory == null)
         initValidatorFactory();

      Validator v = factory.usingContext().traversableResolver(new JCATraversableResolver()).getValidator();

      Set errors = null;
      if (groupsClasses == null || groupsClasses.size() == 0)
      {
         if (trace)
            log.trace("Validating: " + object + " against groups " + Default.class.getName());

         errors = v.validate(object, Default.class);
      }
      else
      {
         Class[] vargs = groupsClasses.toArray(new Class[groupsClasses.size()]);

         if (trace)
            log.trace("Validating: " + object + " against groups " + Arrays.toString(vargs));

         errors = v.validate(object, vargs);
      }

      if (errors != null && errors.size() > 0)
      {
         throw new ConstraintViolationException(errors);
      }
   }

   /**
    * Set the validator factory name
    * @param name The factory name
    */
   public synchronized static void setValidatorFactoryName(String name)
   {
      factoryName = name;
   }

   /**
    * Init the validator factory
    */
   private synchronized void initValidatorFactory()
   {
      Context context = null;
      try
      {
         context = new InitialContext();

         factory = (ValidatorFactory) context.lookup(factoryName);
      }
      catch (Throwable t)
      {
         Configuration configuration = Validation.byDefaultProvider().configure();
         Configuration<?> conf = configuration.traversableResolver(new JCATraversableResolver());

         factory = conf.buildValidatorFactory();
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
