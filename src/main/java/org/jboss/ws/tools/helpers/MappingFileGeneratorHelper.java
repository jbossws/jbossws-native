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
package org.jboss.ws.tools.helpers;

import java.beans.Introspector;
import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPElement;

import org.apache.xerces.xs.XSAttributeDeclaration;
import org.apache.xerces.xs.XSAttributeUse;
import org.apache.xerces.xs.XSComplexTypeDefinition;
import org.apache.xerces.xs.XSElementDeclaration;
import org.apache.xerces.xs.XSModelGroup;
import org.apache.xerces.xs.XSObjectList;
import org.apache.xerces.xs.XSParticle;
import org.apache.xerces.xs.XSSimpleTypeDefinition;
import org.apache.xerces.xs.XSTerm;
import org.apache.xerces.xs.XSTypeDefinition;
import org.jboss.logging.Logger;
import org.jboss.ws.Constants;
import org.jboss.ws.WSException;
import org.jboss.ws.core.jaxrpc.LiteralTypeMapping;
import org.jboss.ws.core.utils.JavaUtils;
import org.jboss.ws.metadata.jaxrpcmapping.ExceptionMapping;
import org.jboss.ws.metadata.jaxrpcmapping.JavaWsdlMapping;
import org.jboss.ws.metadata.jaxrpcmapping.JavaXmlTypeMapping;
import org.jboss.ws.metadata.jaxrpcmapping.MethodParamPartsMapping;
import org.jboss.ws.metadata.jaxrpcmapping.PackageMapping;
import org.jboss.ws.metadata.jaxrpcmapping.PortMapping;
import org.jboss.ws.metadata.jaxrpcmapping.ServiceEndpointInterfaceMapping;
import org.jboss.ws.metadata.jaxrpcmapping.ServiceEndpointMethodMapping;
import org.jboss.ws.metadata.jaxrpcmapping.ServiceInterfaceMapping;
import org.jboss.ws.metadata.jaxrpcmapping.VariableMapping;
import org.jboss.ws.metadata.jaxrpcmapping.WsdlMessageMapping;
import org.jboss.ws.metadata.jaxrpcmapping.WsdlReturnValueMapping;
import org.jboss.ws.metadata.wsdl.NCName;
import org.jboss.ws.metadata.wsdl.WSDLBinding;
import org.jboss.ws.metadata.wsdl.WSDLDefinitions;
import org.jboss.ws.metadata.wsdl.WSDLEndpoint;
import org.jboss.ws.metadata.wsdl.WSDLInterface;
import org.jboss.ws.metadata.wsdl.WSDLInterfaceFault;
import org.jboss.ws.metadata.wsdl.WSDLInterfaceMessageReference;
import org.jboss.ws.metadata.wsdl.WSDLInterfaceOperation;
import org.jboss.ws.metadata.wsdl.WSDLInterfaceOperationInput;
import org.jboss.ws.metadata.wsdl.WSDLInterfaceOperationOutput;
import org.jboss.ws.metadata.wsdl.WSDLProperty;
import org.jboss.ws.metadata.wsdl.WSDLRPCPart;
import org.jboss.ws.metadata.wsdl.WSDLService;
import org.jboss.ws.metadata.wsdl.WSDLTypes;
import org.jboss.ws.metadata.wsdl.WSDLUtils;
import org.jboss.ws.metadata.wsdl.xmlschema.JBossXSModel;
import org.jboss.ws.metadata.wsdl.xsd.SchemaUtils;
import org.jboss.ws.tools.RPCSignature;
import org.jboss.ws.tools.ToolsUtils;
import org.jboss.ws.tools.WSToolsConstants;
import org.jboss.ws.tools.mapping.MappingFileGenerator;
import org.w3c.dom.Element;

/**
 *  Helper class for MappingFileGenerator (only client of this class)
 *  @see MappingFileGenerator
 *  @author <mailto:Anil.Saldhana@jboss.org>Anil Saldhana
 *  @since  Sep 18, 2005
 */
