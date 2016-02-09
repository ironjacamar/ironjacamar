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

import org.ironjacamar.codegenerator.xml.Ra15XmlGen;
import org.ironjacamar.codegenerator.xml.RaXmlGen;

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
    * get right profile ra xmlGen
    *
    * @param def Definition
    * @return RaXmlGen profile ra xmlGen
    */
   @Override
   RaXmlGen getRaXmlGen(Definition def)
   {
      return new Ra15XmlGen();
   }
}
