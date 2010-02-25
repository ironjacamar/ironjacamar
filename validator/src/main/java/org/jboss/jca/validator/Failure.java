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

import java.util.ResourceBundle;

/**
 * Failure
 */
public class Failure
{
   /** New line character */
   private static final String NEW_LINE = System.getProperty("line.separator");

   /** Severity */
   private int severity;

   /** Section */
   private String section;

   /** Description */
   private String description;

   /** Code */
   private String code;

   /**
    * Constructor
    * @param severity The severity
    * @param section The section
    * @param description The description
    */
   public Failure(int severity,
                  String section,
                  String description)
   {
      this(severity, section, description, null);
   }

   /**
    * Constructor
    * @param severity The severity
    * @param section The section
    * @param description The description
    * @param code The code
    */
   public Failure(int severity,
                  String section,
                  String description,
                  String code)
   {
      this.severity = severity;
      this.section = section;
      this.description = description;
      this.code = code;
   }
   
   /**
    * Get the severity
    * @return The severity
    */
   public int getSeverity()
   {
      return severity;
   }

   /**
    * Get the section
    * @return The section
    */
   public String getSection()
   {
      return section;
   }

   /**
    * Get the description
    * @return The description
    */
   public String getDescription()
   {
      return description;
   }

   /**
    * Get the code
    * @return The code
    */
   public String getCode()
   {
      return code;
   }

   /**
    * As text
    * @param rb The resource bundle
    * @return The text representation
    */
   public String asText(ResourceBundle rb)
   {
      StringBuilder sb = new StringBuilder();

      sb = sb.append(rb.getString("severity"));
      sb = sb.append(": ");
      if (severity == Severity.WARNING)
      {
         sb = sb.append(rb.getString("severity.warn"));
      }
      else
      {
         sb = sb.append(rb.getString("severity.error"));
      }
      sb = sb.append(NEW_LINE);

      sb = sb.append(rb.getString("section"));
      sb = sb.append(": ");
      sb = sb.append(section);
      sb = sb.append(NEW_LINE);

      sb = sb.append(rb.getString("description"));
      sb = sb.append(": ");
      sb = sb.append(description);
      sb = sb.append(NEW_LINE);

      if (code != null)
      {
         sb = sb.append(rb.getString("code"));
         sb = sb.append(": ");
         sb = sb.append(code);
         sb = sb.append(NEW_LINE);
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
      return "";
   }
}
