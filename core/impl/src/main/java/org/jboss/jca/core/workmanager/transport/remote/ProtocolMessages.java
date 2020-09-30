/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2012, Red Hat Inc, and individual contributors
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

import org.jboss.jca.core.spi.workmanager.Address;
import org.jboss.jca.core.workmanager.ClassBundle;

import java.io.Serializable;
import java.util.Arrays;

import javax.resource.spi.work.DistributableWork;
import javax.resource.spi.work.WorkException;

/**
 * A ProtocolMessages.
 *
 * @author <a href="stefano.maestri@ironjacamar.org">Stefano Maestri</a>
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class ProtocolMessages
{
   /**
    * A Command of DistributedWorkManager to through network transport
    *
    * @author <a href="stefano.maestri@ironjacamar.org">Stefano Maestri</a>
    */
   public static enum Request
   {
      /** join*/
      JOIN(1, Serializable.class),
      /** leave */
      LEAVE(1, Serializable.class),
      /** get workmanagers */
      GET_WORKMANAGERS(0),
      /** workmanager add */
      WORKMANAGER_ADD(2, Address.class, Serializable.class),
      /** workmanager remove */
      WORKMANAGER_REMOVE(1, Address.class),
      /** update short running free */
      UPDATE_SHORTRUNNING_FREE(2, Address.class, Long.class),
      /** update long running free */
      UPDATE_LONGRUNNING_FREE(2, Address.class, Long.class),
      /** get short running free */
      GET_SHORTRUNNING_FREE(1, Address.class),
      /** get long running free */
      GET_LONGRUNNING_FREE(1, Address.class),

      /** GET_DISTRIBUTED_STATISTICS */
      GET_DISTRIBUTED_STATISTICS(1, Address.class),
      /** CLEAR_DISTRIBUTED_STATISTICS */
      CLEAR_DISTRIBUTED_STATISTICS(1, Address.class),
      /** DELTA_DOWORK_ACCEPTED */
      DELTA_DOWORK_ACCEPTED(1, Address.class),
      /** DELTA_DOWORK_REJECTED */
      DELTA_DOWORK_REJECTED(1, Address.class),
      /** DELTA_STARTWORK_ACCEPTED */
      DELTA_STARTWORK_ACCEPTED(1, Address.class),
      /** DELTA_STARTWORK_REJECTED */
      DELTA_STARTWORK_REJECTED(1, Address.class),
      /** DELTA_SCHEDULEWORK_ACCEPTED */
      DELTA_SCHEDULEWORK_ACCEPTED(1, Address.class),
      /** DELTA_SCHEDULEWORK_REJECTED */
      DELTA_SCHEDULEWORK_REJECTED(1, Address.class),
      /** DELTA_WORK_SUCCESSFUL */
      DELTA_WORK_SUCCESSFUL(1, Address.class),
      /** DELTA_WORK_FAILED */
      DELTA_WORK_FAILED(1, Address.class),

      /** PING */
      PING(0),
      /** do work */
      DO_WORK(3, Address.class, ClassBundle.class, DistributableWork.class),
      /** schedule work */
      SCHEDULE_WORK(3, Address.class, ClassBundle.class, DistributableWork.class),
      /** start work */
      START_WORK(3, Address.class, ClassBundle.class, DistributableWork.class);

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
   * @author <a href="stefano.maestri@ironjacamar.org">Stefano Maestri</a>
   *
   */
   public enum Response
   {
      /** OK_VOID */
      OK_VOID(0),
      /** OK_SERIALIZABLE */
      OK_SERIALIZABLE(1, Serializable.class),
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

   /**
    *
    * A ResponseValue.
    *
    * @author <a href="stefano.maestri@ironjacamar.org">Stefano Maestri</a>
    *
    */
   public static class ResponseValues implements Serializable
   {
      /** Serial version uid */
      private static final long serialVersionUID = 1L;

      private final Response response;

      private final Serializable[] values;

      /**
       * Create a new ResponseValue.
       *
       * @param response the response
       * @param values values to return
       */
      public ResponseValues(Response response, Serializable... values)
      {
         super();
         this.response = response;
         this.values = values;
      }

      /**
       * Get the response.
       *
       * @return the response.
       */
      public final Response getResponse()
      {
         return response;
      }

      /**
       * Get the value.
       *
       * @return the value.
       */
      public final Serializable[] getValues()
      {
         return values;
      }
   }

   /**
   *
   * A ResponseValue.
   *
   * @author <a href="stefano.maestri@ironjacamar.org">Stefano Maestri</a>
   *
   */
   public static class RequestValues implements Serializable
   {
      /** Serial version uid */
      private static final long serialVersionUID = 1L;

      private final Request request;

      private final Serializable[] values;

      /**
       * Create a new RequestValue.
       *
       * @param request the request
       * @param values params to send with request
       */
      public RequestValues(Request request, Serializable... values)
      {
         super();
         this.request = request;
         this.values = values;
      }

      /**
       * Get the value.
       *
       * @return the value.
       */
      public final Serializable[] getValues()
      {
         return values;
      }

      /**
       * Get the request.
       *
       * @return the request.
       */
      public final Request getRequest()
      {
         return request;
      }

   }



}
