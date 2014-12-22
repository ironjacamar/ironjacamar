/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2014, Red Hat Inc, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the Eclipse Public License 1.0 as
 * published by the Free Software Foundation.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the Eclipse
 * Public License for more details.
 *
 * You should have received a copy of the Eclipse Public License 
 * along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.ironjacamar.common.metadata.resourceadapter;

import org.ironjacamar.common.api.metadata.resourceadapter.Activations;

import java.io.InputStream;
import java.io.StringReader;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * ResourceAdapters 1.1 tests
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class ResourceAdapters11TestCase
{
   /**
    * Read
    * @throws Exception In case of an error
    */
   @Test
   public void testRead() throws Exception
   {
      ResourceAdapterParser parser = new ResourceAdapterParser();

      InputStream is = ResourceAdapters11TestCase.class.getClassLoader().
         getResourceAsStream("../../resources/test/resourceadapter/dashra-1.1.xml");
      assertNotNull(is);

      Activations a = parser.parse(is);
      assertNotNull(a);

      is.close();
   }

   /**
    * Write
    * @throws Exception In case of an error
    */
   @Test
   public void testWrite() throws Exception
   {
      ResourceAdapterParser parser = new ResourceAdapterParser();

      InputStream is = ResourceAdapters11TestCase.class.getClassLoader().
         getResourceAsStream("../../resources/test/resourceadapter/dashra-1.1.xml");
      assertNotNull(is);

      Activations a = parser.parse(is);
      assertNotNull(a);

      is.close();

      StringReader sr = new StringReader(a.toString());
      XMLStreamReader xsr = XMLInputFactory.newInstance().createXMLStreamReader(sr);
      Activations an = parser.parse(xsr);

      assertEquals(a, an);
   }

   /**
    * Equal
    * @throws Exception In case of an error
    */
   @Test
   public void testEqual() throws Exception
   {
      ResourceAdapterParser parser = new ResourceAdapterParser();

      InputStream is1 = ResourceAdapters11TestCase.class.getClassLoader().
         getResourceAsStream("../../resources/test/resourceadapter/dashra-1.1.xml");
      assertNotNull(is1);

      Activations a1 = parser.parse(is1);
      assertNotNull(a1);

      is1.close();

      InputStream is2 = ResourceAdapters11TestCase.class.getClassLoader().
         getResourceAsStream("../../resources/test/resourceadapter/dashra-1.1.xml");
      assertNotNull(is2);

      Activations a2 = parser.parse(is2);
      assertNotNull(a2);

      is2.close();

      assertEquals(a1, a2);
   }
}
