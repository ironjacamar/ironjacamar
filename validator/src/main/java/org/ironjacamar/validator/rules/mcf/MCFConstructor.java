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


package org.ironjacamar.validator.rules.mcf;

import org.ironjacamar.validator.Failure;
import org.ironjacamar.validator.Key;
import org.ironjacamar.validator.Rule;
import org.ironjacamar.validator.Severity;
import org.ironjacamar.validator.Validate;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * A ManagedConnectionFactory must have a default constructor
 *
 * @author Jeff Zhang</a>
 * @version $Revision: $
 */
public class MCFConstructor implements Rule
{
   /** Section */
   private static final String SECTION = "19.3";

   /**
    * Constructor
    */
   public MCFConstructor()
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
      if (vo != null &&
          Key.MANAGED_CONNECTION_FACTORY == vo.getKey() &&
          vo.getClazz() != null)
      {
         try
         {
            SecurityActions.getConstructor(vo.getClazz(), (Class[])null);
         }
         catch (Throwable t)
         {
            List<Failure> failures = new ArrayList<Failure>(1);

            Failure failure = new Failure(Severity.ERROR,
                                          SECTION,
                                          rb.getString("mcf.MCFConstructor"));
            failures.add(failure);

            return failures;
         }
      }

      return null;
   }
}
