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

/*
 Some of the retry logic in this class is heavily borrowed from the
 fantastic droid-fu project: https://github.com/donnfelker/droid-fu
 */

package com.loopj.android.http;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Iterator;

import javax.net.ssl.SSLException;

import org.apache.http.NoHttpResponseException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;

import android.os.SystemClock;

class RetryHandler implements HttpRequestRetryHandler {

	private static final int RETRY_SLEEP_TIME_MILLIS = 1500;
	// 异常白名单
	private static HashSet<Class<?>> exceptionWhitelist = new HashSet<Class<?>>();
	// 异常黑名单
	private static HashSet<Class<?>> exceptionBlacklist = new HashSet<Class<?>>();

	static {
		// *** 异常白名单 ***
		// Retry if the server dropped connection on us
		exceptionWhitelist.add(NoHttpResponseException.class);// NoHttpResponseException：如果服务器丢掉了连接，那么就重试
		// retry-this, since it may happens as part of a Wi-Fi to 3G failover
		exceptionWhitelist.add(UnknownHostException.class);// UnknownHostException有可能发生在网络由Wifi切换到3G时的网络故障
		// retry-this, since it may happens as part of a Wi-Fi to 3G failover
		exceptionWhitelist.add(SocketException.class);// SocketException有可能发生在网络由Wifi切换到3G时的网络故障

		// *** 异常黑名单：永远不要重试的异常 ***
		// never retry timeouts
		exceptionBlacklist.add(InterruptedIOException.class);
		// never retry SSL handshake failures
		exceptionBlacklist.add(SSLException.class);// 不要重试SSL握手异常
	}

	private final int maxRetries;

	public RetryHandler(int maxRetries) {
		this.maxRetries = maxRetries;
	}

	@Override
	public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
		boolean retry = true;

		Boolean b = (Boolean) context.getAttribute(ExecutionContext.HTTP_REQ_SENT);
		boolean sent = (b != null && b.booleanValue());

		// 这个必须为首要条件，注意if-else的顺序不可颠倒
		if (executionCount > maxRetries) {
			// Do not retry if over max retry count
			// 已经超过了最多重试次数，那么就不要继续了
			retry = false;
		} else if (isInList(exceptionBlacklist, exception)) {// 检测异常是否被列入黑名单，如果被异常被列入则取消重试
			// immediately cancel retry if the error is blacklisted(被列入黑名单)
			retry = false;
		} else if (isInList(exceptionWhitelist, exception)) {// 白名单内的异常可重试
			// immediately retry if error is whitelisted
			retry = true;
		} else if (!sent) {
			// 对于大多数的异常，只有当请求还没有被完全发送出去的时候才继续重试
			// for most other errors, retry only if request hasn't been fully
			// sent yet
			retry = true;
		}

		if (retry) {
			// resend all idempotent(幂等) requests 如果请求被被认为是幂等的，那么就重试
			HttpUriRequest currentReq = (HttpUriRequest) context.getAttribute(ExecutionContext.HTTP_REQUEST);
			String requestType = currentReq.getMethod();
			retry = !requestType.equals("POST");
			// TODO 另外的一个处理方式为（摘自网上）
			// retry = !(currentReq instanceof HttpEntityEnclosingRequest);
		}

		if (retry) {
			SystemClock.sleep(RETRY_SLEEP_TIME_MILLIS);
		} else {
			exception.printStackTrace();
		}

		return retry;
	}

	protected boolean isInList(HashSet<Class<?>> list, Throwable error) {
		Iterator<Class<?>> itr = list.iterator();
		while (itr.hasNext()) {
			if (itr.next().isInstance(error)) {
				return true;
			}
		}
		return false;
	}
}