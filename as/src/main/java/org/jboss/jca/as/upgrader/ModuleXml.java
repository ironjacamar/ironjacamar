/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.jca.as.upgrader;

import java.util.ArrayList;
import java.util.List;

/**
 * module.xml definitions
 * 
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class ModuleXml
{
   private static final String HEADER[] = {
      "<?xml version=\"1.0\" encoding=\"UTF-8\"?>",
      "",
      "<!--",
      "  ~ JBoss, Home of Professional Open Source.",
      "  ~ Copyright 2011, Red Hat, Inc., and individual contributors",
      "  ~ as indicated by the @author tags. See the copyright.txt file in the",
      "  ~ distribution for a full listing of individual contributors.",
      "  ~",
      "  ~ This is free software; you can redistribute it and/or modify it",
      "  ~ under the terms of the GNU Lesser General Public License as",
      "  ~ published by the Free Software Foundation; either version 2.1 of",
      "  ~ the License, or (at your option) any later version.",
      "  ~",
      "  ~ This software is distributed in the hope that it will be useful,",
      "  ~ but WITHOUT ANY WARRANTY; without even the implied warranty of",
      "  ~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU",
      "  ~ Lesser General Public License for more details.",
      "  ~",
      "  ~ You should have received a copy of the GNU Lesser General Public",
      "  ~ License along with this software; if not, write to the Free",
      "  ~ Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA",
      "  ~ 02110-1301 USA, or see the FSF site: http://www.fsf.org.",
      " -->"
   };

   /**
    * Get the API definiton
    * @param version The version
    * @return The value
    */
   public static List<String> getApi(String version)
   {
      List<String> result = new ArrayList<String>();

      for (String s : HEADER)
      {
         result.add(s);
      }

      result.add("");

      result.add("<module xmlns=\"urn:jboss:module:1.0\" name=\"org.jboss.ironjacamar.api\">");
      result.add("  <resources>");
      result.add("    <resource-root path=\"ironjacamar-common-api-" + version + ".jar\"/>");
      result.add("    <resource-root path=\"ironjacamar-common-spi-" + version + ".jar\"/>");
      result.add("    <resource-root path=\"ironjacamar-core-api-" + version + ".jar\"/>");
      result.add("  </resources>");

      result.add("");
  
      result.add("  <dependencies>");
      result.add("    <module name=\"javax.api\"/>");
      result.add("    <module name=\"javax.resource.api\"/>");
      result.add("    <module name=\"org.jboss.common-core\"/>");
      result.add("    <module name=\"org.jboss.logging\"/>");
      result.add("    <module name=\"org.jboss.threads\"/>");
      result.add("  </dependencies>");
      result.add("</module>");

      return result;
   }

   /**
    * Get the implementation definiton
    * @param version The version
    * @return The value
    */
   public static List<String> getImplementation(String version)
   {
      List<String> result = new ArrayList<String>();

      for (String s : HEADER)
      {
         result.add(s);
      }

      result.add("");

      result.add("<module xmlns=\"urn:jboss:module:1.0\" name=\"org.jboss.ironjacamar.impl\">");
      result.add("  <resources>");
      result.add("    <resource-root path=\"ironjacamar-common-impl-" + version + ".jar\"/>");
      result.add("    <resource-root path=\"ironjacamar-core-impl-" + version + ".jar\"/>");
      result.add("    <resource-root path=\"ironjacamar-deployers-common-" + version + ".jar\"/>");
      result.add("    <resource-root path=\"ironjacamar-validator-" + version + ".jar\"/>");
      result.add("  </resources>");

      result.add("");
  
      result.add("  <dependencies>");
      result.add("    <module name=\"javax.api\"/>");
      result.add("    <module name=\"javax.resource.api\"/>");
      result.add("    <module name=\"javax.security.auth.message.api\"/>");
      result.add("    <module name=\"javax.validation.api\"/>");
      result.add("    <module name=\"javax.xml.stream.api\"/>");
      result.add("    <module name=\"org.hibernate.validator\"/>");
      result.add("    <module name=\"org.jboss.as.naming\"/>");
      result.add("    <module name=\"org.jboss.as.transactions\"/>");
      result.add("    <module name=\"org.jboss.common-core\"/>");
      result.add("    <module name=\"org.jboss.ironjacamar.api\"/>");
      result.add("    <module name=\"org.jboss.jboss-transaction-spi\"/>");
      result.add("    <module name=\"org.jboss.logging\"/>");
      result.add("    <module name=\"org.jboss.threads\"/>");
      result.add("    <module name=\"org.picketbox\"/>");
      result.add("  </dependencies>");
      result.add("</module>");

      return result;
   }

   /**
    * Get the JDBC definiton
    * @param version The version
    * @return The value
    */
   public static List<String> getJdbc(String version)
   {
      List<String> result = new ArrayList<String>();

      for (String s : HEADER)
      {
         result.add(s);
      }

      result.add("");

      result.add("<module xmlns=\"urn:jboss:module:1.0\" name=\"org.jboss.ironjacamar.jdbcadapters\">");
      result.add("  <resources>");
      result.add("    <resource-root path=\"ironjacamar-jdbc-" + version + ".jar\"/>");
      result.add("  </resources>");

      result.add("");
  
      result.add("  <dependencies>");
      result.add("    <module name=\"javax.api\"/>");
      result.add("    <module name=\"javax.resource.api\"/>");
      result.add("    <module name=\"javax.validation.api\"/>");
      result.add("    <module name=\"javax.xml.stream.api\"/>");
      result.add("    <module name=\"org.hibernate.validator\"/>");
      result.add("    <module name=\"org.jboss.as.naming\"/>");
      result.add("    <module name=\"org.jboss.as.transactions\"/>");
      result.add("    <module name=\"org.jboss.common-core\"/>");
      result.add("    <module name=\"org.jboss.ironjacamar.api\"/>");
      result.add("    <module name=\"org.jboss.jboss-transaction-spi\"/>");
      result.add("    <module name=\"org.jboss.logging\"/>");
      result.add("    <module name=\"org.jboss.threads\"/>");
      result.add("    <module name=\"org.picketbox\"/>");
      result.add("  </dependencies>");
      result.add("</module>");

      return result;
   }
}
