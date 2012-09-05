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

import org.jboss.jca.common.api.metadata.JCAMetadata;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;

import org.jboss.logging.Logger;

import static org.junit.Assert.*;

/**
 * A base class for DS parsing tests
 * 
 * @author <a href="mailto:vrastsel@redhat.com">Vladimir Rastseluev</a>
 *
 */

public abstract class ParserTestBase
{

   /**
    * parser
    */
   protected static MetadataParser parser;

   /**
    *  logger
    */
   protected static Logger log = Logger.getLogger(ParserTestBase.class);


   /**
    * gets JCA Metadata object from file
    * 
    * @param xmlFile - to parse
    * 
    * @return resulting object
    * 
    * @throws Exception in case of error
    */
   protected JCAMetadata doParse(File xmlFile) throws Exception
   {
      FileInputStream is = null;

      try
      {
         is = new FileInputStream(xmlFile);
         return parser.parse(is);
      }
      finally
      {
         if (is != null)
            is.close();
      }
   }

   /**
    * Parses metadata, then tries to re-parse it, using its xml representation, 
    * compares resulting metadata with re-parsed one
    * 
    * @param xmlFile to parse
    * @return resulting metadata 
    *
    * @throws Exception in case of error
    */
   protected JCAMetadata doReParse(File xmlFile) throws Exception
   {
      JCAMetadata ds = doParse(xmlFile);
      JCAMetadata ds1 = reParse(ds);
      checkEquals(ds, ds1);
      return ds;
   }

   /**
    * Returns resource file by name
    * 
    * @param fileName in resource directory
    * @return File
    * @throws Exception in case of error
    */
   protected File getFile(String fileName) throws Exception
   {
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      return new File(cl.getResource(fileName).toURI());
   }

   /**
    * Returns metadata object as a result of parsing file from resource folder
    * 
    * @param fileName to parse
    * @return resulting object
    * @throws Exception in case of error
    */
   protected JCAMetadata parseOjbectsFromFile(String fileName) throws Exception
   {
      return doReParse(getFile(fileName));
   }

   /**
    * Re-parses JCAMetadata from toString() presentation
    * @param ds JCAMetadata object to re-parse
    * @return re-parsed JCAMetadata object
    * @throws Exception in case of error
    */
   protected JCAMetadata reParse(JCAMetadata ds) throws Exception
   {
      ByteArrayInputStream bais = null;
      try
      {
         bais = new ByteArrayInputStream(ds.toString().getBytes("UTF-8"));
         return parser.parse(bais);
      }
      finally
      {
         if (bais != null)
            bais.close();
      }

   }
   
   /**
    * Checks if objects of metadata and their string representation are equal
    * @param m1 - first object
    * @param m2 - second object
    */
   protected void checkEquals(JCAMetadata m1, JCAMetadata m2)
   {
      String metadatas = m1.toString() + "\n" + m2.toString();
      log.info("Check equals:" + metadatas);
      assertEquals("Strings are not equal.\n" + metadatas, m1.toString(),
            m2.toString());
      assertTrue("Objects are not equal:\n" + metadatas, m1.equals(m2));
 
   }
}
