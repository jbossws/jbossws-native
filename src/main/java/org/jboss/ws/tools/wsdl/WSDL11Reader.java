/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.ws.tools.wsdl;

// $Id$

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.wsdl.Binding;
import javax.wsdl.BindingInput;
import javax.wsdl.BindingOperation;
import javax.wsdl.BindingOutput;
import javax.wsdl.Definition;
import javax.wsdl.Fault;
import javax.wsdl.Import;
import javax.wsdl.Input;
import javax.wsdl.Message;
import javax.wsdl.Operation;
import javax.wsdl.Output;
import javax.wsdl.Part;
import javax.wsdl.Port;
import javax.wsdl.PortType;
import javax.wsdl.Service;
import javax.wsdl.Types;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.UnknownExtensibilityElement;
import javax.wsdl.extensions.mime.MIMEContent;
import javax.wsdl.extensions.mime.MIMEMultipartRelated;
import javax.wsdl.extensions.mime.MIMEPart;
import javax.wsdl.extensions.schema.Schema;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.extensions.soap.SOAPBinding;
import javax.wsdl.extensions.soap.SOAPBody;
import javax.wsdl.extensions.soap.SOAPHeader;
import javax.wsdl.extensions.soap.SOAPOperation;
import javax.xml.namespace.QName;

import org.jboss.logging.Logger;
import org.jboss.ws.Constants;
import org.jboss.ws.WSException;
import org.jboss.ws.core.jaxrpc.Style;
import org.jboss.ws.core.utils.DOMUtils;
import org.jboss.ws.core.utils.DOMWriter;
import org.jboss.ws.core.utils.ResourceURL;
import org.jboss.ws.metadata.wsdl.NCName;
import org.jboss.ws.metadata.wsdl.WSDLBinding;
import org.jboss.ws.metadata.wsdl.WSDLBindingMessageReference;
import org.jboss.ws.metadata.wsdl.WSDLBindingOperation;
import org.jboss.ws.metadata.wsdl.WSDLBindingOperationInput;
import org.jboss.ws.metadata.wsdl.WSDLBindingOperationOutput;
import org.jboss.ws.metadata.wsdl.WSDLDefinitions;
import org.jboss.ws.metadata.wsdl.WSDLEndpoint;
import org.jboss.ws.metadata.wsdl.WSDLInterface;
import org.jboss.ws.metadata.wsdl.WSDLInterfaceFault;
import org.jboss.ws.metadata.wsdl.WSDLInterfaceOperation;
import org.jboss.ws.metadata.wsdl.WSDLInterfaceOperationInput;
import org.jboss.ws.metadata.wsdl.WSDLInterfaceOperationOutfault;
import org.jboss.ws.metadata.wsdl.WSDLInterfaceOperationOutput;
import org.jboss.ws.metadata.wsdl.WSDLMIMEPart;
import org.jboss.ws.metadata.wsdl.WSDLProperty;
import org.jboss.ws.metadata.wsdl.WSDLRPCPart;
import org.jboss.ws.metadata.wsdl.WSDLRPCSignatureItem;
import org.jboss.ws.metadata.wsdl.WSDLSOAPHeader;
import org.jboss.ws.metadata.wsdl.WSDLService;
import org.jboss.ws.metadata.wsdl.WSDLTypes;
import org.jboss.ws.metadata.wsdl.WSDLUtils;
import org.jboss.ws.metadata.wsdl.XSModelTypes;
import org.jboss.ws.metadata.wsdl.WSDLRPCSignatureItem.Direction;
import org.jboss.ws.metadata.wsdl.xmlschema.JBossXSModel;
import org.jboss.ws.metadata.wsdl.xsd.SchemaUtils;
import org.jboss.ws.tools.JavaToXSD;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * A helper that translates a WSDL-1.1 object graph into a WSDL-2.0 object graph.
 *
 * @author Thomas.Diesler@jboss.org
 * @author Anil.Saldhana@jboss.org
 * @author <a href="jason.greene@jboss.com">Jason T. Greene</a>
 * @since 10-Oct-2004
 */
public class WSDL11Reader
{
   // provide logging
   private static final Logger log = Logger.getLogger(WSDL11Reader.class);

   private static QName SOAP12_BINDING = new QName(Constants.NS_SOAP12, "binding");
   private static QName SOAP12_BODY = new QName(Constants.NS_SOAP12, "body");
   private static QName SOAP12_OPERATION = new QName(Constants.NS_SOAP12, "operation");
   private static QName SOAP12_ADDRESS = new QName(Constants.NS_SOAP12, "address");

   private WSDLDefinitions destWsdl;

   // Maps wsdl message parts to their corresponding element names
   private Map<String, QName> messagePartToElementMap = new HashMap<String, QName>();

   // Map of <ns,URL> for schemalocation keyed by namespace
   private Map<String, URL> schemaLocationsMap = new HashMap<String, URL>();

   private LinkedHashMap<QName, Binding> allBindings;
   private LinkedHashMap<QName, Binding> portTypeBindings;

   // Temporary files used by this reader.
   private List<File> tempFiles = new ArrayList<File>();

