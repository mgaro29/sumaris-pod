package net.sumaris.core.dao.referential;

/*-
 * #%L
 * SUMARiS:: Core
 * %%
 * Copyright (C) 2018 SUMARiS Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import net.sumaris.core.dao.technical.Beans;
import net.sumaris.core.dao.technical.hibernate.HibernateDaoSupport;
import net.sumaris.core.model.referential.Parameter;
import net.sumaris.core.model.referential.Pmfm;
import net.sumaris.core.model.referential.pmfm.Method;
import net.sumaris.core.model.referential.taxon.ReferenceTaxon;
import net.sumaris.core.model.referential.taxon.TaxonName;
import net.sumaris.core.vo.referential.ParameterValueType;
import net.sumaris.core.vo.referential.PmfmVO;
import net.sumaris.core.vo.referential.ReferentialVO;
import net.sumaris.core.vo.referential.TaxonNameVO;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.stream.Collectors;

@Repository("taxonNameDao")
public class TaxonNameDaoImpl extends HibernateDaoSupport implements TaxonNameDao {

    /** Logger. */
    private static final Logger log =
            LoggerFactory.getLogger(TaxonNameDaoImpl.class);

    @Override
    public TaxonNameVO getTaxonNameReferent(Integer referenceTaxonId) {
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<TaxonName> query = builder.createQuery(TaxonName.class);
        Root<TaxonName> root = query.from(TaxonName.class);

        ParameterExpression<Integer> idParam = builder.parameter(Integer.class);

        query.select(root)
                .where(builder.equal(root.get(TaxonName.PROPERTY_REFERENCE_TAXON).get(ReferenceTaxon.PROPERTY_ID), idParam));

        TypedQuery<TaxonName> q = getEntityManager().createQuery(query)
                .setParameter(idParam, referenceTaxonId);
        return toTaxonNameVO(q.getSingleResult());
    }

    protected TaxonNameVO toTaxonNameVO(TaxonName source) {
        if (source == null) return null;

        TaxonNameVO target = new TaxonNameVO();

        Beans.copyProperties(source, target);

        // Reference taxon
        target.setReferenceTaxonId(source.getReferenceTaxon().getId());
        return target;
    }
}