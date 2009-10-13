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

package org.jboss.jca.core.bv;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.metadata.BeanDescriptor;

import org.jboss.logging.Logger;

/**
 * Serializable validator
 *
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 * @version $Revision: $
 */
public class SerializableValidator implements Validator, Serializable
{
   /** Serial version uid */
   private static final long serialVersionUID = 1L;

   /** The logger */
   private static Logger log = Logger.getLogger(SerializableValidator.class);

   /** The validator */
   private transient Validator validator;

   /**
    * Constructor
    */
   public SerializableValidator()
   {
      this(BeanValidationUtil.createValidator());
   }

   /**
    * Constructor
    * @param v The validator
    */
   public SerializableValidator(Validator v)
   {
      this.validator = v;
   }

   /**
    * Get the constraints for a class
    * @param clazz The class
    * @return The bean descriptor
    */
   public BeanDescriptor getConstraintsForClass(Class<?> clazz)
   {
      return validator.getConstraintsForClass(clazz);
   }

   /**
    * Validate
    * @param object The object
    * @param groups The groups
    * @return The constraint violations
    */
   public <T> Set<ConstraintViolation<T>> validate(T object, Class<?>... groups)
   {
      return validator.validate(object, groups);
   }

   /**
    * Validate property
    * @param object The object
    * @param propertyName The property name
    * @param groups The groups
    * @return The constraint violations
    */
   public <T> Set<ConstraintViolation<T>> validateProperty(T object, String propertyName, Class<?>... groups)
   {
      return validator.validateProperty(object, propertyName, groups);
   }

   /**
    * Validate value
    * @param beanType The bean type
    * @param propertyName The property name
    * @param value The value
    * @param groups The groups
    * @return The constraint violations
    */
   public <T> Set<ConstraintViolation<T>> validateValue(Class<T> beanType, 
                                                        String propertyName, 
                                                        Object value, 
                                                        Class<?>... groups)
   {
      return validator.validateValue(beanType, propertyName, value, groups);
   }

   /**
    * Unwrap
    * @param type The type
    * @return The context
    */
   public <T> T unwrap(Class<T> type)
   {
      return validator.unwrap(type);
   }

   /**
    * Write the object - Nothing is written as the validator factory is transient
    * @param out The output stream
    * @exception IOException Thrown if an error occurs
    */
   private void writeObject(ObjectOutputStream out) throws IOException
   {
   }

   /**
    * Read the object - Nothing is read as the validator factory is transient.
    * A new instance is created
    * @param out The output stream
    * @exception IOException Thrown if an error occurs
    */
   private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
   {
      validator = BeanValidationUtil.createValidator();
   }
}
