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

package com.openslx.eaas.imagearchive.api.v2.common;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.services.security.SecuredInternal;
import org.jboss.resteasy.annotations.Form;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;


public interface IReadable<T>
{
	@GET
	@SecuredInternal
	@Path("/{id}/url")
	@Produces(MediaType.APPLICATION_JSON)
	String resolve(@PathParam("id") String id, @Form ResolveOptionsV2 options) throws BWFLAException;

	@GET
	@Path("/{id}")
	@SecuredInternal
	@Produces({
			MediaType.APPLICATION_JSON,
			MediaType.APPLICATION_OCTET_STREAM
	})
	T fetch(@PathParam("id") String id) throws BWFLAException;
}
