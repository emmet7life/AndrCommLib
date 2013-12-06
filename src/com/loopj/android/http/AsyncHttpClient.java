/*
    Android Asynchronous Http Client
    Copyright (c) 2011 James Smith <james@loopj.com>
    http://loopj.com

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
 */

package com.loopj.android.http;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.HttpVersion;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.HttpEntityWrapper;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.SyncBasicHttpContext;
import org.mrchen.commlib.helper.LogHelper;

import android.content.Context;

/**
 * The AsyncHttpClient can be used to make asynchronous GET, POST, PUT and
 * DELETE HTTP requests in your Android applications. Requests can be made with
 * additional parameters by passing a {@link RequestParams} instance, and
 * responses can be handled by passing an anonymously overridden
 * {@link AsyncHttpResponseHandler} instance.
 * <p>
 * For example:
 * <p>
 * 
 * <pre>
 * AsyncHttpClient client = new AsyncHttpClient();
 * client.get(&quot;http://www.google.com&quot;, new AsyncHttpResponseHandler() {
 * 	&#064;Override
 * 	public void onSuccess(String response) {
 * 		System.out.println(response);
 * 	}
 * });
 * </pre>
 */
public class AsyncHttpClient {

	private static final String TAG = "AsyncHttpClient";
	private static final String VERSION = "1.4.3";

	private static final int DEFAULT_MAX_CONNECTIONS = 10;// 同一条线路(连接到相同主机的线路)最大连接数
	private static final int DEFAULT_SOCKET_TIMEOUT = 5 * 1000;// 连接超时时间，作者把时间设置为10秒，貌似有点太长了，这里我修改一下设置为5秒
	private static final int DEFAULT_MAX_RETRIES = 5;// 重试的最大次数
	private static final int DEFAULT_SOCKET_BUFFER_SIZE = 8192;// 连接的默认缓存大小
	private static final String HEADER_ACCEPT_ENCODING = "Accept-Encoding";
	private static final String ENCODING_GZIP = "gzip";

	private static int maxConnections = DEFAULT_MAX_CONNECTIONS;
	private static int socketTimeout = DEFAULT_SOCKET_TIMEOUT;

	private final DefaultHttpClient httpClient;// 负责执行GET/POST请求的请求器
	private final HttpContext httpContext;// 连接上下文对象
	private ThreadPoolExecutor threadPool;// 线程池

	private final Map<Context, List<WeakReference<Future<?>>>> requestMap;
	private final Map<String, String> clientHeaderMap;// 保存头信息的Map对象

