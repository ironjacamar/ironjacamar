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
 * A BuildGradleGen.
 *
 * @author Jeff Zhang
 * @version $Revision: $
 */
public class BuildGradleGen extends AbstractXmlGen
{
   @Override
   public void writeXmlBody(Definition def, Writer out) throws IOException
   {
      URL buildFile = BuildIvyXmlGen.class.getResource("/build.gradle.template");
      String buildString = Utils.readFileIntoString(buildFile);

      Map<String, String> map = new HashMap<>();
      map.put("gradle.package.name", def.getRaPackage());

      Template template = new SimpleTemplate(buildString);
      template.process(map, out);
   }
}
