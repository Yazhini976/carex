package com.carex.service.intelligence.llm;

import java.util.Optional;

/**
 * Resilient Hybrid LLM Router Service for CAREX.
 * Supports cloud LLM providers (Groq, OpenAI, Gemini) and local Ollama instances
 * with zero-failure fallback.
 */
public interface LlmSpecialtyRouterService {

    record LlmRoutingResult(
            String specialtyName,
            double confidence,
            String reasoning,
            String providerUsed
    ) {}

    /**
     * Routes patient symptom text to a medical specialty using LLM inference
     * with automated fallback.
     *
     * @param inputText Natural language symptoms described by the patient.
     * @return Routing result with specialty, confidence score, and explainable rationale.
     */
    Optional<LlmRoutingResult> routeWithLlm(String inputText);
}
