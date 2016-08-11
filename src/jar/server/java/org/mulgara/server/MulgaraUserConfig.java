/*
 * The contents of this file are subject to the Open Software License
 * Version 3.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.opensource.org/licenses/osl-3.0.txt
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 */

package org.mulgara.server;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.mulgara.config.ContentHandler;
import org.mulgara.config.DefaultContentHandler;
import org.mulgara.config.ExternalConfigPaths;
import org.mulgara.config.Jetty;
import org.mulgara.config.MulgaraConfig;
import org.mulgara.config.PersistentNodePoolFactory;
import org.mulgara.config.PersistentResolverFactory;
import org.mulgara.config.PersistentStringPoolFactory;
import org.mulgara.config.RelatedQueryHandler;
import org.mulgara.config.ResolverFactory;
import org.mulgara.config.RuleLoader;
import org.mulgara.config.SecurityAdapterFactory;
import org.mulgara.config.TemporaryNodePoolFactory;
import org.mulgara.config.TemporaryResolverFactory;
import org.mulgara.config.TemporaryStringPoolFactory;
import org.mulgara.config.XpathFunctionResolver;

/**
 * This class wraps the system configuration, and a user configuration, using any
 * elements present in the user configuration to override (or add to) the system
 * config.
 *
 * @created Jun 28, 2009
 * @author Paula Gearon
 * @copyright &copy; 2009 <a href="http://www.duraspace.org/">DuraSpace</a>
 */
public class MulgaraUserConfig extends MulgaraConfig {

  /** Generated serialization ID */
  private static final long serialVersionUID = -4722202287520168362L;

  protected static final Logger log = Logger.getLogger(MulgaraUserConfig.class.getName());

  /** The embedded configuration file path */
  protected static final String CONFIG_PATH = "conf/mulgara-x-config.xml";

  /** Structure holding all the system configuration data. This is overridden or added to be user data. */
  private final MulgaraConfig systemConfig;

  /** Structure holding the used data. May be null, in which case {@link #usr} is false. */
  private final MulgaraConfig userConfig;

  /** Flag to indicate user data should be used */
  private final boolean usr;

  private List<ContentHandler> contentHandlers;

  private List<ResolverFactory> resolverFactories;

  private List<RuleLoader> ruleLoaders;

  private List<SecurityAdapterFactory> securityAdapterFactories;

  private List<XpathFunctionResolver> xpathFunctionResolvers;

  /**
   * Create the config object, to look like a standard configuration.
   * @throws IOException 
   * @throws ValidationException 
   * @throws MarshalException 
   */
  public MulgaraUserConfig(String configUrlStr) throws MarshalException, ValidationException, IOException {
    // Set up the system configuration. This should never fail, unless someone adjusted it incorrectly.
    URL sysUrl = ClassLoader.getSystemResource(CONFIG_PATH);
    if (sysUrl == null) throw new IOException("Unable to locate embedded server configuration file");

    systemConfig = MulgaraConfig.unmarshal(new InputStreamReader(sysUrl.openStream()));

    // Set up the user configuration. Users can create problems for themselves here.
    if (configUrlStr != null) {
      URL userUrl = new URL(configUrlStr);
      userConfig = MulgaraConfig.unmarshal(new InputStreamReader(userUrl.openStream()));
      usr = true;
    } else {
      userConfig = null;
      usr = false;
    }
    validate();

    initializeLists();
  }


  /**
   * Populate the lists from the configuration, starting with the system configuration
   * and either appending or replacing with items from the used configuration.
   */
  @SuppressWarnings("unchecked")
  private void initializeLists() {
    if (userConfig != null) {
      contentHandlers = initializeList((Enumeration<ContentHandler>)systemConfig.enumerateContentHandler(),
               (Enumeration<ContentHandler>)userConfig.enumerateContentHandler());
      resolverFactories = initializeList((Enumeration<ResolverFactory>)systemConfig.enumerateResolverFactory(),
               (Enumeration<ResolverFactory>)userConfig.enumerateResolverFactory());
      ruleLoaders = initializeList((Enumeration<RuleLoader>)systemConfig.enumerateRuleLoader(),
               (Enumeration<RuleLoader>)userConfig.enumerateRuleLoader());
      securityAdapterFactories = initializeList((Enumeration<SecurityAdapterFactory>)systemConfig.enumerateSecurityAdapterFactory(),
               (Enumeration<SecurityAdapterFactory>)userConfig.enumerateSecurityAdapterFactory());
      xpathFunctionResolvers = initializeList((Enumeration<XpathFunctionResolver>)systemConfig.enumerateXpathFunctionResolver(),
               (Enumeration<XpathFunctionResolver>)userConfig.enumerateXpathFunctionResolver());
    } else {
      contentHandlers = Collections.list((Enumeration<ContentHandler>)systemConfig.enumerateContentHandler());
      resolverFactories = Collections.list((Enumeration<ResolverFactory>)systemConfig.enumerateResolverFactory());
      ruleLoaders = Collections.list((Enumeration<RuleLoader>)systemConfig.enumerateRuleLoader());
      securityAdapterFactories = Collections.list((Enumeration<SecurityAdapterFactory>)systemConfig.enumerateSecurityAdapterFactory());
      xpathFunctionResolvers = Collections.list((Enumeration<XpathFunctionResolver>)systemConfig.enumerateXpathFunctionResolver());
    }
  }


