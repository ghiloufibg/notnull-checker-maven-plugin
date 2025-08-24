package com.ghiloufi.notnullchecker.validation;

import com.github.javaparser.ast.expr.AnnotationExpr;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ViolationCandidate(
    @NotNull ViolationCandidateEnum kind,
    @NotBlank String name,
    @NotBlank String type,
    @NotBlank String fileName,
    @NotBlank String position,
    @NotNull List<AnnotationExpr> annotations) {}
