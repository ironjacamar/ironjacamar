<?xml version="1.0"?>
<%@page contentType="text/html" import="java.io.*,java.net.*,java.util.*,org.jboss.jca.web.console.*"%>

<%!
 
   /**
    * Translate HTML tags and single and double quotes.
    */
   public String translateMetaCharacters(Object value)
   {
      if(value == null) 
         return null;
          
      String s = String.valueOf(value);   
      String sanitizedName = s.replace("<", "&lt;");
      sanitizedName = sanitizedName.replace(">", "&gt;");
      sanitizedName = sanitizedName.replace("\"", "&quot;");
      sanitizedName = sanitizedName.replace("\'", "&apos;");
      return sanitizedName;
   }
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
<head>
  <title>IronJacamar Management Console</title>
  <link rel="stylesheet" href="jboss.css" type="text/css" />
  <link rel="shortcut icon" href="logo.ico" />
  <meta http-equiv="cache-control" content="no-cache"/>
</head>

<body>

  <table>
    <tr>
      <td><img src="/console/logo.png" alt="IronJacamar"/></td>
    </tr>
  </table>
  
  &nbsp;

<%
   out.println("<table>");
   Iterator mbeans = (Iterator) request.getAttribute("mbeans");
   int i = 0;
   while( mbeans.hasNext() )
   {
      DomainData domainData = (DomainData) mbeans.next();
      out.println(" <tr>");
      out.println("  <th>");
      out.println("   <h2>" + domainData.getDomainName() + "</h2>");
      out.println("  </th>");
      out.println(" </tr>");
      out.println(" <tr>");
      out.println("  <td>");
      out.println("    <ul>");
      MBeanData[] data = domainData.getData();
      for (int d = 0; d < data.length; d++)
      {
         String name = data[d].getObjectName().toString();
         String properties = translateMetaCharacters(data[d].getNameProperties());
         out.println("     <li><a href=\"HtmlAdaptor?action=inspectMBean&amp;name=" + URLEncoder.encode(name, "UTF-8") + "\">"+URLDecoder.decode(properties, "UTF-8")+"</a></li>");
      }
      out.println("   </ul>");
      out.println("  </td>");
      out.println(" </tr>");
   }
   out.println("</table>");
%>

  <table width="100%" class="copyright">
    <tr>
      <td class="copyright">
        Copyright &#169; 2012  <a href="http://www.jboss.org/ironjacamar" target="_blank">JBoss, by Red Hat</a>
      </td>
    </tr>
  </table>


</body>
</html>
