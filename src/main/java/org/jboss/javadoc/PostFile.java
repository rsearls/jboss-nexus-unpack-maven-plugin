/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat, Inc., and individual contributors
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
package org.jboss.javadoc;

import java.io.File;
import java.io.IOException;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.HttpVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;


/**
 * REST POST request generator.
 *
 * User: rsearls
 * Date: 2/3/16
 */
public class PostFile {
   protected HttpPost httppost;
   protected RequestBuilder requestBuilderPost;
   protected MultipartEntityBuilder mpEntityBuilder;
   protected File filePath;
   private String username;
   private String password;

   public PostFile(final String fullUrl, final File file, final String username, final String password){
      this.httppost = new HttpPost(fullUrl);
      this.filePath = file;
      this.mpEntityBuilder = MultipartEntityBuilder.create();
      this.username = username;
      this.password = password;

      requestBuilderPost = RequestBuilder.post();
      requestBuilderPost.setUri(fullUrl);

   }

   public StatusLine post() throws IOException {

      mpEntityBuilder.addBinaryBody("--upload-file", this.filePath);

      String encoding = new String(Base64.encodeBase64((username + ":" + password).getBytes()));
      requestBuilderPost.addHeader("Authorization", "Basic " + encoding);
      requestBuilderPost.addHeader("Accept", "*/*");
      requestBuilderPost.setVersion(HttpVersion.HTTP_1_1);
      requestBuilderPost.setEntity(mpEntityBuilder.build());

      SocketConfig socketConfig = SocketConfig.custom().setTcpNoDelay(true)
         .setSoTimeout(10000).build();
      CloseableHttpClient closeableHttpClient = HttpClientBuilder.create()
            .setDefaultSocketConfig(socketConfig).build();
      CloseableHttpResponse response = closeableHttpClient.execute(requestBuilderPost.build());

      HttpEntity resEntity = response.getEntity();
      if (resEntity != null) {
         EntityUtils.consume(resEntity);
      }
      return response.getStatusLine();
   }
}