  /**
   * Populate a single list from the configuration, starting with the system configuration
   * and either appending or replacing with items from the used configuration.
   * @param <T> The item type in the list.
   * @param sysEnum The system configured list.
   * @param userEnum The user configured list. This overrides the system configuration.
   */
  private <T> List<T> initializeList(Enumeration<T> sysEnum, Enumeration<T> userEnum) {
    // map the classname types in the elements to the element
    Map<String,T> sysHandlers = new HashMap<String,T>();
    List<T> result = new ArrayList<T>();

    // add the system elements
    while (sysEnum.hasMoreElements()) {
      T handler = sysEnum.nextElement();
      if (isDisabled(handler)) {
        log.warn("Unexpected disabled module in configuration file: " + getType(handler));
        continue;
      }
      result.add(handler);
      sysHandlers.put(getType(handler), handler);
    }

    // add the user elements, overriding where necessary
    while (userEnum.hasMoreElements()) {
      T handler = userEnum.nextElement();
      T oldHandler = sysHandlers.get(getType(handler));
      // element already existed, replace/remove it
      if (oldHandler != null) result.remove(oldHandler);
      // remove the element
      if (isDisabled(handler)) continue;
      // add or replace the element
      result.add(handler);
    }
    return result;
  }


  /**
   * Asks a configuration item if it is disabled, from any configuration item that supports it.
   * @param o An instance of a configuration class that supports .isDisabled()
   * @return <code>true</code> if the item has the disabled attribute set.
   */
  private boolean isDisabled(Object o) {
    if (o instanceof ContentHandler) return ((ContentHandler)o).isDisabled();
    if (o instanceof ResolverFactory) return ((ResolverFactory)o).isDisabled();
    if (o instanceof RuleLoader) return ((RuleLoader)o).isDisabled();
    if (o instanceof SecurityAdapterFactory) return ((SecurityAdapterFactory)o).isDisabled();
    if (o instanceof XpathFunctionResolver) return ((XpathFunctionResolver)o).isDisabled();
    return true;
  }


  /**
   * Get the type from a group of classes that all support the method.
   * @param o An instance of a configuration class that supports .getType()
   * @return The name of the type for the object.
   */
  private String getType(Object o) {
    if (o instanceof ContentHandler) return ((ContentHandler)o).getType();
    if (o instanceof ResolverFactory) return ((ResolverFactory)o).getType();
    if (o instanceof RuleLoader) return ((RuleLoader)o).getType();
    if (o instanceof SecurityAdapterFactory) return ((SecurityAdapterFactory)o).getType();
    return "";
  }


  /**
   * @see org.mulgara.config.MulgaraConfig#enumerateContentHandler()
   */
  @Override
  public Enumeration<org.mulgara.config.ContentHandler> enumerateContentHandler() {
    return Collections.enumeration(contentHandlers);
  }


  /**
   * @see org.mulgara.config.MulgaraConfig#enumerateResolverFactory()
   */
  @Override
  public Enumeration<org.mulgara.config.ResolverFactory> enumerateResolverFactory() {
    return Collections.enumeration(resolverFactories);
  }


  /**
   * @see org.mulgara.config.MulgaraConfig#enumerateRuleLoader()
   */
  @Override
  public Enumeration<org.mulgara.config.RuleLoader> enumerateRuleLoader() {
    return Collections.enumeration(ruleLoaders);
  }

  /**
   * @see org.mulgara.config.MulgaraConfig#enumerateSecurityAdapterFactory()
   */
  @Override
  public Enumeration<org.mulgara.config.SecurityAdapterFactory> enumerateSecurityAdapterFactory() {
    return Collections.enumeration(securityAdapterFactories);
  }

  /**
   * @see org.mulgara.config.MulgaraConfig#enumerateXpathFunctionResolver()
   */
  @Override
  public Enumeration<org.mulgara.config.XpathFunctionResolver> enumerateXpathFunctionResolver() {
    return Collections.enumeration(xpathFunctionResolvers);
  }


  /**
   * @see org.mulgara.config.MulgaraConfig#getContentHandler()
   */
  @Override
  public ContentHandler[] getContentHandler() {
    return contentHandlers.toArray(new ContentHandler[contentHandlers.size()]);
  }

  /**
   * @see org.mulgara.config.MulgaraConfig#getContentHandler(int)
   */
  @Override
  public ContentHandler getContentHandler(int index) throws IndexOutOfBoundsException {
    return contentHandlers.get(index);
  }

