package com.nimbleways.springboilerplate.entities;

import org.apache.commons.lang3.StringUtils;

public enum ProductType {

    NORMAL,

    SEASONAL,

    EXPIRABLE;


    public static ProductType of(String type){

        switch (StringUtils.upperCase(type)){

            case ("NORMAL") :
                    return NORMAL;

            case ("SEASONAL") :
                    return SEASONAL;

            case ("EXPIRABLE") :
                    return EXPIRABLE;

            default:
                return NORMAL;
        }

    }
}
