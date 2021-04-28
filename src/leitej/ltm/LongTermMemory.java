/*******************************************************************************
 * Copyright Julio Leite
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as
 * published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

package leitej.ltm;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import leitej.exception.ClosedLtRtException;
import leitej.exception.IllegalArgumentLtRtException;
import leitej.exception.LtmLtRtException;
import leitej.exception.ObjectPoolLtException;
import leitej.log.Logger;
import leitej.util.data.AbstractDataProxy;
import leitej.util.data.Cache;
import leitej.util.data.CacheSoft;

/**
 * @author Julio Leite
 *
 */
public final class LongTermMemory extends AbstractDataProxy<LtmObjectModelling, DataProxyHandler> {

	private static final long serialVersionUID = -2286855060098042062L;

	private static final Logger LOG = Logger.getInstance();

	private static final Map<Class<?>, Cache<Long, LtmObjectModelling>> CACHE = new HashMap<>();
	private static final LongTermMemory INSTANCE = new LongTermMemory();

	public static LongTermMemory getInstance() {
		return INSTANCE;
	}

	private static final <T extends LtmObjectModelling> Cache<Long, LtmObjectModelling> cache(final Class<T> ltmClass) {
		Cache<Long, LtmObjectModelling> cache;
		synchronized (CACHE) {
			cache = CACHE.get(ltmClass);
			if (cache == null) {
				cache = new CacheSoft<>();
				CACHE.put(ltmClass, cache);
			}
		}
		return cache;
	}

	private static final <T extends LtmObjectModelling> T cacheGet(final Class<T> ltmClass, final long id) {
		return ltmClass.cast(cache(ltmClass).get(id));
	}

	private static final <T extends LtmObjectModelling> void cachePut(final Class<T> ltmClass, final T ltm) {
		cache(ltmClass).set(ltm.getId(), ltm);
	}

	private static final <T extends LtmObjectModelling> void cacheDel(final Class<T> ltmClass, final long id) {
		cache(ltmClass).remove(id);
	}

	public static void erase() throws LtmLtRtException {
		LOG.warn("Erasing long term memory");
		synchronized (CACHE) {
			try {
				DataProxyHandler.eraseAll();
				for (final Cache<Long, LtmObjectModelling> cache : CACHE.values()) {
					cache.clear();
				}
			} catch (ClosedLtRtException | ObjectPoolLtException | SQLException | InterruptedException e) {
				throw new LtmLtRtException(e);
			}
		}
	}

	private LongTermMemory() {
		super(Serializable.class);
		LOG.debug("new instance");
	}

	public <T extends LtmObjectModelling> T newRecord(final Class<T> ltmClass) throws LtmLtRtException {
		try {
			synchronized (ltmClass) {
				final T ltm = newProxyInstance(ltmClass, new DataProxyHandler(ltmClass));
				cachePut(ltmClass, ltm);
				return ltm;
			}
		} catch (IllegalArgumentLtRtException | ClosedLtRtException | ObjectPoolLtException | InterruptedException
				| SQLException e) {
			throw new LtmLtRtException(e);
		}
	}

	public <T extends LtmObjectModelling> T fetch(final Class<T> ltmClass, final long id) throws LtmLtRtException {
		try {
			synchronized (ltmClass) {
				T ltm = cacheGet(ltmClass, id);
				if (ltm == null) {
					ltm = newProxyInstance(ltmClass, new DataProxyHandler(ltmClass, id));
					cachePut(ltmClass, ltm);
				}
				return ltm;
			}
		} catch (IllegalArgumentLtRtException | ClosedLtRtException | ObjectPoolLtException | InterruptedException
				| SQLException e) {
			throw new LtmLtRtException(e);
		}
	}

	public <T extends LtmObjectModelling> void forgets(final T record) throws LtmLtRtException {
		final DataProxyHandler dph = INSTANCE.getInvocationHandler(record);
		final Class<T> ltmClass = dph.getInterface();
		final long id = record.getId();
		try {
			synchronized (ltmClass) {
				DataProxyHandler.delete(dph.getPreparedClass(), id);
				cacheDel(ltmClass, id);
			}
		} catch (ClosedLtRtException | ObjectPoolLtException | IllegalArgumentException | InterruptedException
				| SQLException e) {
			throw new LtmLtRtException(e);
		}
	}

	public <T extends LtmObjectModelling> Iterator<T> search(final LtmFilter<T> ltmFilter) throws LtmLtRtException {
		return new LtmIteractor<>(ltmFilter.getLTMClass(), ltmFilter.getQueryFilter(), ltmFilter.getParams(),
				ltmFilter.getTypes());
	}

}
