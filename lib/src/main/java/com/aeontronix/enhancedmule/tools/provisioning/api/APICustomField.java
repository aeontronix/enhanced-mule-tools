/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools.provisioning.api;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class APICustomField {
    private Object textValue;
    private Object numberValue;
    private Object textListValue;
    private Object numberListValue;
    private Object dateValue;
    private Object enumValue;
    private String displayName;
    private String tagType;
    private String dataType;
    private String key;

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getTagType() {
        return tagType;
    }

    public void setTagType(String tagType) {
        this.tagType = tagType;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Object getTextValue() {
        return textValue;
    }

    public void setTextValue(Object textValue) {
        this.textValue = textValue;
    }

    public Object getNumberValue() {
        return numberValue;
    }

    public void setNumberValue(Object numberValue) {
        this.numberValue = numberValue;
    }

    public Object getTextListValue() {
        return textListValue;
    }

    public void setTextListValue(Object textListValue) {
        this.textListValue = textListValue;
    }

    public Object getNumberListValue() {
        return numberListValue;
    }

    public void setNumberListValue(Object numberListValue) {
        this.numberListValue = numberListValue;
    }

    public Object getDateValue() {
        return dateValue;
    }

    public void setDateValue(Object dateValue) {
        this.dateValue = dateValue;
    }

    public Object getEnumValue() {
        return enumValue;
    }

    public void setEnumValue(Object enumValue) {
        this.enumValue = enumValue;
    }

    @JsonIgnore
    public Object getValue() {
        if( textValue != null ) {
            return textValue;
        }
        if( numberValue != null ) {
            return numberValue;
        }
        if( textListValue != null ) {
            return textListValue;
        }
        if( numberListValue != null ) {
            return numberListValue;
        }
        if( dateValue != null ) {
            return dateValue;
        }
        if( enumValue != null ) {
            return enumValue;
        }
        return null;
    }
}
