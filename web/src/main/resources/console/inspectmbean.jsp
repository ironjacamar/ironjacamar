<?xml version="1.0"?>
<%@page contentType="text/html"
   import="java.net.*,java.io.*,java.util.*,javax.management.*,javax.management.modelmbean.*,
           org.jboss.jca.web.console.*,
           org.dom4j.io.HTMLWriter,
           org.dom4j.tree.FlyweightCDATA,
           java.lang.reflect.Array,
           java.beans.PropertyEditor,
           org.jboss.util.propertyeditor.PropertyEditors"
%>

<%!
    private static final Comparator MBEAN_FEATURE_INFO_COMPARATOR = new Comparator()
    {
      public int compare(Object value1, Object value2)
      {
        MBeanFeatureInfo featureInfo1 = (MBeanFeatureInfo) value1;
        MBeanFeatureInfo featureInfo2 = (MBeanFeatureInfo) value2;

        String name1 = featureInfo1.getName();
        String name2 = featureInfo2.getName();

        return name1.compareTo(name2);
      }

      public boolean equals(Object other)
      {
        return this == other;
      }
    };

    String sep = System.getProperty("line.separator","\n");

    public String fixDescription(String desc)
    {
      if (desc == null || desc.equals(""))
      {
        return "(no description)";
      }
      return desc;
    }

    public String fixValue(Object value)
    {
        if (value == null)
            return null;
        String s = String.valueOf(value);
        StringWriter sw = new StringWriter();
        HTMLWriter hw = new HTMLWriter(sw);
        try
        {
           // hw.write(s); // strips whitespace
           hw.write(new FlyweightCDATA(s));
	   s = sw.toString();
        }
        catch(Exception e)
        {
        }
        return s;
    }

    public String fixValueForAttribute(Object value)
    {
        if (value == null)
            return null;
      String s = String.valueOf(value);
       StringWriter sw = new StringWriter();
       HTMLWriter hw = new HTMLWriter(sw);
       try
       {
          hw.write(s);
          s = sw.toString();
       }
       catch(Exception e)
       {
       }
       return s;
    }
    
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

<!DOCTYPE html 
    PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<html>

<head>
   <title>IronJacamar Management Console</title>
   <link rel="stylesheet" href="jboss.css" type="text/css" />
   <meta http-equiv="cache-control" content="no-cache" />
</head>

<jsp:useBean id='mbeanData' class='org.jboss.jca.web.console.MBeanData' scope='request'/>
<%
   if (mbeanData.getObjectName() == null)
   {
%>
<jsp:forward page="/" />
<%
   }
   ObjectName objectName = mbeanData.getObjectName();
   String objectNameString = mbeanData.getName();
   String quotedObjectNameString = URLEncoder.encode(mbeanData.getName(), "UTF-8");
   MBeanInfo mbeanInfo = mbeanData.getMetaData();
   MBeanAttributeInfo[] attributeInfo = mbeanInfo.getAttributes();
   MBeanOperationInfo[] operationInfo = mbeanInfo.getOperations();

   //FIXME: Seems to create ArrayIndexOutofBoundsException when uncommented
   /*Arrays.sort(attributeInfo, MBEAN_FEATURE_INFO_COMPARATOR);

   HashMap operationInfoIndexMap = new HashMap();
   for (int a = 0; a < operationInfo.length; a++)
   {
      MBeanOperationInfo opInfo = operationInfo[a];
      operationInfoIndexMap.put(opInfo, String.valueOf(a));
   }

   Arrays.sort(operationInfo, MBEAN_FEATURE_INFO_COMPARATOR);
   */
%>

<body leftmargin="10" rightmargin="10" topmargin="10">

<table>
 <tr>
  <td height="105" align="left"><h1>IronJacamar Management Console</h1></td>
  <td height="105" align="right" width="300" nowrap>
    <p>
      <input type="button" value="Back to Agent" onClick="javascript:location='HtmlAdaptor?action=displayMBeans'"/>
    </p>
    <p>
      <input type="button" value="Refresh MBean View" onClick="javascript:location='HtmlAdaptor?action=inspectMBean&amp;name=<%= URLEncoder.encode(request.getParameter("name"),"UTF-8") %>'"/>
    </p>
  </td>
 </tr>
</table>

&nbsp;

<%
   boolean odd = true;
%>

