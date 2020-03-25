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

package net.sumaris.rdf.service.data;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.sumaris.core.dao.technical.model.IEntity;
import net.sumaris.core.dao.technical.model.IUpdateDateEntityBean;
import net.sumaris.core.exception.SumarisTechnicalException;
import net.sumaris.core.vo.IValueObject;
import net.sumaris.rdf.config.RdfConfiguration;
import net.sumaris.rdf.dao.RdfModelDao;
import net.sumaris.rdf.dao.cache.RdfCacheConfiguration;
import net.sumaris.rdf.model.IModelVisitor;
import net.sumaris.rdf.model.ModelVocabulary;
import net.sumaris.rdf.model.ModelEntities;
import net.sumaris.rdf.model.ModelType;
import net.sumaris.rdf.service.schema.RdfSchemaExportService;
import net.sumaris.rdf.util.Bean2Owl;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.DC_11;
import org.reflections.Reflections;
import org.reflections.scanners.Scanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.persistence.Entity;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

@Service("rdfDataExportService")
@ConditionalOnBean({RdfConfiguration.class})
public class RdfDataExportServiceImpl implements RdfDataExportService {

    private static final Logger log = LoggerFactory.getLogger(RdfDataExportServiceImpl.class);

    @Autowired
    protected RdfConfiguration config;

    @Autowired
    protected RdfModelDao modelDao;

    @Autowired
    protected RdfCacheConfiguration cacheConfiguration;

    @Autowired
    protected RdfSchemaExportService schemaExportService;

    protected Bean2Owl beanConverter;

    protected List<IModelVisitor> modelVisitors = Lists.newCopyOnWriteArrayList();

    @PostConstruct
    protected void afterPropertiesSet() {

        beanConverter = new Bean2Owl(config.getModelBaseUri());
    }

    @Override
    public void register(IModelVisitor visitor) {
        if (!modelVisitors.contains(modelVisitors)) modelVisitors.add(visitor);
    }

    @Override
    public Model getIndividuals(@Nullable RdfDataExportOptions options) {

        // Make sure to fix options (set packages, ...)
        fillOptions(options);

        // Create base model
        Model model = ModelFactory.createDefaultModel();
        String schemaUri = schemaExportService.getOntologySchemaUri();
        model.setNsPrefix(schemaExportService.getOntologySchemaPrefix(), schemaUri);
        model.setNsPrefix("dc", DC_11.getURI()); // http://purl.org/dc/elements/1.1/

        boolean hasClassName = StringUtils.isNotBlank(options.getClassName());

        if (options.getDomain() == ModelVocabulary.DATA && !hasClassName) {
            throw new IllegalArgumentException("Unable to export data without a class name!");
        }

        final int entityGraphDepth =  options.getDomain() == ModelVocabulary.DATA ? 3 : 0; // TODO: check if enought

        // When having classname and id
        if (hasClassName && StringUtils.isNotBlank(options.getId())) {
            // Get the bean
            IUpdateDateEntityBean entity = modelDao.getById(options.getDomain(), options.getClassName(), IUpdateDateEntityBean.class, options.getId());

            // Convert into model
            Resource beanModel = beanConverter.bean2Owl(model, schemaUri, entity, entityGraphDepth, ModelEntities.propertyIncludes, ModelEntities.propertyExcludes);

            // Notify visitor
            onInidividualCreated(model, beanModel, entity.getClass());
        }
        else {
            getClassesAsStream(options)
                .forEach(clazz -> modelDao.streamAll(options.getDomain(), clazz.getSimpleName(), IEntity.class)
                    .forEach(entity -> {
                        // Create the resource
                        Resource beanModel = beanConverter.bean2Owl(model, schemaUri, entity, entityGraphDepth, ModelEntities.propertyIncludes, ModelEntities.propertyExcludes);

                        // Notify visitor
                        onInidividualCreated(model, beanModel, clazz);
                    }));
        }

        return model;
    }