public class MappingFileGeneratorHelper
{
   // provide logging
   private static final Logger log = Logger.getLogger(MappingFileGeneratorHelper.class);
   private WSDLDefinitions wsdlDefinitions = null;
   private String serviceName = null;
   private String packageName = null;
   private Set<String> registeredTypes = new HashSet<String>();

   private LiteralTypeMapping typeMapping = null;
   private String wsdlStyle;

   private WSDLUtils utils = WSDLUtils.getInstance();

   private String parameterStyle;

   public MappingFileGeneratorHelper(WSDLDefinitions wsdl, String sname, String pname, Class seiClass, String tns, LiteralTypeMapping ltm, String paramStyle)
   {
      this.wsdlDefinitions = wsdl;
      this.serviceName = sname;
      this.packageName = pname;
      this.typeMapping = ltm;

      this.wsdlStyle = utils.getWSDLStyle(wsdl);
      this.parameterStyle = paramStyle;
      checkEssentials();
   }

   public PackageMapping constructPackageMapping(JavaWsdlMapping jwm,
         String packageType, String ns)
   {
      PackageMapping pk = new PackageMapping(jwm);
      pk.setPackageType(packageType);
      pk.setNamespaceURI(ns);
      return pk;
   }


   public ServiceInterfaceMapping constructServiceInterfaceMapping(JavaWsdlMapping jwm, WSDLService ser)
   {
      serviceName = ser.getName().toString();
      String javaServiceName = serviceName;
      //Check if the serviceName conflicts with a portType or interface name
      if(wsdlDefinitions.getInterface(new NCName(serviceName)) != null)
         javaServiceName += "_Service";

      if (this.serviceName == null || serviceName.length() == 0)
         throw new IllegalArgumentException("MappingFileGenerator:Service Name is null");

      String targetNS = wsdlDefinitions.getTargetNamespace();
      String prefix = WSToolsConstants.WSTOOLS_CONSTANT_MAPPING_SERVICE_PREFIX;
      ServiceInterfaceMapping sim = new ServiceInterfaceMapping(jwm);
      sim.setServiceInterface(packageName + "." + javaServiceName);
      sim.setWsdlServiceName(new QName(targetNS, serviceName, prefix) );

      WSDLEndpoint[] endpoints = ser.getEndpoints();
      int lenendpoints = 0;
      if (endpoints != null)
         lenendpoints = endpoints.length;
      for (int j = 0; j < lenendpoints; j++)
      {
         WSDLEndpoint endpt = endpoints[j];
         String portname = endpt.getName().toString();
         //port mapping
         PortMapping pm = new PortMapping(sim);
         pm.setPortName(portname);
         pm.setJavaPortName(portname);
         sim.addPortMapping(pm);
      }
      return sim;
   }


   public void constructServiceEndpointInterfaceMapping(JavaWsdlMapping jwm, WSDLService ser)
   {
      serviceName = ser.getName().toString();
      if (this.serviceName == null || serviceName.length() == 0)
         throw new IllegalArgumentException("MappingFileGenerator:Service Name is null");

      String targetNS = wsdlDefinitions.getTargetNamespace();

      WSDLEndpoint[] endpoints = ser.getEndpoints();
      int lenendpoints = 0;
      if (endpoints != null)
         lenendpoints = endpoints.length;
      for (int j = 0; j < lenendpoints; j++)
      {
         WSDLEndpoint endpt = endpoints[j];
         QName binding = endpt.getBinding();
         WSDLBinding wsdlbind = wsdlDefinitions.getBinding(new NCName(binding.getLocalPart()));
         String bindName = wsdlbind.getName().toString();
         String portTypeName = wsdlbind.getInterfaceName().getLocalPart();
         WSDLInterface wsdlintf = wsdlDefinitions.getInterface(new NCName(portTypeName));
         String portName = wsdlintf.getName().toString();
         String javaPortName = utils.chopPortType(portName);
         if (wsdlDefinitions.getService(new NCName(javaPortName)) != null)
            javaPortName += "_PortType";

         ServiceEndpointInterfaceMapping seim = new ServiceEndpointInterfaceMapping(jwm);
         seim.setServiceEndpointInterface(packageName + "." + javaPortName);
         seim.setWsdlPortType(new QName(targetNS,portName,"portTypeNS"));
         seim.setWsdlBinding(new QName(targetNS,bindName,"bindingNS"));
         constructServiceEndpointMethodMapping(seim, wsdlintf);

         jwm.addServiceEndpointInterfaceMappings(seim);
      }
   }

