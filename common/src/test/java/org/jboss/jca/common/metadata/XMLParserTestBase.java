/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.jca.common.metadata;

import org.jboss.jca.common.api.metadata.CopyableMetaData;
import org.jboss.jca.common.api.metadata.JCAMetadata;

import org.junit.Test;
/**
 *
 * A ResourceAdapterParserTestCase.
 *
 * @author <a href="mailto:vrastsel@redhat.com">Vladimir Rastseluev</a>
 *
 */
public abstract class XMLParserTestBase extends ParserTestBase
{
   /**
    * file name for parsing
    */
   protected static String parsedFileName;

   /**
    * shouldParseXML
    * @throws Exception in case of error
    */
   @Test
   public void shouldParseXML() throws Exception
   {

      JCAMetadata result =  doParse(getFile(parsedFileName));
      checkMetadata(result);
      JCAMetadata result1 =  reParse(result);
      checkMetadata(result1);
      checkEquals(result, result1);
      if (result instanceof CopyableMetaData)
      {
         JCAMetadata result2 = (JCAMetadata)((CopyableMetaData)result).copy();
         checkMetadata(result2);
         checkEquals(result, result2);
         checkEquals(result1, result2);
      }
   }

   /**
    * this methos should be implemented by subclass
    * 
    * @param result of parsing
    */
   public abstract void checkMetadata(JCAMetadata result);

}
