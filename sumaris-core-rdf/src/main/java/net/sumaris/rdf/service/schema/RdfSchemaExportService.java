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

package net.sumaris.rdf.service.schema;

import net.sumaris.rdf.model.IModelVisitor;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Model;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;

@Transactional(readOnly = true)
public interface RdfSchemaExportService {

    String getOntologySchemaPrefix();

    String getOntologySchemaUri();


    /**
     * Register a schema visitor
     * @param visitor
     */
    void register(IModelVisitor visitor);

    /**
     * Get schema, as an ontology (classes with properties)
     * @param options export options
     * @return a schema representation as an ontology
     */
    //@Cacheable(cacheNames = RdfCacheNames.ONTOLOGY_BY_NAME, key="#options.hashCode()", condition = " #options != null", unless = "#result == null")
    OntModel getSchemaOntology(@Nullable RdfSchemaExportOptions options);
}
