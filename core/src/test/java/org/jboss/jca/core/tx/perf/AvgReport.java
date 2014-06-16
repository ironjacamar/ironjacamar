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
import java.util.StringTokenizer;
import java.util.TreeMap;

/**
 * Average performance reports
 *
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class AvgReport
{
   /**
    * Read the perf.dat file
    * @param noopTS The NoopTS data
    * @param narayanaMem The Narayana/MEM data
    * @param narayanaFile The Narayana/FILE data
    * @param fr The file reader
    * @exception Exception If an error occurs
    */
   private static void readPerfDat(SortedMap<String, SortedMap<Integer, Integer>> noopTS,
                                   SortedMap<String, SortedMap<Integer, Integer>> narayanaMem,
                                   SortedMap<String, SortedMap<Integer, Integer>> narayanaFile,
                                   FileReader fr) throws Exception
   {
      LineNumberReader r = new LineNumberReader(fr);
      String s = r.readLine();
      while (s != null)
      {
         SortedMap<Integer, Integer> noopTSNoTransactionData = noopTS.get("NoTransaction");
         if (noopTSNoTransactionData == null)
            noopTSNoTransactionData = new TreeMap<Integer, Integer>();

         SortedMap<Integer, Integer> narayanaMemNoTransactionData = narayanaMem.get("NoTransaction");
         if (narayanaMemNoTransactionData == null)
            narayanaMemNoTransactionData = new TreeMap<Integer, Integer>();

         SortedMap<Integer, Integer> narayanaFileNoTransactionData = narayanaFile.get("NoTransaction");
         if (narayanaFileNoTransactionData == null)
            narayanaFileNoTransactionData = new TreeMap<Integer, Integer>();

         SortedMap<Integer, Integer> noopTSLocalTransactionData = noopTS.get("LocalTransaction");
         if (noopTSLocalTransactionData == null)
            noopTSLocalTransactionData = new TreeMap<Integer, Integer>();

         SortedMap<Integer, Integer> narayanaMemLocalTransactionData = narayanaMem.get("LocalTransaction");
         if (narayanaMemLocalTransactionData == null)
            narayanaMemLocalTransactionData = new TreeMap<Integer, Integer>();

         SortedMap<Integer, Integer> narayanaFileLocalTransactionData = narayanaFile.get("LocalTransaction");
         if (narayanaFileLocalTransactionData == null)
            narayanaFileLocalTransactionData = new TreeMap<Integer, Integer>();

         SortedMap<Integer, Integer> noopTSXATransactionData = noopTS.get("XATransaction");
         if (noopTSXATransactionData == null)
            noopTSXATransactionData = new TreeMap<Integer, Integer>();

         SortedMap<Integer, Integer> narayanaMemXATransactionData = narayanaMem.get("XATransaction");
         if (narayanaMemXATransactionData == null)
            narayanaMemXATransactionData = new TreeMap<Integer, Integer>();

         SortedMap<Integer, Integer> narayanaFileXATransactionData = narayanaFile.get("XATransaction");
         if (narayanaFileXATransactionData == null)
            narayanaFileXATransactionData = new TreeMap<Integer, Integer>();

         StringTokenizer st = new StringTokenizer(s, "\t");

         Integer client = Integer.valueOf(st.nextToken());

         Integer noopTSNoTransaction = Integer.valueOf(st.nextToken());
         Integer narayanaMemNoTransaction = Integer.valueOf(st.nextToken());
         Integer narayanaFileNoTransaction = Integer.valueOf(st.nextToken());

         Integer noopTSLocalTransaction = Integer.valueOf(st.nextToken());
         Integer narayanaMemLocalTransaction = Integer.valueOf(st.nextToken());
         Integer narayanaFileLocalTransaction = Integer.valueOf(st.nextToken());

         Integer noopTSXATransaction = Integer.valueOf(st.nextToken());
         Integer narayanaMemXATransaction = Integer.valueOf(st.nextToken());
         Integer narayanaFileXATransaction = Integer.valueOf(st.nextToken());

         noopTSNoTransactionData.put(client, noopTSNoTransaction);
         narayanaMemNoTransactionData.put(client, narayanaMemNoTransaction);
         narayanaFileNoTransactionData.put(client, narayanaFileNoTransaction);

         noopTSLocalTransactionData.put(client, noopTSLocalTransaction);
         narayanaMemLocalTransactionData.put(client, narayanaMemLocalTransaction);
         narayanaFileLocalTransactionData.put(client, narayanaFileLocalTransaction);

         noopTSXATransactionData.put(client, noopTSXATransaction);
         narayanaMemXATransactionData.put(client, narayanaMemXATransaction);
         narayanaFileXATransactionData.put(client, narayanaFileXATransaction);

         noopTS.put("NoTransaction", noopTSNoTransactionData);
         noopTS.put("LocalTransaction", noopTSLocalTransactionData);
         noopTS.put("XATransaction", noopTSXATransactionData);

         narayanaMem.put("NoTransaction", narayanaMemNoTransactionData);
         narayanaMem.put("LocalTransaction", narayanaMemLocalTransactionData);
         narayanaMem.put("XATransaction", narayanaMemXATransactionData);

         narayanaFile.put("NoTransaction", narayanaFileNoTransactionData);
         narayanaFile.put("LocalTransaction", narayanaFileLocalTransactionData);
         narayanaFile.put("XATransaction", narayanaFileXATransactionData);

         s = r.readLine();
      }
   }

   /**
    * Add the data
    * @param input The input
    * @param result The result
    */
   private static void addPerfData(SortedMap<String, SortedMap<Integer, Integer>> input,
                                   SortedMap<String, SortedMap<Integer, Integer>> result)
   {
      for (String key : input.keySet())
      {
         SortedMap<Integer, Integer> inputData = input.get(key);

         for (Integer inputDataKey : inputData.keySet())
         {
            Integer inputValue = inputData.get(inputDataKey);

            SortedMap<Integer, Integer> resultData = result.get(key);
            if (resultData == null)
               resultData = new TreeMap<Integer, Integer>();

            Integer resultValue = resultData.get(inputDataKey);
            if (resultValue == null)
               resultValue = Integer.valueOf(0);

            Integer resultNewValue = Integer.valueOf(inputValue.intValue() + resultValue.intValue());

            resultData.put(inputDataKey, resultNewValue);
            result.put(key, resultData);
         }
      }
   }

   /**
    * Average the data
    * @param input The input
    * @param factor The factor
    * @return The result
    */
   private static SortedMap<String, SortedMap<Integer, Integer>>
   avgPerfData(SortedMap<String, SortedMap<Integer, Integer>> input, int factor)
   {
      SortedMap<String, SortedMap<Integer, Integer>> result =
         new TreeMap<String, SortedMap<Integer, Integer>>();

      for (String key : input.keySet())
      {
         SortedMap<Integer, Integer> inputData = input.get(key);

         for (Integer inputDataKey : inputData.keySet())
         {
            Integer inputValue = inputData.get(inputDataKey);

            SortedMap<Integer, Integer> resultData = result.get(key);
            if (resultData == null)
               resultData = new TreeMap<Integer, Integer>();

            Integer resultValue = Integer.valueOf(inputValue.intValue() / factor);

            resultData.put(inputDataKey, resultValue);
            result.put(key, resultData);
         }
      }

      return result;
   }

   /**
    * Main
    * @param args The arguments
    */
   public static void main(String[] args)
   {
      if (args.length < 1)
      {
         System.out.println("Usage: AvgReport file.dat [...]");
         return;
      }

      SortedMap<String, SortedMap<Integer, Integer>> noopTSResult =
         new TreeMap<String, SortedMap<Integer, Integer>>();
      SortedMap<String, SortedMap<Integer, Integer>> narayanaMemResult =
         new TreeMap<String, SortedMap<Integer, Integer>>();
      SortedMap<String, SortedMap<Integer, Integer>> narayanaFileResult =
         new TreeMap<String, SortedMap<Integer, Integer>>();

      for (String name : args)
      {
         File input = new File(name);
         FileReader inputR = null;
         try
         {
            inputR = new FileReader(input);

            SortedMap<String, SortedMap<Integer, Integer>> noopTSData =
               new TreeMap<String, SortedMap<Integer, Integer>>();
            SortedMap<String, SortedMap<Integer, Integer>> narayanaMemData =
               new TreeMap<String, SortedMap<Integer, Integer>>();
            SortedMap<String, SortedMap<Integer, Integer>> narayanaFileData =
               new TreeMap<String, SortedMap<Integer, Integer>>();

            readPerfDat(noopTSData, narayanaMemData, narayanaFileData, inputR);

            addPerfData(noopTSData, noopTSResult);
            addPerfData(narayanaMemData, narayanaMemResult);
            addPerfData(narayanaFileData, narayanaFileResult);
         }
         catch (Exception e)
         {
            e.printStackTrace();
         }
         finally
         {
            if (inputR != null)
            {
               try
               {
                  inputR.close();
               }
               catch (Exception e)
               {
                  // Ignore
               }
            }
         }
      }

      SortedMap<String, SortedMap<Integer, Integer>> noopTSAvg = avgPerfData(noopTSResult, args.length);
      SortedMap<String, SortedMap<Integer, Integer>> narayanaMemAvg = avgPerfData(narayanaMemResult, args.length);
      SortedMap<String, SortedMap<Integer, Integer>> narayanaFileAvg = avgPerfData(narayanaFileResult, args.length);

      FileWriter perfDat = null;
      FileWriter perfPlot = null;
      try
      {
         perfDat = new FileWriter("perf-avg.dat");
         perfPlot = new FileWriter("perf-avg.plot");

         PerfUtil.generatePerfDat(noopTSAvg, narayanaMemAvg, narayanaFileAvg, perfDat);
         PerfUtil.generatePerfPlot("perf-avg", perfPlot);
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
      finally
      {
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

