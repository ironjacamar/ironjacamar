/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2014, Red Hat Inc, and individual contributors
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
package org.jboss.jca.core.tx.perf;

import java.io.FileWriter;
import java.util.SortedMap;

/**
 * Performance utilities
 *
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class PerfUtil
{
   /**
    * Generate the perf.dat file
    * @param noopTS The NoopTS data
    * @param narayanaMem The Narayana/MEM data
    * @param narayanaFile The Narayana/FILE data
    * @param fw The file writer
    * @exception Exception If an error occurs
    */
   static void generatePerfDat(SortedMap<String, SortedMap<Integer, Integer>> noopTS,
                               SortedMap<String, SortedMap<Integer, Integer>> narayanaMem,
                               SortedMap<String, SortedMap<Integer, Integer>> narayanaFile,
                               FileWriter fw) throws Exception
   {
      SortedMap<Integer, Integer> noopTSNoTransaction = noopTS.get("NoTransaction");
      SortedMap<Integer, Integer> narayanaMemNoTransaction = narayanaMem.get("NoTransaction");
      SortedMap<Integer, Integer> narayanaFileNoTransaction = narayanaFile.get("NoTransaction");

      SortedMap<Integer, Integer> noopTSLocalTransaction = noopTS.get("LocalTransaction");
      SortedMap<Integer, Integer> narayanaMemLocalTransaction = narayanaMem.get("LocalTransaction");
      SortedMap<Integer, Integer> narayanaFileLocalTransaction = narayanaFile.get("LocalTransaction");

      SortedMap<Integer, Integer> noopTSXATransaction = noopTS.get("XATransaction");
      SortedMap<Integer, Integer> narayanaMemXATransaction = narayanaMem.get("XATransaction");
      SortedMap<Integer, Integer> narayanaFileXATransaction = narayanaFile.get("XATransaction");

      System.out.println("Clients\t\tNoopTS\t\tNarayana/Mem\t%\t\tNoopTS\t\tNarayana/File\t%");

      for (Integer key : noopTSNoTransaction.keySet())
      {
         perfDatLine(key,
                     noopTSNoTransaction.get(key),
                     narayanaMemNoTransaction.get(key),
                     narayanaFileNoTransaction.get(key),
                     noopTSLocalTransaction.get(key),
                     narayanaMemLocalTransaction.get(key),
                     narayanaFileLocalTransaction.get(key),
                     noopTSXATransaction.get(key),
                     narayanaMemXATransaction.get(key),
                     narayanaFileXATransaction.get(key),
                     fw);

         System.out.print(key);
         System.out.print("\t\t");

         // NoTransaction
         System.out.print(noopTSNoTransaction.get(key));
         System.out.print("\t\t");
         System.out.print(narayanaMemNoTransaction.get(key));
         System.out.print("\t\t");

         double d = (narayanaMemNoTransaction.get(key).doubleValue() /
                     noopTSNoTransaction.get(key).doubleValue()) * 100;
         System.out.print((int)d);
         System.out.print("\t\t");

         System.out.print(noopTSNoTransaction.get(key));
         System.out.print("\t\t");
         System.out.print(narayanaFileNoTransaction.get(key));
         System.out.print("\t\t");

         d = (narayanaFileNoTransaction.get(key).doubleValue() / noopTSNoTransaction.get(key).doubleValue()) * 100;
         System.out.println((int)d);

         // LocalTransaction
         System.out.print("\t\t");
         System.out.print(noopTSLocalTransaction.get(key));
         System.out.print("\t\t");
         System.out.print(narayanaMemLocalTransaction.get(key));
         System.out.print("\t\t");

         d = (narayanaMemLocalTransaction.get(key).doubleValue() / noopTSLocalTransaction.get(key).doubleValue()) * 100;
         System.out.print((int)d);

         System.out.print("\t\t");
         System.out.print(noopTSLocalTransaction.get(key));
         System.out.print("\t\t");
         System.out.print(narayanaFileLocalTransaction.get(key));
         System.out.print("\t\t");

         d = (narayanaFileLocalTransaction.get(key).doubleValue() /
              noopTSLocalTransaction.get(key).doubleValue()) * 100;
         System.out.println((int)d);

         // XATransaction
         System.out.print("\t\t");
         System.out.print(noopTSXATransaction.get(key));
         System.out.print("\t\t");
         System.out.print(narayanaMemXATransaction.get(key));
         System.out.print("\t\t");

         d = (narayanaMemXATransaction.get(key).doubleValue() / noopTSXATransaction.get(key).doubleValue()) * 100;
         System.out.print((int)d);
         System.out.print("\t\t");

         System.out.print(noopTSXATransaction.get(key));
         System.out.print("\t\t");
         System.out.print(narayanaFileXATransaction.get(key));
         System.out.print("\t\t");

         d = (narayanaFileXATransaction.get(key).doubleValue() / noopTSXATransaction.get(key).doubleValue()) * 100;
         System.out.println((int)d);
      }
   }

   /**
    * Write perf.dat line
    * @param client client
    * @param c1 c1
    * @param c2 c2
    * @param c3 c3
    * @param c4 c4
    * @param c5 c5
    * @param c6 c6
    * @param c7 c7
    * @param c8 c8
    * @param c9 c9
    * @param fw file writer
    * @exception Exception If an error occurs
    */
   static void perfDatLine(Integer client,
                           Integer c1,
                           Integer c2,
                           Integer c3,
                           Integer c4,
                           Integer c5,
                           Integer c6,
                           Integer c7,
                           Integer c8,
                           Integer c9,
                           FileWriter fw) throws Exception
   {
      writeString(fw, Integer.toString(client));
      writeTAB(fw);

      writeString(fw, Integer.toString(c1));
      writeTAB(fw);

      writeString(fw, Integer.toString(c2));
      writeTAB(fw);

      writeString(fw, Integer.toString(c3));
      writeTAB(fw);

      writeString(fw, Integer.toString(c4));
      writeTAB(fw);

      writeString(fw, Integer.toString(c5));
      writeTAB(fw);

      writeString(fw, Integer.toString(c6));
      writeTAB(fw);

      writeString(fw, Integer.toString(c7));
      writeTAB(fw);

      writeString(fw, Integer.toString(c8));
      writeTAB(fw);

      writeString(fw, Integer.toString(c9));

      writeEOL(fw);
   }

   /**
    * Generate the perf.plot file
    * @param name The file name
    * @param fw The file writer
    * @exception Exception If an error occurs
    */
   static void generatePerfPlot(String name, FileWriter fw) throws Exception
   {
      writeString(fw, "set autoscale");
      writeEOL(fw);

      writeString(fw, "unset log");
      writeEOL(fw);

      writeString(fw, "unset label");
      writeEOL(fw);

      writeString(fw, "set xtic auto");
      writeEOL(fw);

      writeString(fw, "set ytic auto");
      writeEOL(fw);

      writeString(fw, "set title \"IronJacamar performance test suite\"");
      writeEOL(fw);

      writeString(fw, "set xlabel \"Clients\"");
      writeEOL(fw);

      writeString(fw, "set ylabel \"TX/sec\"");
      writeEOL(fw);

      writeString(fw, "set terminal png size 1024,768");
      writeEOL(fw);

      writeString(fw, "set output \"" + name + ".png\"");
      writeEOL(fw);

      writeString(fw, "plot \"" + name + ".dat\" u 1:2 t \'NoopTS-NoTransaction\' w linespoints, \\");
      writeEOL(fw);

      writeString(fw, "     \"" + name + ".dat\" u 1:3 t \'NarayanaMem-NoTransaction\' w linespoints, \\");
      writeEOL(fw);

      writeString(fw, "     \"" + name + ".dat\" u 1:4 t \'NarayanaFile-NoTransaction\' w linespoints, \\");
      writeEOL(fw);

      writeString(fw, "     \"" + name + ".dat\" u 1:5 t \'NoopTS-LocalTransaction\' w linespoints, \\");
      writeEOL(fw);

      writeString(fw, "     \"" + name + ".dat\" u 1:6 t \'NarayanaMem-LocalTransaction\' w linespoints, \\");
      writeEOL(fw);

      writeString(fw, "     \"" + name + ".dat\" u 1:7 t \'NarayanaFile-LocalTransaction\' w linespoints, \\");
      writeEOL(fw);

      writeString(fw, "     \"" + name + ".dat\" u 1:8 t \'NoopTS-XATransaction\' w linespoints, \\");
      writeEOL(fw);

      writeString(fw, "     \"" + name + ".dat\" u 1:9 t \'NarayanaMem-XATransaction\' w linespoints, \\");
      writeEOL(fw);

      writeString(fw, "     \"" + name + ".dat\" u 1:10 t \'NarayanaFile-XATransaction\' w linespoints");
      writeEOL(fw);
   }

   /**
    * Write string
    * @param fw The file writer
    * @param s The string
    * @exception Exception If an error occurs
    */
   private static void writeString(FileWriter fw, String s) throws Exception
   {
      for (int i = 0; i < s.length(); i++)
      {
         fw.write((int)s.charAt(i));
      }
   }

   /**
    * Write EOL
    * @param fw The file writer
    * @exception Exception If an error occurs
    */
   private static void writeEOL(FileWriter fw) throws Exception
   {
      fw.write((int)'\n');
   }

   /**
    * Write TAB
    * @param fw The file writer
    * @exception Exception If an error occurs
    */
   private static void writeTAB(FileWriter fw) throws Exception
   {
      fw.write((int)'\t');
   }
}
