package org.jboss.test.ws.jaxws.jbws2000;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.activation.DataHandler;
import javax.ejb.Stateless;
import javax.jws.WebService;

@Stateless
@WebService(
  endpointInterface = "org.jboss.test.ws.jaxws.jbws2000.FileTransferService",
  name = "FileTransfer",
  targetNamespace = "http://service.mtom.test.net/"
)
public class FileTransferServiceImpl implements FileTransferService {

   public boolean transferFile(String fileName, DataHandler contents) {
      final List<File> tempFiles = new ArrayList<File>();
      final File deploymentTempDirectory = getTempDirectory();
      try {
         FileOutputStream fileOutputStream = null;
         try {
            final File outputFile = new File(deploymentTempDirectory, fileName);

            System.out.println("Write file '"+fileName+"' to dir " + deploymentTempDirectory);
            
            fileOutputStream = new FileOutputStream(outputFile);
            contents.writeTo(fileOutputStream);
            tempFiles.add(outputFile);
         } finally {
            if (fileOutputStream != null) {
               fileOutputStream.close();
            }
         }

         return true;
      } catch (Exception e) {
         throw new RuntimeException("Failed to schedule deployment", e);
      }
   }

   private File getTempDirectory() {
      final File deploymentTempDirectory = new File(System.getProperty("java.io.tmpdir"), Long.toString(System.currentTimeMillis()));
      deploymentTempDirectory.mkdir();
      return deploymentTempDirectory;
   }
}