  /**
   * @see org.mulgara.config.MulgaraConfig#getContentHandlerCount()
   */
  @Override
  public int getContentHandlerCount() {
    return contentHandlers.size();
  }

  /**
   * @see org.mulgara.config.MulgaraConfig#getDefaultContentHandler()
   */
  @Override
  public DefaultContentHandler getDefaultContentHandler() {
    if (usr) {
      DefaultContentHandler handler = userConfig.getDefaultContentHandler();
      if (handler != null) return handler;
    }
    return systemConfig.getDefaultContentHandler();
  }

  /**
   * @see org.mulgara.config.MulgaraConfig#getDefaultGraph()
   */
  @Override
  public String getDefaultGraph() {
    if (usr) {
      String graph = userConfig.getDefaultGraph();
      if (graph != null) return graph;
    }
    return systemConfig.getDefaultGraph();
  }

  /**
   * @see org.mulgara.config.MulgaraConfig#getExternalConfigPaths()
   */
  @Override
  public ExternalConfigPaths getExternalConfigPaths() {
    if (usr) {
      ExternalConfigPaths paths = userConfig.getExternalConfigPaths();
      if (paths != null) return paths;
    }
    return systemConfig.getExternalConfigPaths();
  }

  /**
   * @see org.mulgara.config.MulgaraConfig#getIdleTimeout()
   */
  @Override
  public int getIdleTimeout() {
    return (usr && userConfig.hasIdleTimeout()) ? userConfig.getIdleTimeout() : systemConfig.getIdleTimeout();
  }

  /**
   * Note that this configuration is expected to be completely overridden, or not at all!
   * @see org.mulgara.config.MulgaraConfig#getJetty()
   */
  @Override
  public Jetty getJetty() {
    if (usr) {
      Jetty jetty = userConfig.getJetty();
      if (jetty != null) return jetty;
    }
    return systemConfig.getJetty();
  }

  /**
   * @see org.mulgara.config.MulgaraConfig#getMulgaraHost()
   */
  @Override
  public String getMulgaraHost() {
    if (usr) {
      String host = userConfig.getMulgaraHost();
      if (host != null) return host;
    }
    return systemConfig.getMulgaraHost();
  }

  /**
   * @see org.mulgara.config.MulgaraConfig#getPersistencePath()
   */
  @Override
  public String getPersistencePath() {
    if (usr) {
      String path = userConfig.getPersistencePath();
      if (path != null) return path;
    }
    return systemConfig.getPersistencePath();
  }

  /**
   * @see org.mulgara.config.MulgaraConfig#getPersistentNodePoolFactory()
   */
  @Override
  public PersistentNodePoolFactory getPersistentNodePoolFactory() {
    if (usr) {
      PersistentNodePoolFactory factory = userConfig.getPersistentNodePoolFactory();
      if (factory != null) return factory;
    }
    return systemConfig.getPersistentNodePoolFactory();
  }

  /**
   * @see org.mulgara.config.MulgaraConfig#getPersistentResolverFactory()
   */
  @Override
  public PersistentResolverFactory getPersistentResolverFactory() {
    if (usr) {
      PersistentResolverFactory factory = userConfig.getPersistentResolverFactory();
      if (factory != null) return factory;
    }
    return systemConfig.getPersistentResolverFactory();
  }

  /**
   * @see org.mulgara.config.MulgaraConfig#getPersistentStringPoolFactory()
   */
  @Override
  public PersistentStringPoolFactory getPersistentStringPoolFactory() {
    if (usr) {
      PersistentStringPoolFactory factory = userConfig.getPersistentStringPoolFactory();
      if (factory != null) return factory;
    }
    return systemConfig.getPersistentStringPoolFactory();
  }

  /**
   * @see org.mulgara.config.MulgaraConfig#getRelatedQueryHandler()
   */
  @Override
  public RelatedQueryHandler getRelatedQueryHandler() {
    if (usr) {
      RelatedQueryHandler handler = userConfig.getRelatedQueryHandler();
      if (handler != null) return handler;
    }
    return systemConfig.getRelatedQueryHandler();
  }

  /**
   * @see org.mulgara.config.MulgaraConfig#getResolverFactory()
   */
  @Override
  public ResolverFactory[] getResolverFactory() {
    return resolverFactories.toArray(new ResolverFactory[resolverFactories.size()]);
  }

  /**
   * @see org.mulgara.config.MulgaraConfig#getResolverFactory(int)
   */
  @Override
  public ResolverFactory getResolverFactory(int index) throws IndexOutOfBoundsException {
    return resolverFactories.get(index);
  }

  /**
   * @see org.mulgara.config.MulgaraConfig#getResolverFactoryCount()
   */
  @Override
  public int getResolverFactoryCount() {
    return resolverFactories.size();
  }

