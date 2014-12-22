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
package org.ironjacamar.common.metadata.ironjacamar;

import org.ironjacamar.common.api.metadata.resourceadapter.Activation;

import java.io.InputStream;
import java.io.StringReader;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * IronJacamar 1.2 tests
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class IronJacamar12TestCase
{
   /**
    * Read
    * @throws Exception In case of an error
    */
   @Test
   public void testRead() throws Exception
   {
      IronJacamarParser parser = new IronJacamarParser();

      InputStream is = IronJacamar12TestCase.class.getClassLoader().
         getResourceAsStream("../../resources/test/ironjacamar/ironjacamar-1.2.xml");
      assertNotNull(is);

      Activation a = parser.parse(is);
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
      IronJacamarParser parser = new IronJacamarParser();

      InputStream is = IronJacamar12TestCase.class.getClassLoader().
         getResourceAsStream("../../resources/test/ironjacamar/ironjacamar-1.2.xml");
      assertNotNull(is);

      Activation a = parser.parse(is);
      assertNotNull(a);

      is.close();

      StringReader sr = new StringReader(a.toString());
      XMLStreamReader xsr = XMLInputFactory.newInstance().createXMLStreamReader(sr);
      Activation an = parser.parse(xsr);

      assertEquals(a, an);
   }

   /**
    * Equal
    * @throws Exception In case of an error
    */
   @Test
   public void testEqual() throws Exception
   {
      IronJacamarParser parser = new IronJacamarParser();

      InputStream is1 = IronJacamar12TestCase.class.getClassLoader().
         getResourceAsStream("../../resources/test/ironjacamar/ironjacamar-1.2.xml");
      assertNotNull(is1);

      Activation a1 = parser.parse(is1);
      assertNotNull(a1);

      is1.close();

      InputStream is2 = IronJacamar12TestCase.class.getClassLoader().
         getResourceAsStream("../../resources/test/ironjacamar/ironjacamar-1.2.xml");
      assertNotNull(is2);

      Activation a2 = parser.parse(is2);
      assertNotNull(a2);

      is2.close();

      assertEquals(a1, a2);
   }
}
