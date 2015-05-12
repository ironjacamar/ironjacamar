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


package org.ironjacamar.validator;

import java.util.Collection;
import java.util.ResourceBundle;

/**
 * Failure helper
 */
public class FailureHelper
{
   /** New line character */
   private static final String NEW_LINE = SecurityActions.getSystemProperty("line.separator");

   /** List of failures */
   private final Collection<Failure> failures;

   /**
    * Constructor
    * @param failures The list of failures
    */
   public FailureHelper(Collection<Failure> failures)
   {
      this.failures = failures;
   }

   /**
    * As text
    * @param rb The resource bundle
    * @return The text representation
    */
   public String asText(ResourceBundle rb)
   {
      StringBuilder sb = new StringBuilder();

      if (failures != null)
      {
         // PRE-TEXT
         int i = 0;
         for (Failure failure : failures)
         {
            Failure f = failure;

            sb = sb.append(f.asText(rb));

            if (i < failures.size() - 1)
               sb = sb.append(NEW_LINE);

            i++;
         }

         // POST-TEXT
      }

      return sb.toString();
   }

   /**
    * As XML
    * @param rb The resource bundle
    * @return The XML representation
    */
   public String asXML(ResourceBundle rb)
   {
      StringBuilder sb = new StringBuilder();

      if (failures != null)
      {
         // PRE-XML

         int i = 0;
         for (Failure failure : failures)
         {
            Failure f = failure;

            sb = sb.append(f.asXML(rb));

            if (i < failures.size() - 1)
               sb = sb.append(NEW_LINE);
            i++;
         }

         // POST-XML
      }

      return sb.toString();
   }
}