	/**
	 * Creates a new AsyncHttpClient.
	 */
	public AsyncHttpClient() {
		BasicHttpParams httpParams = new BasicHttpParams();
		/*
		 * ConnManagerParams
		 */
		/**
		 * 连接池管理器
		 * 
		 * ThreadSafeClientConnManager是一个复杂的实现来管理客户端连接池，它也可以从多个执行线程中服务连接请求。
		 * 对每个基本的路由，连接都是池管理的。对于路由的请求，管理器在池中有可用的持久性连接，将被从池中租赁连接服务，而不是创建一个新的连接。
		 * 
		 * ThreadSafeClientConnManager维护每个基本路由的最大连接限制。
		 * 每个默认的实现对每个给定路由将会创建不超过两个的并发连接
		 * ，而总共也不会超过20个连接。对于很多真实的应用程序，这个限制也证明很大的制约，特别是他们在服务中使用HTTP作为传输协议
		 * 。连接限制，也可以使用HTTP参数来进行调整。
		 * 
		 * 2.9 连接管理参数 这些是可以用于定制标准HTTP连接管理器实现的参数：
		 * 
		 * 'http.conn-manager.timeout'：定义了当从ClientConnectionManager中检索ManagedClientConnection实例时使用的毫秒级的超时时间。这个参数期望得到一个java.lan
		 * g . L o n g 类 型 的 值 。 如 果 这 个 参 数 没 有 被 设 置 ， 连 接 请 求 就 不 会 超 时 （ 无 限
		 * 大 的 超 时 时 间 ） 。
		 * 'http.conn-manager.max-per-route'：定义了每个路由连接的最大数量。这个限制由客户端连接管理器来解释，而且应
		 * 用 于 独 立 的 管 理 器 实 例 。 这 个 参 数 期 望 得 到 一 个 C o n n P e r R o u t e 类 型
		 * 的 值 。
		 * 'http.conn-manager.max-total'：定义了总共连接的最大数目。这个限制由客户端连接管理器来解释，而且应用于独立的管
		 * 理 器 实 例 。 这 个 参 数 期 望 得 到 一 个 j a v a . l a n g . I n t e g e r 类 型 的
		 * 值 。
		 */
		// 设置获取连接的最大等待时间
		ConnManagerParams.setTimeout(httpParams, socketTimeout);
		// 设置每个路由最大连接数
		ConnManagerParams.setMaxConnectionsPerRoute(httpParams, new ConnPerRouteBean(maxConnections));
		// 还可以对特定的主机设置最大连接数
		/**
		 * // 增加每个路由的默认最大连接到20 <br>
		 * ConnPerRouteBean connPerRoute = new ConnPerRouteBean(20); <br>
		 * // 对localhost:80增加最大连接到50 <br>
		 * HttpHost localhost = new HttpHost("locahost", 80);<br>
		 * connPerRoute.setMaxForRoute(new HttpRoute(localhost), 50);<br>
		 * ConnManagerParams.setMaxConnectionsPerRoute(params, connPerRoute);<br>
		 */
		// 设置最大连接数
		// ConnManagerParams.setMaxTotalConnections(httpParams,
		// DEFAULT_MAX_CONNECTIONS);
		ConnManagerParams.setMaxTotalConnections(httpParams, 50);// 这个应该设置的大一点吧！？
		/*
		 * HttpConnectionParams
		 */
		// 设置读取超时时间
		HttpConnectionParams.setSoTimeout(httpParams, socketTimeout);
		// 设置连接超时时间
		HttpConnectionParams.setConnectionTimeout(httpParams, socketTimeout);
		// 设置连接是不是立即发送
		HttpConnectionParams.setTcpNoDelay(httpParams, true);
		// 设置连接缓存大小
		HttpConnectionParams.setSocketBufferSize(httpParams, DEFAULT_SOCKET_BUFFER_SIZE);
		HttpConnectionParams.setStaleCheckingEnabled(httpParams, false);// 出于性能的关键操作，检查应该被关闭。
		/*
		 * HttpProtocolParams
		 */
		// 设置协议的版本
		HttpProtocolParams.setVersion(httpParams, HttpVersion.HTTP_1_1);
		// 设置UserAgent
		// HttpProtocolParams.setUserAgent(httpParams,
		// String.format("android-async-http/%s (http://loopj.com/android-async-http)",
		// VERSION));
		HttpProtocolParams.setUserAgent(httpParams,
				"JUC (Linux; U; 2.3.7; zh-cn; MB200; 320*480) UCWEB7.9.3.103/139/999");
		// UC安卓浏览器的UserAgent
		// JUC (Linux; U; 2.3.7; zh-cn; MB200; 320*480) UCWEB7.9.3.103/139/999
		// 设置Except 100 continue:false
		// 参考资料：http://blog.csdn.net/androidzhaoxiaogang/article/details/8164637
		HttpProtocolParams.setUseExpectContinue(httpParams, false);
		/*
		 * 定义了重定向是否应该自动处理。这个参数期望得到一个java.lang.Boolean类型的值。如果这个参数没有被设置，
		 * HttpClient将会自动处理重定向。
		 */
		// httpParams.setParameter("http.protocol.handle-redirects", false);
		/*
		 * 定义了是否相对的重定向应该被拒绝。HTTP规范需要位置值是一个绝对URI。这个参数期望得到一个java.lang.Boolean类型的值。
		 * 如果这个参数没有被设置，那么就允许相对重定向。
		 */
		// httpParams.setParameter("http.protocol.reject-relative-redirect",
		// true);
		/*
		 * 这个重定向数字的限制意在防止由破碎的服务器端脚本引发的死循环。这个参数期望得到一个java.lang.Integer类型的值。
		 * 如果这个参数没有被设置，那么只允许不多余100次重定向。
		 */
		// httpParams.setParameter("http.protocol.max-redirects", 10);//
		// 定义了要遵循重定向的最大数量
		/*
		 * 定义环形重定向（重定向到相同路径）是否被允许。HTTP规范在环形重定向没有足够清晰的允许表述，因此这作为可选的是可以开启的。
		 * 这个参数期望得到一个java.lang.Boolean类型的值。如果这个参数没有被设置，那么环形重定向就不允许。
		 */
		// httpParams.setParameter("http.protocol.allow-circular-redirects'",
		// false);
		/*
		 * 定义了每次请求默认发送的头部信息。这个参数期望得到一个包含Header对象的java.util.Collection类型值。
		 */
		// httpParams.setParameter("http.default-headers'", Collection);

		// 设置支持的请求类型
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		// 套接字工厂：PlainSocketFactory是创建和初始化普通的(不加密的)套接字的默认工厂。
		schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
		// LayeredSocketFactory是SocketFactory接口的扩展。分层的套接字工厂可以创建在已经存在的普通套
		// 接字之上的分层套接字。套接字分层主要通过代理来创建安全的套接字。HttpClient附带实现了SSL/TLS分层的SSLSocketFactory。
		// 请注意HttpClient不使用任何自定义加密功能。它完全依赖于标准的Java密码学（JCE）和安全套接字（JSEE）扩展。
		schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
		// 每一个默认的HttpClient使用BrowserCompatHostnameVerifier的实现
		// 如果需要的话，它可以指定不同的主机名验证器实现。
		// SSLSocketFactory sf = new
		// SSLSocketFactory(SSLContext.getInstance("TLS"));
		// sf.setHostnameVerifier(SSLSocketFactory.STRICT_HOSTNAME_VERIFIER);

		/**
		 * 多线程执行请求
		 * 
		 * 当配备连接池管理器时，比如ThreadSafeClientConnManager，HttpClient可以同时被用来执行多个请求
		 * ，使用多线程执行。
		 * 
		 * ThreadSafeClientConnManager将会分配基于它的配置的连接。如果对于给定路由的所有连接都被租出了，
		 * 那么连接的请求将会阻塞，直到一个连接被释放回连接池。它可以通过设置
		 * 'http.conn-manager.timeout'为一个正数来保证连接管理器不会在连接请求执行时无限期的被阻塞。如果连接请求不能在给定的时间周期内被响应，
		 * 将 会 抛 出 C o n n e c t i o n P o o l T i m e o u t E x c e p t i o n 异
		 * 常 。
		 */
		ThreadSafeClientConnManager cm = new ThreadSafeClientConnManager(httpParams, schemeRegistry);
		// 在最新版本的httpclient中ThreadSafeClientConnManager已被@Deprecated掉，这里使用PoolingClientConnectionManager。
		// ClientConnectionManager conMgr = new
		// PoolingClientConnectionManager(schemeRegistry);
		// 上下文环境对象
		httpContext = new SyncBasicHttpContext(new BasicHttpContext());
		httpClient = new DefaultHttpClient(cm, httpParams);
		// ************** 基本参数配置完成 **************
		// HttpClient代理配置
		// 尽管HttpClient了解复杂的路由模式和代理链，它仅支持简单直接的或开箱的跳式代理连接。
		// 告诉HttpClient通过代理去连接到目标主机的最简单方式是通过设置默认的代理参数：
		// HttpHost proxy = new HttpHost("someproxy", 8080);
		// httpclient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY,
		// proxy);
		// 也可以构建HttpClient使用标准的JRE代理选择器来获得代理信息：
		// ProxySelectorRoutePlanner routePlanner = new
		// ProxySelectorRoutePlanner(
		// httpclient.getConnectionManager().getSchemeRegistry(),
		// ProxySelector.getDefault());
		// httpclient.setRoutePlanner(routePlanner);
		// 另外一种选择，可以提供一个定制的RoutePlanner实现来获得HTTP路由计算处理上的复杂的控制：
		// httpclient.setRoutePlanner(new HttpRoutePlanner() {
		// public HttpRoute determineRoute(HttpHost target,
		// HttpRequest request,
		// HttpContext context) throws HttpException {
		// return new HttpRoute(target, null, new HttpHost("someproxy", 8080),
		// "https".equalsIgnoreCase(target.getSchemeName()));
		// }
		// });

		// 在天朝，这个或许是有必要设置的

		/**
		 * 连接保持活动的策略<br>
		 * <br>
		 * HTTP规范没有确定一个持久连接可能或应该保持活动多长时间。一些HTTP服务器使用非标准的头部信息Keep-
		 * Alive来告诉客户端它们想在服务器端保持连接活动的周期秒数
		 * 。如果这个信息可用，HttClient就会利用这个它。如果头部信息Keep-Alive在响应中不存在
		 * ，HttpClient假设连接无限期的保持活动
		 * 。然而许多现实中的HTTP服务器配置了在特定不活动周期之后丢掉持久连接来保存系统资源，往往这是不通知客户端的
		 * 。如果默认的策略证明是过于乐观的，那么就会有人想提供一个定制的保持活动策略。
		 */
		httpClient.setKeepAliveStrategy(new ConnectionKeepAliveStrategy() {

			@Override
			public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
				LogHelper.i(TAG, "setKeepAliveStrategy 连接保持活动的策略");
				// 兑现'keep-alive'头部信息
				HeaderElementIterator it = new BasicHeaderElementIterator(response.headerIterator(HTTP.CONN_KEEP_ALIVE));
				// if (it != null) {
				while (it.hasNext()) {
					HeaderElement he = it.nextElement();
					String param = he.getName();
					String value = he.getValue();
					if (value != null && param.equalsIgnoreCase("timeout")) {
						try {
							return Long.parseLong(value) * 1000;
						} catch (NumberFormatException ignore) {
						}
					}
				}
				HttpHost target = (HttpHost) context.getAttribute(ExecutionContext.HTTP_TARGET_HOST);
				// 针对特定的主机站点保持的时间减少
				String specialHostName = "www.manhua.weibo.com";
				if (specialHostName.equalsIgnoreCase(target.getHostName())) {
					// 只保持活动5秒
					return 5 * 1000;
				} else {
					// 否则保持活动30秒
					return 30 * 1000;
				}
				// }
				// return 0;
			}
		});

