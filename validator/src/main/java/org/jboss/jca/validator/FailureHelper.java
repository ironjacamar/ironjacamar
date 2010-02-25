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

package org.jboss.jca.validator;

import java.util.List;
import java.util.ResourceBundle;

/**
 * Failure helper
 */
public class FailureHelper
{
   /** New line character */
   private static final String NEW_LINE = System.getProperty("line.separator");

   /** List of failures */
   private List<Failure> failures;

   /**
    * Constructor
    * @param failures The list of failures
    */
   public FailureHelper(List<Failure> failures)
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

         for (int i = 0; i < failures.size(); i++)
         {
            Failure f = failures.get(i);

            sb = sb.append(f.asText(rb));
            
            if (i < failures.size() - 1)
               sb = sb.append(NEW_LINE);
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

         for (int i = 0; i < failures.size(); i++)
         {
            Failure f = failures.get(i);

            sb = sb.append(f.asXML(rb));
            
            if (i < failures.size() - 1)
               sb = sb.append(NEW_LINE);
         }

         // POST-XML
      }

      return sb.toString();
   }
}
