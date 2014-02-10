<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE xsl:transform [
  <!ENTITY copy "&#x00A9;">
  <!ENTITY reg  "&#x00AE;">
]>

<!-- Transform sheet for converting MathML to XHTML -->
<xsl:transform version="1.0"
  xmlns     ="http://www.w3.org/1999/xhtml"
  xmlns:html="http://www.w3.org/1999/xhtml"
  xmlns:xsl ="http://www.w3.org/1999/XSL/Transform">

<!-- Output character set (FIXME - doesn't propagate non-default value to
     xsl:output's encoding or media-type attributes!) -->
<xsl:param name="charset" select="'UTF-8'"/>

<!-- Output settings -->
<xsl:output
  doctype-public="-//W3C//DTD XHTML 1.0 Transitional//EN"
  doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"
  encoding      ="ISO-8859-1"
  media-type    ="text/html;charset=ISO-8859-1"/>

<!-- Top-level template -->
<xsl:template match="/">
<html>
<head>
<title>Mulgara Metadata Store Project</title>
</head>
<body>
<abstract>
The Mulgara Metadata Store project.
</abstract>
<p>
The Mulgara Metadata Store project is an implementation of a
distributed secure RDF store. More generally it supports the storage
of arbitrary data as triples.
</p>
<p>
The project is modularized into components above the class level, roughly
corresponding to java packages.
<em>To do: record or generate dependency information between components.</em>
<dl>
<xsl:apply-templates select="project/target[@description][substring(@name, string-length(@name) - string-length('.jar') + 1) = '.jar']"/>
</dl>
</p>
</body>
</html>
</xsl:template>

<!-- Targets -->
<xsl:template match="target">
<dt><xsl:value-of select="@name"/></dt>
<xsl:if test="@description">
<dd>
<xsl:value-of select="@description"/>
</dd>
</xsl:if>
</xsl:template>

</xsl:transform>