		// 设置请求发送之前的处理过程，此处拦截请求用来填充必要的头信息
		httpClient.addRequestInterceptor(new HttpRequestInterceptor() {
			@Override
			public void process(HttpRequest request, HttpContext context) {
				LogHelper.i(TAG, "addRequestInterceptor process 设置请求发送之前的处理过程，此处拦截请求用来填充必要的头信息");
				// 若请求类中不包含Accept-Encoding头信息，则默认加上gzip格式的支持
				// Accept-Encoding:客户端向服务器发送的头信息，代表客户端可接受的服务器返回的数据类型
				// 更多信息参考：http://baike.baidu.com/view/9325891.htm
				if (!request.containsHeader(HEADER_ACCEPT_ENCODING)) {
					request.addHeader(HEADER_ACCEPT_ENCODING, ENCODING_GZIP);
				}
				// 填充头信息
				for (String header : clientHeaderMap.keySet()) {
					request.addHeader(header, clientHeaderMap.get(header));
				}
			}
		});

		// 同理。在请求返回的时候填充服务器返回的头信息
		httpClient.addResponseInterceptor(new HttpResponseInterceptor() {
			@Override
			public void process(HttpResponse response, HttpContext context) {
				LogHelper.i(TAG, "addResponseInterceptor process 同理。在请求返回的时候填充服务器返回的头信息");
				final HttpEntity entity = response.getEntity();
				if (entity == null) {
					return;
				}
				// 获取服务器返回的"Content-Encoding"头信息，代表返回的数据类型
				final Header encoding = entity.getContentEncoding();
				if (encoding != null) {
					// 判断是否有"gzip"类型支持
					for (HeaderElement element : encoding.getElements()) {
						if (element.getName().equalsIgnoreCase(ENCODING_GZIP)) {
							response.setEntity(new InflatingEntity(response.getEntity()));
							break;
						}
					}
				}
			}
		});

