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

import org.jboss.jca.codegenerator.Definition;
import org.jboss.jca.codegenerator.SimpleTemplate;
import org.jboss.jca.codegenerator.Template;
import org.jboss.jca.codegenerator.Utils;

import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * A PomXmlGen.
 * 
 * @author Jeff Zhang
 * @version $Revision: $
 */
public class PomXmlGen extends AbstractXmlGen
{
   @Override
   public void writeXmlBody(Definition def, Writer out) throws IOException
   {
      URL buildFile = PomXmlGen.class.getResource("/pom.xml.template");
      String buildString = Utils.readFileIntoString(buildFile);

      String packageName = "";
      String moduleName;
      int pos = def.getRaPackage().lastIndexOf(".");
      if (pos > 0)
      {
         packageName = def.getRaPackage().substring(0, pos);
         moduleName = def.getRaPackage().substring(pos + 1);
      }
      else
      {
         moduleName = def.getRaPackage();
      }
      
      StringBuilder strStartGoal = new StringBuilder();
      if (def.isSupportEis())
      {
         strStartGoal.append("       <plugin>\n");
         strStartGoal.append("         <groupId>org.jboss.ironjacamar</groupId>\n");
         strStartGoal.append("         <artifactId>ironjacamar-test-eis</artifactId>\n");
         strStartGoal.append("         <version>${version.org.jboss.ironjacamar}</version>\n");
         strStartGoal.append("         <executions>\n");
         strStartGoal.append("           <execution>\n");
         strStartGoal.append("             <goals>\n");
         strStartGoal.append("               <goal>start</goal>\n");
         strStartGoal.append("             </goals>\n");
         strStartGoal.append("           </execution>\n");
         strStartGoal.append("         </executions>\n");
         strStartGoal.append("         <configuration>\n");
         strStartGoal.append("           <host>localhost</host>\n");
         strStartGoal.append("           <port>1400</port>\n");
         strStartGoal.append("           <handler>" + def.getRaPackage() + ".EchoHandler</handler>\n");
         strStartGoal.append("           <classpath>\n");
         strStartGoal.append("             <param>target/test-classes</param>\n");
         strStartGoal.append("           </classpath>\n");
         strStartGoal.append("         </configuration>\n");
         strStartGoal.append("       </plugin>\n");
      }
      
      Map<String, String> map = new HashMap<String, String>();
      map.put("pom.package.name", packageName);
      map.put("pom.module.name", moduleName);
      map.put("start.goal", strStartGoal.toString());
      
      Template template = new SimpleTemplate(buildString);
      template.process(map, out);
   }
}
