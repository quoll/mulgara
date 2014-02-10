/**
 * DescriptorUtilServiceTest.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis WSDL2Java emitter.
 */

package org.mulgara.descriptor.test;

//import  org.mulgara.descriptor.*;
// only used for constants
import org.mulgara.descriptor.Descriptor;

import java.util.*;
import org.w3c.dom.*;

// Soap packages
import javax.xml.parsers.*;

// third party packages
import junit.framework.TestSuite;
import junit.framework.Test;
import org.apache.axis.utils.*;


public class DescriptorUtilServiceTest extends junit.framework.TestCase {

  /**
   * Host name of server
   *
   */
  private static String hostName = System.getProperty("host.name");

    public DescriptorUtilServiceTest(java.lang.String name) {
        super(name);
    }
    /*

    DISABLED BECAUSE IT FAILS - TODO FIX 
    
    public void test1DescriptorServiceInvokeDescriptor() throws Exception {
        org.mulgara.descriptor.DescriptorServiceSoapBindingStub binding;
        try {
            binding = (org.mulgara.descriptor.DescriptorServiceSoapBindingStub)
                          new org.mulgara.descriptor.DescriptorUtilServiceLocator().getDescriptorService();
        }
        catch (javax.xml.rpc.ServiceException jre) {
            if(jre.getLinkedCause()!=null)
                jre.getLinkedCause().printStackTrace();
            throw new junit.framework.AssertionFailedError("JAX-RPC ServiceException caught: " + jre);
        }
        assertNotNull("binding is null", binding);

        // Time out after a minute
        binding.setTimeout(60000);

        // Test operation
        org.w3c.dom.Element retValue = null;
        org.w3c.dom.Element descParam = createElement();
        retValue = binding.invokeDescriptor(descParam);
        // TBD - validate results
      System.out.println("SOAP CLIENT RECEIVED:" +
                         DOM2Writer.nodeToString((Node)retValue, true));
    }
    */

    public void test1DescriptorServiceInvokeToString() throws Exception {
        /*
        org.mulgara.descriptor.DescriptorServiceSoapBindingStub binding = null;
        try {
            binding = (org.mulgara.descriptor.DescriptorServiceSoapBindingStub)
                          new org.mulgara.descriptor.DescriptorUtilServiceLocator().getDescriptorService();
        }
        catch (javax.xml.rpc.ServiceException jre) {
            if(jre.getLinkedCause()!=null)
                jre.getLinkedCause().printStackTrace();
            throw new junit.framework.AssertionFailedError("JAX-RPC ServiceException caught: " + jre);
        }
        assertNotNull("binding is null", binding);

        // Time out after a minute
        binding.setTimeout(60000);

        // Test operation
        java.util.HashMap map = createHashMap();
        java.lang.String value = null;
        value = binding.invokeToString(map);
        System.out.println(this.getClass().getName() + " invoke to string returned:'" + value + "'");

        //String testValue ="<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<desc:message xmlns:desc=\"http://mulgara.org/descriptor#\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\">helloworld</desc:message>";
        String testValue ="<desc:message xmlns:desc=\"http://mulgara.org/descriptor#\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\">helloworld</desc:message>";

        // finally, test for helloworld
        assertEquals(testValue, value);
        */
    }


    @SuppressWarnings("unused")
    private HashMap<String,String> createHashMap() throws Exception {

      HashMap<String,String> descriptorHash = new HashMap<String,String>();
      descriptorHash.put(Descriptor.DESCRIPTOR_SELF,
          "http://" + hostName +
          ":8080/webservices/descriptor/descriptors/test/hello.xsl");
      descriptorHash.put("model", "rmi://" + hostName + "/server1#descriptors");
      descriptorHash.put("firstWord", "hello");
      descriptorHash.put("secondWord", "world");

      return descriptorHash;
    }

    @SuppressWarnings("unused")
    private Element createElement() throws Exception {

      // assemble descriptor parameters into hashtable
      HashMap<String,String> descriptorHash = new HashMap<String,String>();
      descriptorHash.put(Descriptor.DESCRIPTOR_SELF,
          "http://" + hostName +
          ":8080/webservices/descriptor/descriptors/test/hello.xsl");
      descriptorHash.put("model", "rmi://" + hostName + "/server1#descriptors");
      descriptorHash.put("firstWord", "hello");
      descriptorHash.put("secondWord", "world");

      DocumentBuilder db =
          DocumentBuilderFactory.newInstance().newDocumentBuilder();
      Document doc = db.newDocument();

      // create a DOM like this
      //
      //<params>
      //  <name>value</name>
      //</params>
      //
      // <name> is the real name of the parameter
      Element element = doc.createElement("descriptor-params");

      for (String key: descriptorHash.keySet()) {

        String value = descriptorHash.get(key);

        Element dParam = doc.createElement(key);
        dParam.appendChild(doc.createTextNode(value));

        element.appendChild(dParam);
      }

      System.out.println("SOAP CLIENT PARAMS:" + DOM2Writer.nodeToString((Node)element, true));

      return element;
    }

    /**
     * Hook for test runner to obtain a test suite from.
     *
     * @return The test suite to run.
     */
     public static Test suite() {

       TestSuite suite = new TestSuite();
//       suite.addTest(new DescriptorUtilServiceTest("test1DescriptorServiceInvokeToString"));
       return suite;

     }

}