   /**
    * Takes a WSDL11 Definition element and converts into
    * our object graph that has been developed for WSDL20
    *
    * @param srcWsdl The src WSDL11 definition
    * @param wsdlLoc The source location, if null we cannot process imports or includes
    */
   public WSDLDefinitions processDefinition(Definition srcWsdl, URL wsdlLoc) throws IOException
   {
      log.trace("processDefinition: " + wsdlLoc);

      destWsdl = new WSDLDefinitions();
      destWsdl.setWsdlTypes(new XSModelTypes());
      destWsdl.setWsdlOneOneDefinition(srcWsdl);
      destWsdl.setWsdlNamespace(Constants.NS_WSDL11);

      processNamespaces(srcWsdl);
      processTypes(srcWsdl, wsdlLoc);
      processServices(srcWsdl);

      if (getAllDefinedBindings(srcWsdl).size() != destWsdl.getBindings().length)
         processUnreachableBindings(srcWsdl);

      cleanupTemporaryFiles();

      return destWsdl;
   }

   private void cleanupTemporaryFiles()
   {
     for (File current : tempFiles)
     {
        current.delete();
     }
   }

   // process all bindings not within service separetly
   private void processUnreachableBindings(Definition srcWsdl)
   {
      log.trace("processUnreachableBindings");

      Iterator it = getAllDefinedBindings(srcWsdl).values().iterator();
      while (it.hasNext())
      {
         Binding srcBinding = (Binding)it.next();
         QName srcQName = srcBinding.getQName();

         WSDLBinding destBinding = destWsdl.getBinding(new NCName(srcQName));
         if (destBinding == null)
         {
            processBinding(srcWsdl, srcBinding);
         }
      }
   }

   private void processNamespaces(Definition srcWsdl)
   {
      String targetNS = srcWsdl.getTargetNamespace();
      destWsdl.setTargetNamespace(targetNS);

      // Copy wsdl namespaces
      Map nsMap = srcWsdl.getNamespaces();
      Iterator iter = nsMap.entrySet().iterator();
      while (iter.hasNext())
      {
         Map.Entry entry = (Map.Entry)iter.next();
         String prefix = (String)entry.getKey();
         String nsURI = (String)entry.getValue();
         destWsdl.registerNamespaceURI(nsURI, prefix);
      }
   }

   private void processTypes(Definition srcWsdl, URL wsdlLoc) throws IOException
   {
      log.trace("BEGIN processTypes: " + wsdlLoc);

      WSDLTypes destTypes = destWsdl.getWsdlTypes();

      Types srcTypes = srcWsdl.getTypes();
      if (srcTypes != null && srcTypes.getExtensibilityElements().size() > 0)
      {
         List extElements = srcTypes.getExtensibilityElements();
         int len = extElements.size();

         for (int i = 0; i < len; i++)
         {
            ExtensibilityElement extElement = (ExtensibilityElement)extElements.get(i);

            Element domElement;
            if (extElement instanceof Schema)
            {
               domElement = ((Schema)extElement).getElement();
            }
            else if (extElement instanceof UnknownExtensibilityElement)
            {
               domElement = ((UnknownExtensibilityElement)extElement).getElement();
            }
            else
            {
               throw new WSException("Unsupported extensibility element: " + extElement);
            }

            Element domElementClone = (Element)domElement.cloneNode(true);
            copyParentNamespaceDeclarations(domElementClone, domElement);

            String localname = domElementClone.getLocalName();
            try
            {
               if ("import".equals(localname))
               {
                  processSchemaImport(destTypes, wsdlLoc, domElementClone);
               }
               else if ("schema".equals(localname))
               {
                  processSchemaInclude(destTypes, wsdlLoc, domElementClone);
               }
               else
               {
                  throw new IllegalArgumentException("Unsuported schema element: " + localname);
               }
            }
            catch (IOException e)
            {
               throw new WSException("Cannot extract schema definition", e);
            }
         }

         if (len > 0)
         {
            JavaToXSD jxsd = new JavaToXSD();
            JBossXSModel xsmodel = jxsd.parseSchema(schemaLocationsMap);
            WSDLUtils.addSchemaModel(destTypes, destWsdl.getTargetNamespace(), xsmodel);
         }
      }
      else
      {
         log.trace("Empty wsdl types element, processing imports");
         Iterator it = srcWsdl.getImports().values().iterator();
         while (it.hasNext())
         {
            List<Import> srcImports = (List<Import>)it.next();
            for (Import srcImport : srcImports)
            {
               Definition impDefinition = srcImport.getDefinition();
               String impLoc = impDefinition.getDocumentBaseURI();
               processTypes(impDefinition, new URL(impLoc));
            }
         }
      }

      log.trace("END processTypes: " + wsdlLoc + "\n" + destTypes);
   }

   private void copyParentNamespaceDeclarations(Element destElement, Element srcElement)
   {
      Node parent = srcElement.getParentNode();
      while (parent != null)
      {
         if (parent.hasAttributes())
         {
            NamedNodeMap attributes = parent.getAttributes();
            for (int i = 0; i < attributes.getLength(); i++)
            {
               Attr attr = (Attr)attributes.item(i);
               String name = attr.getName();
               String value = attr.getValue();
               if (name.startsWith("xmlns:") && destElement.hasAttribute(name) == false)
                  destElement.setAttribute(name, value);
            }
         }
         parent = parent.getParentNode();
      }
   }

   private void processSchemaImport(WSDLTypes types, URL wsdlLoc, Element importEl) throws IOException
   {
      if (wsdlLoc == null)
         throw new IllegalArgumentException("Cannot process import, parent location not set");

      log.trace("processSchemaImport: " + wsdlLoc);

      String location = getOptionalAttribute(importEl, "schemaLocation");
      if (location == null)
         throw new IllegalArgumentException("schemaLocation is null for xsd:import");

      URL locationURL = getLocationURL(wsdlLoc, location);
      Element rootElement = DOMUtils.parse(new ResourceURL(locationURL).openStream());
      URL newloc = processSchemaInclude(types, locationURL, rootElement);
      if (newloc != null)
         importEl.setAttribute("schemaLocation", newloc.toExternalForm());
   }