  /**
   * @see org.mulgara.config.MulgaraConfig#getXpathFunctionResolver()
   */
  @Override
  public XpathFunctionResolver[] getXpathFunctionResolver() {
    return xpathFunctionResolvers.toArray(new XpathFunctionResolver[xpathFunctionResolvers.size()]);
  }

  /**
   * @see org.mulgara.config.MulgaraConfig#getXpathFunctionResolver(int)
   */
  @Override
  public XpathFunctionResolver getXpathFunctionResolver(int index) throws IndexOutOfBoundsException {
    return xpathFunctionResolvers.get(index);
  }

  /**
   * @see org.mulgara.config.MulgaraConfig#getXpathFunctionResolver()
   */
  @Override
  public int getXpathFunctionResolverCount() {
    return xpathFunctionResolvers.size();
  }

  /**
   * @see org.mulgara.config.MulgaraConfig#getRMIPort()
   */
  @Override
  public int getRMIPort() {
    return (usr && userConfig.hasRMIPort()) ? userConfig.getRMIPort() : systemConfig.getRMIPort();
  }

  /**
   * @see org.mulgara.config.MulgaraConfig#getRMIInterrupt()
   */
  @Override
  public boolean getRMIInterrupt() {
    return (usr && userConfig.hasRMIInterrupt()) ? userConfig.getRMIInterrupt() : systemConfig.getRMIInterrupt();
  }

  /**
   * @see org.mulgara.config.MulgaraConfig#getRuleLoader()
   */
  @Override
  public RuleLoader[] getRuleLoader() {
    return ruleLoaders.toArray(new RuleLoader[ruleLoaders.size()]);
  }

  /**
   * @see org.mulgara.config.MulgaraConfig#getRuleLoader(int)
   */
  @Override
  public RuleLoader getRuleLoader(int index) throws IndexOutOfBoundsException {
    return ruleLoaders.get(index);
  }

  /**
   * @see org.mulgara.config.MulgaraConfig#getRuleLoaderCount()
   */
  @Override
  public int getRuleLoaderCount() {
    return ruleLoaders.size();
  }

  /**
   * @see org.mulgara.config.MulgaraConfig#getSecurityAdapterFactory()
   */
  @Override
  public SecurityAdapterFactory[] getSecurityAdapterFactory() {
    return securityAdapterFactories.toArray(new SecurityAdapterFactory[securityAdapterFactories.size()]);
  }

  /**
   * @see org.mulgara.config.MulgaraConfig#getSecurityAdapterFactory(int)
   */
  @Override
  public SecurityAdapterFactory getSecurityAdapterFactory(int index) throws IndexOutOfBoundsException {
    return securityAdapterFactories.get(index);
  }

  /**
   * @see org.mulgara.config.MulgaraConfig#getSecurityAdapterFactoryCount()
   */
  @Override
  public int getSecurityAdapterFactoryCount() {
    return securityAdapterFactories.size();
  }

  /**
   * @see org.mulgara.config.MulgaraConfig#getServerName()
   */
  @Override
  public String getServerName() {
    if (usr) {
      String name = userConfig.getServerName();
      if (name != null) return name;
    }
    return systemConfig.getServerName();
  }

  /**
   * @see org.mulgara.config.MulgaraConfig#getSmtp()
   */
  @Override
  public String getSmtp() {
    if (usr) {
      String name = userConfig.getSmtp();
      if (name != null) return name;
    }
    return systemConfig.getSmtp();
  }

  /**
   * @see org.mulgara.config.MulgaraConfig#getStartupScript()
   */
  @Override
  public String getStartupScript() {
    if (usr) {
      String script = userConfig.getStartupScript();
      if (script != null) return script;
    }
    return systemConfig.getStartupScript();
  }

  /**
   * @see org.mulgara.config.MulgaraConfig#getTemporaryNodePoolFactory()
   */
  @Override
  public TemporaryNodePoolFactory getTemporaryNodePoolFactory() {
    if (usr) {
      TemporaryNodePoolFactory factory = userConfig.getTemporaryNodePoolFactory();
      if (factory != null) return factory;
    }
    return systemConfig.getTemporaryNodePoolFactory();
  }

  /**
   * @see org.mulgara.config.MulgaraConfig#getTemporaryResolverFactory()
   */
  @Override
  public TemporaryResolverFactory getTemporaryResolverFactory() {
    if (usr) {
      TemporaryResolverFactory factory = userConfig.getTemporaryResolverFactory();
      if (factory != null) return factory;
    }
    return systemConfig.getTemporaryResolverFactory();
  }

  /**
   * @see org.mulgara.config.MulgaraConfig#getTemporaryStringPoolFactory()
   */
  @Override
  public TemporaryStringPoolFactory getTemporaryStringPoolFactory() {
    if (usr) {
      TemporaryStringPoolFactory factory = userConfig.getTemporaryStringPoolFactory();
      if (factory != null) return factory;
    }
    return systemConfig.getTemporaryStringPoolFactory();
  }

