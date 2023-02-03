/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2009, Red Hat Inc, and individual contributors
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
package org.jboss.jca.web.console;

import java.io.IOException;
import java.net.URLDecoder;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;

import javax.management.AttributeList;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.jboss.logging.Logger;

/** 
 * The HTML adaptor controller servlet.
 *
 * @author <a href="mailto:sstark@redhat.com">Scott Stark</a>
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class HtmlAdaptorServlet extends HttpServlet
{
   private static final long serialVersionUID = 1L;

   private static Logger log = Logger.getLogger(HtmlAdaptorServlet.class);

   private static final String ACTION_PARAM = "action";
   private static final String DISPLAY_MBEANS_ACTION = "displayMBeans";
   private static final String INSPECT_MBEAN_ACTION = "inspectMBean";
   private static final String UPDATE_ATTRIBUTES_ACTION = "updateAttributes";
   private static final String INVOKE_OP_ACTION = "invokeOp";
   private static final String INVOKE_OP_BY_NAME_ACTION = "invokeOpByName";

   /** 
    * Constructor
    */
   public HtmlAdaptorServlet()
   {
   }

   /**
    * Init
    * @param config The servlet configuration
    * @exception ServletException Thrown if an error occurs
    */
   public void init(ServletConfig config) throws ServletException
   {
      super.init(config);
   }

   /**
    * Destroy
    */
   public void destroy()
   {
   }
   
   /**
    * GET
    * @param request The HTTP request
    * @param response The HTTP response
    * @exception ServletException Thrown if an error occurs
    * @exception IOException Thrown if an I/O error occurs
    */
   protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException
   {
      processRequest(request, response);
   }

   /**
    * POST
    * @param request The HTTP request
    * @param response The HTTP response
    * @exception ServletException Thrown if an error occurs
    * @exception IOException Thrown if an I/O error occurs
    */
   protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException
   {
      processRequest(request, response);
   }

   /**
    * Process the request
    * @param request The HTTP request
    * @param response The HTTP response
    * @exception ServletException Thrown if an error occurs
    * @exception IOException Thrown if an I/O error occurs
    */
   protected void processRequest(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException
   {
      String action = request.getParameter(ACTION_PARAM);

      if (action == null)
         action = DISPLAY_MBEANS_ACTION;

      if (action.equals(DISPLAY_MBEANS_ACTION))
         displayMBeans(request, response);
      else if (action.equals(INSPECT_MBEAN_ACTION))
         inspectMBean(request, response);
      else if (action.equals(UPDATE_ATTRIBUTES_ACTION))
         updateAttributes(request, response);
      else if (action.equals(INVOKE_OP_ACTION))
         invokeOp(request, response);
      else if (action.equals(INVOKE_OP_BY_NAME_ACTION))
         invokeOpByName(request, response);
   }

   /**
    * Display all MBeans
    * @param request The HTTP request
    * @param response The HTTP response
    * @exception ServletException Thrown if an error occurs
    * @exception IOException Thrown if an I/O error occurs
    */
   private void displayMBeans(HttpServletRequest request, HttpServletResponse response) 
      throws ServletException, IOException
   {
      Iterator mbeans;

      try
      {
         mbeans = getDomainData();
      }
      catch (Exception e)
      {
         throw new ServletException("Failed to get MBeans", e);
      }

      request.setAttribute("mbeans", mbeans);
      RequestDispatcher rd = this.getServletContext().getRequestDispatcher("/displaymbeans.jsp");
      rd.forward(request, response);
   }

   /**
    * Display a MBeans attributes and operations
    * @param request The HTTP request
    * @param response The HTTP response
    * @exception ServletException Thrown if an error occurs
    * @exception IOException Thrown if an I/O error occurs
    */
   private void inspectMBean(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException
   {
      String name = request.getParameter("name");

      log.tracef("inspectMBean, name=%s", name);

      try
      {
         MBeanData data = getMBeanData(name);
         request.setAttribute("mbeanData", data);

         RequestDispatcher rd = this.getServletContext().getRequestDispatcher("/inspectmbean.jsp");
         rd.forward(request, response);
      }
      catch (Exception e)
      {
         throw new ServletException("Failed to get MBean data", e);
      }
   }
   
   /**
    * Update the writable attributes of a MBean
    * @param request The HTTP request
    * @param response The HTTP response
    * @exception ServletException Thrown if an error occurs
    * @exception IOException Thrown if an I/O error occurs
    */
   private void updateAttributes(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException
   {
      String name = request.getParameter("name");

      log.tracef("updateAttributes, name=%s", name);

      Enumeration paramNames = request.getParameterNames();
      HashMap<String, String> attributes = new HashMap<String, String>();

      while (paramNames.hasMoreElements())
      {
         String param = (String)paramNames.nextElement();

         if (param.equals("name") || param.equals("action"))
            continue;

         String value = request.getParameter(param);

         log.tracef("name=%s, value='%s'", param, value);

         // Ignore null values, these are empty write-only fields
         if (value == null || value.length() == 0)
            continue;

         attributes.put(param, value);
      }

      try
      {
         AttributeList newAttributes = setAttributes(name, attributes);
         MBeanData data = getMBeanData(name);
         request.setAttribute("mbeanData", data);

         RequestDispatcher rd = this.getServletContext().getRequestDispatcher("/inspectmbean.jsp");
         rd.forward(request, response);
      }
      catch (Exception e)
      {
         throw new ServletException("Failed to update attributes", e);
      }
   }

   /** 
    * Invoke a MBean operation given the index into the MBeanOperationInfo{}
    * array of the mbean.
    * @param request The HTTP request
    * @param response The HTTP response
    * @exception ServletException Thrown if an error occurs
    * @exception IOException Thrown if an I/O error occurs
    */
   private void invokeOp(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException
   {
      String reqname = request.getParameter("name");

      String name = URLDecoder.decode(reqname, "UTF-8");

      log.tracef("invokeOp, name=%s", name);

      String[] args = getArgs(request);
      String methodIndex = request.getParameter("methodIndex");

      if (methodIndex == null || methodIndex.length() == 0)
         throw new ServletException("No methodIndex given in invokeOp form");

      int index = Integer.parseInt(methodIndex);
      try
      {
         OpResultInfo opResult = invokeOp(name, index, args);
         request.setAttribute("opResultInfo", opResult);

         RequestDispatcher rd = this.getServletContext().getRequestDispatcher("/displayopresult.jsp");
         rd.forward(request, response);
      }
      catch (Exception e)
      {
         throw new ServletException("Failed to invoke operation", e);
      }
   }

   /**
    * Invoke a MBean operation given the method name and its signature.
    * @param request The HTTP request
    * @param response The HTTP response
    * @exception ServletException Thrown if an error occurs
    * @exception IOException Thrown if an I/O error occurs
    */
   private void invokeOpByName(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException
   {
      String name = request.getParameter("name");

      log.tracef("invokeOpByName, name=%s", name);

      String[] argTypes = request.getParameterValues("argType");
      String[] args = getArgs(request);
      String methodName = request.getParameter("methodName");

      if (methodName == null)
         throw new ServletException("No methodName given in invokeOpByName form");

      try
      {
         OpResultInfo opResult = invokeOpByName(name, methodName, argTypes, args);
         request.setAttribute("opResultInfo", opResult);

         RequestDispatcher rd = this.getServletContext().getRequestDispatcher("/displayopresult.jsp");
         rd.forward(request, response);
      }
      catch (Exception e)
      {
         throw new ServletException("Failed to invoke operation", e);
      }
   }

   /** 
    * Extract the argN values from the request into a String[]
    * @param request The HTTP request
    * @return The argument values
    */
   private String[] getArgs(HttpServletRequest request)
   {
      ArrayList<String> argList = new ArrayList<String>();

      for (int i = 0; true; i++)
      {
         String name = "arg" + i;
         String value = request.getParameter(name);

         if (value == null)
            break;

         argList.add(value);

         log.tracef("%s=%s", name, value);
      }

      String[] args = new String[argList.size()];
      argList.toArray(args);
      return args;
   }

   /** 
    * Get the MBean data for a bean
    * @param name The name of the bean
    * @return The data
    * @exception PrivilegedExceptionAction Thrown if the operation cannot be performed
    */
   private MBeanData getMBeanData(final String name) throws PrivilegedActionException
   {
      return AccessController.doPrivileged(new PrivilegedExceptionAction<MBeanData>()
      {
         public MBeanData run() throws Exception
         {
            return Server.getMBeanData(name);
         }
      });
   }
   
   /** 
    * Get the domain data
    * @return A data iterator
    * @exception PrivilegedExceptionAction Thrown if the operation cannot be performed
    */
   @SuppressWarnings("unchecked")
   private Iterator getDomainData() throws PrivilegedActionException
   {
      return AccessController.doPrivileged(new PrivilegedExceptionAction<Iterator>()
      {
         public Iterator run() throws Exception
         {
            return Server.getDomainData();
         }
      });
   }
   
   /** 
    * Invoke an operation on a MBean
    * @param name The name of the bean
    * @param index The operation index
    * @param args The operation arguments
    * @return The operation result
    * @exception PrivilegedExceptionAction Thrown if the operation cannot be performed
    */
   private OpResultInfo invokeOp(final String name, final int index, final String[] args) 
      throws PrivilegedActionException
   {
      return AccessController.doPrivileged(new PrivilegedExceptionAction<OpResultInfo>()
      {
         public OpResultInfo run() throws Exception
         {
            return Server.invokeOp(name, index, args);
         }
      });
   }
   
   /**
    * Invoke an operation on a MBean
    * @param name The name of the bean
    * @param methodName The operation name
    * @param argTypes The argument types
    * @param args The operation arguments
    * @return The operation result
    * @exception PrivilegedExceptionAction Thrown if the operation cannot be performed
    */
   private OpResultInfo invokeOpByName(final String name, 
                                       final String methodName,
                                       final String[] argTypes,
                                       final String[] args)
      throws PrivilegedActionException
   {
      return AccessController.doPrivileged(new PrivilegedExceptionAction<OpResultInfo>()
      {
         public OpResultInfo run() throws Exception
         {
            return Server.invokeOpByName(name, methodName, argTypes, args);
         }
      });
   }
   
   /**
    * Set attributes on a MBean
    * @param name The name of the bean
    * @param attributes The attributes
    * @return The updated attributes list
    * @exception PrivilegedExceptionAction Thrown if the operation cannot be performed
    */
   @SuppressWarnings({ "unchecked" })
   private AttributeList setAttributes(final String name, final HashMap attributes) throws PrivilegedActionException
   {
      return AccessController.doPrivileged(new PrivilegedExceptionAction<AttributeList>()
      {
         public AttributeList run() throws Exception
         {
            return Server.setAttributes(name, attributes);
         }
      });
   }
}
