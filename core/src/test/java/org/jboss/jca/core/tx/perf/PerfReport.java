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

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.LineNumberReader;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Performance report using GNU plot
 *
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class PerfReport
{
   /**
    * Get data
    * @param fr The file reader
    * @return The data
    * @exception Exception If an error occurs
    */
   static SortedMap<String, SortedMap<Integer, Integer>> getData(FileReader fr) throws Exception
   {
      SortedMap<String, SortedMap<Integer, Integer>> data =
         new TreeMap<String, SortedMap<Integer, Integer>>();

      LineNumberReader r = new LineNumberReader(fr);
      String s = r.readLine();
      while (s != null)
      {
         if (s.indexOf("PERF-DATA") != -1)
         {
            s = s.substring(s.indexOf("PERF-DATA") + 11);
            String[] d = s.split(",");

            String type = d[0];
            Integer c = Integer.valueOf(d[1]);
            Integer tx = Integer.valueOf(d[2]);

            SortedMap<Integer, Integer> m = data.get(type);

            if (m == null)
               m = new TreeMap<Integer, Integer>();

            m.put(c, tx);

            data.put(type, m);
         }

         s = r.readLine();
      }

      return data;
   }

   /**
    * Main
    * @param args The arguments
    */
   public static void main(String[] args)
   {
      if (args.length < 3)
      {
         System.out.println("Usage: PerfReport noopts.txt narayana-mem.txt narayana-file.txt");
         return;
      }

      File noopTS = new File(args[0]);
      File narayanaMem = new File(args[1]);
      File narayanaFile = new File(args[2]);

      FileReader noopTSR = null;
      FileReader narayanaMemR = null;
      FileReader narayanaFileR = null;
      FileWriter perfDat = null;
      FileWriter perfPlot = null;

      try
      {
         noopTSR = new FileReader(noopTS);
         narayanaMemR = new FileReader(narayanaMem);
         narayanaFileR = new FileReader(narayanaFile);

         perfDat = new FileWriter("perf.dat");
         perfPlot = new FileWriter("perf.plot");

         SortedMap<String, SortedMap<Integer, Integer>> noopTSData = getData(noopTSR);
         SortedMap<String, SortedMap<Integer, Integer>> narayanaMemData = getData(narayanaMemR);
         SortedMap<String, SortedMap<Integer, Integer>> narayanaFileData = getData(narayanaFileR);

         PerfUtil.generatePerfDat(noopTSData, narayanaMemData, narayanaFileData, perfDat);
         PerfUtil.generatePerfPlot("perf", perfPlot);
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
      finally
      {
         if (noopTSR != null)
         {
            try
            {
               noopTSR.close();
            }
            catch (Exception e)
            {
               // Ignore
            }
         }
         if (narayanaMemR != null)
         {
            try
            {
               narayanaMemR.close();
            }
            catch (Exception e)
            {
               // Ignore
            }
         }
         if (narayanaFileR != null)
         {
            try
            {
               narayanaFileR.close();
            }
            catch (Exception e)
            {
               // Ignore
            }
         }

         if (perfDat != null)
         {
            try
            {
               perfDat.flush();
               perfDat.close();
            }
            catch (Exception e)
            {
               // Ignore
            }
         }

         if (perfPlot != null)
         {
            try
            {
               perfPlot.flush();
               perfPlot.close();
            }
            catch (Exception e)
            {
               // Ignore
            }
         }
      }
   }
}