   private URL processSchemaInclude(WSDLTypes types, URL wsdlLoc, Element schemaEl) throws IOException
   {
      if (wsdlLoc == null)
         throw new IllegalArgumentException("Cannot process iclude, parent location not set");

      File tmpFile = null;
      if (wsdlLoc == null)
         throw new IllegalArgumentException("Cannot process include, parent location not set");

      log.trace("processSchemaInclude: " + wsdlLoc);

      String schemaPrefix = schemaEl.getPrefix();

      String importTag = (schemaPrefix == null) ? "import" : schemaPrefix + ":import";
      Element importElement = schemaEl.getOwnerDocument().createElementNS(Constants.NS_SCHEMA_XSD, importTag);
      importElement.setAttribute("namespace", Constants.URI_SOAP11_ENC);
      schemaEl.insertBefore(importElement, DOMUtils.getFirstChildElement(schemaEl));

      // Handle schema includes
      Iterator it = DOMUtils.getChildElements(schemaEl, new QName(Constants.NS_SCHEMA_XSD, "include"));
      while (it.hasNext())
      {
         Element includeEl = (Element)it.next();
         String location = getOptionalAttribute(includeEl, "schemaLocation");
         if (location == null)
            throw new IllegalArgumentException("schemaLocation is null for xsd:include");

         URL locationURL = getLocationURL(wsdlLoc, location);
         Element rootElement = DOMUtils.parse(new ResourceURL(locationURL).openStream());
         URL newloc = processSchemaInclude(types, locationURL, rootElement);
         if (newloc != null)
            includeEl.setAttribute("schemaLocation", newloc.toExternalForm());
      }

      String targetNS = getOptionalAttribute(schemaEl, "targetNamespace");
      if (targetNS != null)
      {
         log.trace("processSchemaInclude: [targetNS=" + targetNS + ",parentURL=" + wsdlLoc + "]");

         tmpFile = SchemaUtils.getSchemaTempFile(targetNS);
         tempFiles.add(tmpFile);

         FileWriter fwrite = new FileWriter(tmpFile);
         new DOMWriter(fwrite).setPrettyprint(true).print(schemaEl);
         fwrite.close();

         schemaLocationsMap.put(targetNS, tmpFile.toURL());
      }

      // schema elements that have no target namespace are skipped
      //
      //  <xsd:schema>
      //    <xsd:import namespace="http://org.jboss.webservice/example/types" schemaLocation="Hello.xsd"/>
      //    <xsd:import namespace="http://org.jboss.webservice/example/types/arrays/org/jboss/test/webservice/admindevel" schemaLocation="subdir/HelloArr.xsd"/>
      //  </xsd:schema>
      if (targetNS == null)
      {
         log.trace("Schema element without target namespace in: " + wsdlLoc);
      }

      handleSchemaImports(schemaEl, wsdlLoc);

      return tmpFile != null ? tmpFile.toURL() : null;
   }

   private void handleSchemaImports(Element schemaEl, URL wsdlLoc) throws MalformedURLException
   {
      if (wsdlLoc == null)
         throw new IllegalArgumentException("Cannot process import, parent location not set");

      Iterator it = DOMUtils.getChildElements(schemaEl, new QName(Constants.NS_SCHEMA_XSD, "import"));
      while (it.hasNext())
      {
         Element includeEl = (Element)it.next();
         String schemaLocation = getOptionalAttribute(includeEl, "schemaLocation");
         String namespace = getOptionalAttribute(includeEl, "namespace");

         log.trace("handleSchemaImport: [namespace=" + namespace + ",schemaLocation=" + schemaLocation + "]");

         // Skip, let the entity resolver resolve these
         if (namespace != null && schemaLocation != null)
         {
            URL currLoc = getLocationURL(wsdlLoc, schemaLocation);
            schemaLocationsMap.put(namespace, currLoc);
         }
         else
         {
            log.trace("Skip schema import: [namespace=" + namespace + ",schemaLocation=" + schemaLocation + "]");
         }
      }
   }

   private URL getLocationURL(URL parentURL, String location) throws MalformedURLException
   {
      log.trace("getLocationURL: [location=" + location + ",parent=" + parentURL + "]");

      URL locationURL = null;
      try
      {
         locationURL = new URL(location);
      }
      catch (MalformedURLException e)
      {
         // ignore malformed URL
      }

      if (locationURL == null)
      {
         String parentProtocol = parentURL.getProtocol();
         if (parentProtocol.indexOf("file") >= 0 && !location.startsWith("/"))
         {
            String path = parentURL.toExternalForm();
            path = path.substring(0, path.lastIndexOf("/"));
            locationURL = new URL(path + "/" + location);
         }
         else if (parentProtocol.startsWith("http") && location.startsWith("/"))
         {
            String path = parentProtocol + "://" + parentURL.getHost() + ":" + parentURL.getPort();
            locationURL = new URL(path + location);
         }
         else if (parentProtocol.equals("jar") && !location.startsWith("/"))
         {
            String path = parentURL.toExternalForm();
            path = path.substring(0, path.lastIndexOf("/"));
            locationURL = new URL(path + "/" + location);
         }
         else
         {
            throw new WSException("Unsupported schemaLocation: " + location);
         }
      }

      log.trace("Modified schemaLocation: " + locationURL);
      return locationURL;
   }