  /**
   * @see org.mulgara.config.MulgaraConfig#getTransactionTimeout()
   */
  @Override
  public int getTransactionTimeout() {
    return (usr && userConfig.hasTransactionTimeout()) ? userConfig.getTransactionTimeout() : systemConfig.getTransactionTimeout();
  }

  /**
   * @see org.mulgara.config.MulgaraConfig#getTripleStoreImplementation()
   */
  @Override
  public String getTripleStoreImplementation() {
    if (usr) {
      String impl = userConfig.getTripleStoreImplementation();
      if (impl != null) return impl;
    }
    return systemConfig.getTripleStoreImplementation();
  }

  /**
   * @see org.mulgara.config.MulgaraConfig#hasIdleTimeout()
   */
  @Override
  public boolean hasIdleTimeout() {
    return (usr && userConfig.hasIdleTimeout()) || systemConfig.hasIdleTimeout();
  }

  /**
   * @see org.mulgara.config.MulgaraConfig#hasRMIPort()
   */
  @Override
  public boolean hasRMIPort() {
    return (usr && userConfig.hasRMIPort()) || systemConfig.hasRMIPort();
  }

  /**
   * @see org.mulgara.config.MulgaraConfig#hasRMIInterrupt()
   */
  @Override
  public boolean hasRMIInterrupt() {
    return (usr && userConfig.hasRMIInterrupt()) || systemConfig.hasRMIInterrupt();
  }

  /**
   * @see org.mulgara.config.MulgaraConfig#hasTransactionTimeout()
   */
  @Override
  public boolean hasTransactionTimeout() {
    return (usr && userConfig.hasTransactionTimeout()) || systemConfig.hasTransactionTimeout();
  }

  /**
   * @see org.mulgara.config.MulgaraConfig#isRMIInterrupt()
   */
  @Override
  public boolean isRMIInterrupt() {
    return getRMIInterrupt();
  }

  /**
   * @see org.mulgara.config.MulgaraConfig#isValid()
   */
  @Override
  public boolean isValid() {
    return systemConfig.isValid() && (userConfig == null || userConfig.isValid());
  }

  /**
   * @see org.mulgara.config.MulgaraConfig#validate()
   */
  @Override
  public void validate() throws ValidationException {
    systemConfig.validate();
    if (usr) userConfig.validate();
  }

  ///////////////////////////////////////////////////////////////////////////////
  // All write operations are invalid on this class
  ///////////////////////////////////////////////////////////////////////////////

  /**
   * @see org.mulgara.config.MulgaraConfig#addContentHandler(org.mulgara.config.ContentHandler)
   */
  @Override
  public void addContentHandler(ContentHandler contentHandler) throws IndexOutOfBoundsException {
    throw new UnsupportedOperationException("Read only class");
  }


  /**
   * @see org.mulgara.config.MulgaraConfig#addContentHandler(int, org.mulgara.config.ContentHandler)
   */
  @Override
  public void addContentHandler(int index, ContentHandler contentHandler) throws IndexOutOfBoundsException {
    throw new UnsupportedOperationException("Read only class");
  }


  /**
   * @see org.mulgara.config.MulgaraConfig#addResolverFactory(int, org.mulgara.config.ResolverFactory)
   */
  @Override
  public void addResolverFactory(int index, ResolverFactory resolverFactory) throws IndexOutOfBoundsException {
    throw new UnsupportedOperationException("Read only class");
  }


  /**
   * @see org.mulgara.config.MulgaraConfig#addResolverFactory(org.mulgara.config.ResolverFactory)
   */
  @Override
  public void addResolverFactory(ResolverFactory resolverFactory) throws IndexOutOfBoundsException {
    throw new UnsupportedOperationException("Read only class");
  }


  /**
   * @see org.mulgara.config.MulgaraConfig#addRuleLoader(int, org.mulgara.config.RuleLoader)
   */
  @Override
  public void addRuleLoader(int index, RuleLoader ruleLoader) throws IndexOutOfBoundsException {
    throw new UnsupportedOperationException("Read only class");
  }


  /**
   * @see org.mulgara.config.MulgaraConfig#addRuleLoader(org.mulgara.config.RuleLoader)
   */
  @Override
  public void addRuleLoader(RuleLoader ruleLoader) throws IndexOutOfBoundsException {
    throw new UnsupportedOperationException("Read only class");
  }


  /**
   * @see org.mulgara.config.MulgaraConfig#addSecurityAdapterFactory(int, org.mulgara.config.SecurityAdapterFactory)
   */
  @Override
  public void addSecurityAdapterFactory(int index, SecurityAdapterFactory securityAdapterFactory) throws IndexOutOfBoundsException {
    throw new UnsupportedOperationException("Read only class");
  }


