package net.sumaris.core.dao.technical;

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

import com.google.common.collect.Maps;
import net.sumaris.core.dao.technical.hibernate.HibernateDaoSupport;
import net.sumaris.core.model.technical.Software;
import net.sumaris.core.model.technical.SoftwareProperty;
import net.sumaris.core.vo.technical.ConfigurationVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Objects;

@Repository("softwareDao")
public class SoftwareDaoImpl extends HibernateDaoSupport implements SoftwareDao{

    @Autowired
    private SoftwareRepository repository;

    public ConfigurationVO get(String label) {
        return toVO(repository.getSoftware(label));
    }

    public ConfigurationVO save(ConfigurationVO source)  {

        Software target = toEntity(source);

        if (source.getId() == null) {
            getEntityManager().persist(target);
            source.setId(target.getId());
        }
        else {
            getEntityManager().merge(target);
        }

        return source;
    }

    /* -- protected methods -- */

    protected ConfigurationVO toVO(Software source) {
        if (source == null) return null;

        ConfigurationVO target = new ConfigurationVO();

        Beans.copyProperties(source, target);

        // properties
        Map<String, String> properties = Maps.newHashMap();
        Beans.getStream(source.getProperties())
                .filter(prop -> Objects.nonNull(prop)
                        && Objects.nonNull(prop.getLabel())
                        && Objects.nonNull(prop.getName())
                )
                .forEach(prop -> {
                    if (properties.containsKey(prop.getLabel())) {
                        logger.warn(String.format("Duplicate software property with label {%s}. Overriding existing value with {%s}", prop.getLabel(), prop.getName()));
                    }
                    properties.put(prop.getLabel(), prop.getName());
                });
        target.setProperties(properties);

        return target;
    }

    protected Software toEntity(ConfigurationVO source) {
        Software target = repository.getSoftware(source.getLabel());

        Beans.copyProperties(source, target);

        return target;
    }

}
