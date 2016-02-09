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

import org.ironjacamar.codegenerator.Definition;
import org.ironjacamar.codegenerator.SimpleTemplate;
import org.ironjacamar.codegenerator.Template;
import org.ironjacamar.codegenerator.Utils;

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

      String raxml = def.isUseAnnotation() ? "" : "<raXmlFile>src/main/resources/META-INF/ra.xml</raXmlFile>";

      StringBuilder strStartGoal = new StringBuilder();
      StringBuilder strStopGoal = new StringBuilder();
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
         strStartGoal.append("           <handler>").append(def.getRaPackage()).append(".")
               .append(def.getDefaultValue()).append("Handler</handler>\n");
         strStartGoal.append("           <classpath>\n");
         strStartGoal.append("             <param>target/test-classes</param>\n");
         strStartGoal.append("           </classpath>\n");
         strStartGoal.append("         </configuration>\n");
         strStartGoal.append("       </plugin>\n");

         strStopGoal.append("       <plugin>\n");
         strStopGoal.append("         <groupId>org.jboss.ironjacamar</groupId>\n");
         strStopGoal.append("         <artifactId>ironjacamar-test-eis</artifactId>\n");
         strStopGoal.append("         <version>${version.org.jboss.ironjacamar}</version>\n");
         strStopGoal.append("         <executions>\n");
         strStopGoal.append("           <execution>\n");
         strStopGoal.append("             <goals>\n");
         strStopGoal.append("               <goal>stop</goal>\n");
         strStopGoal.append("             </goals>\n");
         strStopGoal.append("           </execution>\n");
         strStopGoal.append("         </executions>\n");
         strStopGoal.append("         <configuration>\n");
         strStopGoal.append("           <host>localhost</host>\n");
         strStopGoal.append("           <port>1400</port>\n");
         strStopGoal.append("         </configuration>\n");
         strStopGoal.append("       </plugin>\n");
      }

      Map<String, String> map = new HashMap<>();
      map.put("pom.package.name", packageName);
      map.put("pom.module.name", moduleName);
      map.put("raxml.file", raxml);
      map.put("start.goal", strStartGoal.toString());
      map.put("stop.goal", strStopGoal.toString());

      Template template = new SimpleTemplate(buildString);
      template.process(map, out);
   }
}
