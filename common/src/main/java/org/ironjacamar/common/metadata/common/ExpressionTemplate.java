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

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A service class for expressions parsing
 *
 * @author <a href="vrastseluev@gmail.com">Vladimir Rastseluev</a>
 *
 */
public class ExpressionTemplate
{
   /** expression text */
   private String text;
   /** expression template */
   private String template;
   /** expression entities */
   private Map<String, Expression> entities;
   /** if the template contains complex expression on some level*/
   private boolean complex;

   /**
    * A constructor
    * @param expression to parse
    */
   public ExpressionTemplate(String expression)
   {
      text = expression;
      complex  = false;
      entities = new HashMap<>();
      parse();
   }

   /**
    * Parse a text and get a template and expression entities
    */
   private void parse()
   {
      template = text;
      if (StringUtils.isEmptyTrimmed(template))
         return;
      int index = 0;
      while (template.indexOf("${") != -1)
      {
         int from = template.lastIndexOf("${");
         int to = template.indexOf("}", from + 2);
         if (to == -1)
         {
            template = text;
            complex = false;
            entities.clear();
            return;
         }

         int dv = template.indexOf(":", from + 2);
         if (dv != -1 && dv > to)
         {
            dv = -1;
         }
         String systemProperty = null;
         String defaultValue = null;
         String s = template.substring(from + 2, to);
         if ("/".equals(s))
         {
            systemProperty = File.separator;
         }
         else if (":".equals(s))
         {
            systemProperty = File.pathSeparator;
            dv = -1;
         }
         else
         {
            systemProperty = SecurityActions.getSystemProperty(s);
         }

         if (dv != -1)
         {
            s = template.substring(from + 2, dv);
            systemProperty = SecurityActions.getSystemProperty(s);
            defaultValue = template.substring(dv + 1, to);
         }
         String prefix = "";
         String postfix = "";
         String key = StringUtils.createKey(index++);
         updateComplex(defaultValue);
         entities.put(key, new Expression(s, defaultValue, systemProperty));

         if (from != 0)
         {
            prefix = template.substring(0, from);
         }
         if (to + 1 < template.length())
         {
            postfix = template.substring(to + 1);
         }
         template = prefix + key + postfix;
      }
      updateComplex(template);
   }

   /**
    * Updates the complexness of the expression based on a String value
    * @param string value
    */
   private void updateComplex(String string)
   {
      if (string != null && StringUtils.getExpressionKey(string) != null
            && !string.equals(StringUtils.getExpressionKey(string)))
      {
         complex = true;
      }
   }
   /**
    * {@inheritDoc}
    */
   public boolean isComplex()
   {
      return complex;
   }

   /**
    * {@inheritDoc}
    */
   public String getText()
   {
      return text;
   }

   /**
    * {@inheritDoc}
    */
   public String getTemplate()
   {
      return template;
   }

   /**
    * {@inheritDoc}
    */
   public Map<String, Expression> getEntities()
   {
      return Collections.unmodifiableMap(entities);
   }

   /**
    * Resolves the template to the String value
    * @return resulting String
    */
   public String getValue()
   {
      return resolveTemplate(true);
   }

   /**
    * Resolves the template to the String value depending on boolean switch
    * @param toValue if equals true - all Expression entities within the template
    * will be presented by their values, otherwise - by their substitutions
    * @return resulting String
    */
   private String resolveTemplate(boolean toValue)
   {
      String result = template;
      if (StringUtils.isEmptyTrimmed(result))
         return result;

      String key;

      while ((key = StringUtils.getExpressionKey(result)) != null)
      {
         String subs;
         Expression ex = entities.get(key);
         String nKey = StringUtils.getExpressionKey(ex.getDefaultValue());
         if (toValue)
            subs = ex.getValue();
         else if (nKey != null && ex.getResolvedValue() != null &&
               ex.getDefaultValue().equals(nKey))
         {
            entities.get(nKey).setResolvedValue(ex.getResolvedValue());
            subs = ex.toString();
         }
         else
            subs = ex.toSubstitution();

         result = result.replace(key, subs);
      }
      return result;
   }

   /**
    * Get the substitutions value of the template
    * @return resulting String
    */
   public String getSubstitution()
   {
      return resolveTemplate(false);
   }

}
