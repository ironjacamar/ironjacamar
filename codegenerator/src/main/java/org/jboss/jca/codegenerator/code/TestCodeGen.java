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
package org.jboss.jca.codegenerator.code;

import org.jboss.jca.codegenerator.BasicType;
import org.jboss.jca.codegenerator.Definition;
import org.jboss.jca.codegenerator.MethodForConnection;
import org.jboss.jca.codegenerator.MethodParam;

import java.io.IOException;
import java.io.Writer;

/**
 * A Test CodeGen.
 * 
 * @author Jeff Zhang
 * @version $Revision: $
 */
public class TestCodeGen extends AbstractCodeGen
{

   /**
    * Output class
    * @param def definition
    * @param out Writer
    * @throws IOException ioException
    */
   @Override
   public void writeClassBody(Definition def, Writer out) throws IOException
   {
      int indent = 1;
      out.write("@RunWith(Arquillian.class)");
      writeEol(out);
      out.write("public class " + getClassName(def));
      writeLeftCurlyBracket(out, 0);
      writeIndent(out, indent);
      out.write("private static Logger log = Logger.getLogger(\"" + getClassName(def) + "\");");
      writeEol(out);
      writeEol(out);
      writeIndent(out, indent);
      out.write("private static String deploymentName = \"" + getClassName(def) + "\";");
      writeEol(out);
      writeEol(out);

      writeDeployment(def, out, indent);
      writeResource(def, out, indent);
      
      for (int num = 0; num < def.getMcfDefs().size(); num++)
      {
         if (def.getMcfDefs().get(num).isDefineMethodInConnection())
         {
            writeTestMethod(def, out, indent, num + 1);
         }
         else
            writeTestBasic(def, out, indent, num + 1);
      }

      writeRightCurlyBracket(out, 0);
   }
   
   /**
    * Output class import
    * @param def definition
    * @param out Writer
    * @throws IOException ioException
    */
   @Override
   public void writeImport(Definition def, Writer out) throws IOException
   {
      out.write("package " + def.getRaPackage() + ";");
      writeEol(out);
      writeEol(out);
      out.write("import java.util.UUID;");
      writeEol(out);
      out.write("import java.util.logging.Logger;");
      writeEol(out);
      writeEol(out);
      out.write("import javax.annotation.Resource;");
      writeEol(out);
      writeEol(out);
      out.write("import org.jboss.arquillian.container.test.api.Deployment;");
      writeEol(out);
      out.write("import org.jboss.arquillian.junit.Arquillian;");
      writeEol(out);
      writeEol(out);
      out.write("import org.jboss.shrinkwrap.api.ShrinkWrap;");
      writeEol(out);
      out.write("import org.jboss.shrinkwrap.api.spec.JavaArchive;");
      writeEol(out);
      out.write("import org.jboss.shrinkwrap.api.spec.ResourceAdapterArchive;");
      writeEol(out);
      writeEol(out);
      out.write("import org.junit.Test;");
      writeEol(out);
      out.write("import org.junit.runner.RunWith;");
      writeEol(out);
      out.write("import static org.junit.Assert.*;");
      writeEol(out);
      writeEol(out);

      out.write("import " + def.getRaPackage() + ".*;");
      writeEol(out);
      writeEol(out);
   }
   
   /**
    * get this class name
    * @param def definition
    * @return String class name
    */
   @Override
   public String getClassName(Definition def)
   {
      return "ConnectorTestCase";
   }
   
