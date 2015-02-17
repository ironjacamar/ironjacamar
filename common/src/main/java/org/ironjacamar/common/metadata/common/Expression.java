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
package org.ironjacamar.common.metadata.common;

/**
 * An expression entity
 *
 * @author <a href="vrastseluev@gmail.com">Vladimir Rastseluev</a>
 *
 */
public class Expression
{
   /** The key */
   private String key;
   /** A default value */
   private String defaultValue;
   /** A resolved value */
   private String resolvedValue;

   /**
    * {@inheritDoc}
    */
   public String getKey()
   {
      return key;
   }

   /**
    * {@inheritDoc}
    */
   public String getDefaultValue()
   {
      return defaultValue;
   }

   /**
    * {@inheritDoc}
    */
   public String getResolvedValue()
   {
      return resolvedValue;
   }

   /**
    * {@inheritDoc}
    */
   public void setResolvedValue(String value)
   {
      resolvedValue = value;
   }

   /**
    * An expression constructor
    * @param k key
    * @param value by default
    * @param resolved value
    */
   public Expression(String k, String value, String resolved)
   {
      key = k;
      defaultValue = value;
      resolvedValue = resolved;
   }

   /**
    * {@inheritDoc}
    */
   public String toString()
   {
      return "${" + key + (defaultValue == null ? "" : ":" + defaultValue) + "}";
   }

   /**
    * Substitutes the default value by the resolved one in the expression
    *
    * @return resulting expression
    */
   public String toSubstitution()
   {
      return "${" + key + (defaultValue == null ? "" : ":" +
            (resolvedValue == null ? defaultValue : resolvedValue)) + "}";
   }

   /**
    * Resolves the expression
    * @return the value of the expression
    */
   public String getValue()
   {
      return StringUtils.isEmptyTrimmed(resolvedValue) ? (StringUtils.isEmptyTrimmed(defaultValue) ?
            "" : defaultValue) : resolvedValue;
   }
}
