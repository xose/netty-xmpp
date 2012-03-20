/**
 * Copyright 2012 José Martínez
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package es.udc.pfc.xmpp.component;

import static com.google.common.base.Preconditions.checkNotNull;

import java.net.SocketAddress;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.execution.ExecutionHandler;
import org.jboss.netty.handler.execution.OrderedMemoryAwareThreadPoolExecutor;
import org.jboss.netty.util.CharsetUtil;

import com.google.common.util.concurrent.AbstractExecutionThreadService;

import es.udc.pfc.xmpp.handler.XEP0114Decoder;
import es.udc.pfc.xmpp.handler.XMLElementDecoder;
import es.udc.pfc.xmpp.handler.XMLFrameDecoder;
import es.udc.pfc.xmpp.handler.XMPPStreamHandler;
import es.udc.pfc.xmpp.stanza.JID;

public class ComponentService extends AbstractExecutionThreadService {

	private final XMPPComponent component;
	private final SocketAddress serverAddress;
	private final String xmppHost;
	private final String xmppSecret;

	private ExecutionHandler executionHandler;
	private ClientBootstrap bootstrap;
	private Channel channel;

	public ComponentService(XMPPComponent component, SocketAddress serverAddress, String xmppHost, String xmppSecret) {
		this.component = checkNotNull(component);
		this.serverAddress = checkNotNull(serverAddress);
		this.xmppHost = checkNotNull(xmppHost);
		this.xmppSecret = checkNotNull(xmppSecret);
	}

	@Override
	protected void startUp() throws Exception {
		executionHandler = new ExecutionHandler(new OrderedMemoryAwareThreadPoolExecutor(4, 0, 0));
		bootstrap = new ClientBootstrap(new NioClientSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool()));
		bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
			@Override
			public ChannelPipeline getPipeline() throws Exception {
				final ChannelPipeline pipeline = Channels.pipeline();

				//pipeline.addLast("logger", new LoggingHandler(InternalLogLevel.INFO));
				pipeline.addLast("xmlFramer", new XMLFrameDecoder());
				pipeline.addLast("xmlDecoder", new XMLElementDecoder());
				pipeline.addLast("xmppDecoder", new XEP0114Decoder(xmppHost, xmppSecret));
				pipeline.addLast("executor", executionHandler);
				pipeline.addLast("xmppHandler", new XMPPStreamHandler(component));
				return pipeline;
			}
		});

		final ChannelFuture future = bootstrap.connect(serverAddress).await();
		if (!future.isSuccess()) {
			bootstrap.releaseExternalResources();
			executionHandler.releaseExternalResources();
			future.rethrowIfFailed();
		}
		channel = future.getChannel();
		component.init(channel, JID.jid("localhost"), JID.jid(xmppHost)); // FIXME
	}

	@Override
	protected void run() throws Exception {
		channel.getCloseFuture().awaitUninterruptibly().rethrowIfFailed();
	}

	@Override
	protected void shutDown() throws Exception {
		bootstrap.releaseExternalResources();
		executionHandler.releaseExternalResources();
	}

	@Override
	protected void triggerShutdown() {
		if (channel != null && channel.isConnected()) {
			Channels.disconnect(channel);
		}
	}

	public final void send(final String data) {
		checkNotNull(data);
		if (channel != null && channel.isConnected()) {
			Channels.write(channel, ChannelBuffers.copiedBuffer(data, CharsetUtil.UTF_8));
		}
	}

}
