<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ page import="java.net.*, org.mu
  String hostname = null;
String currentHostUrl = null;
try {

  if (hostname == null) {
    hostname = (String)getServletContext().getAttribute(HttpServicesImpl.BOUND_HOST_NAME_KEY);
    
    // determine the requesting URL
    currentHostUrl = HttpUtils.getRequestURL(request).toString();

    // check it to see if the hostname is in it
    if (!currentHostUrl.startsWith("http://" + hostname + ":")) {
  String configuredHostUrl = currentHostUrl.replaceFirst("http://[a-zA-Z0-9.]*:", 
      "http://" + hostname + ":");

  // do redirect
  response.sendRedirect(configuredHostUrl);
    }
  }

} catch (Exception e) {
  System.out.println(e.getMessage());
}

String URL2Here = currentHostUrl.substring(0, currentHostUrl.length() - "index.jsp".length());
String descriptorModel = (String)getServletContext().getAttribute(HttpServicesImpl.SERVER_MODEL_URI_KEY) + "#descriptors";
ERVER_MODEL_URI_KEY) + "#descriptors";
  
%>

<html>
<head>
  <title>Mulgara Descriptor</title>
<link media="all" href="/all.css" type="text/css" title="Default" rel="stylesheet">
<link media="screen" href="/default.css" type="text/css" title="Default" rel="stylesheet">
<link media="print" href="/print.css" type="text/css" rel="stylesheet">
<link href="/burnt.css" title="Burnt" media="screen" type="text/css" rel="alternate stylesheet">
<link rel="icon" type="text/png" href="/images/icons/siteicon.png">
<link rel="shortcut icon" type="text/png" href="/images/icons/siteicon.png">
</head>

<body>
<div id="container">

  <!-- Banner -->
  <div id="banner">
    <h1>mulgara.sourceforge.net</h1>
  </div>

  <div id="content">
    <div id="breadcrumb">
      [ Location:
      <ul>
        <li>descriptors</li>
      </ul>  ]
  </div>  
  <h1>Mulgara Descriptors</h1>
  <ul>
    <li><a href="#overview">Overview</a>
    <li><a href="#tasks">Tasks</a>
    <li><a href="doc/">Documentation</a> 
    <li><a href="examples/">Examples</a>
    <li><a href="tutorial/">Tutorials</a>
  </ul>  

<p/>
  <a name="overview"/>
  <h1>Overview</h1></a>
  Descriptors:
  <ul>
    <li>Allow complex or frequently used queries to be named and invoked by a client. 
    <li>Can perform an iTQL command, including inserting data into Mulgara.
    <li>May return XML, HTML, text or anything XSL is capable of producing.
    <li>Output can be deserialized into java objects.
    <li>Expose their interfaces using RDF stored on a Mulgara server.  
    <li>Are accessible as SOAP services for 3rd party integration.
    <li>May be changed on the fly even if interfaces change.
    <li>Are stored somewhere accessible as a URL such as on a webserver.  
    <li>May call other descriptors in order to perform a sub task. 
  </ul>
  </p>

  
  <a name="tasks"/>
  <h1>Descriptor Tasks</h1></a>
<p>
<p/>
<b>NOTE</b> Buttons labeled 'Invoke' invoke descriptors, therefore some may not work until the descriptors are deployed, which is a task available below. e.g. The List Descriptors avilable task wil not work until the list descriptors descriptor has been deployed.
<p/>


<!-- invoke wizard -->
<p>
<form action="execute">
  <input type="submit" value="Invoke"/>
  Create Descriptor using Wizard&nbsp;&nbsp; 
  <input type="hidden" name="_self" value="<%= URL2Here %>descriptors/default/descriptorWizard.xsl"/>
</form>
</p>

<hr/>

<!-- redeploy descriptors -->
<p>
<form action="deploy">
  <input type="submit" value="ReDeploy"/>
  Redeploy bundled Descriptors - drops existing Descriptors from Mulgara, reloads built-in descriptors, clears descriptors from cache&nbsp;&nbsp; 
  <input type="hidden" name="deployLocalDescriptors" value="true"/>
  <input type="hidden" name="clearLocalDescriptors" value="true"/>
</form>
</p>

<hr/>
<!-- deploy descriptors -->
<p>
<form action="deploy">
  <input type="submit" value="Deploy"/>
  Deploy bundled Descriptors - preserves any existing Descriptors in Mulgara, reloads built-in descriptors, clears descriptors from cache&nbsp;&nbsp; 
  <input type="hidden" name="deployLocalDescriptors" value="true"/>
</form>
</p>

<hr/>
<!-- list on this host -->

<p>
<form action="execute">
  <input type="submit" value="Invoke"/>
  See List of Descriptors available on this host&nbsp;&nbsp; 
  <input type="hidden" name="_self" value="<%= URL2Here %>descriptors/default/descriptorListHTML.xsl"/>
  <input type="hidden" name="descriptorBase" value="<%= URL2Here %>descriptors/"/>
  <input type="hidden" name="model" value="<%= descriptorModel %>"/>
</form>
</p>

<hr/>

<!-- list on other host -->

<p>
<form action="execute">
  <input type="submit" value="Invoke"/>
  See list of Descriptors available from Mulgara model&nbsp;&nbsp;
  <input type="hidden" name="_self" value="<%= URL2Here %>descriptors/default/descriptorListHTML.xsl"/>
  <input type="hidden" name="descriptorBase" value="<%= URL2Here %>descriptors/"/>
  <input type="text" name="model" size="40"/>
</form>
</p>

<hr/>

<!-- purge from other host -->

<p>
<form action="execute">
  <input type="submit" value="Invoke"/>
Purge all cached Descriptors available on this host, list of descriptors available will be shown.
  <input type="hidden" name="_self" value="<%= URL2Here %>descriptors/default/descriptorListHTML.xsl"/>
  <input type="hidden" name="descriptorBase" value="<%= URL2Here %>descriptors/"/>
  <input type="hidden" name="_clearCache" value="true"/>
  <input type="hidden" name="model" value="<%= descriptorModel %>"/>
</form>
</p>

<hr/>

<!-- purge from other host -->

<p>
<form action="execute">
  <input type="submit" value="Invoke"/>
Purge Descriptors from this Mulgara model, list of descriptors available will be shown.&nbsp;&nbsp;
  <input type="hidden" name="_self" value="<%= URL2Here %>descriptors/default/descriptorListHTML.xsl"/>
  <input type="hidden" name="descriptorBase" value="<%= URL2Here %>descriptors/"/>
  <input type="hidden" name="_clearCache" value="true"/>
  <input type="text" name="model" size="40"/>
</form>
</p>


</div></div>
</body>
</html>
