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
package org.jboss.jca.core.workmanager.transport.remote;

import java.util.Arrays;

import javax.resource.spi.work.DistributableWork;
import javax.resource.spi.work.WorkException;

/**
 *
 * A ProtocolMessages.
 *
 * @author <a href="stefano.maestri@jboss.com">Stefano Maestri</a>
 *
 */
public class ProtocolMessages
{
   /**
   *
   * A Command of DistributedWorkManager to through network transport
   *
   * @author <a href="stefano.maestri@jboss.com">Stefano Maestri</a>
   *
   */
   public static enum Request
   {
      /** join*/
      JOIN(2, String.class, String.class),
      /** leave */
      LEAVE(1, String.class),
      /** update short running free */
      UPDATE_SHORTRUNNING_FREE(2, String.class, Integer.class),
      /** update long running free */
      UPDATE_LONGRUNNING_FREE(2, String.class, Integer.class),
      /** get short running free */
      GET_SHORTRUNNING_FREE(0),
      /** get long running free */
      GET_LONGRUNNING_FREE(0),

      /** PING */
      PING(0),
      /** do work */
      DO_WORK(1, DistributableWork.class),
      /** schedule work */
      SCHEDULE_WORK(1, DistributableWork.class),
      /** start work */
      START_WORK(1, DistributableWork.class);

      private final int numberOfParameter;

      private final Class<?>[] typeOfParameters;

      private Request(final int numberOfParameter, final Class<?>... typeOfParameters)
      {
         this.numberOfParameter = numberOfParameter;
         this.typeOfParameters = typeOfParameters;
      }

      /**
       * Get the numberOfParameter.
       *
       * @return the numberOfParameter.
       */
      public int getNumberOfParameter()
      {
         return numberOfParameter;
      }

      /**
       * Get the typeOfParameters.
       *
       * @return the typeOfParameters.
       */
      public Class<?>[] getTypeOfParameters()
      {
         return Arrays.copyOf(typeOfParameters, typeOfParameters.length);
      }

   }

   /**
   *
   * A Command of DistributedWorkManager to through network transport
   *
   * @author <a href="stefano.maestri@jboss.com">Stefano Maestri</a>
   *
   */
   public enum Response
   {
      /** VOID_OK*/
      VOID_OK(0),
      /** LONG return */
      LONG_OK(1, Integer.class),
      /** WORK_EXCEPTION */
      WORK_EXCEPTION(1, WorkException.class),
      /** GENERIC_EXCEPTION */
      GENERIC_EXCEPTION(1, Throwable.class);

      private final int numberOfParameter;

      private final Class<?>[] typeOfParameters;

      private Response(final int numberOfParameter, final Class<?>... typeOfParameters)
      {
         this.numberOfParameter = numberOfParameter;
         this.typeOfParameters = typeOfParameters;
      }

      /**
       * Get the numberOfParameter.
       *
       * @return the numberOfParameter.
       */
      public int getNumberOfParameter()
      {
         return numberOfParameter;
      }

      /**
         * Get the typeOfParameters.
         *
         * @return the typeOfParameters.
         */
      public Class<?>[] getTypeOfParameters()
      {
         return Arrays.copyOf(typeOfParameters, typeOfParameters.length);
      }
   }



}
