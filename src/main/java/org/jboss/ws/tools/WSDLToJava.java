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
package org.jboss.ws.tools;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPElement;

import org.apache.xerces.xs.XSComplexTypeDefinition;
import org.apache.xerces.xs.XSElementDeclaration;
import org.apache.xerces.xs.XSModelGroup;
import org.apache.xerces.xs.XSObjectList;
import org.apache.xerces.xs.XSParticle;
import org.apache.xerces.xs.XSSimpleTypeDefinition;
import org.apache.xerces.xs.XSTerm;
import org.apache.xerces.xs.XSTypeDefinition;
import org.jboss.ws.Constants;
import org.jboss.ws.WSException;
import org.jboss.ws.core.jaxrpc.LiteralTypeMapping;
import org.jboss.ws.core.utils.JavaUtils;
import org.jboss.ws.metadata.wsdl.NCName;
import org.jboss.ws.metadata.wsdl.WSDLDefinitions;
import org.jboss.ws.metadata.wsdl.WSDLException;
import org.jboss.ws.metadata.wsdl.WSDLInterface;
import org.jboss.ws.metadata.wsdl.WSDLInterfaceFault;
import org.jboss.ws.metadata.wsdl.WSDLInterfaceOperation;
import org.jboss.ws.metadata.wsdl.WSDLInterfaceOperationInput;
import org.jboss.ws.metadata.wsdl.WSDLInterfaceOperationOutfault;
import org.jboss.ws.metadata.wsdl.WSDLInterfaceOperationOutput;
import org.jboss.ws.metadata.wsdl.WSDLRPCPart;
import org.jboss.ws.metadata.wsdl.WSDLUtils;
import org.jboss.ws.metadata.wsdl.xmlschema.JBossXSModel;
import org.jboss.ws.metadata.wsdl.xsd.SchemaUtils;
import org.jboss.ws.tools.interfaces.WSDLToJavaIntf;
import org.jboss.ws.tools.wsdl.WSDLDefinitionsFactory;
import org.w3c.dom.Element;

/**
 * Class that acts as the front door to all wsdl2java needs<br>
 *
 * <br>Note: (Web Services Layer)<br>
 * Method to generate Java SEI is as follows<br>
 * <br>{@link #generateSEI(URL wsdlFile, File dir, boolean annotate)  generateSEI}
 * <br>
 * <br> Please also have a look at the features that can be passed via {@link #setFeature(String name, boolean value) setFeature}
 * <br>
 * <br>Features are:
 * <br>@see org.jboss.ws.Constants.USE_ANNOTATIONS : Should the generated Java Types use annotations
 * @author <mailto:Anil.Saldhana@jboss.org>Anil Saldhana
 * @since Dec 28, 2004
 */
public class WSDLToJava implements WSDLToJavaIntf
{
   private String newline = "\n";

   //protected LiteralTypeMapping typeMapping = new LiteralTypeMapping();
   protected LiteralTypeMapping typeMapping = null;

   protected WSDLDefinitions wsdl = null;

   /**
    * Singleton class that handle many utility functions
    */
   protected WSDLUtils utils = WSDLUtils.getInstance();

   //protected XSDToJavaIntf xsdJava = new XSDToJava();

   //Feature Set
   protected boolean annotate = false;

   protected Map<String,String> namespacePackageMap = null;

   //private String wsdlStyle = Constants.RPC_LITERAL;

   private String seiPkgName = "";

   private String directoryToGenerate = "";

   private String style;
   private String parameterStyle;

   public WSDLToJava()
   {
   }

   /* (non-Javadoc)
    * @see org.jboss.ws.tools.WSDLToJavaIntf#convertWSDL2Java(java.net.URL)
    */
   public WSDLDefinitions convertWSDL2Java(URL wsdlfileurl) throws WSDLException
   {
      checkTypeMapping();
      WSDLDefinitionsFactory wsdlFactory = WSDLDefinitionsFactory.newInstance();
      wsdl = wsdlFactory.parse(wsdlfileurl);

      return wsdl;
   }

