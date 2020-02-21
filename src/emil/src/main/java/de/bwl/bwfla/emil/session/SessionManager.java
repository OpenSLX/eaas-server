/*
 * This file is part of the Emulation-as-a-Service framework.
 *
 * The Emulation-as-a-Service framework is free software: you can
 * redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * The Emulation-as-a-Service framework is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with the Emulation-as-a-Software framework.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package de.bwl.bwfla.emil.session;

import de.bwl.bwfla.api.eaas.ComponentGroupElement;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.eaas.client.ComponentGroupClient;
import de.bwl.bwfla.emil.Components;
import de.bwl.bwfla.emil.datatypes.NetworkRequest;
import de.bwl.bwfla.emil.session.rest.DetachRequest;
import de.bwl.bwfla.emucomp.client.ComponentClient;
import org.apache.tamaya.inject.api.Config;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.NotFoundException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.stream.Collector;
import java.util.stream.Collectors;


@ApplicationScoped
public class SessionManager
{
	@Inject
	private ComponentClient componentClient;

	@Inject
	private ComponentGroupClient groupClient;

	@Inject
	private Components components;

	@Inject
	@Config("components.client_timeout")
	protected Duration sessionExpirationTimeout;

	@Inject
	@Config(value = "ws.eaasgw")
	private String eaasGw;

	private final Logger log = Logger.getLogger(this.getClass().getName());

	private final Map<String, Session> sessions;

	public SessionManager()
	{
		this.sessions = new ConcurrentHashMap<String, Session>();
	}

	public NetworkSession createNetworkSession(String switchId, NetworkRequest request) throws BWFLAException {
		final String id = UUID.randomUUID().toString();
		NetworkSession session = new NetworkSession(id, groupClient.getComponentGroupPort(eaasGw).createGroup(), switchId, request);
		addComponent(session, switchId);
		register(session);
		return session;
	}

	public Session createSession() throws BWFLAException {
		final String id = UUID.randomUUID().toString();
		Session session = new Session(id, groupClient.getComponentGroupPort(eaasGw).createGroup());
		register(session);
		return session;
	}

	public void addComponent(Session session, String componentId) throws BWFLAException {
		groupClient.getComponentGroupPort(eaasGw).add(session.groupId(), componentId);
	}

	public void updateComponent(Session session, ComponentGroupElement component) throws BWFLAException {
		groupClient.getComponentGroupPort(eaasGw).update(session.groupId(), component);
	}

	public void removeComponent(Session session, String componentId) throws BWFLAException {
		groupClient.getComponentGroupPort(eaasGw).remove(session.groupId(), componentId);
	}

	public List<ComponentGroupElement> getComponents(Session session) throws BWFLAException {
		return groupClient.getComponentGroupPort(eaasGw).list(session.groupId());
	}

	public void keepAlive(Session session, Logger log)
	{
		try {
			List<ComponentGroupElement> componentList = getComponents(session);
			for(ComponentGroupElement c : componentList)
			{
				if(!components.keepalive(c.getComponentId(), true))
					componentClient.getComponentPort(eaasGw).keepalive(c.getComponentId());
			}
			session.update();
		} catch (BWFLAException e) {
			// session.setFailed();
			// if(log != null)
			// 	log.severe("keepalive failed for group " + session.groupId() + " setting status to failed");
		}
	}

	/** Registers a new session */
	public void register(Session session)
	{
		sessions.put(session.id(), session);
		log.info("Session '" + session.id() + "' registered");
	}

	/** Unregisters a session */
	public void unregister(String id)
	{
		sessions.remove(id);
		log.info("Session '" + id + "' unregistered");
	}

	/** Returns session */
	public Session get(String id)
	{
		return sessions.get(id);
	}

	/** Returns a list of all session IDs */
	public Collection<Session> list()
	{
		return sessions.entrySet().stream().map(e -> e.getValue()).filter(s -> s.isDetached()).collect(Collectors.toList());
	}

	/** Updates session's lifetime */
	public void setLifetime(String id, long lifetime, TimeUnit unit, String name)
	{
		setLifetime(id, lifetime, unit, name, null);
	}

	/** Updates session's lifetime */
	public void setLifetime(String id, long lifetime, TimeUnit unit, String name, DetachRequest.ComponentTitleCreator componentTitle)
	{
		if (componentTitle != null && componentTitle.getComponentName() != null) {
			ComponentGroupElement groupElement = new ComponentGroupElement();
			groupElement.setComponentId(componentTitle.getComponentId());
			groupElement.setCustomName(componentTitle.getComponentName());
			try {
				Session s = sessions.get(id);
				if(s == null)
					throw new BWFLAException("Session not found " + id);
				updateComponent(sessions.get(id), groupElement);
			} catch (BWFLAException e) {
				log.severe(e.getMessage());
			}
		}

		try {
			List <ComponentGroupElement> sessionsList = getComponents(sessions.get(id));
			for (int i = 0; i < sessionsList.size(); i++) {
			}
		} catch (BWFLAException e) {
			e.printStackTrace();
		}

		sessions.computeIfPresent(id, (unused, session) -> {
			session.setName(name);
			if (lifetime < 0) {
				session.setExpirationTimestamp(-1);
			}
			else {
				final long timestamp = SessionManager.timems() + unit.toMillis(lifetime);
				session.setExpirationTimestamp(timestamp);
			}
			return session;
		});
	}

	/** Runs internal keepalives calls for detached session's resources */
	void update(ExecutorService executor)
	{
		final List<String> idsToRemove = new ArrayList<String>();
		final long curtime = SessionManager.timems();
		sessions.forEach((id, session) -> {
//			if(session.isFailed())
//			{
//				idsToRemove.add(id);
//				return;
//			}
			if(session.isDetached()) {
				if (session.getExpirationTimestamp() > 0 && curtime > session.getExpirationTimestamp()) {
					idsToRemove.add(id);
					return;
				}
				executor.execute(new SessionKeepAliveTask(session, log));
			}
			else
			{
				if (session.getLastUpdate() + (2L * sessionExpirationTimeout.toMillis()) < curtime)
				{
					idsToRemove.add(id);
					return;
				}
			}
		});

		idsToRemove.forEach((id) -> {
			log.info("Session '" + id + "' expired!");
			this.unregister(id);
		});
	}

	public static long timems()
	{
		return System.currentTimeMillis();
	}

	public void remove(String id, List<String> resources) {
		Session session = get(id);
		for(String componentId : resources)
		{
			log.info("deleting component id: " + componentId + "from session " + id);
			try {
				removeComponent(session, componentId);
			} catch (BWFLAException e) {
				e.printStackTrace();
			}
			try {
				components.releaseComponent(componentId);
			}
			catch (NotFoundException ignore) { }
		}

		try {
			List<ComponentGroupElement> sessionsList = getComponents(sessions.get(id));
			for (ComponentGroupElement element : sessionsList) {
				String cId = element.getComponentId();
				if(components.hasComponentSession(cId))
					return;
			}
			log.info("deleting session " + id + " looks empty... " + sessionsList.size());
			unregister(id);
		} catch (BWFLAException e)
		{
			e.printStackTrace();
		}
	}

	private class SessionKeepAliveTask implements Runnable
	{
		private final Session session;
		private final Logger log;

		public SessionKeepAliveTask(Session session, Logger log)
		{
			this.session = session;
			this.log = log;
		}

		@Override
		public void run()
		{
			keepAlive(session, log);
		}
	}
}
