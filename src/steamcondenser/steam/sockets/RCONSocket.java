/** 
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the new BSD License.
 */

package steamcondenser.steam.sockets;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.TimeoutException;

import steamcondenser.SteamCondenserException;
import steamcondenser.steam.packets.rcon.RCONPacket;
import steamcondenser.steam.packets.rcon.RCONPacketFactory;

public class RCONSocket extends SteamSocket
{
    public RCONSocket(InetAddress ipAddress, int portNumber)
    throws IOException
    {
	super(ipAddress, portNumber);
	this.channel = SocketChannel.open();
    }

    public void send(RCONPacket dataPacket)
    throws IOException
    {
	if(!((SocketChannel) this.channel).isConnected())
	{
	    ((SocketChannel) this.channel).connect(this.remoteSocket);
	    this.channel.configureBlocking(false);
	}

	this.buffer = ByteBuffer.wrap(dataPacket.getBytes());
	((SocketChannel) this.channel).write(this.buffer);
    }

    public RCONPacket getReply()
    throws IOException, TimeoutException, SteamCondenserException
    {
	this.receivePacket(1440);
	String packetData = new String(this.buffer.array()).substring(0, this.buffer.limit());
	int packetSize = (int) this.buffer.getLong() + 4;
	
	if(packetSize > 1440)
	{
	    int remainingBytes = packetSize - 1440;
	    do
	    {
		if(remainingBytes < 1440)
		{
		    this.receivePacket(remainingBytes);
		}
		else
		{
		    this.receivePacket(1440);
		}
		packetData += new String(this.buffer.array()).substring(0, this.buffer.limit());
		remainingBytes -= this.buffer.limit();
	    }
	    while(remainingBytes > 0);
	}
	
	return RCONPacketFactory.getPacketFromData(packetData.getBytes());
    }
}
