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

package com.openslx.eaas.imagearchive.client.endpoint.v2.common;

import com.openslx.eaas.common.databind.Streamable;
import com.openslx.eaas.imagearchive.api.v2.common.FetchOptionsV2;
import com.openslx.eaas.imagearchive.api.v2.common.IManyReadable;
import de.bwl.bwfla.common.exceptions.BWFLAException;


public abstract class AbstractResourceRWM<T> extends AbstractResourceRW<T>
{
	protected final Class<T> clazz;


	// ===== IManyReadable API ==============================

	public Streamable<T> fetch() throws BWFLAException
	{
		return this.fetch((FetchOptionsV2) null);
	}

	public Streamable<T> fetch(FetchOptionsV2 options) throws BWFLAException
	{
		final var response = this.manyreadable()
				.fetch(options);

		return Streamable.of(response, clazz);
	}


	// ===== Internal Helpers ==============================

	protected AbstractResourceRWM(Class<T> clazz)
	{
		this.clazz = clazz;
	}

	protected abstract IManyReadable<T> manyreadable();
}
