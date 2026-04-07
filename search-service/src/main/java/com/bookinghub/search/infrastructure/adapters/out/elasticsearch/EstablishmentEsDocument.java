package com.bookinghub.search.infrastructure.adapters.out.elasticsearch;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.GeoPointField;
import org.springframework.data.elasticsearch.annotations.InnerField;
import org.springframework.data.elasticsearch.annotations.MultiField;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

@Document(indexName = "establishments")
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
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

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
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

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProfessionalEsEntry {
        @Field(type = FieldType.Keyword)
        private String professionalId;

        @Field(type = FieldType.Text)
        private String name;

        @Field(type = FieldType.Text)
        private List<String> specialties;
    }
}