   public void constructServiceEndpointMethodMapping(
         ServiceEndpointInterfaceMapping seim, WSDLInterface intf )
   {
      WSDLInterfaceOperation[] wioparr = intf.getOperations();
      int len = 0;
      if (wioparr != null)
         len = wioparr.length;
      for (int j = 0; j < len; j++)
      {
         WSDLInterfaceOperation wiop = wioparr[j];
         String opname = wiop.getName().toString();
         ServiceEndpointMethodMapping semm = new ServiceEndpointMethodMapping(seim);
         semm.setJavaMethodName(ToolsUtils.firstLetterLowerCase(opname));
         semm.setWsdlOperation(opname);
         semm.setWrappedElement(isWrapped());

         if (isDocStyle())
            constructDOCParameters(semm, wiop);
         else
            constructRPCParameters(semm, wiop);

         seim.addServiceEndpointMethodMapping(semm);
      }
   }

   private void constructDOCParameters(ServiceEndpointMethodMapping semm, WSDLInterfaceOperation wiop)
   {
      WSDLInterfaceOperationInput win = WSDLUtils.getWsdl11Input(wiop);
      MethodParamPartsMapping mpin = null;
      if (win != null)
      {
         QName xmlName = win.getElement();
         QName xmlType = win.getXMLType();
         String partName = win.getPartName();
         String wsdlMessageName = win.getMessageName().getLocalPart();

         if (isWrapped())
         {
            JBossXSModel schemaModel = WSDLUtils.getSchemaModel(wsdlDefinitions.getWsdlTypes());
            XSTypeDefinition xt = schemaModel.getTypeDefinition(xmlType.getLocalPart(),xmlType.getNamespaceURI());
            unwrapRequest(semm, wsdlMessageName, xt);
         }
         else
         {
            mpin = getMethodParamPartsMapping(semm,xmlName, xmlType, 0, wsdlMessageName, "IN", partName, false, true);
            semm.addMethodParamPartsMapping(mpin);
         }
      }

      WSDLInterfaceOperationOutput output = WSDLUtils.getWsdl11Output(wiop);
      if (output != null)
      {
         QName xmlType = output.getXMLType();

         QName messageName = output.getMessageName();
         if (isWrapped())
         {
            JBossXSModel schemaModel = WSDLUtils.getSchemaModel(wsdlDefinitions.getWsdlTypes());
            XSTypeDefinition xt = schemaModel.getTypeDefinition(xmlType.getLocalPart(),xmlType.getNamespaceURI());
            unwrapResponse(semm, messageName, xt);
         }
         else if (win.getElement() != null && win.getElement().equals(output.getElement()))
         {
            mpin.getWsdlMessageMapping().setParameterMode("INOUT");
         }
         else
         {
            WsdlReturnValueMapping wrvm = new WsdlReturnValueMapping(semm);
            wrvm.setMethodReturnValue(getJavaTypeAsString(output.getElement(), output.getXMLType(), false, true));
            wrvm.setWsdlMessage(new QName(messageName.getNamespaceURI(), messageName.getLocalPart(),  WSToolsConstants.WSTOOLS_CONSTANT_MAPPING_WSDL_MESSAGE_NS));
            wrvm.setWsdlMessagePartName(output.getPartName());
            semm.setWsdlReturnValueMapping(wrvm);
         }
      }
   }

   private String getMode(WSDLInterfaceOperation op, String name)
   {
      WSDLInterfaceOperationInput in = WSDLUtils.getWsdl11Input(op);
      WSDLInterfaceOperationOutput out = WSDLUtils.getWsdl11Output(op);

      boolean i = false ,o = false;
      if (in != null && in.getChildPart(name) != null)
         i = true;
      if (out != null && out.getChildPart(name) != null)
         o = true;

      if (i && o)
         return "INOUT";

      if (o)
         return "OUT";

      return "IN";
   }

