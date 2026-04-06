package com.bookinghub.search.infrastructure.adapters.out.elasticsearch;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

import java.util.List;

@Document(indexName = "establishments")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class EstablishmentEsDocument {

    @Id
    private String id;

    @MultiField(mainField = @Field(type = FieldType.Text), otherFields = {
            @InnerField(suffix = "keyword", type = FieldType.Keyword)
    })
    private String name;

    @Field(type = FieldType.Text)
    private String description;

    @Field(type = FieldType.Keyword)
    private String city;

    @Field(type = FieldType.Keyword)
    private String state;

    @Field(type = FieldType.Keyword)
    private String zipCode;

    @GeoPointField
    private GeoPoint geoPoint;

    @Field(type = FieldType.Nested)
    private List<ServiceEsEntry> services;

    @Field(type = FieldType.Nested)
    private List<ProfessionalEsEntry> professionals;

    @Field(type = FieldType.Float)
    private Double minPrice;

    @Field(type = FieldType.Float)
    private Double maxPrice;

    @Field(type = FieldType.Float)
    private Double ratingSum;

    @Field(type = FieldType.Float)
    private Double averageRating;

    @Field(type = FieldType.Integer)
    private Integer totalReviews;

    @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ServiceEsEntry {
        @Field(type = FieldType.Keyword)
        private String serviceId;

        @MultiField(mainField = @Field(type = FieldType.Text), otherFields = {
                @InnerField(suffix = "keyword", type = FieldType.Keyword)
        })
        private String title;

        @Field(type = FieldType.Float)
        private Double minPrice;

        @Field(type = FieldType.Float)
        private Double maxPrice;
    }

    @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ProfessionalEsEntry {
        @Field(type = FieldType.Keyword)
        private String professionalId;

        @Field(type = FieldType.Text)
        private String name;

        @Field(type = FieldType.Text)
        private List<String> specialties;
    }
}