   private void processPortType(Definition srcWsdl, PortType srcPortType)
   {
      log.trace("processPortType: " + srcPortType.getQName());

      QName qname = srcPortType.getQName();
      NCName ncName = new NCName(qname);
      if (destWsdl.getInterface(ncName) == null)
      {
         WSDLInterface destInterface = new WSDLInterface(destWsdl);
         destInterface.setName(ncName);
         destInterface.setQName(qname);

         // eventing extensions
         QName eventSourceProp = (QName)srcPortType.getExtensionAttribute(Constants.WSDL_ATTRIBUTE_WSE_EVENTSOURCE);
         if (eventSourceProp != null && eventSourceProp.getLocalPart().equals(Boolean.TRUE.toString()))
         {
            destInterface.addProperty(new WSDLProperty(Constants.WSDL_PROPERTY_EVENTSOURCE, eventSourceProp.getLocalPart()));
         }

         destWsdl.addInterface(destInterface);

         processPortTypeOperations(srcWsdl, destInterface, srcPortType);
      }
   }

   private void processPortTypeOperations(Definition srcWsdl, WSDLInterface destInterface, PortType srcPortType)
   {
      Iterator itOperations = srcPortType.getOperations().iterator();
      while (itOperations.hasNext())
      {
         Operation srcOperation = (Operation)itOperations.next();

         WSDLInterfaceOperation destOperation = new WSDLInterfaceOperation(destInterface);
         destOperation.setName(new NCName(srcOperation.getName()));
         destOperation.setStyle(getOperationStyle(srcWsdl, srcPortType, srcOperation));

         processOperationInput(srcWsdl, srcOperation, destOperation, srcPortType);
         processOperationOutput(srcWsdl, srcOperation, destOperation, srcPortType);
         processOperationFaults(srcOperation, destOperation, destInterface);

         destInterface.addOperation(destOperation);
      }
   }

   private void processOperationInput(Definition srcWsdl, Operation srcOperation, WSDLInterfaceOperation destOperation, PortType srcPortType)
   {
      Input srcInput = srcOperation.getInput();
      if (srcInput != null)
      {
         Message srcMessage = srcInput.getMessage();
         log.trace("processOperationInput: " + srcMessage.getQName());

         QName wsaAction = (QName)srcInput.getExtensionAttribute(Constants.WSDL_ATTRIBUTE_WSA_ACTION);
         if (wsaAction != null)
            destOperation.addProperty(new WSDLProperty(Constants.WSDL_PROPERTY_ACTION_IN, wsaAction.getLocalPart()));

         List<String> paramOrder = (List<String>) srcOperation.getParameterOrdering();
         if (paramOrder != null)
         {
            for (String name : paramOrder)
            {
               if (srcMessage.getPart(name) != null)
                  destOperation.addRpcSignatureItem(new WSDLRPCSignatureItem(name));
            }
         }

         WSDLInterfaceOperationInput rpcInput = new WSDLInterfaceOperationInput(destOperation);
         for (Part srcPart : (List<Part>) srcMessage.getOrderedParts(paramOrder))
         {
            if (Constants.URI_STYLE_IRI == destOperation.getStyle())
            {
               WSDLInterfaceOperationInput destInput = new WSDLInterfaceOperationInput(destOperation);
               QName elementName = messagePartToElementName(srcWsdl, srcPortType, srcOperation, srcMessage, srcPart);
               destInput.setElement(elementName);

               //Lets remember the Message name
               destInput.setMessageName(srcMessage.getQName());
               destOperation.addProperty(new WSDLProperty(Constants.WSDL_PROPERTY_MESSAGE_NAME_IN, srcMessage.getQName().getLocalPart()));

               destInput.setPartName(srcPart.getName());

               destOperation.addInput(destInput);
            }
            else
            {
               // If we don't have a type then we aren't a valid RPC parameter
               // This could happen on a header element, in which case the
               // binding will pick it up
               QName xmlType = srcPart.getTypeName();
               if (xmlType != null)
                  rpcInput.addChildPart(new WSDLRPCPart(srcPart.getName(), destWsdl.registerQName(xmlType)));
               else
                  messagePartToElementName(srcWsdl, srcPortType, srcOperation, srcMessage, srcPart);
            }
         }
         if (Constants.URI_STYLE_RPC == destOperation.getStyle())
         {
            // This is really a place holder, but also the actual value used in
            // WSDL 2.0 RPC bindings
            rpcInput.setElement(destOperation.getQName());
            rpcInput.setMessageName(srcMessage.getQName());
            destOperation.addInput(rpcInput);
         }
      }
   }