   private void constructRPCParameters(ServiceEndpointMethodMapping semm, WSDLInterfaceOperation wiop)
   {
      WSDLInterfaceOperationInput win = WSDLUtils.getWsdl11Input(wiop);
      if (win == null)
         throw new WSException("RPC endpoints require an input message");
      String wsdlMessageName = win.getMessageName().getLocalPart();

      RPCSignature signature = new RPCSignature(wiop);
      int i = 0;
      for (WSDLRPCPart part : signature.parameters())
      {
         String partName = part.getName();
         QName xmlName = new QName(partName);
         QName xmlType = part.getType();

         MethodParamPartsMapping mpin = getMethodParamPartsMapping(semm,xmlName, xmlType,
               i++, wsdlMessageName, getMode(wiop, part.getName()), partName, false, true);

         semm.addMethodParamPartsMapping(mpin);
      }

      WSDLRPCPart returnParameter = signature.returnParameter();
      if (returnParameter != null)
      {
         String partName = returnParameter.getName();
         QName xmlName = new QName(partName);
         QName xmlType = returnParameter.getType();

         WsdlReturnValueMapping wrvm = new WsdlReturnValueMapping(semm);
         wrvm.setMethodReturnValue(getJavaTypeAsString(xmlName, xmlType, false, true));
         QName messageName = WSDLUtils.getWsdl11Output(wiop).getMessageName();
         wrvm.setWsdlMessage(new QName(messageName.getNamespaceURI(), messageName.getLocalPart(),  WSToolsConstants.WSTOOLS_CONSTANT_MAPPING_WSDL_MESSAGE_NS));
         wrvm.setWsdlMessagePartName(partName);
         semm.setWsdlReturnValueMapping(wrvm);
      }
   }

   public void constructJavaXmlTypeMapping(JavaWsdlMapping jwm)
   {
      WSDLInterface[] intfArr = wsdlDefinitions.getInterfaces();
      int len = intfArr != null ? intfArr.length : 0;
      for(int i = 0 ; i < len ; i++)
      {
         WSDLInterface wi = intfArr[i];
         WSDLInterfaceOperation[] ops = wi.getOperations();
         int lenOps = ops.length;
         for (int j = 0; j < lenOps; j++)
         {
            WSDLInterfaceOperation op  = ops[j];
            for (WSDLInterfaceOperationInput input : op.getInputs())
            {
               if (isDocStyle())
               {
                  XSTypeDefinition xt = getXSType( input );
                  addJavaXMLTypeMap(xt, input.getElement().getLocalPart(), "", jwm, false);
               }
               else
               {
                  for (WSDLRPCPart part : input.getChildParts())
                     addJavaXMLTypeMap(getXSType(part.getType()), "", "", jwm, true);
               }
            }

            for (WSDLInterfaceOperationOutput output : op.getOutputs())
            {
               if (isDocStyle())
               {
                  XSTypeDefinition xt = getXSType( output );
                  addJavaXMLTypeMap(xt, output.getElement().getLocalPart(), "", jwm, false);
               }
               else
               {
                  for (WSDLRPCPart part : output.getChildParts())
                     addJavaXMLTypeMap(getXSType(part.getType()), "", "", jwm, true);
               }
            }

            for (WSDLInterfaceFault fault : wi.getFaults())
            {
               QName xmlType = fault.getXmlType();
               QName xmlName = fault.getElement();

               WSDLTypes types = wsdlDefinitions.getWsdlTypes();
               JBossXSModel xsmodel = WSDLUtils.getSchemaModel(types);
               XSTypeDefinition xt = xsmodel.getTypeDefinition(xmlType.getLocalPart(),xmlType.getNamespaceURI());
               addJavaXMLTypeMap(xt, xmlName.getLocalPart(), "", jwm, true);

               ExceptionMapping exceptionMapping = new ExceptionMapping(jwm);
               exceptionMapping.setExceptionType(getJavaTypeAsString(null, xmlType, false, true));
               exceptionMapping.setWsdlMessage(new QName(wsdlDefinitions.getTargetNamespace(), fault.getName().toString()));
               jwm.addExceptionMappings(exceptionMapping);
            }
         }//end for
      }
   }

