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


package org.ironjacamar.validator.rules.as;

import org.ironjacamar.validator.Failure;
import org.ironjacamar.validator.Key;
import org.ironjacamar.validator.Rule;
import org.ironjacamar.validator.Severity;
import org.ironjacamar.validator.Validate;
import org.ironjacamar.validator.ValidateClass;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * An ActivationSpec class must be non-null
 */
public class ASNull implements Rule
{
   /** Section */
   private static final String SECTION = "13.4.2.2";

   /**
    * Constructor
    */
   public ASNull()
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
      if (vo != null && Key.ACTIVATION_SPEC == vo.getKey())
      {
         if (vo.getClazz() == null)
         {
            ValidateClass vc = (ValidateClass)vo;

            List<Failure> failures = new ArrayList<Failure>(1);
            Failure failure = null;

            String code = null;
            if (vc.getClassName() != null)
            {
               code = vc.getClassName().equals("") ? "<empty>" : vc.getClassName() +
                  " (" + vc.getClassLoader().toString() + ")";
            }

            if (code != null)
            {
               failure = new Failure(Severity.ERROR,
                                     SECTION,
                                     rb.getString("as.ASNull"),
                                     code);
            }
            else
            {
               failure = new Failure(Severity.ERROR,
                                     SECTION,
                                     rb.getString("as.ASNull"));
            }

            failures.add(failure);

            return failures;
         }
      }

      return null;
   }
}

