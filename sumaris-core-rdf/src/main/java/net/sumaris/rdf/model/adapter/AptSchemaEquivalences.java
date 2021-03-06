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

package net.sumaris.rdf.model.adapter;

import fr.eaufrance.sandre.schema.apt.APT;
import net.sumaris.core.model.referential.taxon.TaxonName;
import net.sumaris.rdf.config.RdfConfiguration;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component("aptSchemaEquivalences")
@ConditionalOnBean({RdfConfiguration.class})
@ConditionalOnProperty(
        prefix = "rdf.equivalences",
        name = {"sandre.enabled"},
        matchIfMissing = true)
public class AptSchemaEquivalences extends AbstractSchemaEquivalences {

    private static final Logger log = LoggerFactory.getLogger(AptSchemaEquivalences.class);

    @Override
    public void visitModel(Model model, String ns, String schemaUri) {
        log.info("Adding {{}} equivalences to {{}}...", APT.PREFIX, schemaUri);
    }

    @Override
    public void visitClass(Model model, Resource ontClass, Class clazz) {
        if (TaxonName.class == clazz) {
            log.info("Adding {{}} equivalence on Class {{}}...", APT.PREFIX, clazz.getSimpleName());

            ontClass.addProperty(equivalentClass, APT.AppelTaxon.asResource());
        }
    }
}
