package com.wayapay.thirdpartyintegrationservice.exceptionhandling;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class EnumValidator implements ConstraintValidator<EnumConstraint, String> {

    private EnumConstraint annotation;

    @Override
    public void initialize(EnumConstraint enumConstraint) {
        this.annotation = enumConstraint;
    }

    @Override
    public boolean isValid(String valueForValidation, ConstraintValidatorContext constraintValidatorContext) {

        boolean result = false;

        Object[] enumValues = this.annotation.enumClass().getEnumConstants();

        if(enumValues != null) {
            for(Object enumValue:enumValues) {
                if(valueForValidation.equals(enumValue.toString())
                        || (this.annotation.ignoreCase() && valueForValidation.equalsIgnoreCase(enumValue.toString()))) {
                    result = true;

                    if (valueForValidation.equalsIgnoreCase("UNKNOWN")){
                        //it is actually invalid.
                        result = false;
                    }

                    break;
                }
            }
        }

        return result;
    }
}