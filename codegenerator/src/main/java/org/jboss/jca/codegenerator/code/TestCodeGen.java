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
      out.write("private static Logger log = Logger.getLogger(" + getClassName(def) + ".class);");
      writeEol(out);
      writeIndent(out, indent);
      out.write("private static final String JNDI_PREFIX = \"java:/eis/\";");
      writeEol(out);
      writeIndent(out, indent);
      out.write("private static String deploymentName = null;");
      writeEol(out);

      writeDeployment(def, out, indent);
      
      if (def.isDefineMethodInConnection())
      {
         writeTestMethod(def, out, indent);
      }
      else
         writeTestBasic(def, out, indent);

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
      out.write("import javax.naming.Context;");
      writeEol(out);
      out.write("import javax.naming.InitialContext;");
      writeEol(out);
      out.write("import javax.naming.NamingException;");
      writeEol(out);
      out.write("import org.jboss.arquillian.api.Deployment;");
      writeEol(out);
      out.write("import org.jboss.arquillian.junit.Arquillian;");
      writeEol(out);
      out.write("import org.jboss.logging.Logger;");
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
      out.write("deploymentName = UUID.randomUUID().toString();");
      writeEol(out);
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
      out.write(def.getMcfClass() + ".class, " + def.getMcClass() + ".class, " + 
         def.getMcMetaClass() + ".class, " + def.getCmClass() + ".class, ");
      if (def.isUseCciConnection())
      {
         out.write(def.getCciConnFactoryClass() + ".class, " + def.getCciConnFactoryClass() + ".class, " + 
            def.getConnMetaClass() + ".class, " + def.getRaMetaClass() + ".class, " + 
            def.getConnSpecClass() + ".class");
      }
      else
      {
         out.write(def.getCfInterfaceClass() + ".class, " + def.getCfClass() + ".class, " + 
            def.getConnInterfaceClass() + ".class, " + def.getConnImplClass() + ".class");
      }
      
      out.write(");");
      writeEol(out);
      writeIndent(out, indent + 1);
      out.write("raa.addLibrary(ja);");
      writeEol(out);
      writeEol(out);
      if (!def.isUseAnnotation())
      {
         writeIndent(out, indent + 1);
         out.write("raa.addManifestResource(\"META-INF/ra.xml\", \"ra.xml\");");
         writeEol(out);
         writeEol(out);
      }
      writeIndent(out, indent + 1);
      out.write("return raa;");
      writeEol(out);
      
      writeRightCurlyBracket(out, indent);
      writeEol(out);
   }
   
   /**
    * Output test basic method
    * @param def definition
    * @param out Writer
    * @param indent space number
    * @throws IOException ioException
    */
   private void writeTestBasic(Definition def, Writer out, int indent) throws IOException
   {
      writeIndent(out, indent);
      out.write("/**");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * Test Basic");
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
      out.write("public void testBasic() throws Throwable");
      writeLeftCurlyBracket(out, indent);
      
      writeIndent(out, indent + 1);
      out.write("Context context = null;");
      writeEol(out);
      writeIndent(out, indent + 1);
      out.write("try");
      writeLeftCurlyBracket(out, indent + 1);
      writeIndent(out, indent + 2);
      out.write("context = new InitialContext();");
      writeEol(out);
      writeIndent(out, indent + 2);
      out.write("Object o = context.lookup(JNDI_PREFIX + deploymentName);");
      writeEol(out);
      writeIndent(out, indent + 2);
      out.write("assertNotNull(o);");
      writeRightCurlyBracket(out, indent + 1);
      writeIndent(out, indent + 1);
      out.write("catch (Throwable t)");
      writeLeftCurlyBracket(out, indent + 1);
      writeIndent(out, indent + 2);
      out.write("log.error(t.getMessage(), t);");
      writeEol(out);
      writeIndent(out, indent + 2);
      out.write("fail(t.getMessage());");
      writeRightCurlyBracket(out, indent + 1);
      writeIndent(out, indent + 1);
      out.write("finally");
      writeLeftCurlyBracket(out, indent + 1);
      writeIndent(out, indent + 2);
      out.write("if (context != null)");
      writeLeftCurlyBracket(out, indent + 2);
      writeIndent(out, indent + 3);
      out.write("try");
      writeLeftCurlyBracket(out, indent + 3);
      writeIndent(out, indent + 4);
      out.write("context.close();");
      writeRightCurlyBracket(out, indent + 3);
      writeIndent(out, indent + 3);
      out.write("catch (NamingException ne)");
      writeLeftCurlyBracket(out, indent + 3);
      writeIndent(out, indent + 4);
      out.write("// Ignore");
      writeRightCurlyBracket(out, indent + 3);
      writeRightCurlyBracket(out, indent + 2);
      writeRightCurlyBracket(out, indent + 1);
      writeRightCurlyBracket(out, indent);
   }
   
   /**
    * Output test generated method
    * @param def definition
    * @param out Writer
    * @param indent space number
    * @throws IOException ioException
    */
   private void writeTestMethod(Definition def, Writer out, int indent) throws IOException
   {
      for (MethodForConnection method : def.getMethods())
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
         out.write("public void test" + upcaseFirst(method.getMethodName()) + "() throws Throwable");
         writeLeftCurlyBracket(out, indent);
         
         writeIndent(out, indent + 1);
         out.write("Context context = null;");
         writeEol(out);
         writeIndent(out, indent + 1);
         out.write("try");
         writeLeftCurlyBracket(out, indent + 1);
         writeIndent(out, indent + 2);
         out.write("context = new InitialContext();");
         writeEol(out);
         writeIndent(out, indent + 2);
         out.write(def.getCfInterfaceClass() + " cf = (" + def.getCfInterfaceClass() + 
            ")context.lookup(JNDI_PREFIX + deploymentName);");
         writeEol(out);
         writeIndent(out, indent + 2);
         out.write("assertNotNull(cf);");
         writeEol(out);
         writeIndent(out, indent + 2);
         out.write(def.getConnInterfaceClass() + " c = cf.getConnection();");
         writeEol(out);
         writeIndent(out, indent + 2);
         out.write("assertNotNull(c);");
         writeEol(out);
         
         writeIndent(out, indent + 2);
         if (!method.getReturnType().equals("void"))
         {
            out.write(method.getReturnType() + " result = ");
         }
         out.write("c." + method.getMethodName() + "(");
         int paramSize = method.getParams().size();
         for (int i = 0; i < paramSize; i++)
         {
            MethodForConnection.Param param = method.getParams().get(i);
            out.write(BasicType.defaultValue(param.getType()));
            if (i + 1 < paramSize)
               out.write(", ");
         }
         out.write(");");
         writeEol(out);
         
         writeRightCurlyBracket(out, indent + 1);
         writeIndent(out, indent + 1);
         out.write("catch (Throwable t)");
         writeLeftCurlyBracket(out, indent + 1);
         writeIndent(out, indent + 2);
         out.write("log.error(t.getMessage(), t);");
         writeEol(out);
         writeIndent(out, indent + 2);
         out.write("fail(t.getMessage());");
         writeRightCurlyBracket(out, indent + 1);
         writeIndent(out, indent + 1);
         out.write("finally");
         writeLeftCurlyBracket(out, indent + 1);
         writeIndent(out, indent + 2);
         out.write("if (context != null)");
         writeLeftCurlyBracket(out, indent + 2);
         writeIndent(out, indent + 3);
         out.write("try");
         writeLeftCurlyBracket(out, indent + 3);
         writeIndent(out, indent + 4);
         out.write("context.close();");
         writeRightCurlyBracket(out, indent + 3);
         writeIndent(out, indent + 3);
         out.write("catch (NamingException ne)");
         writeLeftCurlyBracket(out, indent + 3);
         writeIndent(out, indent + 4);
         out.write("// Ignore");
         writeRightCurlyBracket(out, indent + 3);
         writeRightCurlyBracket(out, indent + 2);
         writeRightCurlyBracket(out, indent + 1);
         writeRightCurlyBracket(out, indent);
         writeEol(out);
      }
   }
}
