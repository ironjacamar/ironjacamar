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

import org.jboss.jca.codegenerator.xml.Ra10XmlGen;
import org.jboss.jca.codegenerator.xml.RaXmlGen;

/**
 * A JCA10Profile.
 * 
 * @author Jeff Zhang
 * @version $Revision: $
 */
public class JCA10Profile extends BaseProfile
{

   /**
    * JCA10Profile
    */
   public JCA10Profile()
   {
   }
   
  
   /**
    * generate code
    * @param def Definition 
    */
   @Override
   public void generate(Definition def)
   {
      generateOutboundCode(def);

      generateAntXml(def.getOutputDir());
      generateRaXml(def, def.getOutputDir());
   }


   /**
    * get right profile ra xmlGen
    * @param def Definition
    * @return RaXmlGen profile ra xmlGen
    */
   @Override
   RaXmlGen getRaXmlGen(Definition def)
   {
      return new Ra10XmlGen();
   }
}
