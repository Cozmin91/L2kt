package com.l2kt.commons.mmocore;

public interface IClientFactory<T extends MMOClient<?>>
{
	public T create(final MMOConnection<T> con);
}