    /* -- protected methods -- */


    protected RdfDataExportOptions fillOptions(RdfDataExportOptions options) {
        Preconditions.checkNotNull(options);

        ModelVocabulary domain = options.getDomain();
        if (domain == null) {
            if (StringUtils.isNotBlank(options.getClassName())) {
                domain = modelDao.getDomainByClassName(options.getClassName());
            }
            else {
                domain = ModelVocabulary.REFERENTIAL; // default
            }
            options.setDomain(domain);
        }

        switch(domain) {
            case DATA:
                options.setAnnotatedType(Entity.class);
                options.setPackages(Lists.newArrayList("net.sumaris.core.model.data"));
                break;
            case REFERENTIAL:
                options.setAnnotatedType(Entity.class);
                options.setPackages(ImmutableList.of(
                        "net.sumaris.core.model.administration",
                        "net.sumaris.core.model.referential"
                ));
                break;
            case SOCIAL:
                options.setAnnotatedType(Entity.class);
                options.setPackages(ImmutableList.of(
                        "net.sumaris.core.model.administration.user",
                        "net.sumaris.core.model.social"
                ));
                break;
            case TECHNICAL:
                options.setAnnotatedType(Entity.class);
                options.setPackages(ImmutableList.of(
                        "net.sumaris.core.model.file",
                        "net.sumaris.core.model.technical"
                ));
                break;
            case VO:
                options.setType(IValueObject.class);
                options.setPackages(Lists.newArrayList("net.sumaris.core.vo"));
                break;
            default:
                throw new SumarisTechnicalException(String.format("Unknown ontology {%s}", domain));
        }
        return options;
    }

    @Override
    public String getModelDataUri() {
        String uri = config.getModelBaseUri() + ModelType.SCHEMA.name().toLowerCase() + "/";

        // model should ends with '/'
        if (uri.endsWith("#")) {
            uri = uri.substring(0, uri.length() -1);
        }
        if (!uri.endsWith("/")) {
            uri += "/";
        }
        return uri;
    }

    protected Stream<Class<?>> getClassesAsStream(RdfDataExportOptions options) {

        Reflections reflections;
        Stream<Class<?>> result;

        // Define class scanner
        Scanner[] scanners = null;
        if (options.getAnnotatedType() != null) {
            scanners = new Scanner[] { new SubTypesScanner(false), new TypeAnnotationsScanner() };
        }

        // Collect by package
        if (CollectionUtils.isNotEmpty(options.getPackages())) {
            if (scanners != null) {
                reflections = new Reflections(options.getPackages(), scanners);
            }
            else {
                reflections = new Reflections(options.getPackages());
            }
        }
        // Or collect all
        else {
            reflections = Reflections.collect();
        }


        // get by type
        if (options.getType() != null) {
            result = reflections.getSubTypesOf(options.getType()).stream();
        }

        // Get by annotated type
        else if (options.getAnnotatedType() != null) {
            result = reflections.getTypesAnnotatedWith(options.getAnnotatedType()).stream();
        }

        // Get all classes
        else {
            result = reflections.getSubTypesOf(Object.class).stream();
        }

        // Filter by class names
        final Set<String> classNames = (options.getClassName() != null) ?
                modelDao.getClassNamesByRootClass(options.getDomain(), options.getClassName()) :
                (options.getDomain() != null) ? modelDao.getClassNamesByDomain(options.getDomain()) : null;
        if (CollectionUtils.isNotEmpty(classNames)) {
            return result.filter(clazz -> classNames.contains(clazz.getSimpleName()) || classNames.contains(clazz.getSimpleName().toLowerCase()));
        }

        return result;
    }

    public void onInidividualCreated(Model model, Resource individual, Class clazz) {
        modelVisitors.forEach(visitor -> visitor.visitIndividual(model, individual, clazz));
    }


}