   private void unwrapRequest(ServiceEndpointMethodMapping methodMapping, String messageName, XSTypeDefinition xt)
   {
      if (xt instanceof XSComplexTypeDefinition == false)
         throw new WSException("Tried to unwrap a non-complex type.");

      XSComplexTypeDefinition wrapper = (XSComplexTypeDefinition)xt;
      XSParticle particle = wrapper.getParticle();
      XSTerm term = particle.getTerm();
      if (term instanceof XSModelGroup == false)
         throw new WSException("Expected model group, could not unwrap");
      unwrapRequestParticles(methodMapping, messageName, (XSModelGroup)term);
   }

   private int unwrapRequestParticles(ServiceEndpointMethodMapping methodMapping, String messageName, XSModelGroup group)
   {
      if (group.getCompositor() != XSModelGroup.COMPOSITOR_SEQUENCE)
         throw new WSException("Only a sequence type can be unwrapped.");

      int elementCount = 0;
      XSObjectList particles = group.getParticles();
      for (int i = 0; i < particles.getLength(); i++)
      {
         XSParticle particle = (XSParticle) particles.item(i);
         XSTerm term = particle.getTerm();
         if (term instanceof XSModelGroup)
         {
            elementCount += unwrapRequestParticles(methodMapping, messageName, (XSModelGroup)term);
         }
         else if (term instanceof XSElementDeclaration)
         {
            XSElementDeclaration element = (XSElementDeclaration)term;
            QName xmlName = new QName(element.getNamespace(), element.getName());
            QName xmlType = new QName(element.getTypeDefinition().getNamespace(), element.getTypeDefinition().getName());
            boolean array = particle.getMaxOccursUnbounded() || particle.getMaxOccurs() > 1;
            MethodParamPartsMapping parts = getMethodParamPartsMapping(methodMapping, xmlName, xmlType, elementCount, messageName, "IN", xmlName.getLocalPart(), array,
                  !element.getNillable());
            methodMapping.addMethodParamPartsMapping(parts);
            elementCount++;
         }
      }

      return elementCount;
   }

   private void unwrapResponse(ServiceEndpointMethodMapping methodMapping, QName messageName, XSTypeDefinition xt)
   {
      if (xt instanceof XSComplexTypeDefinition == false)
         throw new WSException("Tried to unwrap a non-complex type.");

      XSComplexTypeDefinition wrapper = (XSComplexTypeDefinition)xt;
      XSParticle particle = wrapper.getParticle();
      XSTerm term = particle.getTerm();
      if (term instanceof XSModelGroup == false)
         throw new WSException("Expected model group, could not unwrap");
      unwrapResponseParticles(methodMapping, messageName, (XSModelGroup)term);
   }

   private boolean unwrapResponseParticles(ServiceEndpointMethodMapping methodMapping, QName messageName, XSModelGroup group)
   {
      if (group.getCompositor() != XSModelGroup.COMPOSITOR_SEQUENCE)
         throw new WSException("Only a sequence type can be unwrapped.");

      XSObjectList particles = group.getParticles();
      String returnType = null;
      for (int i = 0; i < particles.getLength(); i++)
      {
         XSParticle particle = (XSParticle) particles.item(i);
         XSTerm term = particle.getTerm();
         if (term instanceof XSModelGroup)
         {
            if (unwrapResponseParticles(methodMapping, messageName, (XSModelGroup)term))
               return true;
         }
         else if (term instanceof XSElementDeclaration)
         {
            XSElementDeclaration element = (XSElementDeclaration)term;
            QName xmlName = new QName(element.getNamespace(), element.getName());
            QName xmlType = new QName(element.getTypeDefinition().getNamespace(), element.getTypeDefinition().getName());
            boolean array = particle.getMaxOccursUnbounded() || particle.getMaxOccurs() > 1;
            StringBuilder buf = new StringBuilder();

            String javaType = getJavaTypeAsString(xmlName, xmlType, array, !element.getNillable());

            WsdlReturnValueMapping wrvm = new WsdlReturnValueMapping(methodMapping);
            wrvm.setMethodReturnValue(javaType);
            wrvm.setWsdlMessage(new QName(messageName.getNamespaceURI(), messageName.getLocalPart(), WSToolsConstants.WSTOOLS_CONSTANT_MAPPING_WSDL_MESSAGE_NS));
            wrvm.setWsdlMessagePartName(xmlName.getLocalPart());
            methodMapping.setWsdlReturnValueMapping(wrvm);

            return true;
         }
      }

      return false;
   }



