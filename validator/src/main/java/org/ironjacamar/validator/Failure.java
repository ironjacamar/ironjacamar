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

import java.io.Serializable;
import java.util.ResourceBundle;

/**
 * Failure
 */
public class Failure implements Serializable
{
   /** SerialVersionUID */
   private static final long serialVersionUID = 1L;

   /** New line character */
   private static final String NEW_LINE = SecurityActions.getSystemProperty("line.separator");

   /** Severity */
   private final int severity;

   /** Section */
   private final String section;

   /** Description */
   private final String description;

   /** Code */
   private final String code;

   /**
    * Constructor
    * @param severity The severity
    * @param section The section
    * @param description The description
    */
   public Failure(int severity, String section, String description)
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
   public Failure(int severity, String section, String description, String code)
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

   /**
    * {@inheritDoc}
    *
    * @see Object#hashCode()
    */
   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((code == null) ? 0 : code.hashCode());
      result = prime * result + ((description == null) ? 0 : description.hashCode());
      result = prime * result + ((section == null) ? 0 : section.hashCode());
      result = prime * result + severity;
      return result;
   }

   /**
    * {@inheritDoc}
    *
    * @see Object#equals(Object)
    */
   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (!(obj instanceof Failure))
         return false;
      Failure other = (Failure) obj;
      if (code == null)
      {
         if (other.code != null)
            return false;
      }
      else if (!code.equals(other.code))
         return false;
      if (description == null)
      {
         if (other.description != null)
            return false;
      }
      else if (!description.equals(other.description))
         return false;
      if (section == null)
      {
         if (other.section != null)
            return false;
      }
      else if (!section.equals(other.section))
         return false;
      if (severity != other.severity)
         return false;
      return true;
   }

   /**
    * {@inheritDoc}
    *
    * @see Object#toString()
    */
   @Override
   public String toString()
   {
      return "Failure [severity=" + severity + ", section=" + section + ", description=" + description +
         ", code=" + code + "]";
   }
}
