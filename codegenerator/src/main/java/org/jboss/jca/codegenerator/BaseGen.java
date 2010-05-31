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
package org.jboss.jca.codegenerator;

import java.io.IOException;
import java.io.Writer;
import java.net.URL;

/**
 * Base Generation class.
 * 
 * @author Jeff Zhang
 * @version $Revision: $
 */
public class BaseGen
{
   /**
    * Output eol 
    * @param out Writer
    * @throws IOException ioException
    */
   void writeEol(Writer out) throws IOException
   {
      out.write("\n");
   }
   
   /**
    * Output space
    * @param out Writer
    * @param indent space number
    * @throws IOException ioException
    */
   void writeIndent(Writer out, int indent) throws IOException
   {
      for (int i = 0; i < indent; i++)
         out.write("   ");      
   }

   /**
    * Output class head, for example license
    * @param def definition
    * @param out Writer
    * @throws IOException ioException
    */
   void writeheader(Definition def, Writer out) throws IOException
   {
      URL headerFile = BaseGen.class.getResource("/header.template");
      String headerString = Utils.readFileIntoString(headerFile);
      out.write(headerString);
      writeEol(out);
   }
}