   private void checkEssentials()
   {
      if(typeMapping == null)
         throw new WSException("typeMapping is null");
   }

   private XSTypeDefinition getXSType(WSDLInterfaceMessageReference part)
   {
      //Check if there are any custom properties
      WSDLInterfaceOperation op  = part.getWsdlOperation();
      String zeroarg1 = null;
      String zeroarg2 = null;
      WSDLProperty prop1 = op.getProperty(Constants.WSDL_PROPERTY_ZERO_ARGS);
      if (prop1 != null)
         zeroarg1 = prop1.getValue();
      if(zeroarg1 != null && zeroarg2 != null && zeroarg1.equals(zeroarg2) == false)
         return null;
      if (zeroarg1 != null && "true".equals(zeroarg1))
         return null;

      QName xmlType = part.getXMLType();

      WSDLTypes types = wsdlDefinitions.getWsdlTypes();
      JBossXSModel xsmodel = WSDLUtils.getSchemaModel(types);
      return xsmodel.getTypeDefinition(xmlType.getLocalPart(),xmlType.getNamespaceURI());
   }

   private XSTypeDefinition getXSType(QName xmlType)
   {
      WSDLTypes types = wsdlDefinitions.getWsdlTypes();
      JBossXSModel xsmodel = WSDLUtils.getSchemaModel(types);
      return xsmodel.getTypeDefinition(xmlType.getLocalPart(),xmlType.getNamespaceURI());
   }

   private void addJavaXMLTypeMap(XSTypeDefinition xt,String name, String containingElement, JavaWsdlMapping jwm, boolean skipWrapperArray)
   {
      JavaXmlTypeMapping jxtm = null;

      if(xt instanceof XSComplexTypeDefinition)
      {

         XSModelGroup xm = null;
         XSComplexTypeDefinition xc = (XSComplexTypeDefinition)xt;
         if(xc.getContentType() != XSComplexTypeDefinition.CONTENTTYPE_EMPTY)
         {
            XSParticle xp = xc.getParticle();
            if (xp != null)
            {
               XSTerm xterm = xp.getTerm();
               if(xterm instanceof XSModelGroup)
                  xm = (XSModelGroup)xterm;
            }
         }

         if ((skipWrapperArray && isRepresentsArray(xt)) == false)
         {
            jxtm = new JavaXmlTypeMapping(jwm);
            String javaType;
            String localName = xt.getName();

            // Anonymous
            if (localName == null)
            {
               javaType = getJavaTypeAsString(null, new QName(containingElement + name), false, true);
               localName = ">" + name;
               jxtm.setAnonymousTypeQName(new QName(xt.getNamespace(), localName, "typeNS"));
            }
            else
            {
               javaType = getJavaTypeAsString(null, new QName(localName), false, true);
               jxtm.setRootTypeQName(new QName(xt.getNamespace(), xt.getName(), "typeNS"));
            }

            if (registeredTypes.contains(javaType))
               return;

            jxtm.setJavaType(javaType);
            jxtm.setQNameScope("complexType");

            registeredTypes.add(javaType);
            jwm.addJavaXmlTypeMappings(jxtm);
            // addJavaXMLTypeMapping(jwm, jxtm

            if (xm != null)
            {
               addVariableMappingMap(xm, jxtm, javaType);
            }

            // Add simple content if it exists
            XSSimpleTypeDefinition simple = xc.getSimpleType();
            if (simple != null)
            {
               addJavaXMLTypeMap(simple, xc.getName(), "", jwm, skipWrapperArray);
            }

            // Add attributes
            XSObjectList attributeUses = ((XSComplexTypeDefinition)xc).getAttributeUses();
            if (attributeUses != null)
               addAttributeMappings(attributeUses, jxtm);
         }

         if (xm != null)
            addGroup(xm, jwm);
      }

      // Add enum simpleType support
   }


