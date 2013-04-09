/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2010, Red Hat Inc, and individual contributors
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

import org.jboss.jca.codegenerator.Definition;
import org.jboss.jca.codegenerator.SimpleTemplate;
import org.jboss.jca.codegenerator.Template;
import org.jboss.jca.codegenerator.Utils;

import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * A BuildIvyXmlGen.
 * 
 * @author Jeff Zhang
 * @version $Revision: $
 */
public class BuildIvyXmlGen extends AbstractXmlGen
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
      
      URL buildFile = BuildIvyXmlGen.class.getResource("/build.ivy.xml.template");
      String buildString = Utils.readFileIntoString(buildFile);
      
      String defName = "jca-test";
      if (def.getRaPackage() != null && !def.getRaPackage().equals(""))
      {
         if (def.getRaPackage().indexOf('.') >= 0)
         {
            defName = def.getRaPackage().substring(def.getRaPackage().lastIndexOf('.') + 1);
         }
         else
            defName = def.getRaPackage();
      }
      
      StringBuilder strStartTask = new StringBuilder();
      if (def.isSupportEis())
      {
         strStartTask.append("    <start host=\"localhost\" port=\"1400\"\n");
         strStartTask.append("      handler=\"" + def.getRaPackage() + "." + 
            def.getDefaultValue() + "Handler\">\n");
         strStartTask.append("      <classpath>\n");
         strStartTask.append("        <pathelement location=\"${build.dir}/test\"/>\n");
         strStartTask.append("      </classpath>\n");
         strStartTask.append("    </start>\n");
      }
      StringBuilder strStopTask = new StringBuilder();
      if (def.isSupportEis())
      {
         strStopTask.append("    <stop host=\"localhost\" port=\"1400\"/>\n");
      }
      
      Map<String, String> map = new HashMap<String, String>();
      map.put("def.name", defName.toLowerCase(Locale.US));
      map.put("mbean.class", def.getRaPackage() + ".mbean." + def.getMbeanInterfaceClass());
      map.put("start.task", strStartTask.toString());
      map.put("stop.task", strStopTask.toString());
      
      Template template = new SimpleTemplate(buildString);
      template.process(map, out);
   }
}
