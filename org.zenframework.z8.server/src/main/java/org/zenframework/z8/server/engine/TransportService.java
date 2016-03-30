package org.zenframework.z8.server.engine;

import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.ie.ExportMessages;
import org.zenframework.z8.server.ie.IeUtil;
import org.zenframework.z8.server.ie.Message;
import org.zenframework.z8.server.logs.Trace;

public class TransportService extends RmiServer implements ITransportService {

	private static final long serialVersionUID = 6031141331643514419L;

	private static final Log LOG = LogFactory.getLog(TransportService.class);

	private static TransportService INSTANCE;

	private final Collection<String> registered = Collections.synchronizedCollection(new LinkedList<String>());

	private TransportService(ServerConfig config) throws RemoteException {
		super(ITransportService.class);
	}

	public static void start(ServerConfig config) throws RemoteException {
		if (INSTANCE == null) {
			INSTANCE = new TransportService(config);
			INSTANCE.start();
		}
	}

	@Override
	public void start() throws RemoteException {
		super.start();
		Trace.logEvent("TS: transport server started at '" + getUrl() + "'");
	}

	@Override
	public void checkRegistration(String selfAddress) throws RemoteException {
		if (!registered.contains(selfAddress)) {
			registered.add(selfAddress);
			new Registrator(selfAddress).start();
		}
	}

	@Override
	public void sendMessage(Message message) throws RemoteException {
		String url = getUrl();
		try {
			URI uri = new URI(url);
			url = IeUtil.getUrl(uri.getScheme(), uri.getHost() + ':' + uri.getPort());
		} catch (URISyntaxException e) {
			LOG.debug("Can't parse URI '" + url + "'", e);
		}
		try {
			new ExportMessages.CLASS<ExportMessages>().get().addMessage(message, url);
		} catch (Throwable e) {
			throw new RemoteException("Can't import message '" + message.getId() + "' from '" + message.getSender() + "'", e);
		}
	}

	private static class Registrator extends Thread {

		final String address;

		Registrator(String address) {
			super("TransportRegistrator-" + address);
			this.address = address;
			setDaemon(true);
		}

		@Override
		public void run() {
			while (true) {
				try {
					Rmi.get(ITransportCenter.class, Z8Context.getConfig().getTransportCenterHost(),
							Z8Context.getConfig().getTransportCenterPort()).registerTransportServer(address,
							Z8Context.getConfig().getRmiRegistryPort());
					break;
				} catch (Exception e) {
					LOG.debug("Can't register transport server for '" + address + "'. Retrying...", e);
					try {
						Thread.sleep(10000);
					} catch (InterruptedException e1) {
						LOG.error("Can't register transport server for '" + address + "'. Thread interrupted", e1);
						break;
					}
				}
			}
		}

	}

}
