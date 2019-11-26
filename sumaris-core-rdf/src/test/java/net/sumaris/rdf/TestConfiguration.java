/*
 * #%L
 * SUMARiS
 * %%
 * Copyright (C) 2019 SUMARiS Consortium
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

package net.sumaris.rdf;

import net.sumaris.core.config.SumarisConfiguration;
import net.sumaris.rdf.dao.DatabaseFixtures;
import org.springframework.context.annotation.Bean;

/**
 * @author peck7 on 05/12/2018.
 */

@org.springframework.boot.test.context.TestConfiguration
public abstract class TestConfiguration extends net.sumaris.core.test.TestConfiguration {

    @Bean
    public DatabaseFixtures databaseFixtures() {
        return new DatabaseFixtures();
    }

    @Bean
    public static SumarisConfiguration sumarisConfiguration() {
        return initConfiguration("sumaris-core-rdf-test.properties");
    }

}