   private void addVariableMappingMap(XSModelGroup xm, JavaXmlTypeMapping jxtm, String javaType)
   {
      XSObjectList xo = xm.getParticles();
      int len = xo != null ? xo.getLength() : 0;
      for (int i = 0; i < len; i++)
      {
         XSTerm xsterm = ((XSParticle) xo.item(i)).getTerm();
         if (xsterm instanceof XSModelGroup)
            addVariableMappingMap((XSModelGroup) xsterm, jxtm, javaType);
         else if (xsterm instanceof XSElementDeclaration)
         {
            XSElementDeclaration xe = (XSElementDeclaration) xsterm;
            VariableMapping vm = new VariableMapping(jxtm);
            String name = xe.getName();
            // JBWS-1170 Convert characters which are illegal in Java identifiers
            vm.setJavaVariableName(ToolsUtils.convertInvalidCharacters(Introspector.decapitalize(name)));
            vm.setXmlElementName(name);
            jxtm.addVariableMapping(vm);
         }
      }
   }

   private void addAttributeMappings(XSObjectList attributes, JavaXmlTypeMapping jxtm)
   {
      for (int i = 0; i < attributes.getLength(); i++)
      {
         XSAttributeUse obj = (XSAttributeUse)attributes.item(i);
         XSAttributeDeclaration att = obj.getAttrDeclaration();
         XSSimpleTypeDefinition simple = att.getTypeDefinition();
         addJavaXMLTypeMap(simple, "none", "", jxtm.getJavaWsdlMapping(), true);
         VariableMapping vm = new VariableMapping(jxtm);
         String name = att.getName();
         vm.setXmlAttributeName(name);
         // JBWS-1170 Convert characters which are illegal in Java identifiers
         vm.setJavaVariableName(ToolsUtils.convertInvalidCharacters(Introspector.decapitalize(name)));
         jxtm.addVariableMapping(vm);
      }
   }

   private void addGroup(XSModelGroup xm, JavaWsdlMapping jwm)
   {
      XSObjectList xo = xm.getParticles();
      int len = xo != null ? xo.getLength() : 0;
      for(int i = 0; i < len ; i++)
      {
         XSTerm xsterm = ((XSParticle)xo.item(i)).getTerm();
         if(xsterm instanceof XSModelGroup)
         {
            addGroup((XSModelGroup)xsterm, jwm);
         }
         else if(xsterm instanceof XSElementDeclaration)
         {
            XSElementDeclaration xe = (XSElementDeclaration)xsterm;
            XSTypeDefinition typeDefinition = xe.getTypeDefinition();
            addJavaXMLTypeMap(typeDefinition, xe.getName(), xe.getEnclosingCTDefinition().getName(), jwm, true);
         }
      }
   }

   private String getJavaTypeAsString(QName xmlName, QName xmlType, boolean array, boolean primitive)
   {
      String jtype = null;

      String arraySuffix = (array) ? "[]" : "";
      if (! isDocStyle())
      {
         JBossXSModel xsmodel = WSDLUtils.getSchemaModel(wsdlDefinitions.getWsdlTypes());
         XSTypeDefinition xt = xsmodel.getTypeDefinition(xmlType.getLocalPart(),xmlType.getNamespaceURI());

         XSElementDeclaration unwrapped = SchemaUtils.unwrapArrayType(xt);
         StringBuilder builder = new StringBuilder();

         while (unwrapped != null)
         {
            xt = unwrapped.getTypeDefinition();
            primitive = !unwrapped.getNillable();
            builder.append("[]");
            unwrapped = SchemaUtils.unwrapArrayType(xt);
         }
         if (builder.length() > 0)
         {
            xmlType = new QName(xt.getNamespace(), xt.getName());
            arraySuffix = builder.toString();
         }
      }

      //First try to get it from the typeMapping
      Class javaType = typeMapping.getJavaType(xmlType, primitive);
      /**
       * Special case - when qname=xsd:anyType && javaType == Element
       * then cls has to be javax.xml.soap.SOAPElement
       */
      if( xmlType.getNamespaceURI().equals(Constants.NS_SCHEMA_XSD)
            && "anyType".equals(xmlType.getLocalPart()) && javaType == Element.class)
         javaType = SOAPElement.class;
      javaType = this.makeCustomDecisions(javaType,xmlName,xmlType);

      if(javaType == null)
      {
         log.debug("Typemapping lookup failed for "+xmlName);
         log.debug("Falling back to identifier generation");
         String className = xmlType.getLocalPart();
         if (className.charAt(0) == '>')
            className = className.substring(1);
         jtype = packageName + "." + utils.firstLetterUpperCase(className);
      }
      else
      {
         //Handle arrays
         if(javaType.isArray())
         {
            jtype = JavaUtils.getSourceName(javaType);
         }
         else
            jtype = javaType.getName();
      }

      return jtype + arraySuffix;
   }

