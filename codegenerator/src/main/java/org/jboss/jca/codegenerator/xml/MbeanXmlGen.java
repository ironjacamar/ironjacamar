/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2011, Red Hat Inc, and individual contributors
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
 * A MbeanXmlGen.
 * 
 * @author Jeff Zhang
 * @version $Revision: $
 */
public class MbeanXmlGen extends AbstractXmlGen
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

      URL buildFile = MbeanXmlGen.class.getResource("/mbean.xml.template");
      String buildString = Utils.readFileIntoString(buildFile);
      
      Map<String, String> map = new HashMap<String, String>();
      map.put("mbean.name", def.getDefaultValue());
      map.put("mbean.interface", def.getRaPackage() + ".mbean." + def.getMbeanInterfaceClass());
      map.put("mbean.impl", def.getRaPackage() + ".mbean." + def.getMbeanImplClass());
      
      Template template = new SimpleTemplate(buildString);
      template.process(map, out);
   }
}
