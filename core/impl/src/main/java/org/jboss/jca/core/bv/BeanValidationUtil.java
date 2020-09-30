/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2009, Red Hat Inc, and individual contributors
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

package org.jboss.jca.core.bv;

import javax.validation.Configuration;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

/**
 * Bean validation utility
 *
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 * @version $Revision: $
 */
public class BeanValidationUtil
{
   /**
    * Constructor
    */
   private BeanValidationUtil()
   {
   }

   /**
    * Create a validator factory
    * @return The factory
    */
   public static ValidatorFactory createValidatorFactory()
   {
      Configuration configuration = Validation.byDefaultProvider().configure();
      Configuration<?> conf = configuration.traversableResolver(new JCATraversableResolver());

      return conf.buildValidatorFactory();
   }

   /**
    * Create a validator
    * @return The validator
    */
   public static Validator createValidator()
   {
      ValidatorFactory vf = createValidatorFactory();

      return vf.getValidator();
   }
}