   /* (non-Javadoc)
    * @see org.jboss.ws.tools.WSDLToJavaIntf#getFeature(java.lang.String)
    */
   public boolean getFeature( String name)
   {
      if(name == null)
         throw new IllegalArgumentException("Illegal null argument:name");

      if(name.equalsIgnoreCase(WSToolsConstants.WSTOOLS_FEATURE_USE_ANNOTATIONS))
          return annotate ;

      throw new WSException("Feature:"+ name + " not recognized");
   }

   /* (non-Javadoc)
    * @see org.jboss.ws.tools.WSDLToJavaIntf#setFeature(java.lang.String, boolean)
    */
   public void setFeature( String name, boolean value)
   {
      if(name == null)
         throw new IllegalArgumentException("Illegal null argument:name");

      if(name.equalsIgnoreCase(WSToolsConstants.WSTOOLS_FEATURE_USE_ANNOTATIONS))
          annotate = value;
   }

   /* (non-Javadoc)
    * @see org.jboss.ws.tools.WSDLToJavaIntf#generateSEI(java.net.URL, java.io.File, boolean)
    */
   public void generateSEI( URL wsdlFile, File dir, boolean annotate) throws IOException
   {
      checkTypeMapping();
      WSDLDefinitions wsdl = convertWSDL2Java(  wsdlFile);
      this.annotate = annotate;
      this.directoryToGenerate = dir.getAbsolutePath();
      generateSEI(  wsdl,   dir);
   }

   /* (non-Javadoc)
    * @see org.jboss.ws.tools.WSDLToJavaIntf#generateSEI(org.jboss.ws.metadata.wsdl.WSDLDefinitions, java.io.File)
    */
   public void generateSEI(WSDLDefinitions wsdl, File dir) throws IOException
   {
      checkTypeMapping();
      this.directoryToGenerate = dir.getAbsolutePath();
      this.wsdl = wsdl;
      style = utils.getWSDLStyle(wsdl);


      //TODO: Handle annotations flag, as per JAX-WS 2.0 Spec.
      //Given the WSDL Object Tree, generate the SEI
      //Also take in the location where the SEI should be written
     // String typeNS = wsdl.getNamespaceURI(WSDLConstants.PREFIX_TNS);
      String targetNS = wsdl.getTargetNamespace();
      //Check if there is an user override
      String packageName = namespacePackageMap != null?namespacePackageMap.get(targetNS):null;
      if(packageName == null || packageName.length() == 0)
         packageName = NamespacePackageMapping.getJavaPackageName(targetNS);

      this.seiPkgName = packageName;

      File dirloc = utils.createPackage(dir.getAbsolutePath(), packageName);
      createSEI(dirloc, wsdl);
      //xsdJava.generateJavaSource(wsdl.getTypes().getSchemaModel(), dir, packageName, true);
   }


   public Map<String, String> getNamespacePackageMap()
   {
      return namespacePackageMap;
   }

   /* (non-Javadoc)
    * @see org.jboss.ws.tools.WSDLToJavaIntf#setPackageNamespaceMap(java.util.Map)
    */
   public void setNamespacePackageMap(Map<String, String> map)
   {
     //Lets convert the package->namespace map to namespace->package map
     Set keys = map.keySet();
     Iterator<String> iter = keys.iterator();
     while(iter != null && iter.hasNext())
     {
        if(namespacePackageMap == null) namespacePackageMap = new HashMap<String,String>();
        String pkg = iter.next();
        namespacePackageMap.put(map.get(pkg),pkg);
     }
   }

   public void setTypeMapping(LiteralTypeMapping tm)
   {
      this.typeMapping = tm;
   }

   private class WrappedArray
   {
      public QName xmlType;
      public XSTypeDefinition xt;
      public String suffix;
      public boolean nillable;

      public WrappedArray(XSTypeDefinition xt)
      {
         this.xt = xt;
      }

      public boolean unwrap()
      {
         if (! Constants.DOCUMENT_LITERAL.equals(style))
         {
            XSElementDeclaration unwrapped = SchemaUtils.unwrapArrayType(xt);
            StringBuilder builder = new StringBuilder();
            while (unwrapped != null)
            {
               xt = unwrapped.getTypeDefinition();
               nillable = unwrapped.getNillable();
               builder.append("[]");
               unwrapped = SchemaUtils.unwrapArrayType(xt);
            }
            if (builder.length() > 0)
            {
               xmlType = new QName(xt.getNamespace(), xt.getName());
               suffix = builder.toString();
               return true;
            }
         }

         return false;
      }
   }


