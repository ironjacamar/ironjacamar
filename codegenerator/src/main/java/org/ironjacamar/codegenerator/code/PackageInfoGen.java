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

package org.ironjacamar.codegenerator.code;

import org.ironjacamar.codegenerator.Definition;

import java.io.IOException;
import java.io.Writer;

/**
 * Created by maeste on 4/1/16.
 */
public class PackageInfoGen extends AbstractCodeGen
{

   private final String subDir;

   /**
    * Default constructor
    */
   public PackageInfoGen()
   {
      subDir = null;
   }

   /**
    * Constructor
    * @param subDir the subDir for this pachìkage-info
    */
   public PackageInfoGen(final String subDir)
   {
      this.subDir = subDir;
   }


   @Override
   public String getClassName(Definition def)
   {
      return null;
   }

   @Override
   public void writeImport(Definition def, Writer out) throws IOException
   {
      if (subDir != null)
      {
         out.write("/** The " + def.getRaPackage() + "." + subDir + " package */\n\n");
         out.write("package " + def.getRaPackage() + "." + subDir + ";\n\n");
      }
      else
      {
         out.write("/** The " + def.getRaPackage() + " package */\n\n");
         out.write("package " + def.getRaPackage() + ";\n\n");
      }

   }

   @Override
   public void writeClassBody(Definition def, Writer out) throws IOException
   {

   }

   @Override
   public void writeClassComment(Definition def, Writer out) throws IOException
   {

   }
}
