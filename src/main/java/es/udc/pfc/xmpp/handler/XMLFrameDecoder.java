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

package es.udc.pfc.xmpp.handler;

import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.events.XMLEvent;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.FrameDecoder;

import com.fasterxml.aalto.AsyncXMLInputFactory;
import com.fasterxml.aalto.AsyncXMLStreamReader;
import com.fasterxml.aalto.evt.EventAllocatorImpl;
import com.fasterxml.aalto.stax.InputFactoryImpl;

/**
 * Decodes an XML stream into XML Events.
 */
public class XMLFrameDecoder extends FrameDecoder {

	private static final AsyncXMLInputFactory factory;
	private static final EventAllocatorImpl allocator;

	static {
		factory = new InputFactoryImpl();
		factory.setProperty(AsyncXMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
		allocator = EventAllocatorImpl.getDefaultInstance();
	}

	private final AsyncXMLStreamReader reader;

	public XMLFrameDecoder() {
		super(true);

		reader = factory.createAsyncXMLStreamReader();
	}

	@Override
	protected Object decode(ChannelHandlerContext ctx, Channel channel, ChannelBuffer buffer) throws Exception {
		final List<XMLEvent> result = new ArrayList<XMLEvent>();

		byte[] chunk = new byte[buffer.readableBytes()];
		buffer.readBytes(chunk);
		
		reader.getInputFeeder().feedInput(chunk, 0, chunk.length);

		while (reader.hasNext() && reader.next() != AsyncXMLStreamReader.EVENT_INCOMPLETE) {
			result.add(allocator.allocate(reader));
		}

		return result;
	}

}
