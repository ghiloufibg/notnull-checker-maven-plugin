package com.ghiloufi.notnullchecker.validation;

import jakarta.validation.constraints.NotBlank;

public enum ViolationCandidateEnum {
  FIELD("Field"),
  PARAMETER("Parameter"),
  CONSTRUCTOR_PARAMETER("Constructor parameter"),
  TYPE_PARAMETER("Type parameter"),
  COLLECTION_ELEMENT("Collection element"),
  RETURN_TYPE("Return type"),
  RETURN_NULL_LITERAL("Return null literal"),
  LOCAL_NULL_ASSIGNMENT("Local null assignment"),
  METHOD_NULL_ARGUMENT("Method null argument"),
  CONSTRUCTOR_NULL_ARGUMENT("Constructor null argument"),
  LAMBDA_RETURN_NULL_LITERAL("Lambda return null"),
  LAMBDA_PARAMETER("Lambda parameter");

  private final String value;

  ViolationCandidateEnum(@NotBlank String value) {
    this.value = value;
  }

  public String value() {
    return this.value;
  }
}
