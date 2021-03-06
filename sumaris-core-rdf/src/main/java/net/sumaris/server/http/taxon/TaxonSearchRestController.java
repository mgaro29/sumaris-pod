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

package net.sumaris.server.http.taxon;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Maps;
import net.sumaris.core.dao.technical.Page;
import net.sumaris.core.util.StringUtils;
import net.sumaris.rdf.config.RdfConfiguration;
import net.sumaris.rdf.model.ModelURIs;
import net.sumaris.rdf.service.data.RdfDataExportOptions;
import net.sumaris.rdf.service.data.RdfDataExportService;
import net.sumaris.rdf.service.schema.RdfSchemaOptions;
import net.sumaris.rdf.service.schema.RdfSchemaService;
import net.sumaris.rdf.util.ModelUtils;
import net.sumaris.server.http.rest.RdfFormat;
import net.sumaris.server.http.rest.RdfMediaType;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;


@RestController
@ConditionalOnBean({RdfConfiguration.class})
public class TaxonSearchRestController {

    protected static final String EXTENSION_PATH_PARAM = ".{extension:[a-z0-9-_]+}";

    // search path
    public static final String SEARCH_PATH = "/api/taxon/search";

    private static final Logger log = LoggerFactory.getLogger(TaxonSearchRestController.class);

    @Resource
    private RdfSchemaService schemaExportService;

    @Resource
    private RdfConfiguration config;


    @PostConstruct
    public void init() {
        log.info("Starting Taxon endpoint {{}}...", SEARCH_PATH);
    }

    @PostMapping(
            value = {
                    SEARCH_PATH
            },
            produces = {
                    RdfMediaType.APPLICATION_RDF_XML_VALUE,
                    RdfMediaType.APPLICATION_XML_VALUE,
                    RdfMediaType.APPLICATION_RDF_JSON_VALUE,
                    RdfMediaType.APPLICATION_JSON_VALUE,
                    RdfMediaType.APPLICATION_JSON_LD_VALUE,
                    RdfMediaType.APPLICATION_N_TRIPLES_VALUE,
                    RdfMediaType.APPLICATION_N_QUADS_VALUE,
                    RdfMediaType.TEXT_N3_VALUE,
                    RdfMediaType.APPLICATION_TRIG_VALUE,
                    RdfMediaType.TEXT_TRIG_VALUE,
                    RdfMediaType.APPLICATION_TRIX_VALUE,
                    RdfMediaType.TEXT_TRIX_VALUE,
                    RdfMediaType.APPLICATION_TURTLE_VALUE,
                    RdfMediaType.TEXT_TURTLE_VALUE,
                    // Browser HTML requests
                    MediaType.APPLICATION_XHTML_XML_VALUE,
                    MediaType.TEXT_HTML_VALUE
            })
    public ResponseEntity<byte[]> searchTaxonFromFile(@RequestParam("file") MultipartFile file,
                                                      final HttpServletRequest request) {

        log.info("Receiving taxon file to process {{}}", file.getOriginalFilename());

        // TODO: need implementation

        return ResponseEntity.ok()
                .contentType(RdfMediaType.APPLICATION_JSON)
                .body(new String("{}").getBytes());
    }


    /* -- protected methods -- */

    protected Map<String, String> parseQueryParams(URI uri) {
        Preconditions.checkNotNull(uri);

        Map<String, String> result = Maps.newHashMap();

        String query = uri.getQuery();
        if (StringUtils.isNotBlank(query)) {
            for (String paramStr : Splitter.on('&').omitEmptyStrings().trimResults().split(query)) {
                String[] paramParts = paramStr.split("=");
                if (paramParts.length == 1) {
                    result.put(paramParts[0], "true");
                }
                else if (paramParts.length == 2) {
                    result.put(paramParts[0], paramParts[1]);
                }
                else {
                    // Ignore
                    if (log.isInfoEnabled()) log.info("Skipping invalid IRI's query parameter: " + paramStr);
                }
            }
        }
        return result;
    }

    protected RdfFormat findRdfFormat(final HttpServletRequest request, @Nullable final String userFormat, @Nullable final RdfFormat defaultFormat) {
        if (StringUtils.isNotBlank(userFormat)) {
            return RdfFormat.fromUserString(userFormat)
                    .orElseThrow(() -> new IllegalArgumentException("Unknown output format: " + userFormat));
        }

        // Analyse HTTP header 'Accept' content types
        else {
            return RdfFormat.fromUrlExtension(request.getRequestURI())
                    .orElseGet(() -> {
                        Collection<String> acceptedContentTypes = Splitter.on(",").trimResults().splitToList(request.getHeader(HttpHeaders.ACCEPT));
                        return acceptedContentTypes.stream()
                                .map(contentType -> RdfFormat.fromContentType(contentType).orElse(null))
                                .filter(Objects::nonNull)
                                .findFirst()
                                .orElse(defaultFormat);
                    });
        }
    }
}