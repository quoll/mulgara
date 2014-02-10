<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:stylesheet
  version="1.0"
  xmlns="http://www.w3.org/1999/xhtml"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
  xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
  xmlns:lxslt="http://xml.apache.org/xslt"
  xmlns:xalan="http://xml.apache.org/xalan"
  xmlns:mulgaraDescriptor="mulgaraDescriptor"
  extension-element-prefixes="mulgaraDescriptor"
  exclude-result-prefixes="xsl rdf rdfs lxslt mulgaraAnswer xalan ns1"
  xmlns:mulgaraAnswer="http://mulgara.org/tql#"
  xmlns:desc="http://mulgara.org/descriptor#"
  xmlns:ns1="urn:Query">

  <!-- ============================================== -->
  <!-- Assemble the parameters as used in the query  -->
  <!-- ============================================== -->
  <!-- DESCRIPTOR PARAMS -->
  <xsl:param name="_usage"/> <!-- is set return the USAGE RDF -->
  <xsl:param name="_self"/>

  <!-- USER PARAMS -->
  <xsl:param name="model"/>
  <xsl:param name="url"/>

  <!-- ============================================== -->
  <!-- Match the Solution -->
  <!-- ============================================== -->
  <xsl:template match="/">
    <xsl:choose>
      <xsl:when test="$_usage">
        <xsl:call-template name="usage"/>
      </xsl:when>
      <xsl:otherwise>
        <!-- EXECUTE THE QUERY to get Reflector info  -->
        <xsl:variable name="answer">
          <mulgaraDescriptor:query url="{$url}" model="{$model}">
            <![CDATA[
              select $title $pname $pdesc $ptype from <@@model@@> where
              <@@url@@> <http://purl.org/dc/elements/1.1/title> $title and
              <@@url@@> <http://mulgara.org/descriptor#hasParam> $param and
              $param <http://mulgara.org/descriptor#name> $pname and
              $param <http://mulgara.org/descriptor#description> $pdesc and
              $param <http://mulgara.org/descriptor#type> $ptype;
            ]]>
          </mulgaraDescriptor:query>
        </xsl:variable>
        <!-- Now apply the templates to the answer -->
        <xsl:apply-templates select="xalan:nodeset($answer)/*"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="mulgaraAnswer:query">
    <html>
    <head>
    <title>Descriptor HTML for <xsl:value-of select="$url"/></title>
    <link media="all" href="/all.css" type="text/css" title="Burnt" rel="stylesheet"/>
    <link media="screen" href="/burnt.css" type="text/css" title="Burnt" rel="stylesheet"/>
    <link href="/grey.css" title="Grey" media="screen" type="text/css" rel="alternate stylesheet"/>
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
        <li> <a href="/webservices/descriptor/" title="descriptors">descriptors</a> </li>
        <ul>
          <li>html descriptor</li>
        </ul>
      </ul>  ]
    </div>  


    <!-- first get the title -->
    <h2>Descriptor URL</h2>
    <a href="{$url}" target="xslsource"><xsl:value-of select="$url"/></a>
    <h2><xsl:value-of select="mulgaraAnswer:solution/mulgaraAnswer:title/text()"/></h2>

    <form action="execute">
    <!-- now set up params -->
    <table border="2" cellpadding="3">
    <tr>
    <th colspan="4">
    Descriptor Parameters
    </th>
    </tr>
    <tr>
      <td align="center"><b>Name</b></td>
      <td align="center"><b>Type</b></td>
      <td align="center"><b>Description</b></td>
      <td align="center"><b>Value</b></td>
    </tr>
    <tr>
      <td>usage</td>
      <td>any value</td>
      <td>If usage is set the RDF statements about this Descriptor are returned</td>
      <td></td>
    </tr>
    <xsl:for-each select="mulgaraAnswer:solution/mulgaraAnswer:pname">
    <tr>
      <td>
        <!-- name -->
        <xsl:value-of select="text()"/>
      </td>
      <td>
        <!-- type -->
        <xsl:value-of select="../mulgaraAnswer:ptype/text()"/>
      </td>
      <td>
        <!-- description -->
        <xsl:value-of select="../mulgaraAnswer:pdesc/text()"/>
      </td>
      <td>

        <!-- value - set it to the URL if the name is _self -->
        <xsl:choose>

          <!-- _self - show unmodifiable version -->
          <xsl:when test="text()='_self'">
            <input type="hidden" name="{text()}" value="{$url}"/>
            Full URL as above 
          </xsl:when>
         

          <!-- any other param - show text box -->
          <xsl:otherwise>
            <input type="text" name="{text()}"/>
          </xsl:otherwise>
          
        </xsl:choose>

      </td>
    </tr>
    </xsl:for-each>
    <tr>
      <td colspan="3">Override configured mime type</td>
      <td>
        text/plain<input type="checkbox" name="_mimeType" value="text/plain"/>
      </td>
    </tr>
    <tr>
      <td colspan="4" align="center">
      <input type="submit" value="Invoke Cached Instance"/>
      <input type="submit" name="_clearCache" value="Invoke New Instance"/>
      </td>
    </tr>
    </table>

    </form>
      
