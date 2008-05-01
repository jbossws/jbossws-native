package org.jboss.test.ws.jaxrpc.jbws775;

import java.rmi.Remote;
import java.rmi.RemoteException;

public class TextTranslatorImpl implements TextTranslator, Remote
{
   public String translate(String text, String sourceLanguage, String targetLanguage) throws TDictionaryNotAvailable, TTextNotTranslatable, RemoteException
   {

      String _retVal = null;
      return _retVal;
   }

   public void quoteTranslation(String clientName, String text, String sourceLanguage, String targetLanguage) throws RemoteException
   {
   }

   public TQuoteStatus getQuotationStatus(String clientName) throws RemoteException
   {

      TQuoteStatus _retVal = null;
      return _retVal;
   }
}