   private void processOperationOutput(Definition srcWsdl, Operation srcOperation, WSDLInterfaceOperation destOperation, PortType srcPortType)
   {
      Output srcOutput = srcOperation.getOutput();
      if (srcOutput == null)
      {
         destOperation.setPattern(Constants.WSDL20_PATTERN_IN_ONLY);
         return;
      }

      Message srcMessage = srcOutput.getMessage();
      log.trace("processOperationOutput: " + srcMessage.getQName());

      destOperation.setPattern(Constants.WSDL20_PATTERN_IN_OUT);
      QName wsaAction = (QName) srcOutput.getExtensionAttribute(Constants.WSDL_ATTRIBUTE_WSA_ACTION);
      if (wsaAction != null)
         destOperation.addProperty(new WSDLProperty(Constants.WSDL_PROPERTY_ACTION_OUT, wsaAction.getLocalPart()));

      List<String> paramOrder = (List<String>) srcOperation.getParameterOrdering();
      if (paramOrder != null)
      {
         for (String name : paramOrder)
         {
            if (srcMessage.getPart(name) != null)
            {
               WSDLRPCSignatureItem item = destOperation.getRpcSignatureitem(name);
               if (item != null)
                  item.setDirection(Direction.INOUT);
               else
                  destOperation.addRpcSignatureItem(new WSDLRPCSignatureItem(name, Direction.OUT));
            }
         }
      }

      WSDLInterfaceOperationOutput rpcOutput = new WSDLInterfaceOperationOutput(destOperation);
      for (Part srcPart : (List<Part>) srcMessage.getOrderedParts(null))
      {
         if (Constants.URI_STYLE_IRI == destOperation.getStyle())
         {
            WSDLInterfaceOperationOutput destOutput = new WSDLInterfaceOperationOutput(destOperation);

            QName elementName = messagePartToElementName(srcWsdl, srcPortType, srcOperation, srcMessage, srcPart);
            destOutput.setElement(elementName);

            // Lets remember the Message name
            destOutput.setMessageName(srcMessage.getQName());
            destOperation.addProperty(new WSDLProperty(Constants.WSDL_PROPERTY_MESSAGE_NAME_OUT, srcMessage.getQName()
                  .getLocalPart()));

            // Remember the original part name
            destOutput.setPartName(srcPart.getName());

            destOperation.addOutput(destOutput);
         }
         else
         {
            // If we don't have a type then we aren't a valid RPC parameter
            // This could happen on a header element, in which case the
            // binding will pick it up
            QName xmlType = srcPart.getTypeName();
            if (xmlType != null)
               rpcOutput.addChildPart(new WSDLRPCPart(srcPart.getName(), destWsdl.registerQName(xmlType)));
            else
               messagePartToElementName(srcWsdl, srcPortType, srcOperation, srcMessage, srcPart);
         }
      }

      if (Constants.URI_STYLE_RPC == destOperation.getStyle())
      {
         // This is really a place holder, but also the actual value used in
         // WSDL 2.0 RPC bindings
         QName name = destOperation.getQName();
         rpcOutput.setElement(new QName(name.getNamespaceURI(), name.getLocalPart() + "Response"));
         rpcOutput.setMessageName(srcMessage.getQName());
         destOperation.addOutput(rpcOutput);
      }
   }

   private void processOperationFaults(Operation srcOperation, WSDLInterfaceOperation destOperation, WSDLInterface destInterface)
   {

      Map faults = srcOperation.getFaults();
      Iterator itFaults = faults.values().iterator();
      while (itFaults.hasNext())
      {
         Fault srcFault = (Fault)itFaults.next();
         processOperationFault(destOperation, destInterface, srcFault);
      }
   }

   private void processOperationFault(WSDLInterfaceOperation destOperation, WSDLInterface destInterface, Fault srcFault)
   {
      String faultName = srcFault.getName();
      log.trace("processOperationFault: " + faultName);

      WSDLInterfaceFault destFault = new WSDLInterfaceFault(destInterface);
      NCName ncName = new NCName(faultName);
      destFault.setName(ncName);

      Message message = srcFault.getMessage();
      QName messageName = message.getQName();

      Map partsMap = message.getParts();
      if (partsMap.size() != 1)
         throw new WSException("Unsupported number of fault parts in message " + messageName);

      Part part = (Part)partsMap.values().iterator().next();
      QName xmlName = part.getElementName();

      if (xmlName != null)
      {
         destFault.setElement(xmlName);
      }
      else
      {
         destFault.setElement(messageName);
         log.warn("Unsupported fault message part in message: " + messageName);
      }

      WSDLInterfaceFault prevFault = destInterface.getFault(ncName);
      if (prevFault != null && prevFault.getName().equals(ncName) == false)
         throw new WSException("Fault name must be unique: " + faultName);

      // Add the fault to the interface
      destInterface.addFault(destFault);

      // Add the fault refererence to the operation
      WSDLInterfaceOperationOutfault opOutFault = new WSDLInterfaceOperationOutfault(destOperation);
      opOutFault.setRef(new QName(destWsdl.getTargetNamespace(), faultName));
      destOperation.addOutfault(opOutFault);
   }

   /** Translate the message part name into an XML element name.
    */
   private QName messagePartToElementName(Definition srcWsdl, PortType srcPortType, Operation srcOperation, Message srcMessage, Part srcPart)
   {
      // <part name="param" element="tns:SomeType" />
      QName xmlName = srcPart.getElementName();

      // <part name="param" type="xsd:string" />
      if (xmlName == null)
         xmlName = new QName(srcPart.getName());

      xmlName = destWsdl.registerQName(xmlName);
      String key = srcMessage.getQName() + "->" + srcPart.getName();
      messagePartToElementMap.put(key, xmlName);

      return xmlName;
   }

   private BindingOperation getBindingOperation(Definition srcWsdl, PortType srcPortType, Operation srcOperation)
   {
      Binding srcBinding = getPortTypeBindings(srcWsdl).get(srcPortType.getQName());

      if (srcBinding == null)
         throw new WSException("Cannot find binding for: " + srcPortType.getQName());

      String srcOperationName = srcOperation.getName();
      BindingOperation srcBindingOperation = srcBinding.getBindingOperation(srcOperationName, null, null);
      if (srcBindingOperation == null)
         throw new WSException("Cannot find binding operation for: " + srcOperationName);
      return srcBindingOperation;
   }