<h2>Sample VB SOAP Client</h2>
<pre>
<![CDATA[
Option Explicit

Const ERR_SOAP_FAULT = 1

]]>
' Sample VB SOAP Code to access <xsl:value-of select="$url"/><![CDATA[
'
' function to perform the invokeDescriptor SOAP call
' returns the resulting xml if successfull
' raises an error with the code (vbObjectError + ERR_SOAP_FAULT) if a SOAP fault occurs
Private Function InvokeDescriptor() As String
    
    Dim EndPointURL As String
    Dim NameSpace As String
    Dim Method As String
    Dim Serializer As SoapSerializer
    Dim Reader As SoapReader
    Dim ResultElm As IXMLDOMElement
    Dim FaultElm As IXMLDOMElement
    Dim Connector As SoapConnector
    
    EndPointURL = "http://localhost:8080/soap/servlet/rpcrouter"
    NameSpace = "urn:Descriptor"
    Method = "invokeDescriptor"
    
    Set Connector = New HttpConnector
    Connector.Property("EndPointURL") = EndPointURL
    Connector.Connect
    
    Connector.Property("SoapAction") = "uri:" & Method
    Connector.BeginMessage
    
    Set Serializer = New SoapSerializer
    Serializer.Init Connector.InputStream
    
    Serializer.startEnvelope
    Serializer.startBody
    Serializer.startElement Method, NameSpace, "http://xml.apache.org/xml-soap/literalxml", "m"
    
    ' This is how to add parameters
    Serializer.startElement "name"
    Serializer.startElement "params"
    ]]>
<xsl:for-each select="mulgaraAnswer:solution/mulgaraAnswer:pname">
<xsl:choose>
<xsl:when test="text()='_self'">
    Serializer.startElement "_self"
    Serializer.writeString "<xsl:value-of select="$url"/>"
    Serializer.endElement
</xsl:when>
<xsl:otherwise>
    Serializer.startElement "<xsl:value-of select="text()"/>"
    Serializer.writeString " TODO INSERT <xsl:value-of select="text()"/> VALUE HERE AS A <xsl:value-of select="../mulgaraAnswer:ptype/text()"/>"
    Serializer.endElement
</xsl:otherwise>
</xsl:choose>
</xsl:for-each>
<![CDATA[
    Serializer.endElement
    Serializer.endElement
    Serializer.endElement
    Serializer.endBody
    Serializer.endEnvelope
    
    Connector.EndMessage
    
    Set Reader = New SoapReader
    Reader.Load Connector.OutputStream
    
    If Not Reader.Fault Is Nothing Then
        Err.Raise vbObjectError + ERR_SOAP_FAULT, , Reader.faultstring.Text
    Else
        InvokeDescriptor = Reader.Envelope.xml
    End If
    
End Function

Private Sub Command1_Click()

    On Error GoTo ErrorHandler
    
    MsgBox InvokeDescriptor(), vbOKOnly, "HI!"
    
    Exit Sub
    
ErrorHandler:

    If Err.Number = vbObjectError + ERR_SOAP_FAULT Then
        MsgBox Err.Description, vbCritical, "SOAP Fault!"
    Else
        MsgBox Err.Description, vbCritical, "Error!"
    End If
    
End Sub
]]>
</pre>

