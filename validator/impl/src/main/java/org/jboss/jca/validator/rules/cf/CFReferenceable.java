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

package org.jboss.jca.validator.rules.cf;

import org.jboss.jca.validator.Failure;
import org.jboss.jca.validator.Key;
import org.jboss.jca.validator.Rule;
import org.jboss.jca.validator.Severity;
import org.jboss.jca.validator.Validate;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import jakarta.resource.Referenceable;


/**
 * Referenceable must be implemented if present
 */
public class CFReferenceable implements Rule
{
   /** Section */
   private static final String SECTION = "17.5.1.1";

   /**
    * Constructor
    */
   public CFReferenceable()
   {
   }

   /**
    * Validate
    * @param vo The validate object
    * @param rb The resource bundle 
    * @return The list of failures found; <code>null</code> if none
    */
   @SuppressWarnings("unchecked")
   public List<Failure> validate(Validate vo, ResourceBundle rb)
   {
      if (vo != null && Key.CONNECTION_FACTORY == vo.getKey())
      {
         if (vo.getClazz() != null && !Referenceable.class.isAssignableFrom(vo.getClazz()))
         {
            List<Failure> failures = new ArrayList<Failure>(1);

            Failure failure = new Failure(Severity.ERROR,
                                          SECTION,
                                          rb.getString("cf.CFReferenceable"),
                                          vo.getClazz().getName());
            failures.add(failure);

            return failures;
         }
      }

      return null;
   }
}


