/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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
package org.jboss.ws.core.utils;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CachedOutputStream extends OutputStream
{
   private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;
   private static final File DEFAULT_TEMP_DIR;
   private static int defaultThreshold;
   private static long defaultMaxSize;
   static
   {
      String s = getSystemProperty("java.io.tmpdir");
      if (s != null)
      {
         File f = new File(s);
         if (f.exists() && f.isDirectory())
         {
            DEFAULT_TEMP_DIR = f;
         }
         else
         {
            DEFAULT_TEMP_DIR = null;
         }
      }
      else
      {
         DEFAULT_TEMP_DIR = null;
      }
      setDefaultThreshold(-1);
      setDefaultMaxSize(-1);
   }

   protected boolean outputLocked;
   protected OutputStream currentStream;
   private long threshold = defaultThreshold;
   private long maxSize = defaultMaxSize;
   private long totalLength;
   private boolean inmem;
   private boolean tempFileFailed;
   private File tempFile;
   private File outputDir = DEFAULT_TEMP_DIR;
   private boolean allowDeleteOfFile = true;
   private List<Object> streamList = new ArrayList<Object>();

   public CachedOutputStream(PipedInputStream stream) throws IOException
   {
      currentStream = new PipedOutputStream(stream);
      inmem = true;
   }

   public CachedOutputStream()
   {
      currentStream = new LoadingByteArrayOutputStream(2048);
      inmem = true;
   }

   public CachedOutputStream(long threshold)
   {
      this.threshold = threshold;
      currentStream = new LoadingByteArrayOutputStream(2048);
      inmem = true;
   }

   public void holdTempFile()
   {
      allowDeleteOfFile = false;
   }

   public void releaseTempFileHold()
   {
      allowDeleteOfFile = true;
   }

   public void flush() throws IOException
   {
      currentStream.flush();
   }

   /**
    * Locks the output stream to prevent additional writes, but maintains
    * a pointer to it so an InputStream can be obtained
    * @throws IOException
    */
   public void lockOutputStream() throws IOException
   {
      if (outputLocked)
      {
         return;
      }
      currentStream.flush();
      outputLocked = true;
      streamList.remove(currentStream);
   }

   public void close() throws IOException
   {
      currentStream.flush();
      outputLocked = true;
      currentStream.close();
      maybeDeleteTempFile(currentStream);
   }

   public boolean equals(Object obj)
   {
      if (obj == this)
      {
         return true;
      }
      if (obj instanceof CachedOutputStream)
      {
         return currentStream.equals(((CachedOutputStream) obj).currentStream);
      }
      return currentStream.equals(obj);
   }

   public long size()
   {
      return totalLength;
   }

   public byte[] getBytes() throws IOException
   {
      flush();
      if (inmem)
      {
         if (currentStream instanceof ByteArrayOutputStream)
         {
            return ((ByteArrayOutputStream) currentStream).toByteArray();
         }
         else
         {
            throw new IOException("Unknown format of currentStream");
         }
      }
      else
      {
         FileInputStream fin = new FileInputStream(tempFile);
         return readBytesFromStream(fin);
      }
   }

   public void writeCacheTo(StringBuilder out, String charsetName) throws IOException
   {
      flush();
      if (inmem)
      {
         if (currentStream instanceof ByteArrayOutputStream)
         {
            byte[] bytes = ((ByteArrayOutputStream) currentStream).toByteArray();
            out.append(new String(bytes, charsetName));
         }
         else
         {
            throw new IOException("Unknown format of currentStream");
         }
      }
      else
      {
         FileInputStream fin = new FileInputStream(tempFile);
         byte bytes[] = new byte[1024];
         try
         {
            int x = fin.read(bytes);
            while (x != -1)
            {
               out.append(new String(bytes, 0, x, charsetName));
               x = fin.read(bytes);
            }
         }
         finally
         {
            fin.close();
         }
      }
   }

   /**
    * @return the underlying output stream
    */
   public OutputStream getOut()
   {
      return currentStream;
   }

   public int hashCode()
   {
      return currentStream.hashCode();
   }

   public String toString()
   {
      StringBuilder builder = new StringBuilder().append("[").append(CachedOutputStream.class.getName()).append(" Content: ");
      try
      {
         writeCacheTo(builder, "UTF-8");
      }
      catch (IOException e)
      {
         //ignore
      }
      return builder.append("]").toString();
   }

   protected void onWrite() throws IOException
   {

   }

   private void enforceLimits() throws IOException
   {
      if (maxSize > 0 && totalLength > maxSize)
      {
         throw new IOException();
      }
      if (inmem && totalLength > threshold && currentStream instanceof ByteArrayOutputStream)
      {
         createFileOutputStream();
      }
   }

   public void write(byte[] b, int off, int len) throws IOException
   {
      if (!outputLocked)
      {
         onWrite();
         this.totalLength += len;
         enforceLimits();
         currentStream.write(b, off, len);
      }
   }

   public void write(byte[] b) throws IOException
   {
      if (!outputLocked)
      {
         onWrite();
         this.totalLength += b.length;
         enforceLimits();
         currentStream.write(b);
      }
   }

   public void write(int b) throws IOException
   {
      if (!outputLocked)
      {
         onWrite();
         this.totalLength++;
         enforceLimits();
         currentStream.write(b);
      }
   }

   private void createFileOutputStream() throws IOException
   {
      if (tempFileFailed)
      {
         return;
      }
      ByteArrayOutputStream bout = (ByteArrayOutputStream) currentStream;
      try
      {
         tempFile = File.createTempFile("jbossws-cached-output-stream-", ".tmp", outputDir);
         currentStream = new BufferedOutputStream(new FileOutputStream(tempFile));
         bout.writeTo(currentStream);
         inmem = false;
         streamList.add(currentStream);
      }
      catch (Exception ex)
      {
         tempFileFailed = true;
         if (currentStream != bout)
         {
            currentStream.close();
         }
         deleteTempFile();
         inmem = true;
         currentStream = bout;
      }
   }

   public File getTempFile()
   {
      return tempFile != null && tempFile.exists() ? tempFile : null;
   }

   public InputStream getInputStream() throws IOException
   {
      flush();
      if (inmem)
      {
         if (currentStream instanceof LoadingByteArrayOutputStream)
         {
            return ((LoadingByteArrayOutputStream) currentStream).createInputStream();
         }
         else if (currentStream instanceof ByteArrayOutputStream)
         {
            return new ByteArrayInputStream(((ByteArrayOutputStream) currentStream).toByteArray());
         }
         else if (currentStream instanceof PipedOutputStream)
         {
            return new PipedInputStream((PipedOutputStream) currentStream);
         }
         else
         {
            return null;
         }
      }
      else
      {
         try
         {
            FileInputStream fileInputStream = new FileInputStream(tempFile)
            {
               boolean closed;

               public void close() throws IOException
               {
                  if (!closed)
                  {
                     super.close();
                     maybeDeleteTempFile(this);
                  }
                  closed = true;
               }
            };
            streamList.add(fileInputStream);
            return fileInputStream;
         }
         catch (FileNotFoundException e)
         {
            throw new IOException("Cached file was deleted, " + e.toString());
         }
      }
   }

   private synchronized void deleteTempFile()
   {
      if (tempFile != null)
      {
         File file = tempFile;
         tempFile = null;
         delete(file);
      }
   }

   private void maybeDeleteTempFile(Object stream)
   {
      streamList.remove(stream);
      if (!inmem && tempFile != null && streamList.isEmpty() && allowDeleteOfFile)
      {
         if (currentStream != null)
         {
            try
            {
               currentStream.close();
            }
            catch (Exception e)
            {
               //ignore
            }
         }
         deleteTempFile();
         currentStream = new LoadingByteArrayOutputStream(1024);
         inmem = true;
      }
   }

   public void setOutputDir(File outputDir) throws IOException
   {
      this.outputDir = outputDir;
   }

   public void setThreshold(long threshold)
   {
      this.threshold = threshold;
   }

   public void setMaxSize(long maxSize)
   {
      this.maxSize = maxSize;
   }

   public static void setDefaultMaxSize(long l)
   {
      defaultMaxSize = l;
   }

   public static void setDefaultThreshold(int i)
   {
      if (i < 0)
      {
         i = 64 * 1024;
      }
      defaultThreshold = i;
   }

   private static byte[] readBytesFromStream(InputStream in) throws IOException
   {
      int i = in.available();
      if (i < DEFAULT_BUFFER_SIZE)
      {
         i = DEFAULT_BUFFER_SIZE;
      }
      ByteArrayOutputStream bos = new ByteArrayOutputStream(i);
      copy(in, bos, DEFAULT_BUFFER_SIZE);
      in.close();
      return bos.toByteArray();
   }

   private static int copy(final InputStream input, final OutputStream output, int bufferSize) throws IOException
   {
      int avail = input.available();
      if (avail > 262144)
      {
         avail = 262144;
      }
      if (avail > bufferSize)
      {
         bufferSize = avail;
      }
      final byte[] buffer = new byte[bufferSize];
      int n = 0;
      n = input.read(buffer);
      int total = 0;
      while (-1 != n)
      {
         if (n == 0)
         {
            throw new IOException("0 bytes read in violation of InputStream.read(byte[])");
         }
         output.write(buffer, 0, n);
         total += n;
         n = input.read(buffer);
      }
      return total;
   }

   private static void delete(File f)
   {
      if (!f.delete())
      {
         if (isWindows())
         {
            System.gc();
         }
         try
         {
            Thread.sleep(10);
         }
         catch (InterruptedException ex)
         {
            // Ignore Exception
         }
         if (!f.delete())
         {
            f.deleteOnExit();
         }
      }
   }

   private static boolean isWindows()
   {
      String osName = getSystemProperty("os.name").toLowerCase(Locale.US);
      return osName.indexOf("windows") > -1;
   }

   private static String getSystemProperty(final String name)
   {
      PrivilegedAction<String> action = new PrivilegedAction<String>()
      {
         public String run()
         {
            return System.getProperty(name);
         }
      };
      return AccessController.doPrivileged(action);
   }

   private static class LoadingByteArrayOutputStream extends ByteArrayOutputStream
   {
      public LoadingByteArrayOutputStream(int i)
      {
         super(i);
      }

      public ByteArrayInputStream createInputStream()
      {
         return new ByteArrayInputStream(buf, 0, count)
         {
            public String toString()
            {
               return new String(buf, 0, count);
            }
         };
      }

      public byte[] toByteArray()
      {
         if (count != buf.length)
         {
            buf = super.toByteArray();
         }
         return buf;
      }
   }
}
