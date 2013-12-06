package com.loopj.android.http;

import java.util.concurrent.TimeUnit;

import org.apache.http.conn.ClientConnectionManager;

/**
 * 连接收回策略 <br>
 * <br>
 * 一个经典的阻塞I/O模型的主要缺点是网络套接字仅当I/O操作阻塞时才可以响应I/O事件。当一个连接被释放返回管理器时，
 * 它可以被保持活动状态而却不能监控套接字的状态和响应任何I
 * /O事件。如果连接在服务器端关闭，那么客户端连接也不能去侦测连接状态中的变化和关闭本端的套接字去作出适当响应。 <br>
 * HttpClient通过测试连接是否是过时的来尝试去减轻这个问题，这已经不再有效了，因为它已经在服务器端关闭了，之前使用执行HTTP请求的连接。
 * 过时的连接检查也并不是100%的稳定，反而对每次请求执行还要增加10到30毫秒的开销。唯一可行的而不涉及到每个对空闲连接的套接字模型线程解决方案，
 * 是使用专用的监控线程来收回因为长时间不活动而被认为是过期的连接
 * 。监控线程可以周期地调用ClientConnectionManager#closeExpiredConnections
 * ()方法来关闭所有过期的连接，从连接池中收回关闭的连接
 * 。它也可以选择性调用ClientConnectionManager#closeIdleConnections
 * ()方法来关闭所有已经空闲超过给定时间周期的连接。 <br>
 * 
 * @author chenjianli
 * 
 */
public class ConnectionRecycleSchemeThread extends Thread {

	private final ClientConnectionManager connMgr;
	private volatile boolean shutdown;

	public ConnectionRecycleSchemeThread(ClientConnectionManager connMgr) {
		super();
		this.connMgr = connMgr;
	}

	@Override
	public void run() {
		try {
			while (!shutdown) {
				synchronized (this) {
					wait(5000);
					// 关闭过期连接
					connMgr.closeExpiredConnections();
					// 可选地，关闭空闲超过30秒的连接
					connMgr.closeIdleConnections(30, TimeUnit.SECONDS);
				}
			}
		} catch (InterruptedException ex) {
			// 终止
		}
	}

	public void shutdown() {
		shutdown = true;
		synchronized (this) {
			notifyAll();
		}
	}
}
