package org.jboss.test.ws.jaxrpc.jbws775;

import java.rmi.Remote;
import java.rmi.RemoteException;

import org.jboss.logging.Logger;

public class DocumentTranslatorImpl implements DocumentTranslator, Remote
{
   private Logger log = Logger.getLogger(DocumentTranslatorImpl.class);

   public TDocument translate(TTranslationRequest tRequest) throws TTextNotTranslatable, TDictionaryNotAvailable, RemoteException
   {
      TDocument tDocument = tRequest.getDocument();
      
      TDocumentHead tHead = tDocument.getHead();
      String lang = tHead.getLanguage();
      String title = tHead.getTitle();
      
      log.info("[lang=" + lang + ",title=" + title + "]");
      if ("en".equals(lang) == false || "title".equals(title) == false)
         throw new IllegalStateException("Invalid TDocumentHead");

      return tDocument;
   }

   public void quoteTranslation(TQuotationRequest quotationRequest) throws RemoteException
   {
   }

   public TStatusResponse getQuotationStatus(TStatusRequest statusRequest) throws RemoteException
   {
      TStatusResponse _retVal = null;
      return _retVal;
   }
}
