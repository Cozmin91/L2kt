package com.l2kt.loginserver.network;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.security.interfaces.RSAPrivateKey;
import java.util.logging.Logger;

import com.l2kt.commons.mmocore.MMOClient;
import com.l2kt.commons.mmocore.MMOConnection;
import com.l2kt.commons.mmocore.SendablePacket;
import com.l2kt.commons.random.Rnd;
import com.l2kt.loginserver.LoginController;
import com.l2kt.loginserver.crypt.LoginCrypt;
import com.l2kt.loginserver.crypt.ScrambledKeyPair;

import com.l2kt.loginserver.network.serverpackets.L2LoginServerPacket;
import com.l2kt.loginserver.network.serverpackets.LoginFail;
import com.l2kt.loginserver.network.serverpackets.PlayFail;

/**
 * Represents a client connected into the LoginServer
 */
public final class LoginClient extends MMOClient<MMOConnection<LoginClient>>
{
	private static Logger _log = Logger.getLogger(LoginClient.class.getName());
	
	public static enum LoginClientState
	{
		CONNECTED,
		AUTHED_GG,
		AUTHED_LOGIN
	}
	
	private LoginClientState _state;
	
	private final LoginCrypt _loginCrypt;
	private final ScrambledKeyPair _scrambledPair;
	private final byte[] _blowfishKey;
	
	private String _account;
	private int _accessLevel;
	private int _lastServer;
	private SessionKey _sessionKey;
	private final int _sessionId;
	private boolean _joinedGS;
	
	private final long _connectionStartTime;
	
	public LoginClient(MMOConnection<LoginClient> con)
	{
		super(con);
		
		_state = LoginClientState.CONNECTED;
		_scrambledPair = LoginController.INSTANCE.getScrambledRSAKeyPair();
		_blowfishKey = LoginController.INSTANCE.getBlowfishKey();
		_sessionId = Rnd.nextInt();
		_connectionStartTime = System.currentTimeMillis();
		_loginCrypt = new LoginCrypt();
		_loginCrypt.setKey(_blowfishKey);
	}
	
	@Override
	public boolean decrypt(ByteBuffer buf, int size)
	{
		try
		{
			if (!_loginCrypt.decrypt(buf.array(), buf.position(), size))
			{
				_log.warning("Wrong checksum from client: " + toString());
				super.getConnection().close((SendablePacket<LoginClient>) null);
				return false;
			}
			return true;
		}
		catch (IOException e)
		{
			e.printStackTrace();
			super.getConnection().close((SendablePacket<LoginClient>) null);
			return false;
		}
	}
	
	@Override
	public boolean encrypt(ByteBuffer buf, int size)
	{
		final int offset = buf.position();
		try
		{
			size = _loginCrypt.encrypt(buf.array(), offset, size);
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return false;
		}
		
		buf.position(offset + size);
		return true;
	}
	
	public LoginClientState getState()
	{
		return _state;
	}
	
	public void setState(LoginClientState state)
	{
		_state = state;
	}
	
	public byte[] getBlowfishKey()
	{
		return _blowfishKey;
	}
	
	public byte[] getScrambledModulus()
	{
		return _scrambledPair.getScrambledModulus();
	}
	
	public RSAPrivateKey getRSAPrivateKey()
	{
		return (RSAPrivateKey) _scrambledPair.getKeyPair().getPrivate();
	}
	
	public String getAccount()
	{
		return _account;
	}
	
	public void setAccount(String account)
	{
		_account = account;
	}
	
	public void setAccessLevel(int accessLevel)
	{
		_accessLevel = accessLevel;
	}
	
	public int getAccessLevel()
	{
		return _accessLevel;
	}
	
	public void setLastServer(int lastServer)
	{
		_lastServer = lastServer;
	}
	
	public int getLastServer()
	{
		return _lastServer;
	}
	
	public int getSessionId()
	{
		return _sessionId;
	}
	
	public boolean hasJoinedGS()
	{
		return _joinedGS;
	}
	
	public void setJoinedGS(boolean val)
	{
		_joinedGS = val;
	}
	
	public void setSessionKey(SessionKey sessionKey)
	{
		_sessionKey = sessionKey;
	}
	
	public SessionKey getSessionKey()
	{
		return _sessionKey;
	}
	
	public long getConnectionStartTime()
	{
		return _connectionStartTime;
	}
	
	public void sendPacket(L2LoginServerPacket lsp)
	{
		getConnection().sendPacket(lsp);
	}
	
	public void close(LoginFail reason)
	{
		getConnection().close(reason);
	}
	
	public void close(PlayFail reason)
	{
		getConnection().close(reason);
	}
	
	public void close(L2LoginServerPacket lsp)
	{
		getConnection().close(lsp);
	}
	
	@Override
	public void onDisconnection()
	{
		if (!hasJoinedGS() || (getConnectionStartTime() + LoginController.LOGIN_TIMEOUT) < System.currentTimeMillis())
			LoginController.INSTANCE.removeAuthedLoginClient(getAccount());
	}
	
	@Override
	public String toString()
	{
		final InetAddress address = getConnection().getInetAddress();
		if (getState() == LoginClientState.AUTHED_LOGIN)
			return "[" + getAccount() + " (" + (address == null ? "disconnected" : address.getHostAddress()) + ")]";
		
		return "[" + (address == null ? "disconnected" : address.getHostAddress()) + "]";
	}
	
	@Override
	protected void onForcedDisconnection()
	{
	}
}