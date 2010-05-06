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

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;

/**
 * A JCA16AnnoProfile.
 * 
 * @author Jeff Zhang
 * @version $Revision: $
 */
public class JCA16AnnoProfile implements Profile
{

   /**
    * JCA16AnnoProfile
    */
   public JCA16AnnoProfile()
   {
   }
   
  
   /**
    * generate code
    * @param def Definition 
    * @param packageName the writer to output the text to.
    */
   @Override
   public void generate(Definition def, String packageName)
   {
      FileWriter fw = null;
      
      try
      {
         fw = Utils.createSrcFile(def.getRaClass() + ".java", packageName, def.getOutputDir());

         writeDown(def, fw);
         fw.flush();
         fw.close();
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
   }

   /**
    * Output generation code
    * @param def definition
    * @param out Writer
    * @throws IOException ioException
    */
   public void writeDown(Definition def, Writer out) throws IOException
   {
      writeheader(def, out);
      writeImport(def, out);
      writeClassComment(def, out);
      writeClassBody(def, out);
      
   }

   /**
    * Output class head, for example license
    * @param def definition
    * @param out Writer
    * @throws IOException ioException
    */
   private void writeheader(Definition def, Writer out) throws IOException
   {
      URL headerFile = JCA16AnnoProfile.class.getResource("/header.template");
      String headerString = Utils.readFileIntoString(headerFile);
      out.write(headerString);
      writeEol(out);
   }

   /**
    * Output class comment
    * @param def definition
    * @param out Writer
    * @throws IOException ioException
    */
   private void writeClassComment(Definition def, Writer out) throws IOException
   {
      out.write("/**");
      writeEol(out);
      out.write(" * " + def.getRaClass());
      writeEol(out);
      out.write(" * @version $Revision: $");
      writeEol(out);
      out.write(" */");
      writeEol(out);
   }

   /**
    * Output class import
    * @param def definition
    * @param out Writer
    * @throws IOException ioException
    */
   private void writeImport(Definition def, Writer out) throws IOException
   {
      out.write("package " + def.getRaPackage() + ";");
      writeEol(out);
      writeEol(out);
      out.write("import javax.resource.ResourceException;");
      writeEol(out);
      out.write("import javax.resource.spi.ActivationSpec;");
      writeEol(out);
      out.write("import javax.resource.spi.BootstrapContext;");
      writeEol(out);
      out.write("import javax.resource.spi.ConfigProperty;");
      writeEol(out);
      out.write("import javax.resource.spi.Connector;");
      writeEol(out);
      out.write("import javax.resource.spi.ResourceAdapter;");
      writeEol(out);
      out.write("import javax.resource.spi.ResourceAdapterInternalException;");
      writeEol(out);
      out.write("import javax.resource.spi.endpoint.MessageEndpointFactory;");
      writeEol(out);
      writeEol(out);
      out.write("import javax.transaction.xa.XAResource;");
      writeEol(out);
      writeEol(out);
      out.write("import org.jboss.logging.Logger;");
      writeEol(out);
      writeEol(out);
   }

   /**
    * Output eol 
    * @param out Writer
    * @throws IOException ioException
    */
   private void writeEol(Writer out) throws IOException
   {
      out.write("\n");
   }
   
   /**
    * Output left curly bracket
    * @param out Writer
    */
   private void writeLeftCurlyBracket(Writer out, int indent) throws IOException
   {
      writeEol(out);
      writeIndent(out, indent);
      out.write("{");
      writeEol(out);
   }

   /**
    * Output right curly bracket
    * @param out Writer
    */
   private void writeRightCurlyBracket(Writer out, int indent) throws IOException
   {
      writeEol(out);
      writeIndent(out, indent);
      out.write("}");
      writeEol(out);
   }
   
   /**
    * Output space
    * @param out Writer
    */
   private void writeIndent(Writer out, int indent) throws IOException
   {
      for (int i = 0; i < indent; i++)
         out.write("   ");      
   }

   /**
    * Output class body
    * @param def definition
    * @param out Writer
    */
   private void writeClassBody(Definition def, Writer out) throws IOException
   {
      out.write("@Connector");
      writeEol(out);
      out.write("public class " + def.getRaClass() + " implements ResourceAdapter");
      writeLeftCurlyBracket(out, 0);
      writeEol(out);
      
      int indent = 1;
      writeIndent(out, indent);
      out.write("private static Logger log = Logger.getLogger(" + def.getRaClass() + ".class);");
      writeEol(out);
      writeEol(out);
      
      //constructor
      writeIndent(out, indent);
      out.write("public " + def.getRaClass() + "()");
      writeLeftCurlyBracket(out, indent);
      writeRightCurlyBracket(out, indent);
      writeEol(out);
      
      writeConfigProps(def, out, indent);
      writeEndpointLifecycle(def, out, indent);
      writeLifecycle(def, out, indent);
      writeXAResource(def, out, indent);
      writeHashCode(def, out, indent);
      writeEquals(def, out, indent);
      
      writeRightCurlyBracket(out, 0);
   }

   /**
    * Output Configuration Properties
    * @param def definition
    * @param out Writer
    * @param indent space number
    */
   private void writeConfigProps(Definition def, Writer out, int indent) throws IOException
   {
      if (def.getRaConfigProps() == null)
         return;
      
      for (int i = 0; i < def.getRaConfigProps().size(); i++)
      {
         writeIndent(out, indent);
         out.write("@ConfigProperty(defaultValue=\"" + def.getRaConfigProps().get(i).getValue() + "\")");
         writeEol(out);
         writeIndent(out, indent);
         out.write("private " + 
                   def.getRaConfigProps().get(i).getType() +
                   " " +
                   def.getRaConfigProps().get(i).getName() +
                   ";");
         writeEol(out);
         writeEol(out);
      }

      for (int i = 0; i < def.getRaConfigProps().size(); i++)
      {
         String name = def.getRaConfigProps().get(i).getName();
         String upcaseName = upcaseFisrt(name);
         //set
         writeIndent(out, indent);
         out.write("public void set" + 
                   upcaseName +
                   "(" +
                   def.getRaConfigProps().get(i).getType() +
                   " " +
                   name +
                   ")");
         writeLeftCurlyBracket(out, indent);
         writeIndent(out, indent + 1);
         out.write("this." + name + " = " + name + ";");
         writeRightCurlyBracket(out, indent);
         writeEol(out);
         
         //get
         writeIndent(out, indent);
         out.write("public " + 
                   def.getRaConfigProps().get(i).getType() +
                   " get" +
                   upcaseName +
                   "()");
         writeLeftCurlyBracket(out, indent);
         writeIndent(out, indent + 1);
         out.write("return " + name + ";");
         writeRightCurlyBracket(out, indent);
         writeEol(out);
      }
   }

   /**
    * Upcase first letter
    * @param name string
    * @param out Writer
    * @return String name string
    */
   private String upcaseFisrt(String name)
   {
      StringBuilder sb = new StringBuilder();
      sb.append(name.substring(0, 1).toUpperCase());
      sb.append(name.substring(1));
      return sb.toString();
   }


   /**
    * Output hashCode method
    * @param def definition
    * @param out Writer
    * @param indent space number
    */
   private void writeHashCode(Definition def, Writer out, int indent) throws IOException
   {
      writeIndent(out, indent);
      out.write("@Override");
      writeEol(out);
      writeIndent(out, indent);
      out.write("public int hashCode()");
      writeLeftCurlyBracket(out, indent);
      writeIndent(out, indent + 1);
      out.write("int result = 17;");
      writeEol(out);
      for (int i = 0; i < def.getRaConfigProps().size(); i++)
      {
         writeIndent(out, indent + 1);
         String type = def.getRaConfigProps().get(i).getType();
         if (type.equals("int"))
         {
            out.write("result = 31 * result + " + def.getRaConfigProps().get(i).getName() + ";");
         }
         else if (type.equals("short") || type.equals("char") || type.equals("byte"))
         {
            out.write("result = 31 * result + (int)" + def.getRaConfigProps().get(i).getName() + ";");
         }
         else if (type.equals("boolean"))
         {
            out.write("result = 31 * result + (" + def.getRaConfigProps().get(i).getName() + " ? 0 : 1);");
         }
         else if (type.equals("long"))
         {
            out.write("result = 31 * result + (int)(" + def.getRaConfigProps().get(i).getName() +
               " ^ (" + def.getRaConfigProps().get(i).getName() + " >>> 32));");
         }
         else if (type.equals("float"))
         {
            out.write("result = 31 * result + Float.floatToIntBits(" + def.getRaConfigProps().get(i).getName() + ");");
         }
         else if (type.equals("double"))
         {
            out.write("long tolong = Double.doubleToLongBits(" + def.getRaConfigProps().get(i).getName() + ");");
            writeEol(out);
            writeIndent(out, indent + 1);
            out.write("result = 31 * result + (int)(tolong ^ (tolong >>> 32));");
         }
         else
         {
            out.write("result = 31 * result + " + def.getRaConfigProps().get(i).getName() + ".hashCode();");
         }
         writeEol(out);
      }
      writeIndent(out, indent + 1);
      out.write("return result;");
      writeRightCurlyBracket(out, indent);
      writeEol(out);
   }


   /**
    * Output equals method
    * @param def definition
    * @param out Writer
    * @param indent space number
    */
   private void writeEquals(Definition def, Writer out, int indent) throws IOException
   {
      writeIndent(out, indent);
      out.write("@Override");
      writeEol(out);
      writeIndent(out, indent);
      out.write("public boolean equals(Object other)");
      writeLeftCurlyBracket(out, indent);
      writeIndent(out, indent + 1);
      out.write("if (other == null)");
      writeEol(out);
      writeIndent(out, indent + 2);
      out.write("return false;");
      writeEol(out);
      writeIndent(out, indent + 1);
      out.write("if (other == this)");
      writeEol(out);
      writeIndent(out, indent + 2);
      out.write("return true;");
      writeEol(out);
      writeIndent(out, indent + 1);
      out.write("if (!(other instanceof " + def.getRaClass() + "))");
      writeEol(out);
      writeIndent(out, indent + 2);
      out.write("return false;");
      writeEol(out);
      writeIndent(out, indent + 1);
      out.write(def.getRaClass() + " ra = (" + def.getRaClass() + ")other;");
      writeEol(out);
      writeIndent(out, indent + 1);
      out.write("return ");
      if (def.getRaConfigProps().size() == 0)
      {
         out.write("true");
      }
      for (int i = 0; i < def.getRaConfigProps().size(); i++)
      {
         if (i != 0)
         {
            writeEol(out);
            writeIndent(out, indent + 2);
            out.write("&& ");
         }
         out.write(def.getRaConfigProps().get(i).getName() + " == ra." + def.getRaConfigProps().get(i).getName());
      }
      out.write(";");
      writeRightCurlyBracket(out, indent);
      writeEol(out);
   }

   /**
    * Output getXAResources method
    * @param def definition
    * @param out Writer
    * @param indent space number
    */
   private void writeXAResource(Definition def, Writer out, int indent) throws IOException
   {
      writeIndent(out, indent);
      out.write("public XAResource[] getXAResources(ActivationSpec[] specs)");
      writeEol(out);
      writeIndent(out, indent + 1);
      out.write("throws ResourceException");
      writeLeftCurlyBracket(out, indent);
      writeIndent(out, indent + 1);
      out.write("log.debug(\"call getXAResources\");");
      writeEol(out);
      writeIndent(out, indent + 1);
      out.write("return null;");
      writeRightCurlyBracket(out, indent);
      writeEol(out);
   }

   /**
    * Output Lifecycle method
    * @param def definition
    * @param out Writer
    * @param indent space number
    */
   private void writeLifecycle(Definition def, Writer out, int indent) throws IOException
   {
      writeIndent(out, indent);
      out.write("public void start(BootstrapContext ctx)");
      writeEol(out);
      writeIndent(out, indent + 1);
      out.write("throws ResourceAdapterInternalException");
      writeLeftCurlyBracket(out, indent);
      writeIndent(out, indent + 1);
      out.write("log.debug(\"call start\");");
      writeRightCurlyBracket(out, indent);
      writeEol(out);

      writeIndent(out, indent);
      out.write("public void stop()");
      writeLeftCurlyBracket(out, indent);
      writeIndent(out, indent + 1);
      out.write("log.debug(\"call stop\");");
      writeRightCurlyBracket(out, indent);
      writeEol(out);
   }

   /**
    * Output EndpointLifecycle method
    * @param def definition
    * @param out Writer
    * @param indent space number
    */
   private void writeEndpointLifecycle(Definition def, Writer out, int indent) throws IOException
   {
      writeIndent(out, indent);
      out.write("public void endpointActivation(MessageEndpointFactory endpointFactory,");
      writeEol(out);
      writeIndent(out, indent + 1);
      out.write("ActivationSpec spec) throws ResourceException");
      writeLeftCurlyBracket(out, indent);
      writeIndent(out, indent + 1);
      out.write("log.debug(\"call endpointActivation\");");
      writeRightCurlyBracket(out, indent);
      writeEol(out);

      writeIndent(out, indent);
      out.write("public void endpointDeactivation(MessageEndpointFactory endpointFactory,");
      writeEol(out);
      writeIndent(out, indent + 1);
      out.write("ActivationSpec spec)");
      writeLeftCurlyBracket(out, indent);
      writeIndent(out, indent + 1);
      out.write("log.debug(\"call endpointDeactivation\");");
      writeRightCurlyBracket(out, indent);
      writeEol(out);
   }

}