  /**
   * @see org.mulgara.config.MulgaraConfig#addSecurityAdapterFactory(org.mulgara.config.SecurityAdapterFactory)
   */
  @Override
  public void addSecurityAdapterFactory(SecurityAdapterFactory securityAdapterFactory) throws IndexOutOfBoundsException {
    throw new UnsupportedOperationException("Read only class");
  }


  /**
   * @see org.mulgara.config.MulgaraConfig#deleteIdleTimeout()
   */
  @Override
  public void deleteIdleTimeout() {
    throw new UnsupportedOperationException("Read only class");
  }


  /**
   * @see org.mulgara.config.MulgaraConfig#deleteRMIPort()
   */
  @Override
  public void deleteRMIPort() {
    throw new UnsupportedOperationException("Read only class");
  }


  /**
   * @see org.mulgara.config.MulgaraConfig#deleteRMIInterrupt()
   */
  @Override
  public void deleteRMIInterrupt() {
    throw new UnsupportedOperationException("Read only class");
  }


  /**
   * @see org.mulgara.config.MulgaraConfig#deleteTransactionTimeout()
   */
  @Override
  public void deleteTransactionTimeout() {
    throw new UnsupportedOperationException("Read only class");
  }


  /**
   * @see org.mulgara.config.MulgaraConfig#marshal(org.xml.sax.ContentHandler)
   */
  @Override
  public void marshal(org.xml.sax.ContentHandler handler) throws IOException, MarshalException, ValidationException {
    throw new UnsupportedOperationException("Read only class");
  }


  /**
   * @see org.mulgara.config.MulgaraConfig#marshal(java.io.Writer)
   */
  @Override
  public void marshal(Writer out) throws MarshalException, ValidationException {
    throw new UnsupportedOperationException("Read only class");
  }


  /**
   * @see org.mulgara.config.MulgaraConfig#removeAllContentHandler()
   */
  @Override
  public void removeAllContentHandler() {
    throw new UnsupportedOperationException("Read only class");
  }


  /**
   * @see org.mulgara.config.MulgaraConfig#removeAllResolverFactory()
   */
  @Override
  public void removeAllResolverFactory() {
    throw new UnsupportedOperationException("Read only class");
  }


  /**
   * @see org.mulgara.config.MulgaraConfig#removeAllRuleLoader()
   */
  @Override
  public void removeAllRuleLoader() {
    throw new UnsupportedOperationException("Read only class");
  }


  /**
   * @see org.mulgara.config.MulgaraConfig#removeAllSecurityAdapterFactory()
   */
  @Override
  public void removeAllSecurityAdapterFactory() {
    throw new UnsupportedOperationException("Read only class");
  }


  /**
   * @see org.mulgara.config.MulgaraConfig#removeAllXpathFunctionResolver()
   */
  @Override
  public void removeAllXpathFunctionResolver() {
    throw new UnsupportedOperationException("Read only class");
  }


  /**
   * @see org.mulgara.config.MulgaraConfig#removeContentHandler(org.mulgara.config.ContentHandler)
   */
  @Override
  public boolean removeContentHandler(ContentHandler contentHandler) {
    throw new UnsupportedOperationException("Read only class");
  }


  /**
   * @see org.mulgara.config.MulgaraConfig#removeContentHandlerAt(int)
   */
  @Override
  public ContentHandler removeContentHandlerAt(int index) {
    throw new UnsupportedOperationException("Read only class");
  }


  /**
   * @see org.mulgara.config.MulgaraConfig#removeResolverFactory(org.mulgara.config.ResolverFactory)
   */
  @Override
  public boolean removeResolverFactory(ResolverFactory resolverFactory) {
    throw new UnsupportedOperationException("Read only class");
  }


  /**
   * @see org.mulgara.config.MulgaraConfig#removeResolverFactoryAt(int)
   */
  @Override
  public ResolverFactory removeResolverFactoryAt(int index) {
    throw new UnsupportedOperationException("Read only class");
  }


  /**
   * @see org.mulgara.config.MulgaraConfig#removeRuleLoader(org.mulgara.config.RuleLoader)
   */
  @Override
  public boolean removeRuleLoader(RuleLoader ruleLoader) {
    throw new UnsupportedOperationException("Read only class");
  }


  /**
   * @see org.mulgara.config.MulgaraConfig#removeRuleLoaderAt(int)
   */
  @Override
  public RuleLoader removeRuleLoaderAt(int index) {
    throw new UnsupportedOperationException("Read only class");
  }


  /**
   * @see org.mulgara.config.MulgaraConfig#removeSecurityAdapterFactory(org.mulgara.config.SecurityAdapterFactory)
   */
  @Override
  public boolean removeSecurityAdapterFactory(SecurityAdapterFactory securityAdapterFactory) {
    throw new UnsupportedOperationException("Read only class");
  }


  /**
   * @see org.mulgara.config.MulgaraConfig#removeSecurityAdapterFactoryAt(int)
   */
  @Override
  public SecurityAdapterFactory removeSecurityAdapterFactoryAt(int index) {
    throw new UnsupportedOperationException("Read only class");
  }


