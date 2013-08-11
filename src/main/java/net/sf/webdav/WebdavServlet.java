/*
 * Copyright 1999,2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.sf.webdav;

import javax.servlet.ServletException;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * Servlet which provides support for WebDAV level 2.
 * 
 * the original class is org.apache.catalina.servlets.WebdavServlet by Remy
 * Maucherat, which was heavily changed
 * 
 * @author Remy Maucherat
 */

public class WebdavServlet extends WebDavServletBean {

	@Override
	public void init() throws ServletException {

		// Parameters from web.xml
		String beanName = getServletConfig().getInitParameter(
				"ResourceHandlerBean");
		if (beanName == null) {
			throw new IllegalArgumentException("WebdavServlet requires ResourceHandlerBean parameter");
		}

		IWebdavStore webdavStore = getStoreBean(beanName);

		boolean lazyFolderCreationOnPut = getInitParameter("lazyFolderCreationOnPut") != null
				&& getInitParameter("lazyFolderCreationOnPut").equals("1");

		String dftIndexFile = getInitParameter("default-index-file");
		String insteadOf404 = getInitParameter("instead-of-404");

		int noContentLengthHeader = getIntInitParameter("no-content-length-headers");

		// Lock notifications
		ILockingListener listener = getLockingListenerBean(getInitParameter("LockingListenerBean"));

		super.init(webdavStore, listener, dftIndexFile, insteadOf404,
				noContentLengthHeader, lazyFolderCreationOnPut);
	}

	private int getIntInitParameter(String key) {
		return getInitParameter(key) == null ? -1 : Integer
				.parseInt(getInitParameter(key));
	}
	
	protected IWebdavStore getStoreBean(String beanName) {
		return getWebApplicationContext().getBean(beanName, IWebdavStore.class);
	}

	private WebApplicationContext getWebApplicationContext() {
		WebApplicationContext webApplicationContext = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
		return webApplicationContext;
	}

	protected ILockingListener getLockingListenerBean(String beanName) {
		if (beanName == null) {
			// making ILockingListener optional
			return new ILockingListener() {

				@Override
				public void onLockResource(ITransaction transaction,
						String resourceUri) {
				}

				@Override
				public void onUnlockResource(ITransaction transaction,
						String resourceUri) {
				}
				
			};
		} else {
			return getWebApplicationContext().getBean(beanName, ILockingListener.class);
		}
	}
}