   private String getOperationStyle(Definition srcWsdl, PortType srcPortType, Operation srcOperation)
   {
      Binding srcBinding = getPortTypeBindings(srcWsdl).get(srcPortType.getQName());
      BindingOperation srcBindingOperation = getBindingOperation(srcWsdl, srcPortType, srcOperation);

      String operationStyle = null;
      List<ExtensibilityElement> extList = srcBindingOperation.getExtensibilityElements();
      for (ExtensibilityElement extElement : extList)
      {
         QName elementType = extElement.getElementType();
         if (extElement instanceof SOAPOperation)
         {
            SOAPOperation soapOp = (SOAPOperation)extElement;
            operationStyle = soapOp.getStyle();
         }
         else if (SOAP12_OPERATION.equals(elementType))
         {
            Element domElement = ((UnknownExtensibilityElement)extElement).getElement();
            operationStyle = getOptionalAttribute(domElement, "style");
         }
      }

      if (operationStyle == null)
      {
         for (ExtensibilityElement extElement : (List<ExtensibilityElement>) srcBinding.getExtensibilityElements())
         {
            QName elementType = extElement.getElementType();
            if (extElement instanceof SOAPBinding)
            {
               SOAPBinding soapBinding = (SOAPBinding) extElement;
               operationStyle = soapBinding.getStyle();
            }
            else if (SOAP12_BINDING.equals(elementType))
            {
               Element domElement = ((UnknownExtensibilityElement) extElement).getElement();
               operationStyle = getOptionalAttribute(domElement, "style");
            }
         }
      }

      return  ("rpc".equals(operationStyle)) ? Constants.URI_STYLE_RPC : Constants.URI_STYLE_IRI;
   }

   private void processBinding(Definition srcWsdl, Binding srcBinding)
   {
      QName srcBindingQName = srcBinding.getQName();
      log.trace("processBinding: " + srcBindingQName);

      NCName ncName = new NCName(srcBindingQName);
      if (destWsdl.getBinding(ncName) == null)
      {
         PortType srcPortType = srcBinding.getPortType();
         if (srcPortType == null)
            throw new WSException("Cannot find port type for binding: " + ncName);

         processPortType(srcWsdl, srcPortType);

         WSDLBinding destBinding = new WSDLBinding(destWsdl);
         destBinding.setQName(srcBindingQName);
         destBinding.setName(ncName);
         destBinding.setInterfaceName(srcPortType.getQName());

         String bindingStyle = Style.getDefaultStyle().toString();
         List<ExtensibilityElement> extList = srcBinding.getExtensibilityElements();
         for (ExtensibilityElement extElement : extList)
         {
            QName elementType = extElement.getElementType();
            if (extElement instanceof SOAPBinding)
            {
               destBinding.setType(Constants.NS_SOAP11);
               SOAPBinding soapBinding = (SOAPBinding)extElement;
               bindingStyle = soapBinding.getStyle();
            }
            else if (SOAP12_BINDING.equals(elementType))
            {
               destBinding.setType(Constants.NS_SOAP12);
               Element domElement = ((UnknownExtensibilityElement)extElement).getElement();
               bindingStyle = getOptionalAttribute(domElement, "style");
            }
         }
         destWsdl.addBinding(destBinding);
         processBindingOperations(destBinding, srcBinding, bindingStyle);
      }
   }

   private Map<QName, Binding> getPortTypeBindings(Definition srcWsdl)
   {
      getAllDefinedBindings(srcWsdl);
      return portTypeBindings;
   }

   private Map<QName, Binding> getAllDefinedBindings(Definition srcWsdl)
   {
      if (allBindings != null)
         return allBindings;

      allBindings = new LinkedHashMap<QName, Binding>();
      portTypeBindings = new LinkedHashMap<QName, Binding>();
      Map srcBindings = srcWsdl.getBindings();
      Iterator itBinding = srcBindings.values().iterator();
      while (itBinding.hasNext())
      {
         Binding srcBinding = (Binding)itBinding.next();
         allBindings.put(srcBinding.getQName(), srcBinding);
         portTypeBindings.put(srcBinding.getPortType().getQName(), srcBinding);
      }

      // Bindings not available when pulled in through <wsdl:import>
      // http://sourceforge.net/tracker/index.php?func=detail&aid=1240323&group_id=128811&atid=712792
      Iterator itService = srcWsdl.getServices().values().iterator();
      while (itService.hasNext())
      {
         Service srcService = (Service)itService.next();
         Iterator itPort = srcService.getPorts().values().iterator();
         while (itPort.hasNext())
         {
            Port srcPort = (Port)itPort.next();
            Binding srcBinding = srcPort.getBinding();
            allBindings.put(srcBinding.getQName(), srcBinding);
            portTypeBindings.put(srcBinding.getPortType().getQName(), srcBinding);
         }
      }

      return allBindings;
   }

   private void processBindingOperations(WSDLBinding destBinding, Binding srcBinding, String bindingStyle)
   {
      Iterator it = srcBinding.getBindingOperations().iterator();
      while (it.hasNext())
      {
         BindingOperation srcBindingOperation = (BindingOperation)it.next();
         processBindingOperation(destBinding, bindingStyle, srcBindingOperation);
      }
   }