<!-- 1 -->

<table>
 <tr class='rowodd'><th>ObjectName</th><td colspan="2"><%= objectName %></td></tr>
 <tr class='roweven'><th>Java Class</th><td colspan="2"><jsp:getProperty name='mbeanData' property='className'/></td></tr>
 <tr class='rowodd'><th>Description</th><td colspan="2"><%= fixDescription(mbeanInfo.getDescription())%></td></tr>
</table>

<!-- 2 -->
<br/>
<%
 if (attributeInfo.length > 0) {
%>
<form method="post" action="HtmlAdaptor">
 <input type="hidden" name="action" value="updateAttributes" />
 <input type="hidden" name="name" value="<%= objectNameString %>" />
 <table>
  <tr>
   <th bgcolor='#d9e0e3'>Attribute Name</th>
   <th bgcolor='#d9e0e3'>Access</th>
   <th bgcolor='#d9e0e3'>Type</th>
   <th bgcolor='#d9e0e3'>Description</th>
   <th bgcolor='#d9e0e3'>Attribute Value</th>
  </tr>
<%
  odd = true;
  boolean hasWriteableAttribute=false;
  for(int a = 0; a < attributeInfo.length; a ++)
  {
    MBeanAttributeInfo attrInfo = attributeInfo[a];
    String attrName = attrInfo.getName();
    String attrType = attrInfo.getType();
    AttrResultInfo attrResult = Server.getMBeanAttributeResultInfo(objectNameString, attrInfo);
    String attrValue = attrResult.getAsText();
    String access = "";
    if (attrInfo.isReadable())
      access += "R";
    if (attrInfo.isWritable())
    {
      access += "W";
      hasWriteableAttribute = true;
    }
    String attrDescription = fixDescription(attrInfo.getDescription());
    if (odd)
    {
      out.println("  <tr class='rowodd'>");
    }
    else
    {
      out.println("  <tr class='roweven'>");
    }
    out.println("   <td>"+attrName+"</td>");
    out.println("   <td align='center'>"+access+"</td>");
    out.println("   <td>"+attrType+"</td>");
    out.println("   <td>"+attrDescription+"</td>");
    out.println("   <td>");
    out.println("    <pre>");

    if (attrInfo.isWritable())
    {
      String readonly = attrResult.getEditor() == null ? "class='readonly' readonly" : "class='writable'";
      if (attrType.equals("boolean") || attrType.equals("java.lang.Boolean"))
      {
        Boolean value = attrValue == null || "".equals( attrValue ) ? null : Boolean.valueOf(attrValue);
        String trueChecked = (value == Boolean.TRUE ? "checked" : "");
        String falseChecked = (value == Boolean.FALSE ? "checked" : "");
	String naChecked = value == null ? "checked" : "";
        out.print("<input type='radio' name='"+attrName+"' value='True' "+trueChecked+"/>True");
        out.print("<input type='radio' name='"+attrName+"' value='False' "+falseChecked+"/>False");
	// For wrappers, enable a 'null' selection
	if (attrType.equals("java.lang.Boolean") && PropertyEditors.isNullHandlingEnabled())
        {
           out.print("<input type='radio' name='"+attrName+"' value='' "+naChecked+"/>True");
	}
      }
      else if (attrInfo.isReadable())
      {
	attrValue = fixValueForAttribute(attrValue);
        if (String.valueOf(attrValue).indexOf(sep) == -1)
        {
          out.print("<input type='text' size='80' name='" + attrName + "' value='" + translateMetaCharacters(attrValue) + "' " + readonly + "/>");
        }
        else
        {
          out.print("<textarea cols='80' rows='10' type='text' name='" + attrName + "' "+readonly+">" + attrValue + "</textarea>");
        }
      }
      else
      {
        out.print("<input type='text' name='" + attrName + "' " + readonly + "/>");
      }
    }
    else
    {
      if (attrType.equals("[Ljavax.management.ObjectName;"))
      {
        ObjectName[] names = (ObjectName[]) Server.getMBeanAttributeObject(objectNameString, attrName);
        if (names != null)
        {
          for (int i = 0; i < names.length; i++)
          {
            out.print("<p align='center'><a href='HtmlAdaptor?action=inspectMBean&name=" + URLEncoder.encode(names[i] + "", "UTF-8") + ">" + names[i] + "</a></p>");
          }
        }
      }
      else if (attrType.startsWith("["))
      {
        Object arrayObject = Server.getMBeanAttributeObject(objectNameString, attrName);
        if (arrayObject != null)
        {
          for (int i = 0; i < Array.getLength(arrayObject); ++i)
          {
            out.println(fixValue(Array.get(arrayObject,i)));
          }
        }
      }
      else
      {
        out.print(fixValue(attrValue));
      }
    }

    if (attrType.equals("javax.management.ObjectName"))
    {
      if (attrValue != null)
      {
        out.print("<p align='center'><a href='HtmlAdaptor?action=inspectMBean&name="+URLEncoder.encode(attrValue,"UTF-8")+"'>View MBean</a></p>");
      }
    }
    out.println("    </pre>");
    out.println("   </td>");
    out.println("  </tr>");
    odd = !odd;
  }

  if (hasWriteableAttribute)
  {
    out.println(" <tr><td colspan='4'></td><td><p align='center'><input type='submit' value='Apply Changes'/></p></td></tr>");
  }
%>
 </table>
</form>

<%
 }