  /**
   * @see org.mulgara.config.MulgaraConfig#removeXpathFunctionResolver(org.mulgara.config.XpathFunctionResolver)
   */
  @Override
  public boolean removeXpathFunctionResolver(XpathFunctionResolver resolverFactory) {
    throw new UnsupportedOperationException("Read only class");
  }


  /**
   * @see org.mulgara.config.MulgaraConfig#removeXpathFunctionResolverAt(int)
   */
  @Override
  public XpathFunctionResolver removeXpathFunctionResolverAt(int index) {
    throw new UnsupportedOperationException("Read only class");
  }


  /**
   * @see org.mulgara.config.MulgaraConfig#setContentHandler(org.mulgara.config.ContentHandler[])
   */
  @Override
  public void setContentHandler(ContentHandler[] contentHandlerArray) {
    throw new UnsupportedOperationException("Read only class");
  }


  /**
   * @see org.mulgara.config.MulgaraConfig#setContentHandler(int, org.mulgara.config.ContentHandler)
   */
  @Override
  public void setContentHandler(int index, ContentHandler contentHandler) throws IndexOutOfBoundsException {
    throw new UnsupportedOperationException("Read only class");
  }


  /**
   * @see org.mulgara.config.MulgaraConfig#setDefaultContentHandler(org.mulgara.config.DefaultContentHandler)
   */
  @Override
  public void setDefaultContentHandler(DefaultContentHandler defaultContentHandler) {
    throw new UnsupportedOperationException("Read only class");
  }


  /**
   * @see org.mulgara.config.MulgaraConfig#setDefaultGraph(java.lang.String)
   */
  @Override
  public void setDefaultGraph(String defaultGraph) {
    throw new UnsupportedOperationException("Read only class");
  }


  /**
   * @see org.mulgara.config.MulgaraConfig#setExternalConfigPaths(org.mulgara.config.ExternalConfigPaths)
   */
  @Override
  public void setExternalConfigPaths(ExternalConfigPaths externalConfigPaths) {
    throw new UnsupportedOperationException("Read only class");
  }


  /**
   * @see org.mulgara.config.MulgaraConfig#setIdleTimeout(int)
   */
  @Override
  public void setIdleTimeout(int idleTimeout) {
    throw new UnsupportedOperationException("Read only class");
  }


  /**
   * @see org.mulgara.config.MulgaraConfig#setJetty(org.mulgara.config.Jetty)
   */
  @Override
  public void setJetty(Jetty jetty) {
    throw new UnsupportedOperationException("Read only class");
  }


  /**
   * @see org.mulgara.config.MulgaraConfig#setMulgaraHost(java.lang.String)
   */
  @Override
  public void setMulgaraHost(String mulgaraHost) {
    throw new UnsupportedOperationException("Read only class");
  }


  /**
   * @see org.mulgara.config.MulgaraConfig#setPersistencePath(java.lang.String)
   */
  @Override
  public void setPersistencePath(String persistencePath) {
    throw new UnsupportedOperationException("Read only class");
  }


  /**
   * @see org.mulgara.config.MulgaraConfig#setPersistentNodePoolFactory(org.mulgara.config.PersistentNodePoolFactory)
   */
  @Override
  public void setPersistentNodePoolFactory(PersistentNodePoolFactory persistentNodePoolFactory) {
    throw new UnsupportedOperationException("Read only class");
  }


  /**
   * @see org.mulgara.config.MulgaraConfig#setPersistentResolverFactory(org.mulgara.config.PersistentResolverFactory)
   */
  @Override
  public void setPersistentResolverFactory(PersistentResolverFactory persistentResolverFactory) {
    throw new UnsupportedOperationException("Read only class");
  }


  /**
   * @see org.mulgara.config.MulgaraConfig#setPersistentStringPoolFactory(org.mulgara.config.PersistentStringPoolFactory)
   */
  @Override
  public void setPersistentStringPoolFactory(PersistentStringPoolFactory persistentStringPoolFactory) {
    throw new UnsupportedOperationException("Read only class");
  }


  /**
   * @see org.mulgara.config.MulgaraConfig#setRelatedQueryHandler(org.mulgara.config.RelatedQueryHandler)
   */
  @Override
  public void setRelatedQueryHandler(RelatedQueryHandler relatedQueryHandler) {
    throw new UnsupportedOperationException("Read only class");
  }


  /**
   * @see org.mulgara.config.MulgaraConfig#setResolverFactory(int, org.mulgara.config.ResolverFactory)
   */
  @Override
  public void setResolverFactory(int index, ResolverFactory resolverFactory) throws IndexOutOfBoundsException {
    throw new UnsupportedOperationException("Read only class");
  }


