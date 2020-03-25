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
import net.sumaris.core.dao.technical.model.IUpdateDateEntityBean;
import net.sumaris.core.model.referential.taxon.ReferenceTaxon;
import net.sumaris.core.model.referential.taxon.TaxonName;
import net.sumaris.rdf.config.RdfConfiguration;
import net.sumaris.rdf.model.IModelVisitor;
import net.sumaris.rdf.service.schema.RdfSchemaExportService;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.OWL2;
import org.apache.jena.vocabulary.RDFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.tdwg.rs.DWC;

import javax.annotation.PostConstruct;
import java.util.Objects;

@Component("dwcModelAdapter")
@ConditionalOnBean({RdfConfiguration.class})
@ConditionalOnProperty(
        prefix = "rdf",
        name = {"adapter.dwc.enabled"},
        matchIfMissing = true)
public class DwcModelAdapter extends AbstractModelAdapter {

    private static final Logger log = LoggerFactory.getLogger(DwcModelAdapter.class);


    @Override
    public void visitSchema(Model model, String ns, String schemaUri) {
        log.info("Adding {{}} equivalences to {{}}...", DWC.Terms.PREFIX, schemaUri);
    }

    @Override
    public void visitClass(Model model, Resource ontClass, Class clazz) {
        String classUri = ontClass.getURI();

        // Reference Taxon
        if (clazz == ReferenceTaxon.class) {
            if (log.isDebugEnabled()) log.debug("Adding {{}} equivalence on Class {{}}...", DWC.Terms.PREFIX, clazz.getSimpleName());

            ontClass.addProperty(subClassOf, DWC.Terms.Taxon);

            // Id
            model.getResource(classUri + "#" + ReferenceTaxon.Fields.ID)
                    .addProperty(subPropertyOf, DWC.Terms.taxonID);
        }

        // Taxon Name
        else if (clazz == TaxonName.class) {
            if (log.isDebugEnabled()) log.debug("Adding {{}} equivalence on Class {{}}...", DWC.Terms.PREFIX, clazz.getSimpleName());

            ontClass.addProperty(subClassOf, DWC.Voc.TaxonName);

            // Id
            model.getResource(classUri + "#" + ReferenceTaxon.Fields.ID)
                    .addProperty(subPropertyOf, DWC.Terms.scientificNameID);

            // Complete name
            model.getResource(classUri + "#" + TaxonName.Fields.COMPLETE_NAME)
                .addProperty(subPropertyOf, DWC.Terms.scientificName);
        }
    }
}
