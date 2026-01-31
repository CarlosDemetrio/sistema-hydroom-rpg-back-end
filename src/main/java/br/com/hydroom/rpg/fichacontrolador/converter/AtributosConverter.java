package br.com.hydroom.rpg.fichacontrolador.converter;

import br.com.hydroom.rpg.fichacontrolador.constants.ValidationMessages;
import br.com.hydroom.rpg.fichacontrolador.model.embedded.Atributos;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;

/**
 * Converter JPA para serializar/deserializar Atributos como JSON.
 * Valida estrutura do JSON e previne injeção.
 */
@Converter(autoApply = false)
@Slf4j
public class AtributosConverter implements AttributeConverter<Atributos, String> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(Atributos atributos) {
        if (atributos == null) {
            return null;
        }

        try {
            String json = objectMapper.writeValueAsString(atributos);
            log.debug("Convertendo Atributos para JSON: {}", json);
            return json;
        } catch (JsonProcessingException e) {
            log.error("Erro ao converter Atributos para JSON", e);
            throw new IllegalArgumentException("Erro ao serializar atributos", e);
        }
    }

    @Override
    public Atributos convertToEntityAttribute(String json) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }

        try {
            Atributos atributos = objectMapper.readValue(json, Atributos.class);
            log.debug("Convertendo JSON para Atributos: {}", json);

            // Validação adicional de segurança
            validateAtributos(atributos);

            return atributos;
        } catch (JsonProcessingException e) {
            log.error("Erro ao converter JSON para Atributos: {}", json, e);
            throw new IllegalArgumentException(ValidationMessages.Atributos.JSON_INVALIDO, e);
        }
    }

    /**
     * Validação de segurança para garantir que valores estão em range válido.
     */
    private void validateAtributos(Atributos atributos) {
        if (atributos == null) {
            return;
        }

        validateAttribute("Força", atributos.getForca());
        validateAttribute("Destreza", atributos.getDestreza());
        validateAttribute("Constituição", atributos.getConstituicao());
        validateAttribute("Inteligência", atributos.getInteligencia());
        validateAttribute("Sabedoria", atributos.getSabedoria());
        validateAttribute("Carisma", atributos.getCarisma());
    }

    private void validateAttribute(String name, Integer value) {
        if (value != null && (value < 1 || value > 20)) {
            throw new IllegalArgumentException(
                    String.format(ValidationMessages.Atributos.ATRIBUTO_FORA_RANGE, name, value)
            );
        }
    }
}
