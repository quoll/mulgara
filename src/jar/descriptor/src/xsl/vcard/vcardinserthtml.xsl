<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet
  version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
  xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
  xmlns:dc="http://purl.org/dc/elements/1.1/"
  xmlns:lxslt="http://xml.apache.org/xslt"
  xmlns:xalan="http://xml.apache.org/xalan"
  xmlns:mulgaraDescriptor="mulgaraDescriptor"
  extension-element-prefixes="mulgaraDescriptor"
  exclude-result-prefixes="xsl rdf rdfs lxslt mulgaraAnswer xalan ns1"
  xmlns:mulgaraAnswer="http://mulgara.org/tql#"
  xmlns:desc="http://mulgara.org/descriptor#"
  xmlns:ns1="urn:Query">

  <!-- ============================================== -->
  <!-- Assemble the parameters -->
  <!-- ============================================== -->

  <!-- DESCRIPTOR PARAMS -->
  <xsl:param name="_usage"/> <!-- if set return the USAGE RDF -->
  <xsl:param name="_self"/> <!-- the URL of this descriptor -->

  <!-- USER PARAMS -->

<xsl:param name="model"/>
<xsl:param name="uri"/>
<xsl:param name="fullName"/>
<xsl:param name="title"/>
<xsl:param name="role"/>
<xsl:param name="street"/>
<xsl:param name="locality"/>
<xsl:param name="pcode"/>
<xsl:param name="country"/>
<xsl:param name="email"/>
<xsl:param name="note"/>
<xsl:param name="telephone"/>

  <!-- ============================================== -->
  <!-- Match the Solution -->
  <!-- ============================================== -->
  <xsl:template match="/">
    <xsl:choose>
      <xsl:when test="$_usage">
        <xsl:call-template name="usage"/>
      </xsl:when>
      <xsl:otherwise>
        
      <!-- descriptor -->
      <xsl:choose>
        <xsl:when test="($model and $uri and $fullName and $email)">

         <mulgaraDescriptor:descriptor
            _target="vcardinsert.xsl" 
            _source="{$_self}"
            model="{$model}"
            uri="{$uri}"
            fullName="{$fullName}"
            title="{$title}"
            role="{$role}"
            street="{$street}"
            locality="{$locality}"
            pcode="{$pcode}"
            country="{$country}"
            email="{$email}"
            note="{$note}"
            telephone="{$telephone}"/>

        </xsl:when>
        <xsl:otherwise>

        <html>
          <head>
          <title>Add a VCard to a Mulgara database</title>
          <script type="text/javascript">
          function genURI() {
            document.getElementById("uri").value="urn:vcard:" + Math.round(Math.random() * 100000000);
          }
          </script>
        </head>
        <body>

         <h2>Insert a VCard</h2>

         <form name="execute"  action="execute">

           <input type="hidden" name="_source" value="{$_self}"/>
           <input type="hidden" name="_self" value="vcardinserthtml.xsl"/>

           <!-- params -->
           <table>
             <tr>
               <th colspan="3" bgcolor="lightgrey">New VCard</th>
             </tr>
             <tr>
               <th>Property</th>
               <th>Value</th>
               <th>Required</th>
             </tr>
             <tr>
             <td>Model</td>
             <td><input type="text" name="model" size="40" value="{$model}"/></td>
             <td>Yes</td>
             </tr><tr>
             <td>URI of VCard</td>
             <td><input id="uri" type="text" name="uri" size="28" value="{$uri}" />
               <input type="button" value="Generate" onclick="genURI();return false;"/>
             </td>
             <td>Yes</td>
             </tr><tr>
             <td>Full Name</td>
             <td><input type="text" name="fullName" size="40" value="{$fullName}"/></td>
             <td>Yes</td>
             </tr><tr>
             <td>Title</td>
             <td><input type="text" name="title" size="40" value="{$title}"/></td>
             <td>No</td>
             </tr><tr>
             <td>Role</td>
             <td><input type="text" name="role" size="40" value="{$role}"/></td>
             <td>No</td>
             </tr><tr>
             <td>Street</td>
             <td><input type="text" name="street" size="40" value="{$street}"/></td>
             <td>No</td>
             </tr><tr>
             <td>Locality</td>
             <td><input type="text" name="locality" size="40" value="{$locality}"/></td>
             <td>No</td>
             </tr><tr>
             <td>Post code</td>
             <td><input type="text" name="pcode" size="40" value="{$pcode}"/></td>
             <td>No</td>
             </tr><tr>
             <td>Country</td>
             <td><input type="text" name="country" size="40" value="{$country}"/></td>
             <td>No</td>
             </tr><tr>
             <td>Email</td>
             <td><input type="text" name="email" size="40" value="{$email}"/></td>
             <td>Yes</td>
             </tr><tr>
             <td>Note</td>
             <td><input type="text" name="note" size="40" value="{$note}"/></td>
             <td>No</td>
             </tr><tr>
             <td>Telephone</td>
             <td><input type="text" name="telephone" size="40" value="{$telephone}"/></td>
             <td>No</td>
             </tr>
             <tr align="center">
               <td colspan="3"><input type="submit" value="Insert VCard"/></td>
             </tr>
         </table>

          </form>

        </body>
        </html>

        </xsl:otherwise>
      </xsl:choose>

      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>


  <!-- #################################################################### -->
  <!-- Calls a java class for Mulgara queries -->
  <!-- #################################################################### -->
  <lxslt:component prefix="mulgaraDescriptor" elements="descriptor query debug"
 functions="test">
    <lxslt:script lang="javaclass"
 src="xalan://org.mulgara.descriptor.DescriptorElement"/>
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

      <desc:Descriptor rdf:about="">

        <dc:title>Given a model return a HTML page which will take VCard 
          parameters and insert them into a Mulgara Knowledge store</dc:title>

        <desc:hasParam>
          <desc:Param>
            <desc:name>_self</desc:name>
            <desc:type>String</desc:type>
            <desc:description>The URL of this Descriptor</desc:description>
          </desc:Param>
        </desc:hasParam>



        <!-- Parameter 1 -->

        <desc:hasParam xmlns:desc="http://mulgara.org/descriptor#"
 xmlns="http://www.w3.org/1999/xhtml">