   //***************************************************************************
   //                             PRIVATE METHODS
   //***************************************************************************

   private boolean isDocument()
   {
      return Constants.DOCUMENT_LITERAL.equals(style);
   }
   
   private boolean isWrapped()
   {
      return "wrapped".equals(parameterStyle) && Constants.DOCUMENT_LITERAL.equals(style);
   }

   private void unwrapRequest(StringBuilder buf, XSTypeDefinition xt) throws IOException
   {
      if (xt instanceof XSComplexTypeDefinition == false)
         throw new WSException("Tried to unwrap a non-complex type.");

      XSComplexTypeDefinition wrapper = (XSComplexTypeDefinition)xt;
      XSParticle particle = wrapper.getParticle();
      if (particle == null)
         throw new WSException("Attempt to unwrap a request type with no particles");
      XSTerm term = particle.getTerm();
      if (term instanceof XSModelGroup == false)
         throw new WSException("Expected model group, could not unwrap");
      unwrapRequestParticles(buf, (XSModelGroup)term);

      // We need a wrapper class generated
      generateJavaSource(wrapper, WSDLUtils.getSchemaModel(wsdl.getWsdlTypes()), wrapper.getName());
   }

   private int unwrapRequestParticles(StringBuilder buf, XSModelGroup group) throws IOException
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
            elementCount += unwrapRequestParticles(buf, (XSModelGroup)term);
         }
         else if (term instanceof XSElementDeclaration)
         {
            if (elementCount++ > 0)
               buf.append(", ");
            XSElementDeclaration element = (XSElementDeclaration)term;
            QName xmlName = new QName(element.getNamespace(), element.getName());
            QName xmlType = new QName(element.getTypeDefinition().getNamespace(), element.getTypeDefinition().getName());
            JBossXSModel xsmodel = WSDLUtils.getSchemaModel(wsdl.getWsdlTypes());
            boolean array = particle.getMaxOccursUnbounded() || particle.getMaxOccurs() > 1;
            generateParameter(buf, xmlName, xmlType, xsmodel, element.getTypeDefinition(), array, !element.getNillable(), false);
            buf.append(" ").append(getMethodParam(xmlName));
         }
      }

      return elementCount;
   }

   private String unwrapResponse(XSTypeDefinition xt) throws IOException
   {
      if (xt instanceof XSComplexTypeDefinition == false)
         throw new WSException("Tried to unwrap a non-complex type.");

      XSComplexTypeDefinition wrapper = (XSComplexTypeDefinition)xt;
      XSParticle particle = wrapper.getParticle();
      if (particle == null)
         throw new WSException("Attempt to unwrap a response type with no particles");
      XSTerm term = particle.getTerm();
      if (term instanceof XSModelGroup == false)
         throw new WSException("Expected model group, could not unwrap");
      String returnType = unwrapResponseParticles((XSModelGroup)term);
      // We need a wrapper class generated
      generateJavaSource(wrapper, WSDLUtils.getSchemaModel(wsdl.getWsdlTypes()), wrapper.getName());
      return returnType;
   }

   private String unwrapResponseParticles(XSModelGroup group) throws IOException
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
            returnType = unwrapResponseParticles((XSModelGroup)term);
            if (returnType != null)
               return returnType;
         }
         else if (term instanceof XSElementDeclaration)
         {
            XSElementDeclaration element = (XSElementDeclaration)term;
            QName xmlName = new QName(element.getNamespace(), element.getName());
            QName xmlType = new QName(element.getTypeDefinition().getNamespace(), element.getTypeDefinition().getName());
            JBossXSModel xsmodel = WSDLUtils.getSchemaModel(wsdl.getWsdlTypes());
            boolean array = particle.getMaxOccursUnbounded() || particle.getMaxOccurs() > 1;
            StringBuilder buf = new StringBuilder();
            generateParameter(buf, xmlName, xmlType, xsmodel, element.getTypeDefinition(), array, !element.getNillable(), false);
            return buf.toString();
         }
      }

      return null;
   }

   private void appendMethods(WSDLInterface intf, StringBuilder buf) throws IOException
   {
      buf.append(newline);
      String itfname = intf.getName().toString();
      WSDLInterfaceOperation[] ops = intf.getOperations();
      if (ops == null || ops.length == 0)
         throw new IllegalArgumentException("Interface " + itfname + " doesn't have operations");
      int len = ops != null ? ops.length : 0;


      // FIXME - Add support for headers
      for (int i = 0; i < len; i++)
      {
         WSDLInterfaceOperation op = ops[i];
         //TODO: Take care of multiple outputs
         String returnType = null;

         StringBuilder paramBuffer = new StringBuilder();

         WSDLInterfaceOperationInput input = WSDLUtils.getWsdl11Input(op);
         WSDLInterfaceOperationOutput output = WSDLUtils.getWsdl11Output(op);
         if (isDocument())
         {
            returnType = appendDocParameters(paramBuffer, input, output);
         }
         else
         {
            returnType = appendRpcParameters(paramBuffer, op, output);
         }

         if (returnType == null)
            returnType = "void";

         buf.append("  public " + returnType + "  ");
         buf.append(ToolsUtils.firstLetterLowerCase(op.getName().toString()) );
         buf.append("(").append(paramBuffer);

         buf.append(") throws ");
         //Generate the Exception Types
         WSDLInterfaceOperationOutfault[] outfaults = op.getOutfaults();
         for (int k = 0; k < outfaults.length; k++)
         {
            WSDLInterfaceOperationOutfault fault = outfaults[k];
            QName faultqname = fault.getRef();

            //Get the main fault from the wsdlInterface
            WSDLInterfaceFault intfFault = fault.getWsdlInterfaceOperation().getWsdlInterface().getFault(new NCName(faultqname.getLocalPart()));
            JBossXSModel xsmodel = WSDLUtils.getSchemaModel(wsdl.getWsdlTypes());
            QName faultXMLName = intfFault.getElement();
            QName faultXMLType = intfFault.getXmlType();

            XSElementDeclaration xe = xsmodel.getElementDeclaration(faultXMLName.getLocalPart(),faultXMLName.getNamespaceURI());
            XSTypeDefinition xt =  xe.getTypeDefinition();
            if (! xt.getAnonymous())
               xt = xsmodel.getTypeDefinition(xt.getName(), xt.getNamespace());
            if(xt instanceof XSComplexTypeDefinition)
               generateJavaSource((XSComplexTypeDefinition)xt,xsmodel, faultXMLName.getLocalPart(), true);

            Class cl = getJavaType(faultXMLType, false);
            if (cl == null)
            {
               String faultTypeName = (!xt.getAnonymous()) ? faultXMLType.getLocalPart() : faultXMLName.getLocalPart();
               buf.append(seiPkgName + "." + cleanUpFaultName(faultTypeName));
            }
            else
               buf.append( cl.getName());
            buf.append( "," );
         }
         buf.append(" java.rmi.RemoteException");
         buf.append(";");
         buf.append(newline);
      }
   }

   private String appendRpcParameters(StringBuilder paramBuffer, WSDLInterfaceOperation op, WSDLInterfaceOperationOutput output) throws IOException
   {
      String returnType = null;
      boolean first = true;

      RPCSignature signature = new RPCSignature(op);
      for (WSDLRPCPart part : signature.parameters())
      {

         if (first)
            first = false;
         else
            paramBuffer.append(", ");

         QName xmlName = new QName(part.getName());
         QName xmlType = part.getType();
         JBossXSModel xsmodel = WSDLUtils.getSchemaModel(wsdl.getWsdlTypes());
         XSTypeDefinition xt = xsmodel.getTypeDefinition(xmlType.getLocalPart(),xmlType.getNamespaceURI());

         boolean holder = output != null && output.getChildPart(part.getName()) != null;
         generateParameter(paramBuffer, xmlName, xmlType, xsmodel, xt, false, true, holder);
         paramBuffer.append(" ").append(getMethodParam(xmlName));
      }

      if (signature.returnParameter() != null)
      {
         QName xmlName = new QName(signature.returnParameter().getName());
         QName xmlType = signature.returnParameter().getType();
         JBossXSModel xsmodel = WSDLUtils.getSchemaModel(wsdl.getWsdlTypes());
         XSTypeDefinition xt = xsmodel.getTypeDefinition(xmlType.getLocalPart(),xmlType.getNamespaceURI());
         returnType = getReturnType(xmlName, xmlType, xt);
      }

      return returnType;
   }

   private String appendDocParameters(StringBuilder paramBuffer, WSDLInterfaceOperationInput input, WSDLInterfaceOperationOutput output) throws IOException
   {
      String returnType = null;
      boolean holder = false;
      if (input != null && input.getElement() != null)
      {
         QName xmlName = input.getElement();
         QName xmlType = input.getXMLType();
         JBossXSModel xsmodel = WSDLUtils.getSchemaModel(wsdl.getWsdlTypes());
         XSTypeDefinition xt = xsmodel.getTypeDefinition(xmlType.getLocalPart(),xmlType.getNamespaceURI());

         if (isWrapped())
         {
            unwrapRequest(paramBuffer, xt);
         }
         else
         {
            holder = output != null && xmlName.equals(output.getElement());
            generateParameter(paramBuffer, xmlName, xmlType, xsmodel, xt, false, true, holder);
            paramBuffer.append(" ").append(getMethodParam(xmlName));
         }
      }
      if (!holder && output != null && output.getElement() != null)
      {
         QName xmlName = output.getElement();
         QName xmlType = output.getXMLType();
         JBossXSModel xsmodel = WSDLUtils.getSchemaModel(wsdl.getWsdlTypes());
         XSTypeDefinition xt = xsmodel.getTypeDefinition(xmlType.getLocalPart(),xmlType.getNamespaceURI());

         if (isWrapped())
         {
            returnType = unwrapResponse(xt);
         }
         else
         {
            returnType = getReturnType(xmlName, xmlType, xt);
         }
      }

      return returnType;
   }

   private void generateParameter(StringBuilder buf, QName xmlName, QName xmlType, JBossXSModel xsmodel, XSTypeDefinition xt, boolean array, boolean primitive, boolean holder) throws IOException
   {
      WrappedArray wrappedArray = new WrappedArray(xt);
      String arraySuffix = (array) ? "[]" : "";
      if (wrappedArray.unwrap())
      {
         xt = wrappedArray.xt;
         xmlType = wrappedArray.xmlType;
         primitive = !wrappedArray.nillable;
         arraySuffix = wrappedArray.suffix;
      }

      if (xt instanceof XSSimpleTypeDefinition)
         xmlType = SchemaUtils.handleSimpleType((XSSimpleTypeDefinition) xt);

      Class cl = getJavaType(xmlType, primitive);
      //Class cl = typeMapping.getJavaType(inqname,true);

      if(cl != null)
      {
         if (holder)
            cl = utils.getHolder(cl);

         buf.append(JavaUtils.getSourceName(cl) + arraySuffix);
      }
      else
      {
         //buf.append(inqname.getLocalPart() + " " + inqname.getLocalPart().toLowerCase());
         String className = xmlType.getLocalPart();
         if (className.charAt(0) == '>')
            className = className.substring(1);
         className = utils.firstLetterUpperCase(className);
         buf.append(seiPkgName + "." + className + arraySuffix);

         if(xt instanceof XSComplexTypeDefinition)
            generateJavaSource((XSComplexTypeDefinition)xt, xsmodel, xmlName.getLocalPart());
      }
   }

   private void createSEIFile(WSDLInterface intf, File loc) throws IOException
   {
      String seiName = getServiceEndpointInterfaceName(intf);

      StringBuilder buf = new StringBuilder();
      utils.writeJbossHeader(buf);
      buf.append("package " + seiPkgName + ";" + newline);
      buf.append("public interface  " + seiName + " extends java.rmi.Remote" + newline + "{" + newline);
      appendMethods(intf, buf);
      buf.append("}" + newline);

      File sei = utils.createPhysicalFile(loc, seiName);
      FileWriter writer = new FileWriter(sei);
      writer.write(buf.toString());
      writer.flush();
      writer.close();
   }

   public String getServiceEndpointInterfaceName(WSDLInterface wsdlInterface)
   {
      String seiName = utils.chopPortType(wsdlInterface.getName().toString());

      //Check if the portType name conflicts with a service name
      if (wsdl.getService(new NCName(seiName)) != null)
         seiName += "_PortType";

      return seiName;
   }

   private void createSEI(File loc, WSDLDefinitions wsdl)
   {
      WSDLInterface[] intarr = wsdl.getInterfaces();
      if (intarr == null || intarr.length == 0)
         throw new IllegalArgumentException("Interfaces cannot be zero");
      int len = intarr.length;
      for (int i = 0; i < len; i++)
      {
         WSDLInterface intf = intarr[i];
         try
         {
            createSEIFile(intf, loc);
         }
         catch (IOException e)
         {
            e.printStackTrace();
         }
      }
   }

   private String getReturnType(QName xmlName, QName xmlType, XSTypeDefinition xt) throws IOException
   {
      JBossXSModel xsmodel = WSDLUtils.getSchemaModel(wsdl.getWsdlTypes());

      if (isWrapped())
         return unwrapResponse(xt);

      boolean primitive = true;
      WrappedArray wrappedArray = new WrappedArray(xt);
      String arraySuffix = "";
      if (wrappedArray.unwrap())
      {
         xt = wrappedArray.xt;
         xmlType = wrappedArray.xmlType;
         primitive = !wrappedArray.nillable;
         arraySuffix = wrappedArray.suffix;
      }

      if (xt instanceof XSSimpleTypeDefinition)
         xmlType = SchemaUtils.handleSimpleType((XSSimpleTypeDefinition) xt);

      Class cls = getJavaType(xmlType, primitive);

      if(xt instanceof XSComplexTypeDefinition)
         generateJavaSource((XSComplexTypeDefinition)xt, xsmodel, xmlName.getLocalPart());

      if(cls == null)
      {
         String className = xmlType.getLocalPart();
         if (className.charAt(0) == '>')
            className = className.substring(1);
         className = utils.firstLetterUpperCase(className);
         return seiPkgName + "." + className + arraySuffix;
      }
      if(cls.isArray())
         return JavaUtils.getSourceName(cls);

      return  cls.getName() + arraySuffix;
   }

   /**
    * WSDL may have appended the Faults with 'Fault' or 'Error'
    * @param faultname
    * @return
    */
   private String cleanUpFaultName(String faultname)
   {
      //Clean up the faultname from Error and Fault
      boolean endsfault = faultname.endsWith("Fault");

      if (endsfault)
      {
         int index = faultname.lastIndexOf("Fault");
         faultname = faultname.substring(0, index);
      }
      else
      {
         boolean endsError = faultname.endsWith("Error");
         if (endsError)
         {
            int index = faultname.lastIndexOf("Error");
            faultname = faultname.substring(0, index);
         }
      }
      return JavaUtils.capitalize(faultname);
   }

   private void checkTypeMapping()
   {
      if(typeMapping == null)
         throw new WSException("TypeMapping has not been set.");
   }

   private Class getJavaType(QName qname, boolean primitive)
   {
      Class cls = typeMapping.getJavaType(qname, primitive);
      /**
       * Special case - when qname=xsd:anyType && cls == Element
       * then cls has to be javax.xml.soap.SOAPElement
       */
      if( qname.getNamespaceURI().equals(Constants.NS_SCHEMA_XSD)
            && "anyType".equals(qname.getLocalPart()) && cls == Element.class)
         cls = SOAPElement.class;
      return cls;
   }

   private String getMethodParam(QName qn)
   {
      return ToolsUtils.firstLetterLowerCase(qn.getLocalPart());
   }

   private File getLocationForJavaGeneration()
   {
      return new File( this.directoryToGenerate + "/" + seiPkgName.replace(".","/"));
   }

   private void generateJavaSource(XSComplexTypeDefinition xt, JBossXSModel xsmodel, String containingElement) throws IOException
   {
      generateJavaSource(xt, xsmodel, containingElement, false);
   }

   private void generateJavaSource(XSComplexTypeDefinition xt, JBossXSModel xsmodel, String containingElement, boolean exception) throws IOException
   {
      XSDTypeToJava xtj = new XSDTypeToJava();
      xtj.setTypeMapping(this.typeMapping);
      xtj.createJavaFile((XSComplexTypeDefinition)xt, containingElement, getLocationForJavaGeneration(), seiPkgName, xsmodel, exception);
   }

   public void setParameterStyle(String paramStyle)
   {
      this.parameterStyle = paramStyle;
   }
}