<h2>Sample Java SOAP Client</h2>
TODO
<h2>Sample PERL SOAP Client</h2>
<pre>
<![CDATA[
#!/usr/bin/perl -w

use SOAP::Lite ; 
# uncomment line below and comment line above to turn on debugging 
# use SOAP::Lite +trace => qw(transport debug);

print SOAP::Lite
-> uri('urn:Descriptor')
-> proxy('http://localhost:8080/soap/servlet/rpcrouter')
-> invokeDescriptor(
    SOAP::Data
      ->encodingStyle('http://xml.apache.org/xml-soap/literalxml')
      ->name("name" => { "params" => { 
      ]]>
<xsl:for-each select="mulgaraAnswer:solution/mulgaraAnswer:pname">
<xsl:choose>
<xsl:when test="text()='_self'">
        _self => "<xsl:value-of select="$url"/>",
</xsl:when>
<xsl:otherwise>
  <xsl:value-of select="'        '"/><xsl:value-of select="text()"/> => "INSERT <xsl:value-of select="text()"/> VALUE HERE AS A <xsl:value-of select="../mulgaraAnswer:ptype/text()"/>",
</xsl:otherwise>
</xsl:choose>
</xsl:for-each>
<![CDATA[
      }})
  )          
-> result;
]]>
</pre>

    </div></div>
    </body>
    </html>

  </xsl:template>


  <!-- #################################################################### -->
  <!-- Calls a java class for queries -->
  <!-- #################################################################### -->
  <lxslt:component prefix="mulgaraDescriptor" elements="descriptor query debug" functions="test">
    <lxslt:script lang="javaclass" src="xalan://org.mulgara.descriptor.DescriptorElement"/>
  </lxslt:component>


  <!-- #################################################################### -->
  <!-- USAGE of this descriptor                                             -->
  <!-- #################################################################### -->
  <xsl:template name="usage">
    <rdf:RDF
      xml:lang="en"
      xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
      xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
      xmlns:dc="http://purl.org/dc/elements/1.1/"
      xmlns:desc="http://mulgara.org/descriptor#">

      <!-- Descriptor metadata -->
      <desc:Descriptor rdf:about="">

        <dc:title>Extracts Descriptor Information from a Store as HTML</dc:title>

        <desc:hasParam>
          <desc:Param>
            <desc:name>_self</desc:name>
            <desc:type>String</desc:type>
            <desc:description>The URL of this Descriptor</desc:description>
            <desc:required>Yes</desc:required>
          </desc:Param>
        </desc:hasParam>

        <desc:hasParam>
          <desc:Param>
            <desc:name>url</desc:name>
            <desc:type>String</desc:type>
            <desc:description>The URL of the Descriptor to extract information</desc:description>
            <desc:required>Yes</desc:required>
          </desc:Param>
        </desc:hasParam>

        <desc:hasParam>
          <desc:Param>
            <desc:name>model</desc:name>
            <desc:type>String</desc:type>
            <desc:description>The URI of the model to query</desc:description>
            <desc:required>Yes</desc:required>
          </desc:Param>
        </desc:hasParam>

        <!-- MIME TYPE -->
        <desc:hasMimetype>
          <desc:Mimetype>
            <desc:mime-major>text</desc:mime-major>
            <desc:mime-minor>html</desc:mime-minor>
          </desc:Mimetype>
        </desc:hasMimetype>
      </desc:Descriptor>

    </rdf:RDF>
  </xsl:template>

</xsl:stylesheet>