<desc:Param>
<desc:name>model</desc:name>
<desc:type>String</desc:type>
<desc:required>Yes</desc:required>
<desc:description>The model to store the vcard</desc:description>
</desc:Param>
</desc:hasParam>

   <!-- Parameter 2 -->


<desc:hasParam xmlns:desc="http://mulgara.org/descriptor#"
 xmlns="http://www.w3.org/1999/xhtml">
<desc:Param>
<desc:name>uri</desc:name>
<desc:type>String</desc:type>
<desc:required>Yes</desc:required>
<desc:description>URL for the vCard</desc:description>
</desc:Param>
</desc:hasParam>


<!-- Parameter 3 -->

        <desc:hasParam xmlns:desc="http://mulgara.org/descriptor#"
 xmlns="http://www.w3.org/1999/xhtml">
<desc:Param>
<desc:name>fullName</desc:name>
<desc:type>String</desc:type>
<desc:required>Yes</desc:required>
<desc:description>Full Name</desc:description>
</desc:Param>
</desc:hasParam>


<!-- Parameter 4 -->

        <desc:hasParam xmlns:desc="http://mulgara.org/descriptor#"
 xmlns="http://www.w3.org/1999/xhtml">
<desc:Param>
<desc:name>title</desc:name>
<desc:type>String</desc:type>
<desc:required>No</desc:required>
<desc:description>Title</desc:description>
</desc:Param>
</desc:hasParam>

<!-- Parameter 5 -->

        <desc:hasParam xmlns:desc="http://mulgara.org/descriptor#"
 xmlns="http://www.w3.org/1999/xhtml">
<desc:Param>
<desc:name>role</desc:name>
<desc:type>String</desc:type>
<desc:required>No</desc:required>
<desc:description>Role</desc:description>
</desc:Param>
</desc:hasParam>

<!-- Parameter 6 -->

        <desc:hasParam xmlns:desc="http://mulgara.org/descriptor#"
 xmlns="http://www.w3.org/1999/xhtml">
<desc:Param>
<desc:name>street</desc:name>
<desc:type>String</desc:type>
<desc:required>No</desc:required>
<desc:description>Street</desc:description>
</desc:Param>
</desc:hasParam>


<!-- Parameter 7 -->

        <desc:hasParam xmlns:desc="http://mulgara.org/descriptor#"
 xmlns="http://www.w3.org/1999/xhtml">
<desc:Param>
<desc:name>locality</desc:name>
<desc:type>String</desc:type>
<desc:required>No</desc:required>
<desc:description>Locality</desc:description>
</desc:Param>
</desc:hasParam>

<!-- Parameter 8 -->

        <desc:hasParam xmlns:desc="http://mulgara.org/descriptor#"
 xmlns="http://www.w3.org/1999/xhtml">
<desc:Param>
<desc:name>pcode</desc:name>
<desc:type>String</desc:type>
<desc:required>No</desc:required>
<desc:description>Post code</desc:description>
</desc:Param>
</desc:hasParam>


<!-- Parameter 9 -->

        <desc:hasParam xmlns:desc="http://mulgara.org/descriptor#"
 xmlns="http://www.w3.org/1999/xhtml">
<desc:Param>
<desc:name>country</desc:name>
<desc:type>String</desc:type>
<desc:required>No</desc:required>
<desc:description>country</desc:description>
</desc:Param>
</desc:hasParam>

<!-- Parameter 10 -->


<desc:hasParam xmlns:desc="http://mulgara.org/descriptor#"
 xmlns="http://www.w3.org/1999/xhtml">
<desc:Param>
<desc:name>email</desc:name>
<desc:type>String</desc:type>
<desc:required>Yes</desc:required>
<desc:description>Email address</desc:description>
</desc:Param>
</desc:hasParam>

<!-- Parameter 11 -->

<desc:hasParam xmlns:desc="http://mulgara.org/descriptor#"
 xmlns="http://www.w3.org/1999/xhtml">
<desc:Param>
<desc:name>note</desc:name>
<desc:type>String</desc:type>
<desc:required>No</desc:required>
<desc:description>Note</desc:description>
</desc:Param>
</desc:hasParam>


<!-- Parameter 12 -->

<desc:hasParam xmlns:desc="http://mulgara.org/descriptor#"
 xmlns="http://www.w3.org/1999/xhtml">
<desc:Param>
<desc:name>telephone</desc:name>
<desc:type>String</desc:type>
<desc:required>No</desc:required>
<desc:description>telephone</desc:description>
</desc:Param>
</desc:hasParam>

<!-- MIME TYPE -->


<desc:hasMimetype xmlns:desc="http://mulgara.org/descriptor#"
 xmlns="http://www.w3.org/1999/xhtml">
<desc:Mimetype>
<desc:mime-major>text</desc:mime-major>
<desc:mime-minor>html</desc:mime-minor>
</desc:Mimetype>
</desc:hasMimetype>

      </desc:Descriptor>

    </rdf:RDF>
  </xsl:template>

</xsl:stylesheet>
