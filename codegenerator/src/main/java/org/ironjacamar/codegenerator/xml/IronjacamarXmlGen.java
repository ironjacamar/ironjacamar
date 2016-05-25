/*
 *  IronJacamar, a Java EE Connector Architecture implementation
 *  Copyright 2016, Red Hat Inc, and individual contributors
 *  as indicated by the @author tags. See the copyright.txt file in the
 *  distribution for a full listing of individual contributors.
 *
 *  This is free software; you can redistribute it and/or modify it
 *  under the terms of the Eclipse Public License 1.0 as
 *  published by the Free Software Foundation.
 *
 *  This software is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the Eclipse
 *  Public License for more details.
 *
 *  You should have received a copy of the Eclipse Public License
 *  along with this software; if not, write to the Free
 *  Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 *  02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.ironjacamar.codegenerator.xml;

import org.ironjacamar.codegenerator.ConfigPropType;
import org.ironjacamar.codegenerator.Definition;
import org.ironjacamar.codegenerator.SimpleTemplate;
import org.ironjacamar.codegenerator.Template;
import org.ironjacamar.codegenerator.Utils;

import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An ironjacamar.xml generator
 *
 * @author Jeff Zhang
 */
public class IronjacamarXmlGen extends AbstractXmlGen
{
   @Override
   public void writeXmlBody(Definition def, Writer out) throws IOException
   {
      out.write("<!--");
      writeEol(out);
      writeHeader(def, out);
      out.write("-->");
      writeEol(out);
      writeEol(out);

      URL buildFile = IronjacamarXmlGen.class.getResource("/ironjacamar.xml.template");
      String buildString = Utils.readFileIntoString(buildFile);

      StringBuilder strRaProps = new StringBuilder();
      List<ConfigPropType> raPropsList = def.getRaConfigProps();
      if (def.isUseRa())
         getPropsString(strRaProps, raPropsList, 2);

      StringBuilder strMcf = new StringBuilder();
      strMcf.append("  <connection-definitions>\n");
      for (int num = 0; num < def.getMcfDefs().size(); num++)
      {
         strMcf.append("    <connection-definition class-name=\"");
         strMcf.append(def.getRaPackage());
         strMcf.append(".");
         strMcf.append(def.getMcfDefs().get(num).getMcfClass());
         strMcf.append("\" ");

         if (def.getMcfDefs().get(num).isUseCciConnection())
         {
            strMcf.append("jndi-name=\"java:/eis/");
            strMcf.append(def.getMcfDefs().get(num).getCciConnFactoryClass());
            strMcf.append("\" ");
            strMcf.append("id=\"").append(def.getMcfDefs().get(num).getCciConnFactoryClass());
         }
         else
         {
            strMcf.append("jndi-name=\"java:/eis/");
            strMcf.append(def.getMcfDefs().get(num).getCfInterfaceClass());
            strMcf.append("\" ");
            strMcf.append("id=\"").append(def.getMcfDefs().get(num).getCfInterfaceClass());
         }
         strMcf.append("\">\n");

         StringBuilder strMcfProps = new StringBuilder();
         List<ConfigPropType> mcfPropsList = def.getMcfDefs().get(num).getMcfConfigProps();
         getPropsString(strMcfProps, mcfPropsList, 6);
         strMcf.append(strMcfProps.toString());

         if (def.getSupportTransaction().endsWith("XATransaction"))
         {
            strMcf.append("      <recovery>\n");
            strMcf.append("        <recovery-credential>\n");
            strMcf.append("          <security-domain>DefaultSecurityDomain</security-domain>\n");
            strMcf.append("        </recovery-credential>\n");
            strMcf.append("      </recovery>\n");
         }
         strMcf.append("    </connection-definition>\n");
      }
      strMcf.append("  </connection-definitions>\n");

      StringBuilder strAo = new StringBuilder();
      if (def.isGenAdminObject())
      {
         strAo.append("  <admin-objects>\n");
         for (int i = 0; i < def.getAdminObjects().size(); i++)
         {
            strAo.append("    <admin-object class-name=\"");
            strAo.append(def.getRaPackage());
            strAo.append(".");
            strAo.append(def.getAdminObjects().get(i).getAdminObjectClass());
            strAo.append("\" jndi-name=\"java:/eis/ao/");
            strAo.append(def.getAdminObjects().get(i).getAdminObjectInterface());
            strAo.append("\">\n");
            getPropsString(strAo, def.getAdminObjects().get(i).getAoConfigProps(), 6);
            strAo.append("    </admin-object>\n");
         }
         strAo.append("  </admin-objects>\n");
      }

      Map<String, String> map = new HashMap<>();
      map.put("ra.props", strRaProps.toString());
      map.put("transaction", def.getSupportTransaction());

      map.put("mcfs", strMcf.toString());
      map.put("adminobjects", strAo.toString());
      Template template = new SimpleTemplate(buildString);
      template.process(map, out);
   }

   /**
    * generate properties String
    *
    * @param strProps  the string property
    * @param propsList the properties list
    * @param indent    how much indent
    */
   private void getPropsString(StringBuilder strProps, List<ConfigPropType> propsList, int indent)
   {
      for (ConfigPropType props : propsList)
      {
         for (int i = 0; i < indent; i++)
            strProps.append(" ");
         strProps.append("<config-property name=\"");
         strProps.append(props.getName());
         strProps.append("\">");
         strProps.append(props.getValue());
         strProps.append("</config-property>");
         strProps.append("\n");
      }
   }
}