   /**
    * Output create deployment method
    * @param def definition
    * @param out Writer
    * @param indent space number
    * @throws IOException ioException
    */
   private void writeDeployment(Definition def, Writer out, int indent) throws IOException
   {
      writeIndent(out, indent);
      out.write("/**");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * Define the deployment");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" *");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * @return The deployment archive");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" */");
      writeEol(out);
      
      writeIndent(out, indent);
      out.write("@Deployment");
      writeEol(out);
      writeIndent(out, indent);
      out.write("public static ResourceAdapterArchive createDeployment()");
      writeLeftCurlyBracket(out, indent);

      writeIndent(out, indent + 1);
      out.write("ResourceAdapterArchive raa =");
      writeEol(out);
      writeIndent(out, indent + 2);
      out.write("ShrinkWrap.create(ResourceAdapterArchive.class, deploymentName + \".rar\");");
      writeEol(out);
      writeIndent(out, indent + 1);
      out.write("JavaArchive ja = ShrinkWrap.create(JavaArchive.class, UUID.randomUUID().toString() + \".jar\");");
      writeEol(out);
      writeIndent(out, indent + 1);
      out.write("ja.addClasses(");
      
      if (def.isUseRa())
      {
         out.write(def.getRaClass() + ".class, ");
      }
      for (int num = 0; num < def.getMcfDefs().size(); num++)
      {
         out.write(def.getMcfDefs().get(num).getMcfClass() + ".class, " + 
            def.getMcfDefs().get(num).getMcClass() + ".class, ");
         if (def.getMcfDefs().get(num).isUseCciConnection())
         {
            out.write(def.getMcfDefs().get(num).getCciConnFactoryClass() + ".class, " + 
               def.getMcfDefs().get(num).getCciConnFactoryClass() + ".class, " + 
               def.getMcfDefs().get(num).getConnMetaClass() + ".class, " + 
               def.getRaMetaClass() + ".class, " + 
               def.getMcfDefs().get(num).getConnSpecClass() + ".class");
         }
         else
         {
            out.write(def.getMcfDefs().get(num).getCfInterfaceClass() + ".class, " + 
               def.getMcfDefs().get(num).getCfClass() + ".class, " + 
               def.getMcfDefs().get(num).getConnInterfaceClass() + ".class, " + 
               def.getMcfDefs().get(num).getConnImplClass() + ".class");
         }
         if (num < def.getMcfDefs().size() - 1)
         {
            out.write(",");
            writeEol(out);
            writeIndent(out, indent + 2);
         }
      }
      
      out.write(");");
      writeEol(out);
      writeIndent(out, indent + 1);
      out.write("raa.addAsLibrary(ja);");
      writeEol(out);
      writeEol(out);
      if (!def.isUseAnnotation())
      {
         writeIndent(out, indent + 1);
         out.write("raa.addAsManifestResource(\"META-INF/ra.xml\", \"ra.xml\");");
         writeEol(out);
         writeEol(out);
      }
      writeIndent(out, indent + 1);
      out.write("raa.addAsManifestResource(\"META-INF/ironjacamar.xml\", \"ironjacamar.xml\");");
      writeEol(out);
      writeEol(out);
      writeIndent(out, indent + 1);
      out.write("return raa;");
      
      writeRightCurlyBracket(out, indent);
      writeEol(out);
   }
   
   /**
    * Output resource for conection factory
    * @param def definition
    * @param out Writer
    * @param indent space number
    * @throws IOException ioException
    */
   private void writeResource(Definition def, Writer out, int indent) throws IOException
   {
      for (int num = 0; num < def.getMcfDefs().size(); num++)
      {
         writeIndent(out, indent);
         out.write("/** Resource */");
         writeEol(out);
         writeIndent(out, indent);
         if (def.getMcfDefs().get(num).isUseCciConnection())
         {
            out.write("@Resource(mappedName = \"java:/eis/" + 
                      def.getMcfDefs().get(num).getCciConnFactoryClass() + "\")");
         }
         else
         {
            out.write("@Resource(mappedName = \"java:/eis/" + def.getMcfDefs().get(num).getCfInterfaceClass() + "\")");
         }
         writeEol(out);
         writeIndent(out, indent);
         if (def.getMcfDefs().get(num).isUseCciConnection())
            out.write("private javax.resource.cci.ConnectionFactory");
         else
            out.write("private " + def.getMcfDefs().get(num).getCfInterfaceClass());
         out.write(" connectionFactory" + (num + 1) + ";");
         writeEol(out);
         writeEol(out);
      }
   }
   /**
    * Output test basic method
    * @param def definition
    * @param out Writer
    * @param indent space number
    * @param num number of mcf
    * @throws IOException ioException
    */
   private void writeTestBasic(Definition def, Writer out, int indent, int num) throws IOException
   {
      writeIndent(out, indent);
      out.write("/**");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * Test getConnection");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" *");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * @exception Throwable Thrown if case of an error");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" */");
      writeEol(out);

      writeIndent(out, indent);
      out.write("@Test");
      writeEol(out);
      writeIndent(out, indent);
      out.write("public void testGetConnection" + num + "() throws Throwable");
      writeLeftCurlyBracket(out, indent);
      
      writeIndent(out, indent + 1);
      out.write("assertNotNull(connectionFactory" + num + ");");
      writeEol(out);
      writeIndent(out, indent + 1);
      if (def.getMcfDefs().get(num - 1).isUseCciConnection())
         out.write("javax.resource.cci.Connection");
      else
         out.write(def.getMcfDefs().get(num - 1).getConnInterfaceClass());
      out.write(" connection" + num + " = connectionFactory" + num + ".getConnection();");
      writeEol(out);
      writeIndent(out, indent + 1);
      out.write("assertNotNull(connection" + num + ");");
      writeEol(out);
      writeIndent(out, indent + 1);
      out.write("connection" + num + ".close();");
      writeRightCurlyBracket(out, indent);
   }
   
   /**
    * Output test generated method
    * @param def definition
    * @param out Writer
    * @param indent space number
    * @param num number of mcf
    * @throws IOException ioException
    */
   private void writeTestMethod(Definition def, Writer out, int indent, int num) throws IOException
   {
      for (MethodForConnection method : def.getMcfDefs().get(num - 1).getMethods())
      {
         writeIndent(out, indent);
         out.write("/**");
         writeEol(out);
         writeIndent(out, indent);
         out.write(" * Test " + method.getMethodName());
         writeEol(out);
         writeIndent(out, indent);
         out.write(" *");
         writeEol(out);
         writeIndent(out, indent);
         out.write(" * @exception Throwable Thrown if case of an error");
         writeEol(out);
         writeIndent(out, indent);
         out.write(" */");
         writeEol(out);
   
         writeIndent(out, indent);
         out.write("@Test");
         writeEol(out);
         writeIndent(out, indent);
         out.write("public void test" + upcaseFirst(method.getMethodName()));
         int paramSize = method.getParams().size();

         if (paramSize == 0)
            out.write("NoArg");
         else
         {
            for (int i = 0; i < paramSize; i++)
            {
               MethodParam param = method.getParams().get(i);
               out.write(upcaseFirst(param.getName()));
               String type = param.getType();
               if (type.indexOf(".") > 0)
                  type = type.substring(type.lastIndexOf(".") + 1);
               out.write(type);
            }
         }
         out.write("() throws Throwable");
         writeLeftCurlyBracket(out, indent);

         writeIndent(out, indent + 1);
         out.write("assertNotNull(connectionFactory" + num + ");");
         writeEol(out);
         writeIndent(out, indent + 1);
         if (def.getMcfDefs().get(num - 1).isUseCciConnection())
            out.write("javax.resource.cci.Connection");
         else
            out.write(def.getMcfDefs().get(num - 1).getConnInterfaceClass());
         out.write(" connection" + num + " = connectionFactory" + num + ".getConnection();");
         writeEol(out);
         writeIndent(out, indent + 1);
         out.write("assertNotNull(connection" + num + ");");
         writeEol(out);
         
         writeIndent(out, indent + 1);
         if (!method.getReturnType().equals("void"))
         {
            out.write(method.getReturnType() + " result = ");
         }
         out.write("connection" + num + "." + method.getMethodName() + "(");
         for (int i = 0; i < paramSize; i++)
         {
            MethodParam param = method.getParams().get(i);
            out.write(BasicType.defaultValue(param.getType()));
            if (i + 1 < paramSize)
               out.write(", ");
         }
         out.write(");");
         writeEol(out);
         writeIndent(out, indent + 1);
         out.write("connection" + num + ".close();");
         writeRightCurlyBracket(out, indent);
         writeEol(out);
      }
   }
}
