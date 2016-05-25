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

import org.ironjacamar.codegenerator.BasicType;
import org.ironjacamar.codegenerator.Definition;
import org.ironjacamar.codegenerator.McfDef;
import org.ironjacamar.codegenerator.MethodForConnection;
import org.ironjacamar.codegenerator.MethodParam;

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
    *
    * @param def definition
    * @param out Writer
    * @throws IOException ioException
    */
   @Override
   public void writeClassBody(Definition def, Writer out) throws IOException
   {
      int indent = 1;
      out.write("@RunWith(IronJacamar.class)\n");
      out.write("@SuppressWarnings(\"rawtypes\")\n");
      out.write("public class " + getClassName(def));
      writeLeftCurlyBracket(out, 0);
      writeWithIndent(out, indent, "private static String deploymentName = \"" + getClassName(def) + "\";\n\n");

      writeResource(def, out, indent);
      writeDeployment(def, out, indent);
      writeActivation(def, out, indent);
      for (int num = 0; num < def.getMcfDefs().size(); num++)
      {
         if (def.getMcfDefs().get(num).isDefineMethodInConnection())
         {
            writeTestMethod(def, out, indent, num + 1);
         }
         else
         {
            writeTestBasic(def, out, indent, num + 1);
         }
      }

      writeRightCurlyBracket(out, 0);
   }

   /**
    * Output class import
    *
    * @param def definition
    * @param out Writer
    * @throws IOException ioException
    */
   @Override
   public void writeImport(Definition def, Writer out) throws IOException
   {
      out.write("package " + def.getRaPackage() + ";\n\n");
      out.write("import org.ironjacamar.embedded.Deployment;\n");
      out.write("import org.ironjacamar.embedded.dsl.resourceadapters20.api.ConnectionDefinitionsType;\n");
      out.write("import org.ironjacamar.embedded.dsl.resourceadapters20.api.ResourceAdapterType;\n");
      out.write("import org.ironjacamar.embedded.dsl.resourceadapters20.api.ResourceAdaptersDescriptor;\n");
      out.write("import org.ironjacamar.embedded.junit4.IronJacamar;\n");
      writeEol(out);
      out.write("import javax.annotation.Resource;\n");
      writeEol(out);
      out.write("import org.jboss.shrinkwrap.api.ShrinkWrap;\n");
      out.write("import org.jboss.shrinkwrap.api.asset.StringAsset;\n");
      out.write("import org.jboss.shrinkwrap.api.spec.JavaArchive;\n");
      out.write("import org.jboss.shrinkwrap.api.spec.ResourceAdapterArchive;\n");
      out.write("import org.jboss.shrinkwrap.descriptor.api.Descriptors;\n");
      writeEol(out);
      out.write("import org.junit.Test;\n");
      out.write("import org.junit.runner.RunWith;\n");
      writeEol(out);
      out.write("import static org.junit.Assert.assertNotNull;\n");


   }

   /**
    * get this class name
    *
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
    *
    * @param def    definition
    * @param out    Writer
    * @param indent space number
    * @throws IOException ioException
    */
   private void writeDeployment(Definition def, Writer out, int indent) throws IOException
   {
      writeWithIndent(out, indent, "/**\n");
      writeWithIndent(out, indent, " * Define the deployment\n");
      writeWithIndent(out, indent, " *\n");
      writeWithIndent(out, indent, " * @return The deployment archive\n");
      writeWithIndent(out, indent, " */\n");

      writeWithIndent(out, indent, "@Deployment(order = 1)\n");
      writeWithIndent(out, indent, "public static ResourceAdapterArchive createDeployment()");
      writeLeftCurlyBracket(out, indent);
      writeWithIndent(out, indent + 1, "org.jboss.shrinkwrap.descriptor.api.connector" + def.getVersionNoDot() +
            ".ConnectorDescriptor raXml = Descriptors\n");
      writeWithIndent(out, indent + 2, ".create(org.jboss.shrinkwrap.descriptor.api.connector" + def.getVersionNoDot()
            + " .ConnectorDescriptor.class, " + "\"ra.xml\").version(\"" + def.getVersion() + "\");\n");

      writeWithIndent(out, indent + 1,
            "org.jboss.shrinkwrap.descriptor.api.connector" + def.getVersionNoDot() + ".ResourceadapterType rt = "
                      + "raXml.getOrCreateResourceadapter()" + (def.isUseRa() ? "" : ";") + "\n");
      if (def.isUseRa())
         writeWithIndent(out, indent + 2, ".resourceadapterClass(" + def.getRaClass() + ".class.getName());\n");
      writeWithIndent(out, indent + 1, "org.jboss.shrinkwrap.descriptor.api.connector" + def.getVersionNoDot() +
            ".OutboundResourceadapterType ort = rt\n");
      writeWithIndent(out, indent + 2,
            ".getOrCreateOutboundResourceadapter().transactionSupport(\"" + def.getSupportTransaction() + "\")."
                  + "reauthenticationSupport(false);\n");
      for (McfDef mcfDef : def.getMcfDefs())
      {
         writeWithIndent(out, indent + 1, "ort.createConnectionDefinition()\n");
         writeWithIndent(out, indent + 2,
               ".managedconnectionfactoryClass(" + mcfDef.getMcfClass() + ".class.getName())\n");
         writeWithIndent(out, indent + 2, ".connectionfactoryInterface(" + mcfDef.getCfInterfaceClass() +
               ".class.getName())\n");
         if (mcfDef.isUseCciConnection())
            writeWithIndent(out, indent + 2,
                  ".connectionfactoryImplClass(" + mcfDef.getCciConnFactoryClass() + ".class.getName())\n");
         else
            writeWithIndent(out, indent + 2,
                  ".connectionfactoryImplClass(" + mcfDef.getCfClass() + ".class.getName())\n");
         writeWithIndent(out, indent + 2,
               ".connectionInterface(" + mcfDef.getConnInterfaceClass() + ".class.getName())\n");
         if (mcfDef.isUseCciConnection())
            writeWithIndent(out, indent + 2,
                  ".connectionImplClass(" + mcfDef.getCciConnClass() + ".class.getName());\n");
         else
            writeWithIndent(out, indent + 2,
                  ".connectionImplClass(" + mcfDef.getConnImplClass() + ".class.getName());\n");

      }

      writeWithIndent(out, indent + 1, "ResourceAdapterArchive raa = ShrinkWrap.create(ResourceAdapterArchive.class, "
            + "deploymentName + \".rar\");\n");

      writeWithIndent(out, indent + 1,
            "JavaArchive ja = ShrinkWrap.create(JavaArchive.class, deploymentName + \".rar\");\n");
      if (def.getMcfDefs().size() > 0)
      {
         writeWithIndent(out, indent + 1, "ja.addPackages(true, " +
                         def.getMcfDefs().get(0).getMcfClass() + ".class.getPackage());\n");
      }
      else
      {
         writeWithIndent(out, indent + 1, "ja.addPackages(true, " + def.getRaClass() + ".class.getPackage());\n");
      }

      writeWithIndent(out, indent + 1, "raa.addAsLibrary(ja);\n");
      writeWithIndent(out, indent + 1,
            "raa.addAsManifestResource(new StringAsset(raXml.exportAsString()), \"ra.xml\");\n");

      writeWithIndent(out, indent + 1, "return raa;\n");

      writeRightCurlyBracket(out, indent);
      writeEol(out);
   }

   /**
    * Output create activation method
    *
    * @param def    definition
    * @param out    Writer
    * @param indent space number
    * @throws IOException ioException
    */
   private void writeActivation(Definition def, Writer out, int indent) throws IOException
   {
      writeWithIndent(out, indent, "/**\n");
      writeWithIndent(out, indent, " * The activation\n");
      writeWithIndent(out, indent, " * @throws Throwable In case of an error\n");
      writeWithIndent(out, indent, " */\n");
      writeWithIndent(out, indent, "@Deployment(order = 2)\n");
      writeWithIndent(out, indent, "private ResourceAdaptersDescriptor createActivation() throws Throwable");
      writeLeftCurlyBracket(out, indent);
      indent++;
      writeWithIndent(out, indent,
            "ResourceAdaptersDescriptor dashRaXml = Descriptors.create("
                  + "ResourceAdaptersDescriptor.class, deploymentName + \"-ra.xml\");\n");

      writeWithIndent(out, indent,
            "ResourceAdapterType dashRaXmlRt = dashRaXml.createResourceAdapter().archive("
                  + "deploymentName + \".rar\");\n");

      writeWithIndent(out, indent, "String tsl = \"" + def.getSupportTransaction() + "\";\n");

      writeWithIndent(out, indent, "dashRaXmlRt.transactionSupport(tsl);\n");
      writeWithIndent(out, indent,
            "ConnectionDefinitionsType dashRaXmlCdst = dashRaXmlRt.getOrCreateConnectionDefinitions();\n");
      int count = 0;
      for (McfDef mcfDef : def.getMcfDefs())
      {
         count++;
         writeWithIndent(out, indent,
               "org.ironjacamar.embedded.dsl.resourceadapters20.api.ConnectionDefinitionType dashRaXmlCdt" + count
                     + "= dashRaXmlCdst\n");
         if (mcfDef.isUseCciConnection())
         {
            writeWithIndent(out, indent + 1, ".createConnectionDefinition().className("
                  + mcfDef.getCciConnFactoryClass() + ".class.getName())\n");
            writeWithIndent(out, indent + 1, ".jndiName(\"java:/eis/" + mcfDef.getCciConnFactoryClass() + "\").id(\""
                  + mcfDef.getCciConnFactoryClass() + "\");\n");
         }
         else
         {
            writeWithIndent(out, indent + 1, ".createConnectionDefinition().className("
                  + mcfDef.getCfInterfaceClass() + ".class.getName())\n");
            writeWithIndent(out, indent + 1, ".jndiName(\"java:/eis/" + mcfDef.getCfInterfaceClass()
                  + "\").id(\"" + mcfDef.getCfInterfaceClass() + "\");\n");
         }


         writeWithIndent(out, indent, "if (tsl == null ||\n");
         writeWithIndent(out, indent + 1, "tsl.equals(\"NoTransaction\") ||\n");
         writeWithIndent(out, indent + 1, "tsl.equals(\"LocalTransaction\"))");
         writeLeftCurlyBracket(out, indent);
         writeWithIndent(out, indent + 1,
               "dashRaXmlCdt" + count
                     + ".getOrCreatePool().minPoolSize(0).initialPoolSize(0).maxPoolSize(10);\n");
         writeRightCurlyBracket(out, indent);
         writeWithIndent(out, indent, "else");
         writeLeftCurlyBracket(out, indent);
         writeWithIndent(out, indent + 1,
               "dashRaXmlCdt" + count
                     + ".getOrCreateXaPool().minPoolSize(0).initialPoolSize(0).maxPoolSize(10);\n");
         writeRightCurlyBracket(out, indent);

      }
      writeWithIndent(out, indent, "return dashRaXml;\n");
      writeRightCurlyBracket(out, indent - 1);
      writeEol(out);

   }

   /**
    * Output resource for conection factory
    *
    * @param def    definition
    * @param out    Writer
    * @param indent space number
    * @throws IOException ioException
    */
   private void writeResource(Definition def, Writer out, int indent) throws IOException
   {
      for (int num = 0; num < def.getMcfDefs().size(); num++)
      {
         writeWithIndent(out, indent, "/** Resource */\n");

         if (def.getMcfDefs().get(num).isUseCciConnection())
         {
            writeWithIndent(out, indent, "@Resource(mappedName = \"java:/eis/" +
                  def.getMcfDefs().get(num).getCciConnFactoryClass() + "\")\n");
         }
         else
         {
            writeWithIndent(out, indent,
                  "@Resource(mappedName = \"java:/eis/" + def.getMcfDefs().get(num).getCfInterfaceClass() + "\")\n");
         }
         if (def.getMcfDefs().get(num).isUseCciConnection())
            writeWithIndent(out, indent, "private javax.resource.cci.ConnectionFactory");
         else
            writeWithIndent(out, indent, "private " + def.getMcfDefs().get(num).getCfInterfaceClass());
         out.write(" connectionFactory" + (num + 1) + ";\n\n");
      }
   }

   /**
    * Output test basic method
    *
    * @param def    definition
    * @param out    Writer
    * @param indent space number
    * @param num    number of mcf
    * @throws IOException ioException
    */
   private void writeTestBasic(Definition def, Writer out, int indent, int num) throws IOException
   {
      writeWithIndent(out, indent, "/**\n");
      writeWithIndent(out, indent, " * Test getConnection\n");
      writeWithIndent(out, indent, " *\n");
      writeWithIndent(out, indent, " * @exception Throwable Thrown if case of an error\n");
      writeWithIndent(out, indent, " */\n");

      writeWithIndent(out, indent, "@Test\n");
      writeWithIndent(out, indent, "public void testGetConnection" + num + "() throws Throwable");
      writeLeftCurlyBracket(out, indent);

      writeWithIndent(out, indent + 1, "assertNotNull(connectionFactory" + num + ");\n");
      writeIndent(out, indent + 1);
      if (def.getMcfDefs().get(num - 1).isUseCciConnection())
         out.write("javax.resource.cci.Connection");
      else
         out.write(def.getMcfDefs().get(num - 1).getConnInterfaceClass());
      out.write(" connection" + num + " = connectionFactory" + num + ".getConnection();\n");
      writeWithIndent(out, indent + 1, "assertNotNull(connection" + num + ");\n");
      writeWithIndent(out, indent + 1, "connection" + num + ".close();");
      writeRightCurlyBracket(out, indent);
   }

   /**
    * Output test generated method
    *
    * @param def    definition
    * @param out    Writer
    * @param indent space number
    * @param num    number of mcf
    * @throws IOException ioException
    */
   private void writeTestMethod(Definition def, Writer out, int indent, int num) throws IOException
   {
      for (MethodForConnection method : def.getMcfDefs().get(num - 1).getMethods())
      {
         writeWithIndent(out, indent, "/**\n");
         writeWithIndent(out, indent, " * Test " + method.getMethodName() + "\n");
         writeWithIndent(out, indent, " *\n");
         writeWithIndent(out, indent, " * @exception Throwable Thrown if case of an error\n");
         writeWithIndent(out, indent, " */\n");

         writeWithIndent(out, indent, "@Test\n");
         writeWithIndent(out, indent, "public void test" + upcaseFirst(method.getMethodName()));
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
               if (type.indexOf(".") >= 0)
                  type = type.substring(type.lastIndexOf(".") + 1);
               out.write(type);
            }
         }
         out.write("() throws Throwable");
         writeLeftCurlyBracket(out, indent);

         writeWithIndent(out, indent + 1, "assertNotNull(connectionFactory" + num + ");\n");
         writeIndent(out, indent + 1);
         if (def.getMcfDefs().get(num - 1).isUseCciConnection())
            out.write("javax.resource.cci.Connection");
         else
            out.write(def.getMcfDefs().get(num - 1).getConnInterfaceClass());
         out.write(" connection" + num + " = connectionFactory" + num + ".getConnection();\n");
         writeWithIndent(out, indent + 1, "assertNotNull(connection" + num + ");\n");

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
         out.write(");\n");
         writeWithIndent(out, indent + 1, "connection" + num + ".close();");
         writeRightCurlyBracket(out, indent);
         writeEol(out);
      }
   }
}
