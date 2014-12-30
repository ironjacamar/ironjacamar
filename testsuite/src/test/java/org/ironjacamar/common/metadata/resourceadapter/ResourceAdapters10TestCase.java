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
import java.io.StringWriter;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * ResourceAdapters 1.0 tests
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class ResourceAdapters10TestCase
{
   /**
    * Read
    * @throws Exception In case of an error
    */
   @Test
   public void testRead() throws Exception
   {
      ResourceAdapterParser parser = new ResourceAdapterParser();

      InputStream is = ResourceAdapters10TestCase.class.getClassLoader().
         getResourceAsStream("../../resources/test/resourceadapter/dashra-1.0.xml");
      assertNotNull(is);

      XMLStreamReader xsr = XMLInputFactory.newInstance().createXMLStreamReader(is);

      Activations a = parser.parse(xsr);
      assertNotNull(a);

      is.close();
   }

   /**
    * ToString
    * @throws Exception In case of an error
    */
   @Test
   public void testToString() throws Exception
   {
      ResourceAdapterParser parser = new ResourceAdapterParser();

      InputStream is = ResourceAdapters10TestCase.class.getClassLoader().
         getResourceAsStream("../../resources/test/resourceadapter/dashra-1.0.xml");
      assertNotNull(is);

      XMLStreamReader xsr = XMLInputFactory.newInstance().createXMLStreamReader(is);

      Activations a = parser.parse(xsr);
      assertNotNull(a);

      is.close();

      StringReader sr = new StringReader(a.toString());
      XMLStreamReader nxsr = XMLInputFactory.newInstance().createXMLStreamReader(sr);
      Activations an = parser.parse(nxsr);

      assertEquals(a, an);
   }

   /**
    * Write
    * @throws Exception In case of an error
    */
   @Test
   public void testWrite() throws Exception
   {
      ResourceAdapterParser parser = new ResourceAdapterParser();

      InputStream is = ResourceAdapters10TestCase.class.getClassLoader().
         getResourceAsStream("../../resources/test/resourceadapter/dashra-1.0.xml");
      assertNotNull(is);

      XMLStreamReader xsr = XMLInputFactory.newInstance().createXMLStreamReader(is);

      Activations a = parser.parse(xsr);
      assertNotNull(a);

      is.close();

      StringWriter sw = new StringWriter();
      XMLStreamWriter xsw = XMLOutputFactory.newInstance().createXMLStreamWriter(sw);
      xsw.setDefaultNamespace("");

      xsw.writeStartDocument("UTF-8", "1.0");
      parser.store(a, xsw);
      xsw.writeEndDocument();

      xsw.flush();
      xsw.close();

      assertEquals(a.toString(), sw.toString());
   }

   /**
    * Equal
    * @throws Exception In case of an error
    */
   @Test
   public void testEqual() throws Exception
   {
      ResourceAdapterParser parser = new ResourceAdapterParser();

      InputStream is1 = ResourceAdapters10TestCase.class.getClassLoader().
         getResourceAsStream("../../resources/test/resourceadapter/dashra-1.0.xml");
      assertNotNull(is1);

      XMLStreamReader xsr1 = XMLInputFactory.newInstance().createXMLStreamReader(is1);

      Activations a1 = parser.parse(xsr1);
      assertNotNull(a1);

      is1.close();

      InputStream is2 = ResourceAdapters10TestCase.class.getClassLoader().
         getResourceAsStream("../../resources/test/resourceadapter/dashra-1.0.xml");
      assertNotNull(is2);

      XMLStreamReader xsr2 = XMLInputFactory.newInstance().createXMLStreamReader(is2);

      Activations a2 = parser.parse(xsr2);
      assertNotNull(a2);

      is2.close();

      assertEquals(a1, a2);
   }
}