   private boolean isDocStyle()
   {
      return Constants.DOCUMENT_LITERAL.equals(wsdlStyle);
   }

   /**
    * Checks whether the type represents an array type
    *
    * @param xst
    * @return true: type represents an array
    */
   private boolean isRepresentsArray( XSTypeDefinition xst)
   {
      boolean bool = false;
      if( xst instanceof XSComplexTypeDefinition)
      {
         XSComplexTypeDefinition xc = (XSComplexTypeDefinition)xst;
         if(xc.getContentType() == XSComplexTypeDefinition.CONTENTTYPE_EMPTY)
            return false;
         XSParticle xsp = xc.getParticle();

         if (xsp == null)
            return false;

         XSTerm xsterm = xsp.getTerm();
         if(xsterm instanceof XSModelGroup)
         {
            XSModelGroup xm = (XSModelGroup)xsterm;
            XSObjectList xo = xm.getParticles();
            if(xo.getLength() == 1)
            {
               XSParticle xp = (XSParticle)xo.item(0);
               bool = xp.getMaxOccursUnbounded() || xp.getMaxOccurs() > 1;
            }
         }
      }
      return bool;
   }

   /**
    * Any custom decisions that need to be made will be done here
    *
    * @param javaType
    * @param xmlName
    * @param xmlType
    */
   private Class makeCustomDecisions( Class javaType, QName xmlName, QName xmlType)
   {
      if(javaType != null && xmlType != null)
      {
         if(Byte[].class == javaType && Constants.NS_SCHEMA_XSD.equals(xmlType.getNamespaceURI())
               && "base64Binary".equals(xmlType.getLocalPart()))
            javaType = byte[].class;
      }
      return javaType;
   }

   private boolean isWrapped()
   {
      return "wrapped".equals(parameterStyle) && Constants.DOCUMENT_LITERAL.equals(wsdlStyle);
   }

   private MethodParamPartsMapping getMethodParamPartsMapping(ServiceEndpointMethodMapping semm,
         QName xmlName, QName xmlType,
         int paramPosition, String wsdlMessageName, String paramMode, String wsdlMessagePartName, boolean array, boolean primitive)
   {
      String targetNS = wsdlDefinitions.getTargetNamespace();
      MethodParamPartsMapping mppm = new MethodParamPartsMapping(semm);
      mppm.setParamPosition(paramPosition);
      String javaType = getJavaTypeAsString(xmlName, xmlType, array, primitive);
      mppm.setParamType(javaType);

      //WSDL Message Mapping
      WsdlMessageMapping wmm = new WsdlMessageMapping(mppm);
      wmm.setParameterMode(paramMode);
      String wsdlNS = WSToolsConstants.WSTOOLS_CONSTANT_MAPPING_WSDL_MESSAGE_NS;
      wmm.setWsdlMessage(new QName(targetNS,wsdlMessageName, wsdlNS));
      wmm.setWsdlMessagePartName( wsdlMessagePartName );
      mppm.setWsdlMessageMapping(wmm);
      return mppm;
   }
}
