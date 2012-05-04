<?xml version="1.0"?>
<%@page contentType="text/html"
   import="java.net.*,
           java.io.*,
   	   java.beans.PropertyEditor,
   	   org.jboss.util.propertyeditor.PropertyEditors"
%>

<!DOCTYPE html 
    PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<html>
<head>
   <title>IronJacamar Management Console</title>
   <link rel="stylesheet" href="jboss.css" type="text/css" />
   <link rel="shortcut icon" href="logo.ico" />
   <meta http-equiv="cache-control" content="no-cache" />
</head>

<jsp:useBean id='opResultInfo' class='org.jboss.jca.web.console.OpResultInfo' type='org.jboss.jca.web.console.OpResultInfo' scope='request'/>
<%
   if (opResultInfo.getName() == null)
   {
%>
  	<jsp:forward page="/" />

<%
   }
%>    
<body leftmargin="10" rightmargin="10" topmargin="10">

<table>
 <tr>
  <td height="105" align="left"><h1>IronJacamar Management Console</h1></td>
  <td height="105" align="right" width="300">
    <p>
      <input type="button" value="Back to Agent" onClick="javascript:location='HtmlAdaptor?action=displayMBeans'"/>
    </p>
    <p>
      <input type="button" value="Back to MBean" onClick="javascript:location='HtmlAdaptor?action=inspectMBean&amp;name=<%= request.getParameter("name") %>'"/>
    </p>
    <p>
    <%
      out.print("<input type='button' onClick=\"location='HtmlAdaptor?action=invokeOpByName");
      out.print("&amp;name=" + request.getParameter("name"));
      out.print("&amp;methodName=" + opResultInfo.getName());
    
      for (int i = 0; i < opResultInfo.getArguments().length; i++)
      {
        out.print("&amp;argType=" + opResultInfo.getSignature()[i]);
        out.print("&amp;arg" + i + "=" + opResultInfo.getArguments()[i]);
      }
    
      out.println("'\" value='Reinvoke MBean Operation'/>");
    %>
    </p>
  </td>
 </tr>
</table>

<p>
<%
   if (opResultInfo.getResult() == null)
   {
     out.println("Operation completed successfully without a return value!");
   }
   else
   {
      String opResultString = null;

      PropertyEditor propertyEditor = PropertyEditors.findEditor(opResultInfo.getResult().getClass());
      if (propertyEditor != null)
      {
         propertyEditor.setValue(opResultInfo.getResult());
         opResultString = propertyEditor.getAsText();
      }
      else
      {
         opResultString = opResultInfo.getResult().toString();
      }

      boolean hasPreTag = opResultString.startsWith("<pre>");
      if (!hasPreTag)
         out.println("<pre>");

      out.println(opResultString);

      if (!hasPreTag)
          out.println("</pre>");
   }
%>
</p>

  <table width="100%" class="copyright">
    <tr>
      <td class="copyright">
        Copyright &#169; 2012  <a href="http://www.jboss.org/ironjacamar" target="_blank">JBoss, by Red Hat</a>
      </td>
    </tr>
  </table>

</body>
</html>
