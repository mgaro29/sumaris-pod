package net.sumaris.core.extraction.dao;

/*-
 * #%L
 * Quadrige3 Core :: Client API
 * %%
 * Copyright (C) 2017 - 2018 Ifremer
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import com.google.common.base.Preconditions;
import net.sumaris.core.config.SumarisConfiguration;
import net.sumaris.core.dao.referential.ReferentialDao;
import net.sumaris.core.dao.technical.hibernate.HibernateDaoSupport;
import net.sumaris.core.exception.DataNotFoundException;
import net.sumaris.core.model.referential.IItemReferentialEntity;
import net.sumaris.core.model.referential.location.LocationLevel;
import net.sumaris.core.service.referential.ReferentialService;
import net.sumaris.core.vo.referential.ReferentialVO;
import org.nuiton.i18n.I18n;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataRetrievalFailureException;

import javax.persistence.Query;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Benoit Lavenier <benoit.lavenier@e-is.pro>
 */
public abstract class ExtractionBaseDaoImpl extends HibernateDaoSupport {

    private static final Logger log = LoggerFactory.getLogger(ExtractionBaseDaoImpl.class);

    protected static final String TABLE_NAME_PREFIX = "EXT_";

    @Autowired
    protected SumarisConfiguration configuration;

    @Autowired
    protected ReferentialService referentialService;

    @Autowired
    public ExtractionBaseDaoImpl() {
        super();
    }

    @SuppressWarnings("unchecked")
    protected <R> List<R> query(String query, Class<R> jdbcClass) {
        Query nativeQuery = getEntityManager().createNativeQuery(query);
        Stream<R> resultStream = (Stream<R>) nativeQuery.getResultStream().map(jdbcClass::cast);
        return resultStream.collect(Collectors.toList());
    }

    protected <R> List<R> query(String query, Function<Object[], R> rowMapper) {
        Query nativeQuery = getEntityManager().createNativeQuery(query);
        Stream<Object[]> resultStream = (Stream<Object[]>) nativeQuery.getResultStream();
        return resultStream.map(rowMapper).collect(Collectors.toList());
    }


    protected int queryUpdate(String query) {
        if (log.isDebugEnabled()) log.debug("execute: " + query);
        Query nativeQuery = getEntityManager().createNativeQuery(query);
        return nativeQuery.executeUpdate();
    }

    protected long queryCount(String query) {
        if (log.isDebugEnabled()) log.debug("execute: " + query);
        Query nativeQuery = getEntityManager().createNativeQuery(query);
        Object result = nativeQuery.getSingleResult();
        if (result == null)
            throw new DataRetrievalFailureException(String.format("query count result is null.\nquery: %s", query));
        if (result instanceof Number) {
            return ((Number) result).longValue();
        } else {
            throw new DataRetrievalFailureException(String.format("query count result is not a number: %s \nquery: %s", result, query));
        }
    }

    protected Integer getReferentialIdByUniqueLabel(Class<? extends IItemReferentialEntity> entityClass, String label) {
        return referentialService.getIdByUniqueLabel(entityClass, label);
    }
}