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
package org.ironjacamar.codegenerator;

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
    *
    * @param out Writer
    * @throws IOException ioException
    */
   public void writeEol(Writer out) throws IOException
   {
      out.write("\n");
   }

   /**
    * Output space
    *
    * @param out    Writer
    * @param indent space number
    * @throws IOException ioException
    */
   public void writeIndent(Writer out, int indent) throws IOException
   {
      for (int i = 0; i < indent; i++)
         out.write("   ");
   }

   /**
    * Output space
    *
    * @param out    Writer
    * @param indent space number
    * @param line line to write
    * @throws IOException ioException
    */
   public void writeWithIndent(Writer out, int indent, String line) throws IOException
   {
      for (int i = 0; i < indent; i++)
         out.write("   ");
      out.write(line);
   }

   /**
    * Output class head, for example license
    *
    * @param def definition
    * @param out Writer
    * @throws IOException ioException
    */
   public void writeHeader(Definition def, Writer out) throws IOException
   {
      URL headerFile = BaseGen.class.getResource("/header.template");
      String headerString = Utils.readFileIntoString(headerFile);
      out.write(headerString);
      writeEol(out);
   }
}
