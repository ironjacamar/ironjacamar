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

import org.jboss.jca.codegenerator.xml.Ra15XmlGen;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * A JCA15Profile.
 * 
 * @author Jeff Zhang
 * @version $Revision: $
 */
public class JCA15Profile extends BaseProfile
{

   /**
    * JCA15Profile
    */
   public JCA15Profile()
   {
   }
   
  
   /**
    * generate code
    * @param def Definition 
    */
   @Override
   public void generate(Definition def)
   {
      generateRaCode(def);
      generateOutboundCode(def);
      generateInboundCode(def);

      generateAntXml(def.getOutputDir());
      generateRaXml(def, def.getOutputDir());
   }

   /**
    * generate ra.xml
    * @param def Definition
    * @param outputDir output directory
    */
   @Override
   void generateRaXml(Definition def, String outputDir)
   {
      try
      {
         FileWriter rafw = Utils.createFile("ra.xml", outputDir + File.separatorChar + "META-INF");
         Ra15XmlGen raGen = new Ra15XmlGen();
         raGen.generate(def, rafw);
         rafw.close();
      }
      catch (IOException ioe)
      {
         ioe.printStackTrace();
      }
   }
}