   private void processBindingOperation(WSDLBinding destBinding, String bindingStyle, BindingOperation srcBindingOperation)
   {
      String srcBindingName = srcBindingOperation.getName();
      log.trace("processBindingOperation: " + srcBindingName);

      WSDLInterface destInterface = destBinding.getInterface();
      String namespaceURI = destInterface.getQName().getNamespaceURI();

      WSDLBindingOperation destBindingOperation = new WSDLBindingOperation(destBinding);
      QName refQName = new QName(namespaceURI, srcBindingName);
      destBindingOperation.setRef(refQName);
      destBinding.addOperation(destBindingOperation);

      String opName = srcBindingName;
      WSDLInterfaceOperation destIntfOperation = destInterface.getOperation(new NCName(opName));

      // Process soap:operation@soapAction, soap:operation@style
      List<ExtensibilityElement> extList = srcBindingOperation.getExtensibilityElements();
      for (ExtensibilityElement extElement : extList)
      {
         QName elementType = extElement.getElementType();
         if (extElement instanceof SOAPOperation)
         {
            SOAPOperation soapOp = (SOAPOperation)extElement;
            destBindingOperation.setSOAPAction(soapOp.getSoapActionURI());
         }
         else if (SOAP12_OPERATION.equals(elementType))
         {
            Element domElement = ((UnknownExtensibilityElement)extElement).getElement();
            destBindingOperation.setSOAPAction(getOptionalAttribute(domElement, "soapAction"));
         }
      }

      BindingInput srcBindingInput = srcBindingOperation.getBindingInput();
      if (srcBindingInput != null)
      {
         processBindingInput(destBindingOperation, destIntfOperation, srcBindingOperation, srcBindingInput);
      }

      BindingOutput srcBindingOutput = srcBindingOperation.getBindingOutput();
      if (srcBindingOutput != null)
      {
         processBindingOutput(destBindingOperation, destIntfOperation, srcBindingOperation, srcBindingOutput);
      }
   }

   interface ReferenceCallback
   {
      void removeReference(QName element);
      void removeRPCPart(String partName);
      QName getXmlType(String partName);
   }

   private void processBindingInput(WSDLBindingOperation destBindingOperation, final WSDLInterfaceOperation destIntfOperation, final BindingOperation srcBindingOperation, BindingInput srcBindingInput)
   {
      log.trace("processBindingInput");

      QName soap11Body = new QName(Constants.NS_SOAP11, "body");
      List<ExtensibilityElement> extList = srcBindingInput.getExtensibilityElements();
      WSDLBindingOperationInput input = new WSDLBindingOperationInput(destBindingOperation);
      destBindingOperation.addInput(input);

      ReferenceCallback cb = new ReferenceCallback()
      {
         public QName getXmlType(String partName)
         {
            return srcBindingOperation.getOperation().getInput().getMessage().getPart(partName).getTypeName();
         }

         public void removeReference(QName element)
         {
            WSDLInterfaceOperationInput destIntfInput = destIntfOperation.getInput(element);
            if (destIntfInput == null)
               destIntfOperation.removeInput(element);
         }

         public void removeRPCPart(String partName)
         {
            WSDLInterfaceOperationInput operationInput = destIntfOperation.getInput(destIntfOperation.getQName());
            operationInput.removeChildPart(partName);
         }
      };

      processBindingReference(destBindingOperation, destIntfOperation, soap11Body, extList, input, cb);
   }

   private void processBindingOutput(WSDLBindingOperation destBindingOperation, final WSDLInterfaceOperation destIntfOperation, final BindingOperation srcBindingOperation, BindingOutput srcBindingOutput)
   {
      log.trace("processBindingInput");

      QName soap11Body = new QName(Constants.NS_SOAP11, "body");
      List<ExtensibilityElement> extList = srcBindingOutput.getExtensibilityElements();
      WSDLBindingOperationOutput output = new WSDLBindingOperationOutput(destBindingOperation);
      destBindingOperation.addOutput(output);

      ReferenceCallback cb = new ReferenceCallback()
      {
         public QName getXmlType(String partName)
         {
            return srcBindingOperation.getOperation().getOutput().getMessage().getPart(partName).getTypeName();
         }

         public void removeReference(QName element)
         {
            WSDLInterfaceOperationOutput destIntfOutput = destIntfOperation.getOutput(element);
            if (destIntfOutput == null)
               destIntfOperation.removeInput(element);
         }

         public void removeRPCPart(String partName)
         {
            QName name = destIntfOperation.getQName();
            WSDLInterfaceOperationOutput operationOutput = destIntfOperation.getOutput(new QName(name.getNamespaceURI(), name.getLocalPart() + "Response"));
            operationOutput.removeChildPart(partName);
         }
      };

      processBindingReference(destBindingOperation, destIntfOperation, soap11Body, extList, output, cb);
   }


   private void processBindingReference(WSDLBindingOperation destBindingOperation, WSDLInterfaceOperation destIntfOperation,
         QName soap11Body, List<ExtensibilityElement> extList, WSDLBindingMessageReference reference, ReferenceCallback callback)
   {
      for (ExtensibilityElement extElement : extList)
      {
         QName elementType = extElement.getElementType();
         if (soap11Body.equals(elementType) || SOAP12_BODY.equals(elementType))
         {
            processEncodingStyle(extElement, destBindingOperation);
         }
         else if (extElement instanceof SOAPHeader)
         {
            SOAPHeader header = (SOAPHeader)extElement;
            QName messageQName = header.getMessage();
            String partName = header.getPart();

            String key = messageQName + "->" + partName;
            QName elementName = (QName)messagePartToElementMap.get(key);
            if (elementName == null)
               throw new WSException("Could not determine element name from header: " + partName);

            reference.addSoapHeader(new WSDLSOAPHeader(elementName, partName));
            if (Constants.URI_STYLE_IRI == destIntfOperation.getStyle())
            {
               callback.removeReference(elementName);
            }
            else
            {
               // Just in case
               callback.removeRPCPart(partName);
            }
         }
         else if (extElement instanceof MIMEMultipartRelated)
         {
            MIMEMultipartRelated related = (MIMEMultipartRelated)extElement;
            Iterator i = related.getMIMEParts().iterator();
            while (i.hasNext())
            {
               MIMEPart part = (MIMEPart)i.next();
               Iterator j = part.getExtensibilityElements().iterator();
               String name = null;
               String types = null;

               while (j.hasNext())
               {
                  ExtensibilityElement inner = (ExtensibilityElement)j.next();
                  if (inner instanceof MIMEContent)
                  {
                     MIMEContent content = (MIMEContent)inner;
                     name = content.getPart();
                     if (types == null)
                     {
                        types = content.getType();
                     }
                     else
                     {
                        types += "," + content.getType();
                     }
                  }
               }

               // Found content types in this part
               if (name != null)
               {
                  QName xmlType  = callback.getXmlType(name);
                  reference.addMimePart(new WSDLMIMEPart(name, xmlType, types));
                  if (Constants.URI_STYLE_IRI == destIntfOperation.getStyle())
                  {
                     // A mime part must be defined as <part type="">
                     callback.removeReference(new QName(name));
                  }
                  else
                  {
                     callback.removeRPCPart(name);
                  }
               }
            }
         }
      }
   }


