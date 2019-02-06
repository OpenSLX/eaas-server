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

package de.bwl.bwfla.metadata.oai.harvester;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.bwl.bwfla.metadata.oai.harvester.config.BackendConfig;


public class HarvesterDescription
{
	private BackendConfig config;
	private HarvesterStatus status;


	private HarvesterDescription()
	{
		// Default constructor
	}

	public HarvesterDescription(HarvesterBackend backend)
	{
		this.config = backend.getConfig();
		this.status = backend.getStatus();
	}

	@JsonProperty("config")
	public BackendConfig getBackendConfig()
	{
		return config;
	}

	public void setBackendConfig(BackendConfig config)
	{
		this.config = config;
	}

	@JsonProperty("status")
	public HarvesterStatus getStatus()
	{
		return status;
	}

	public void setStatus(HarvesterStatus status)
	{
		this.status = status;
	}
}