%>

<!-- 3 -->
<br/>
<%
if (operationInfo.length > 0)
{
  out.println(" <table>");
  out.println("  <tr>");
  out.println("   <th bgcolor='#d9e0e3'>Operation</th>");
  out.println("   <th bgcolor='#d9e0e3'>Return Type</th>");
  out.println("   <th bgcolor='#d9e0e3'>Description</th>");
  out.println("   <th bgcolor='#d9e0e3'>Parameters</th>");
  out.println("  </tr>");

  odd = true;
  for (int a = 0; a < operationInfo.length; a++)
  {
    MBeanOperationInfo opInfo = operationInfo[a];
    boolean accept = true;
    if (opInfo instanceof ModelMBeanOperationInfo)
    {
      Descriptor desc = ((ModelMBeanOperationInfo)opInfo).getDescriptor();
      String role = (String)desc.getFieldValue("role");
      if ("getter".equals(role) || "setter".equals(role))
      {
        accept = false;
      }
    }
    if (accept)
    {
      MBeanParameterInfo[] sig = opInfo.getSignature();
      if (odd)
      {
        out.println("  <tr class='rowodd'>");
      }
      else
      {
        out.println("  <tr class='roweven'>");
      }
      out.println("   <td>" + opInfo.getName() + "</td>");
      out.println("   <td>" + opInfo.getReturnType() + "</td>");
      out.println("   <td>" + fixDescription(opInfo.getDescription()) + "</td>");
      out.println("   <td align='center'>");
      out.println("    <form method='post' action='HtmlAdaptor'>");
      out.println("     <input type='hidden' name='action' value='invokeOp'/>");
      out.println("     <input type='hidden' name='name' value='" + quotedObjectNameString + "'/>");
      out.println("     <input type='hidden' name='methodIndex' value='" + a + "'/>");

      if (sig.length > 0)
      {
        out.println("     <table>");
        for (int p = 0; p < sig.length; p++)
        {
          MBeanParameterInfo paramInfo = sig[p];
          String pname = paramInfo.getName();
          String ptype = paramInfo.getType();
          if (pname == null || pname.length() == 0 || pname.equals(ptype))
          {
            pname = "arg" + p;
          }
          String pdesc = fixDescription(paramInfo.getDescription());
          out.println("      <tr>");
          out.println("       <td>" + pname + "</td>");
          out.println("       <td>" + ptype + "</td>");
          out.println("       <td>" + pdesc + "</td>");
          out.print("       <td width='50'>");
          if (ptype.equals("boolean")||ptype.equals("java.lang.Boolean"))
          {
            out.print("<input type='radio' name='arg" + p + "' value='True' checked/>True");
            out.print("<input type='radio' name='arg" + p + "' value='False'/>False");
          }
          else
          {
            out.print("<input type='text' class='writable' name='arg" + p + "'/>");
          }
          out.println("</td>");
          out.println("      </tr>");
        }
        out.println("     </table>");
      }
      else
      {
        out.println("     [no parameters]<BR>");
      }
      out.println("     <input type='submit' value='Invoke'/>");
      out.println("    </form>");
      out.println("  </td>");
      out.println(" </tr>");
      odd = !odd;
    }
  }
  out.println(" </table>");
}
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