  /**
   * @see org.mulgara.config.MulgaraConfig#setResolverFactory(org.mulgara.config.ResolverFactory[])
   */
  @Override
  public void setResolverFactory(ResolverFactory[] resolverFactoryArray) {
    throw new UnsupportedOperationException("Read only class");
  }


  /**
   * @see org.mulgara.config.MulgaraConfig#setRMIPort(int)
   */
  @Override
  public void setRMIPort(int RMIPort) {
    throw new UnsupportedOperationException("Read only class");
  }


  /**
   * @see org.mulgara.config.MulgaraConfig#setRMIInterrupt(boolean)
   */
  @Override
  public void setRMIInterrupt(boolean RMIInterrupt) {
    throw new UnsupportedOperationException("Read only class");
  }


  /**
   * @see org.mulgara.config.MulgaraConfig#setRuleLoader(int, org.mulgara.config.RuleLoader)
   */
  @Override
  public void setRuleLoader(int index, RuleLoader ruleLoader) throws IndexOutOfBoundsException {
    throw new UnsupportedOperationException("Read only class");
  }


  /**
   * @see org.mulgara.config.MulgaraConfig#setRuleLoader(org.mulgara.config.RuleLoader[])
   */
  @Override
  public void setRuleLoader(RuleLoader[] ruleLoaderArray) {
    throw new UnsupportedOperationException("Read only class");
  }


  /**
   * @see org.mulgara.config.MulgaraConfig#setSecurityAdapterFactory(int, org.mulgara.config.SecurityAdapterFactory)
   */
  @Override
  public void setSecurityAdapterFactory(int index, SecurityAdapterFactory securityAdapterFactory) throws IndexOutOfBoundsException {
    throw new UnsupportedOperationException("Read only class");
  }


  /**
   * @see org.mulgara.config.MulgaraConfig#setSecurityAdapterFactory(org.mulgara.config.SecurityAdapterFactory[])
   */
  @Override
  public void setSecurityAdapterFactory(SecurityAdapterFactory[] securityAdapterFactoryArray) {
    throw new UnsupportedOperationException("Read only class");
  }


  /**
   * @see org.mulgara.config.MulgaraConfig#setResolverFactory(int, org.mulgara.config.XpathFunctionResolver)
   */
  @Override
  public void setXpathFunctionResolver(int index, XpathFunctionResolver functionResolver) throws IndexOutOfBoundsException {
    throw new UnsupportedOperationException("Read only class");
  }


  /**
   * @see org.mulgara.config.MulgaraConfig#setResolverFactory(org.mulgara.config.XpathFunctionResolver[])
   */
  @Override
  public void setXpathFunctionResolver(XpathFunctionResolver[] xpathFunctionResolverArray) {
    throw new UnsupportedOperationException("Read only class");
  }


  /**
   * @see org.mulgara.config.MulgaraConfig#setServerName(java.lang.String)
   */
  @Override
  public void setServerName(String serverName) {
    throw new UnsupportedOperationException("Read only class");
  }


  /**
   * @see org.mulgara.config.MulgaraConfig#setSmtp(java.lang.String)
   */
  @Override
  public void setSmtp(String smtp) {
    throw new UnsupportedOperationException("Read only class");
  }


  /**
   * @see org.mulgara.config.MulgaraConfig#setStartupScript(java.lang.String)
   */
  @Override
  public void setStartupScript(String startupScript) {
    throw new UnsupportedOperationException("Read only class");
  }


  /**
   * @see org.mulgara.config.MulgaraConfig#setTemporaryNodePoolFactory(org.mulgara.config.TemporaryNodePoolFactory)
   */
  @Override
  public void setTemporaryNodePoolFactory(TemporaryNodePoolFactory temporaryNodePoolFactory) {
    throw new UnsupportedOperationException("Read only class");
  }


  /**
   * @see org.mulgara.config.MulgaraConfig#setTemporaryResolverFactory(org.mulgara.config.TemporaryResolverFactory)
   */
  @Override
  public void setTemporaryResolverFactory(TemporaryResolverFactory temporaryResolverFactory) {
    throw new UnsupportedOperationException("Read only class");
  }


  /**
   * @see org.mulgara.config.MulgaraConfig#setTemporaryStringPoolFactory(org.mulgara.config.TemporaryStringPoolFactory)
   */
  @Override
  public void setTemporaryStringPoolFactory(TemporaryStringPoolFactory temporaryStringPoolFactory) {
    throw new UnsupportedOperationException("Read only class");
  }


  /**
   * @see org.mulgara.config.MulgaraConfig#setTransactionTimeout(int)
   */
  @Override
  public void setTransactionTimeout(int transactionTimeout) {
    throw new UnsupportedOperationException("Read only class");
  }


  /**
   * @see org.mulgara.config.MulgaraConfig#setTripleStoreImplementation(java.lang.String)
   */
  @Override
  public void setTripleStoreImplementation(String tripleStoreImplementation) {
    throw new UnsupportedOperationException("Read only class");
  }


}
