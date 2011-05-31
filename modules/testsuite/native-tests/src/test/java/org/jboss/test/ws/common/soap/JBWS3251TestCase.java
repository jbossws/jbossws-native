package org.jboss.test.ws.common.soap;

import java.util.Iterator;
import java.util.concurrent.CountDownLatch;

import javax.xml.soap.SOAPElement;
import javax.xml.soap.Name;

import org.jboss.ws.core.soap.MessageFactoryImpl;
import org.jboss.ws.core.soap.NameImpl;
import org.jboss.ws.core.soap.SOAPBodyImpl;
import org.jboss.ws.core.soap.SOAPContentElement;
import org.jboss.ws.core.soap.SOAPElementImpl;
import org.jboss.ws.core.soap.XMLFragment;
import org.jboss.wsf.test.JBossWSTest;

public class JBWS3251TestCase extends JBossWSTest
{
   public void test() throws Exception
   {
      int threadCount = 20;
      SoapElementTester test = new SoapElementTester();
      CountDownLatch latch = new CountDownLatch(threadCount);

      test.latch = latch;

      for(int i=0; i<threadCount; i++)
      {
         Thread t = new Thread(test);
         t.start();
      }

      latch.await();
      Thread.currentThread().sleep(200);

      assertTrue(test.allRunsSuccessful);
   }

   private class SoapElementTester implements Runnable
   {
      private final String soapXml = 
         "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:wsp=\"http://examples.jboss.com/ws\">\n" + 
         "  <soapenv:Header>\n" + 
         "    <wsp:info>\n" + 
         "      <wsp:test>Child of Child</wsp:test>\n" + 
         "    </wsp:info>\n" + 
         "  </soapenv:Header>\n" + 
         "  <soapenv:Body>\n" + 
         "  </soapenv:Body>\n" + 
         "</soapenv:Envelope>";

      private SOAPElement sharedElement;
      public boolean allRunsSuccessful = true;
      public CountDownLatch latch;

      public void run()
      {
         Name name = new NameImpl(
           "Envelope", 
           "soapenv", 
           "http://schemas.xmlsoap.org/soap/envelope/"
         );

         //Do we even need this part?
         SOAPContentElement soapEl = new SOAPContentElement(name);
         soapEl.setXMLFragment(new XMLFragment(soapXml));

         sharedElement = soapEl;

         if(!printNodeNames())
            allRunsSuccessful = false;
      }

      public boolean printNodeNames()
      {
         try
         {
            Iterator i = sharedElement.getChildElements();
            while(i.hasNext())
            {
               Object o = i.next();
               if(!(o instanceof SOAPElement))
                  continue;

               SOAPElement elt = (SOAPElement)o;

               Iterator i2 = elt.getChildElements();
               while(i2.hasNext())
               {
                  o = i2.next();
                  if(!(o instanceof SOAPElement))
                     continue;

                  SOAPElement child = (SOAPElement)o;

                  Iterator i3 = child.getChildElements();
                  while(i3.hasNext())
                  {
                     o = i3.next();
                     if(!(o instanceof SOAPElement))
                        continue;

                     SOAPElement grandchild = (SOAPElement)o;
                  }
               }
            }
         }
         catch(Exception e)
         {
            e.printStackTrace();
            return false;
         }
         finally
         {
            latch.countDown();
         }
         return true;
      }
   }
}
