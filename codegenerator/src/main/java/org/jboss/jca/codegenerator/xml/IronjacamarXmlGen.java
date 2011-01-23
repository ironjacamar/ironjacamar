/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.jca.codegenerator.xml;

import org.jboss.jca.codegenerator.ConfigPropType;
import org.jboss.jca.codegenerator.Definition;
import org.jboss.jca.codegenerator.SimpleTemplate;
import org.jboss.jca.codegenerator.Template;
import org.jboss.jca.codegenerator.Utils;

import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A IvyXmlGen.
 * 
 * @author Jeff Zhang
 * @version $Revision: $
 */
public class IronjacamarXmlGen extends AbstractXmlGen
{
   @Override
   public void writeXmlBody(Definition def, Writer out) throws IOException
   {
      out.write("<!--");
      writeEol(out);
      writeheader(def, out);
      out.write("-->");
      writeEol(out);
      writeEol(out);
      
      URL buildFile = IronjacamarXmlGen.class.getResource("/ironjacamar.xml.template");
      String buildString = Utils.readFileIntoString(buildFile);
      
      StringBuilder strRaProps = new StringBuilder();
      List<ConfigPropType> raPropsList = def.getRaConfigProps();
      getPropsString(strRaProps, raPropsList, 2);
      
      StringBuilder strMcfProps = new StringBuilder();
      List<ConfigPropType> mcfPropsList = def.getMcfConfigProps();
      getPropsString(strMcfProps, mcfPropsList, 6);
      
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
            strAo.append("\" jndi-name=\"java:/");
            strAo.append(def.getAdminObjects().get(i).getAdminObjectClass());
            strAo.append("\">\n");
            getPropsString(strAo, def.getAdminObjects().get(i).getAoConfigProps(), 6);
            strAo.append("    </admin-object>\n");
         }
         strAo.append("  </admin-objects>\n");
      }
      
      Map<String, String> map = new HashMap<String, String>();
      map.put("ra.props", strRaProps.toString());
      map.put("transaction", def.getSupportTransaction());
      map.put("mcf.class", def.getRaPackage() + "." + def.getMcfClass());
      map.put("jndi.name", "java:/eis/" + def.getDefaultValue());
      map.put("pool.name", def.getDefaultValue());
      map.put("mcf.props", strMcfProps.toString());
      map.put("adminobjects", strAo.toString());
      Template template = new SimpleTemplate(buildString);
      template.process(map, out);
   }

   /**
    * generate properties String
    * 
    * @param strProps
    * @param propsList
    * @param indent
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