   private void processEncodingStyle(ExtensibilityElement extElement, WSDLBindingOperation destBindingOperation)
   {
      log.trace("processEncodingStyle");

      String encStyle = null;
      QName elementType = extElement.getElementType();
      if (extElement instanceof SOAPBody)
      {
         SOAPBody body = (SOAPBody)extElement;
         List encStyleList = body.getEncodingStyles();
         if (encStyleList != null)
         {
            if (encStyleList.size() > 1)
               log.warn("Multiple encoding styles not supported: " + encStyleList);

            if (encStyleList.size() > 0)
            {
               encStyle = (String)encStyleList.get(0);
            }
         }
      }
      else if (SOAP12_BODY.equals(elementType))
      {
         Element domElement = ((UnknownExtensibilityElement)extElement).getElement();
         encStyle = getOptionalAttribute(domElement, "encodingStyle");
      }

      if (encStyle != null)
      {
         String setStyle = destBindingOperation.getEncodingStyle();
         if (encStyle.equals(setStyle) == false)
            log.warn("Encoding style '" + encStyle + "' not supported for: " + destBindingOperation.getRef());

         destBindingOperation.setEncodingStyle(encStyle);
      }
   }

   private void processServices(Definition srcWsdl)
   {
      log.trace("BEGIN processServices: " + srcWsdl.getDocumentBaseURI());

      // Each definition needs a clear binding cache
      allBindings = null;

      if (srcWsdl.getServices().size() > 0)
      {
         Iterator it = srcWsdl.getServices().values().iterator();
         while (it.hasNext())
         {
            Service srcService = (Service)it.next();
            QName qname = srcService.getQName();
            WSDLService destService = new WSDLService(destWsdl);
            destService.setName(new NCName(qname));
            destService.setQName(qname);
            destWsdl.addService(destService);
            processPorts(srcWsdl, destService, srcService);
         }
      }
      else
      {
         log.trace("Empty wsdl services, processing imports");
         Iterator it = srcWsdl.getImports().values().iterator();
         while (it.hasNext())
         {
            List<Import> srcImports = (List<Import>)it.next();
            for (Import srcImport : srcImports)
            {
               Definition importDefinition = srcImport.getDefinition();
               processServices(importDefinition);
            }
         }

         // The binding cache must be clear after imports, so that undefined bindings can be located
         allBindings = null;
      }

      log.trace("END processServices: " + srcWsdl.getDocumentBaseURI());
   }

   private void processPorts(Definition srcWsdl, WSDLService destService, Service srcService)
   {
      Iterator it = srcService.getPorts().values().iterator();
      while (it.hasNext())
      {
         Port srcPort = (Port)it.next();
         processPort(srcWsdl, destService, srcPort);
      }
   }

   private void processPort(Definition srcWsdl, WSDLService destService, Port srcPort)
   {
      log.trace("processPort: " + srcPort.getName());

      Binding srcBinding = srcPort.getBinding();

      WSDLEndpoint destEndpoint = new WSDLEndpoint(destService);
      destEndpoint.setName(new NCName(srcPort.getName()));
      destEndpoint.setBinding(srcBinding.getQName());
      destEndpoint.setQName(new QName(srcWsdl.getTargetNamespace(), srcPort.getName()));
      destEndpoint.setAddress(getSOAPAddress(srcPort));
      destService.addEndpoint(destEndpoint);

      processBinding(srcWsdl, srcBinding);
   }

   /** Get the endpoint address from the ports extensible element
    */
   private String getSOAPAddress(Port srcPort)
   {
      String soapAddress = "dummy";

      Iterator it = srcPort.getExtensibilityElements().iterator();
      while (it.hasNext())
      {
         ExtensibilityElement extElement = (ExtensibilityElement)it.next();
         QName elementType = extElement.getElementType();

         if (extElement instanceof SOAPAddress)
         {
            SOAPAddress addr = (SOAPAddress)extElement;
            soapAddress = addr.getLocationURI();
            break;
         }
         else if (SOAP12_ADDRESS.equals(elementType))
         {
            Element domElement = ((UnknownExtensibilityElement)extElement).getElement();
            soapAddress = getOptionalAttribute(domElement, "location");
            break;
         }
         else if ("address".equals(elementType.getLocalPart()))
         {
            log.warn("Unprocessed extension element: " + elementType);
         }
      }

      if (soapAddress == null)
         throw new WSException("Cannot obtain SOAP address");

      return soapAddress;
   }

   private String getOptionalAttribute(Element domElement, String attrName)
   {
      String attrValue = domElement.getAttribute(attrName);
      return (attrValue.length() > 0 ? attrValue : null);
   }
}
