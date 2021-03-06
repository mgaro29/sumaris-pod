package net.sumaris.core.dao.data;

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

import net.sumaris.core.dao.technical.SortDirection;
import net.sumaris.core.dao.technical.jpa.SumarisJpaRepository;
import net.sumaris.core.model.data.IDataEntity;
import net.sumaris.core.vo.data.DataFetchOptions;
import net.sumaris.core.vo.data.IDataVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.lang.Nullable;

import javax.persistence.LockModeType;
import java.io.Serializable;
import java.util.List;

@NoRepositoryBean
public interface DataRepository<
        E extends IDataEntity<ID>,
        ID extends Integer,
        V extends IDataVO<ID>,
        F extends Serializable
        >
        extends SumarisJpaRepository<E, ID, V> {


    // From a filter
    List<V> findAll(@Nullable F filter);

    Page<V> findAll(@Nullable F filter, Pageable pageable);

    List<V> findAll(@Nullable F filter, @Nullable DataFetchOptions fetchOptions);

    List<V> findAll(@Nullable F filter, net.sumaris.core.dao.technical.Page page, @Nullable DataFetchOptions fetchOptions);

    Page<V> findAll(@Nullable F filter, Pageable pageable, @Nullable DataFetchOptions fetchOptions);

    Page<V> findAll(int offset, int size, String sortAttribute, SortDirection sortDirection, DataFetchOptions fetchOptions);

    Page<V> findAll(F filter, int offset, int size, String sortAttribute,
                    SortDirection sortDirection, DataFetchOptions fetchOptions);

    // From a specification
    List<V> findAllVO(@Nullable Specification<E> spec);
    Page<V> findAllVO(@Nullable Specification<E> spec, Pageable pageable);
    List<V> findAllVO(@Nullable Specification<E> spec, DataFetchOptions fetchOptions);
    Page<V> findAllVO(@Nullable Specification<E> spec, Pageable pageable, DataFetchOptions fetchOption);

    long count(F filter);

    V get(ID id);

    V get(ID id, DataFetchOptions fetchOptions);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    V save(V vo);

    Specification<E> toSpecification(@Nullable F filter);

    V control(V vo);

    V validate(V vo);

    V unvalidate(V vo);

    V qualify(V vo);

    V toVO(E source, DataFetchOptions fetchOptions);

    void toVO(E source, V target, DataFetchOptions fetchOptions, boolean copyIfNull);

}