		/**
		 * Cookie策略<br>
		 * <br>
		 * Cookie策略可以在HTTP客户端被设置，如果需要，在HTTP请求级重写。 <br>
		 * // 对每个默认的强制严格cookie策略 <br>
		 * httpClient.getParams().setParameter(ClientPNames.COOKIE_POLICY,
		 * CookiePolicy.BEST_MATCH);<br>
		 * HttpGet httpget = new HttpGet("http://www.broken-server.com/");<br>
		 * // 对这个请求覆盖默认策略<br>
		 * httpget.getParams().setParameter(ClientPNames.COOKIE_POLICY,
		 * CookiePolicy.BROWSER_COMPATIBILITY);<br>
		 */

		// 重试机制处理规则类RetryHandler
		httpClient.setHttpRequestRetryHandler(new RetryHandler(DEFAULT_MAX_RETRIES));

		// 线程数无限制的线程池
		threadPool = (ThreadPoolExecutor) Executors.newCachedThreadPool();

		// 基本对象的构建
		requestMap = new WeakHashMap<Context, List<WeakReference<Future<?>>>>();
		clientHeaderMap = new HashMap<String, String>();
	}

	/**
	 * Get the underlying HttpClient instance. This is useful for setting
	 * additional fine-grained settings for requests by accessing the client's
	 * ConnectionManager, HttpParams and SchemeRegistry.
	 */
	public HttpClient getHttpClient() {
		return this.httpClient;
	}

	/**
	 * Get the underlying HttpContext instance. This is useful for getting and
	 * setting fine-grained settings for requests by accessing the context's
	 * attributes such as the CookieStore.
	 */
	public HttpContext getHttpContext() {
		return this.httpContext;
	}

	/**
	 * DefaultHttpClient是线程安全的。建议相同的这个类的实例被重用于多个请求的执行。<br>
	 * 当一个DefaultHttpClient实例不再需要而且要脱离范围时<br>
	 * ，和它关联的连接管理器必须调用ClientConnectionManager#shutdown()方法关闭。<br>
	 */
	public void shutdown() {
		if (httpClient != null) {
			httpClient.getConnectionManager().shutdown();
		}
	}

	/**
	 * Sets an optional CookieStore to use when making requests
	 * 
	 * @param cookieStore
	 *            The CookieStore implementation to use, usually an instance of
	 *            {@link PersistentCookieStore}
	 */
	public void setCookieStore(CookieStore cookieStore) {
		httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
	}

	/**
	 * Overrides the threadpool implementation used when queuing/pooling
	 * requests. By default, Executors.newCachedThreadPool() is used.
	 * 
	 * @param threadPool
	 *            an instance of {@link ThreadPoolExecutor} to use for
	 *            queuing/pooling requests.
	 */
	public void setThreadPool(ThreadPoolExecutor threadPool) {
		this.threadPool = threadPool;
	}

	/**
	 * Sets the User-Agent header to be sent with each request. By default,
	 * "Android Asynchronous Http Client/VERSION (http://loopj.com/android-async-http/)"
	 * is used.
	 * 
	 * @param userAgent
	 *            the string to use in the User-Agent header.
	 */
	public void setUserAgent(String userAgent) {
		HttpProtocolParams.setUserAgent(this.httpClient.getParams(), userAgent);
	}

	/**
	 * Sets the connection time oout. By default, 10 seconds
	 * 
	 * @param timeout
	 *            the connect/socket timeout in milliseconds
	 */
	public void setTimeout(int timeout) {
		final HttpParams httpParams = this.httpClient.getParams();
		ConnManagerParams.setTimeout(httpParams, timeout);
		HttpConnectionParams.setSoTimeout(httpParams, timeout);
		HttpConnectionParams.setConnectionTimeout(httpParams, timeout);
	}

	/**
	 * Sets the SSLSocketFactory to user when making requests. By default, a
	 * new, default SSLSocketFactory is used.
	 * 
	 * @param sslSocketFactory
	 *            the socket factory to use for https requests.
	 */
	public void setSSLSocketFactory(SSLSocketFactory sslSocketFactory) {
		this.httpClient.getConnectionManager().getSchemeRegistry().register(new Scheme("https", sslSocketFactory, 443));
	}

	/**
	 * Sets headers that will be added to all requests this client makes (before
	 * sending).
	 * 
	 * @param header
	 *            the name of the header
	 * @param value
	 *            the contents of the header
	 */
	public void addHeader(String header, String value) {
		clientHeaderMap.put(header, value);
	}

	/**
	 * Sets basic authentication for the request. Uses AuthScope.ANY. This is
	 * the same as setBasicAuth('username','password',AuthScope.ANY)
	 * 
	 * @param username
	 * @param password
	 */
	public void setBasicAuth(String user, String pass) {
		AuthScope scope = AuthScope.ANY;
		setBasicAuth(user, pass, scope);
	}

	/**
	 * Sets basic authentication for the request. You should pass in your
	 * AuthScope for security. It should be like this
	 * setBasicAuth("username","password", new
	 * AuthScope("host",port,AuthScope.ANY_REALM))
	 * 
	 * @param username
	 * @param password
	 * @param scope
	 *            - an AuthScope object
	 * 
	 */
	public void setBasicAuth(String user, String pass, AuthScope scope) {
		UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(user, pass);
		this.httpClient.getCredentialsProvider().setCredentials(scope, credentials);
	}

	/**
	 * Cancels any pending (or potentially active) requests associated with the
	 * passed Context.
	 * <p>
	 * <b>Note:</b> This will only affect requests which were created with a
	 * non-null android Context. This method is intended to be used in the
	 * onDestroy method of your android activities to destroy all requests which
	 * are no longer required.
	 * 
	 * @param context
	 *            the android Context instance associated to the request.
	 * @param mayInterruptIfRunning
	 *            specifies if active requests should be cancelled along with
	 *            pending requests.
	 */
	public void cancelRequests(Context context, boolean mayInterruptIfRunning) {
		List<WeakReference<Future<?>>> requestList = requestMap.get(context);
		if (requestList != null) {
			for (WeakReference<Future<?>> requestRef : requestList) {
				Future<?> request = requestRef.get();
				if (request != null) {
					request.cancel(mayInterruptIfRunning);
				}
			}
		}
		requestMap.remove(context);
	}

	//
	// HTTP GET Requests
	//

	/**
	 * Perform a HTTP GET request, without any parameters.
	 * 
	 * @param url
	 *            the URL to send the request to.
	 * @param responseHandler
	 *            the response handler instance that should handle the response.
	 */
	public void get(String url, AsyncHttpResponseHandler responseHandler) {
		get(null, url, null, responseHandler);
	}

	/**
	 * Perform a HTTP GET request with parameters.
	 * 
	 * @param url
	 *            the URL to send the request to.
	 * @param params
	 *            additional GET parameters to send with the request.
	 * @param responseHandler
	 *            the response handler instance that should handle the response.
	 */
	public void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
		get(null, url, params, responseHandler);
	}

	/**
	 * Perform a HTTP GET request without any parameters and track the Android
	 * Context which initiated the request.
	 * 
	 * @param context
	 *            the Android Context which initiated the request.
	 * @param url
	 *            the URL to send the request to.
	 * @param responseHandler
	 *            the response handler instance that should handle the response.
	 */
	public void get(Context context, String url, AsyncHttpResponseHandler responseHandler) {
		get(context, url, null, responseHandler);
	}

	/**
	 * Perform a HTTP GET request and track the Android Context which initiated
	 * the request.
	 * 
	 * @param context
	 *            the Android Context which initiated the request.
	 * @param url
	 *            the URL to send the request to.
	 * @param params
	 *            additional GET parameters to send with the request.
	 * @param responseHandler
	 *            the response handler instance that should handle the response.
	 */
	public void get(Context context, String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
		sendRequest(httpClient, httpContext, new HttpGet(getUrlWithQueryString(url, params)), null, responseHandler,
				context);
	}

	/**
	 * Perform a HTTP GET request and track the Android Context which initiated
	 * the request with customized headers
	 * 
	 * @param url
	 *            the URL to send the request to.
	 * @param headers
	 *            set headers only for this request
	 * @param params
	 *            additional GET parameters to send with the request.
	 * @param responseHandler
	 *            the response handler instance that should handle the response.
	 */
	public void get(Context context, String url, Header[] headers, RequestParams params,
			AsyncHttpResponseHandler responseHandler) {
		HttpUriRequest request = new HttpGet(getUrlWithQueryString(url, params));
		if (headers != null)
			request.setHeaders(headers);
		sendRequest(httpClient, httpContext, request, null, responseHandler, context);
	}

	//
	// HTTP POST Requests
	//

	/**
	 * Perform a HTTP POST request, without any parameters.
	 * 
	 * @param url
	 *            the URL to send the request to.
	 * @param responseHandler
	 *            the response handler instance that should handle the response.
	 */
	public void post(String url, AsyncHttpResponseHandler responseHandler) {
		post(null, url, null, responseHandler);
	}

	/**
	 * Perform a HTTP POST request with parameters.
	 * 
	 * @param url
	 *            the URL to send the request to.
	 * @param params
	 *            additional POST parameters or files to send with the request.
	 * @param responseHandler
	 *            the response handler instance that should handle the response.
	 */
	public void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
		post(null, url, params, responseHandler);
	}

	/**
	 * Perform a HTTP POST request and track the Android Context which initiated
	 * the request.
	 * 
	 * @param context
	 *            the Android Context which initiated the request.
	 * @param url
	 *            the URL to send the request to.
	 * @param params
	 *            additional POST parameters or files to send with the request.
	 * @param responseHandler
	 *            the response handler instance that should handle the response.
	 */
	public void post(Context context, String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
		post(context, url, paramsToEntity(params), null, responseHandler);
	}

	/**
	 * Perform a HTTP POST request and track the Android Context which initiated
	 * the request.
	 * 
	 * @param context
	 *            the Android Context which initiated the request.
	 * @param url
	 *            the URL to send the request to.
	 * @param entity
	 *            a raw {@link HttpEntity} to send with the request, for
	 *            example, use this to send string/json/xml payloads to a server
	 *            by passing a {@link org.apache.http.entity.StringEntity}.
	 * @param contentType
	 *            the content type of the payload you are sending, for example
	 *            application/json if sending a json payload.
	 * @param responseHandler
	 *            the response handler instance that should handle the response.
	 */
	public void post(Context context, String url, HttpEntity entity, String contentType,
			AsyncHttpResponseHandler responseHandler) {
		sendRequest(httpClient, httpContext, addEntityToRequestBase(new HttpPost(url), entity), contentType,
				responseHandler, context);
	}

	/**
	 * Perform a HTTP POST request and track the Android Context which initiated
	 * the request. Set headers only for this request
	 * 
	 * @param context
	 *            the Android Context which initiated the request.
	 * @param url
	 *            the URL to send the request to.
	 * @param headers
	 *            set headers only for this request
	 * @param params
	 *            additional POST parameters to send with the request.
	 * @param contentType
	 *            the content type of the payload you are sending, for example
	 *            application/json if sending a json payload.
	 * @param responseHandler
	 *            the response handler instance that should handle the response.
	 */
	public void post(Context context, String url, Header[] headers, RequestParams params, String contentType,
			AsyncHttpResponseHandler responseHandler) {
		HttpEntityEnclosingRequestBase request = new HttpPost(url);
		if (params != null)
			request.setEntity(paramsToEntity(params));
		if (headers != null)
			request.setHeaders(headers);
		sendRequest(httpClient, httpContext, request, contentType, responseHandler, context);
	}

	/**
	 * Perform a HTTP POST request and track the Android Context which initiated
	 * the request. Set headers only for this request
	 * 
	 * @param context
	 *            the Android Context which initiated the request.
	 * @param url
	 *            the URL to send the request to.
	 * @param headers
	 *            set headers only for this request
	 * @param entity
	 *            a raw {@link HttpEntity} to send with the request, for
	 *            example, use this to send string/json/xml payloads to a server
	 *            by passing a {@link org.apache.http.entity.StringEntity}.
	 * @param contentType
	 *            the content type of the payload you are sending, for example
	 *            application/json if sending a json payload.
	 * @param responseHandler
	 *            the response handler instance that should handle the response.
	 */
	public void post(Context context, String url, Header[] headers, HttpEntity entity, String contentType,
			AsyncHttpResponseHandler responseHandler) {
		HttpEntityEnclosingRequestBase request = addEntityToRequestBase(new HttpPost(url), entity);
		if (headers != null)
			request.setHeaders(headers);
		sendRequest(httpClient, httpContext, request, contentType, responseHandler, context);
	}

	//
	// HTTP PUT Requests
	//

	/**
	 * Perform a HTTP PUT request, without any parameters.
	 * 
	 * @param url
	 *            the URL to send the request to.
	 * @param responseHandler
	 *            the response handler instance that should handle the response.
	 */
	public void put(String url, AsyncHttpResponseHandler responseHandler) {
		put(null, url, null, responseHandler);
	}

	/**
	 * Perform a HTTP PUT request with parameters.
	 * 
	 * @param url
	 *            the URL to send the request to.
	 * @param params
	 *            additional PUT parameters or files to send with the request.
	 * @param responseHandler
	 *            the response handler instance that should handle the response.
	 */
	public void put(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
		put(null, url, params, responseHandler);
	}

	/**
	 * Perform a HTTP PUT request and track the Android Context which initiated
	 * the request.
	 * 
	 * @param context
	 *            the Android Context which initiated the request.
	 * @param url
	 *            the URL to send the request to.
	 * @param params
	 *            additional PUT parameters or files to send with the request.
	 * @param responseHandler
	 *            the response handler instance that should handle the response.
	 */
	public void put(Context context, String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
		put(context, url, paramsToEntity(params), null, responseHandler);
	}

	/**
	 * Perform a HTTP PUT request and track the Android Context which initiated
	 * the request. And set one-time headers for the request
	 * 
	 * @param context
	 *            the Android Context which initiated the request.
	 * @param url
	 *            the URL to send the request to.
	 * @param entity
	 *            a raw {@link HttpEntity} to send with the request, for
	 *            example, use this to send string/json/xml payloads to a server
	 *            by passing a {@link org.apache.http.entity.StringEntity}.
	 * @param contentType
	 *            the content type of the payload you are sending, for example
	 *            application/json if sending a json payload.
	 * @param responseHandler
	 *            the response handler instance that should handle the response.
	 */
	public void put(Context context, String url, HttpEntity entity, String contentType,
			AsyncHttpResponseHandler responseHandler) {
		sendRequest(httpClient, httpContext, addEntityToRequestBase(new HttpPut(url), entity), contentType,
				responseHandler, context);
	}

	/**
	 * Perform a HTTP PUT request and track the Android Context which initiated
	 * the request. And set one-time headers for the request
	 * 
	 * @param context
	 *            the Android Context which initiated the request.
	 * @param url
	 *            the URL to send the request to.
	 * @param headers
	 *            set one-time headers for this request
	 * @param entity
	 *            a raw {@link HttpEntity} to send with the request, for
	 *            example, use this to send string/json/xml payloads to a server
	 *            by passing a {@link org.apache.http.entity.StringEntity}.
	 * @param contentType
	 *            the content type of the payload you are sending, for example
	 *            application/json if sending a json payload.
	 * @param responseHandler
	 *            the response handler instance that should handle the response.
	 */
	public void put(Context context, String url, Header[] headers, HttpEntity entity, String contentType,
			AsyncHttpResponseHandler responseHandler) {
		HttpEntityEnclosingRequestBase request = addEntityToRequestBase(new HttpPut(url), entity);
		if (headers != null)
			request.setHeaders(headers);
		sendRequest(httpClient, httpContext, request, contentType, responseHandler, context);
	}

	//
	// HTTP DELETE Requests
	//

	/**
	 * Perform a HTTP DELETE request.
	 * 
	 * @param url
	 *            the URL to send the request to.
	 * @param responseHandler
	 *            the response handler instance that should handle the response.
	 */
	public void delete(String url, AsyncHttpResponseHandler responseHandler) {
		delete(null, url, responseHandler);
	}

	/**
	 * Perform a HTTP DELETE request.
	 * 
	 * @param context
	 *            the Android Context which initiated the request.
	 * @param url
	 *            the URL to send the request to.
	 * @param responseHandler
	 *            the response handler instance that should handle the response.
	 */
	public void delete(Context context, String url, AsyncHttpResponseHandler responseHandler) {
		final HttpDelete delete = new HttpDelete(url);
		sendRequest(httpClient, httpContext, delete, null, responseHandler, context);
	}

	/**
	 * Perform a HTTP DELETE request.
	 * 
	 * @param context
	 *            the Android Context which initiated the request.
	 * @param url
	 *            the URL to send the request to.
	 * @param headers
	 *            set one-time headers for this request
	 * @param responseHandler
	 *            the response handler instance that should handle the response.
	 */
	public void delete(Context context, String url, Header[] headers, AsyncHttpResponseHandler responseHandler) {
		final HttpDelete delete = new HttpDelete(url);
		if (headers != null)
			delete.setHeaders(headers);
		sendRequest(httpClient, httpContext, delete, null, responseHandler, context);
	}

	// Private stuff
	protected void sendRequest(DefaultHttpClient client, HttpContext httpContext, HttpUriRequest uriRequest,
			String contentType, AsyncHttpResponseHandler responseHandler, Context context) {
		if (contentType != null) {
			uriRequest.addHeader("Content-Type", contentType);
		}

		Future<?> request = threadPool.submit(new AsyncHttpRequest(client, httpContext, uriRequest, responseHandler));

		if (context != null) {
			// Add request to request map
			List<WeakReference<Future<?>>> requestList = requestMap.get(context);
			if (requestList == null) {
				requestList = new LinkedList<WeakReference<Future<?>>>();
				requestMap.put(context, requestList);
			}

			requestList.add(new WeakReference<Future<?>>(request));

			// TODO: Remove dead weakrefs from requestLists?
		}
	}

	public static String getUrlWithQueryString(String url, RequestParams params) {
		if (params != null) {
			String paramString = params.getParamString();
			if (url.indexOf("?") == -1) {
				url += "?" + paramString;
			} else {
				url += "&" + paramString;
			}
		}

		return url;
	}

	private HttpEntity paramsToEntity(RequestParams params) {
		HttpEntity entity = null;

		if (params != null) {
			entity = params.getEntity();
		}

		return entity;
	}

	private HttpEntityEnclosingRequestBase addEntityToRequestBase(HttpEntityEnclosingRequestBase requestBase,
			HttpEntity entity) {
		if (entity != null) {
			requestBase.setEntity(entity);
		}

		return requestBase;
	}

	private static class InflatingEntity extends HttpEntityWrapper {
		public InflatingEntity(HttpEntity wrapped) {
			super(wrapped);
		}

		@Override
		public InputStream getContent() throws IOException {
			return new GZIPInputStream(wrappedEntity.getContent());
		}

		@Override
		public long getContentLength() {
			return -1;
		}
	}
}
