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

package net.sumaris.rdf.util;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.vocabulary.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class OwlUtils {

    private static Logger log = LoggerFactory.getLogger(OwlUtils.class);

    public static String ADAGIO_PREFIX = "http://www.e-is.pro/2019/03/adagio/";
    public static ZoneId ZONE_ID = ZoneId.systemDefault();
    public static DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S");
    public static SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");

    public static Method getterOfField(Class t, String field) {
        try {
            Method res = t.getMethod("get" + field.substring(0, 1).toUpperCase() + field.substring(1));
            return res;
        } catch (NoSuchMethodException e) {
            log.error("error in the declaration of allowed ManyToOne " + e.getMessage());
        }
        return null;
    }


    protected Map<Class, Resource> Class2Resources = new HashMap<>();
    protected  Map<Resource, Class> Resources2Class = initStandardTypeMapper();


    protected Map<Resource, Class> initStandardTypeMapper() {
        Map<Class, Resource> res = new HashMap<>();
        res.put(Date.class, XSD.date);
        res.put(LocalDateTime.class, XSD.dateTime);
        res.put(Timestamp.class, XSD.dateTimeStamp);
        res.put(Integer.class, XSD.integer);
        res.put(Short.class, XSD.xshort);
        res.put(Long.class, XSD.xlong);
        res.put(Double.class, XSD.xdouble);
        res.put(Float.class, XSD.xfloat);
        res.put(Boolean.class, XSD.xboolean);
        res.put(long.class, XSD.xlong);
        res.put(int.class, XSD.integer);
        res.put(float.class, XSD.xfloat);
        res.put(double.class, XSD.xdouble);
        res.put(short.class, XSD.xshort);
        res.put(boolean.class, XSD.xboolean);
        res.put(String.class, XSD.xstring);
        res.put(void.class, RDFS.Literal);
        Class2Resources.putAll(res);
        return res.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey, (x, y) -> x));

    }

    protected Optional<Method> setterOfField(Resource schema, Class t, String field, OwlTransformContext context) {
        try {
            Optional<Field> f = fieldOf(schema, t, field, context);
            if (f.isPresent()) {
                String setterName = "set" + f.get().getName().substring(0, 1).toUpperCase() + f.get().getName().substring(1);
                //log.info("setterName " + setterName);
                Method met = t.getMethod(setterName, f.get().getType());
                return Optional.of(met);
            }

        } catch (NoSuchMethodException e) {
            log.warn("NoSuchMethodException setterOfField " + field);
        } catch (NullPointerException e) {
            log.warn("NullPointerException setterOfField " + field);
        }
        return Optional.empty();
    }

    protected Optional<Field> fieldOf(Resource schema, Class t, String name, OwlTransformContext context) {
        try {

            Class ret = context.URI_2_CLASS.get(t.getSimpleName());
            if (ret == null) {
                log.info("error fieldOf " + classToURI(schema, t) + " " + name);
                return Optional.empty();
            } else {
                return Optional.of(ret.getDeclaredField(name));

            }
        } catch (NoSuchFieldException e) {
            log.error("error fieldOf " + t.getSimpleName() + " " + name + " - " + e.getMessage());
        }
        return null;
    }


    protected String classToURI(Resource ont, Class c) {
        String uri = ont + c.getSimpleName();
        if (uri.substring(1).contains("<")) {
            uri = uri.substring(0, uri.indexOf("<"));
        }
//        if (uri.endsWith("<java.lang.Integer, java.util.Date>")) {
//            uri = uri.replace("<java.lang.Integer, java.util.Date>", "");
//        }

        if (uri.contains("$")) {
            log.error("Inner classes not handled " + uri);
        }

        return uri;
    }


    protected boolean isJavaType(Type type) {
        return Class2Resources.keySet().stream().anyMatch(type::equals);
    }

    protected boolean isJavaType(Method getter) {
        return isJavaType(getter.getGenericReturnType());
    }

    protected boolean isJavaType(Field field) {
        return isJavaType(field.getType());
    }


    /**
     * check the getter and its corresponding field's annotations
     *
     * @param met the getter method to test
     * @return true if it is a technical id to exclude from the model
     */
    protected boolean isId(Method met) {
        return "getId".equals(met.getName())
                && Stream.concat(annotsOfField(getFieldOfGetter(met)), Stream.of(met.getAnnotations()))
                .anyMatch(annot -> annot instanceof Id || annot instanceof org.springframework.data.annotation.Id);
    }

    protected boolean isManyToOne(Method met) {
        return annotsOfField(getFieldOfGetter(met)).anyMatch(annot -> annot instanceof ManyToOne) // check the corresponding field's annotations
                ||
                Stream.of(met.getAnnotations()).anyMatch(annot -> annot instanceof ManyToOne)  // check the method's annotations
                ;
    }

    protected Stream<Annotation> annotsOfField(Optional<Field> field) {
        return field.map(field1 -> Stream.of(field1.getAnnotations())).orElseGet(Stream::empty);
    }

    protected boolean isGetter(Method met) {
        return met.getName().startsWith("get") // only getters
                && !"getBytes".equals(met.getName()) // ignore ugly
                && met.getParameterCount() == 0 // ignore getters that are not getters
                && getFieldOfGetter(met).isPresent()
                ;
    }


    protected boolean isSetter(Method met) {
        return met.getName().startsWith("set");
    }

    protected Field getFieldOfGetteR(Method getter) {
        String fieldName = getter.getName().substring(3, 4).toLowerCase() + getter.getName().substring(4);
        try {
            return getter.getDeclaringClass().getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            return null; // this is never going to happen right ?
        }
    }


    protected Optional<Field> getFieldOfGetter(Method getter) {

        String fieldName = getter.getName().substring(3, 4).toLowerCase() + getter.getName().substring(4);
        //log.info("searching field : " + fieldName);
        try {
            return Optional.of(getter.getDeclaringClass().getDeclaredField(fieldName));
        } catch (Exception e) {
            //log.error("field not found : " + fieldName + " for class " + getter.getDeclaringClass() + "  " + e.getMessage());
            return Optional.empty();
        }
    }

    protected Resource getStdType(Field f) {
        return Class2Resources.getOrDefault(f.getType(), RDFS.Literal);
//        return Class2Resources.entrySet().stream()
//                .filter((entry) -> entry.getKey().getTypeName().equals(f.getStdType().getSimpleName()))
//                .map(Map.Entry::getValue)
//                .findAny()
//                .orElse(RDFS.Literal);
    }

    protected Resource getStdType(Type type) {
        return Class2Resources.getOrDefault(type, RDFS.Literal);
//        return Class2Resources.entrySet().stream()
//                .filter((entry) -> entry.getKey().getTypeName().equals(type.getTypeName()))
//                .map(Map.Entry::getValue)
//                .findAny()
//                .orElse(RDFS.Literal);
    }


    // =============== List handling ===============

    protected List<Class> ACCEPTED_LIST_CLASS = Arrays.asList(List.class, ArrayList.class);

    public boolean isListType(Type type) {


        if (type instanceof ParameterizedType) {
            ParameterizedType parameterized = (ParameterizedType) type;// This would be Class<List>, say
            Type raw = parameterized.getRawType();

            return (ACCEPTED_LIST_CLASS.stream() // add set LinkedList... if you wish
                    .anyMatch(x -> x.getCanonicalName().equals(raw.getTypeName())));
        }

        return false;

    }

    public Type getListType(Type type) {
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterized = (ParameterizedType) type;// This would be Class<List>, say
            Type raw = parameterized.getRawType();
            Type own = parameterized.getOwnerType();
            Type[] typeArgs = parameterized.getActualTypeArguments();

            if (ACCEPTED_LIST_CLASS.stream()
                    .anyMatch(x -> x.getCanonicalName().equals(raw.getTypeName()))) {
                return typeArgs[0];
            }
        }
        return null;
    }


    // =============== Define relation  ===============

    public void createOneToMany(OntModel ontoModel, OntClass ontoClass, OntProperty prop, Resource resource) {
        OntClass minCardinalityRestriction = ontoModel.createMinCardinalityRestriction(null, prop, 1);
        ontoClass.addSuperClass(minCardinalityRestriction);
    }

    public void createZeroToMany(OntModel ontoModel, OntClass ontoClass, OntProperty prop, Resource resource) {
        OntClass minCardinalityRestriction = ontoModel.createMinCardinalityRestriction(null, prop, 0);
        ontoClass.addSuperClass(minCardinalityRestriction);
    }

    public void createZeroToOne(OntModel ontoModel, OntClass ontoClass1, OntProperty prop, OntClass ontoClass2) {
        OntClass maxCardinalityRestriction = ontoModel.createMaxCardinalityRestriction(null, prop, 1);
        ontoClass1.addSuperClass(maxCardinalityRestriction);
    }

    public void createOneToOne(OntModel ontoModel, OntClass ontoClass1, OntProperty prop, OntClass ontoClass2) {
        OntClass maxCardinalityRestriction = ontoModel.createMaxCardinalityRestriction(null, prop, 1);
        ontoClass1.addSuperClass(maxCardinalityRestriction);
    }


    // ==== pur utils ====

    public static String delta(long nanoStart) {
        long elapsedTime = System.nanoTime() - nanoStart;
        double seconds = (double) elapsedTime / 1_000_000_000.0;
        return " elapsed " + seconds;
    }

    /**
     * Serialize model in requested format
     *
     * @param model  input model
     * @param format output format if null then output to RDF/XML
     * @return a string representation of the model
     */
    public static String toString(Model model, String format) {

        try (final ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            if (format == null) {
                model.write(os);
            } else {
                model.write(os, format);
            }
            os.flush();
            os.close();
            return new String(os.toByteArray(), "UTF8");
        } catch (IOException e) {
            log.error("doWrite ", e);
        }
        return "there was an error writing the model ";
    }



    public LocalDate convertToLocalDateViaMilisecond(Date dateToConvert) {
        return Instant.ofEpochMilli(dateToConvert.getTime())
                .atZone(ZONE_ID)
                .toLocalDate();
    }

    public LocalDate convertToLocalDateViaInstant(Date dateToConvert) {
        return dateToConvert.toInstant()
                .atZone(ZONE_ID)
                .toLocalDate();
    }

    public Date convertToDateViaInstant(LocalDateTime dateToConvert) {
        return java.util.Date
                .from(dateToConvert.atZone(ZONE_ID)
                        .toInstant());
    }


}