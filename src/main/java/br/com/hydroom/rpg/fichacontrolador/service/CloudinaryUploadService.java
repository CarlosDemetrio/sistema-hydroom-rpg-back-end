package br.com.hydroom.rpg.fichacontrolador.service;

import br.com.hydroom.rpg.fichacontrolador.exception.ExternalServiceException;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * Serviço responsável por upload e remoção de imagens no Cloudinary.
 *
 * <p>Separado do FichaImagemService para facilitar mock nos testes de integração.</p>
 *
 * <ul>
 *   <li>Falha no upload: lança RuntimeException (crítico — usuário não tem imagem)</li>
 *   <li>Falha no destroy: loga e continua (secundário — estado local permanece correto)</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class CloudinaryUploadService {

    private static final Logger log = LoggerFactory.getLogger(CloudinaryUploadService.class);

    private final Cloudinary cloudinary;

    private static final Set<String> TIPOS_PERMITIDOS = Set.of(
            "image/jpeg", "image/png", "image/webp", "image/gif"
    );
    private static final long TAMANHO_MAXIMO_BYTES = 10L * 1024 * 1024; // 10 MB

    /**
     * Faz upload de arquivo para o Cloudinary.
     *
     * @param arquivo  MultipartFile recebido no controller
     * @param folder   Pasta de destino no Cloudinary (ex: "rpg-fichas/1/fichas/42")
     * @return Map com "url" (urlCloudinary) e "public_id" retornados pelo Cloudinary
     * @throws IllegalArgumentException se arquivo inválido (tipo ou tamanho)
     * @throws RuntimeException         se falha de comunicação com o Cloudinary (mapeado para 502)
     */
    public Map<String, String> upload(MultipartFile arquivo, String folder) {
        validarArquivo(arquivo);
        try {
            @SuppressWarnings("rawtypes")
            Map uploadResult = cloudinary.uploader().upload(
                    arquivo.getBytes(),
                    ObjectUtils.asMap(
                            "folder", folder,
                            "use_filename", true,
                            "unique_filename", true
                    )
            );
            return Map.of(
                    "url",       (String) uploadResult.get("secure_url"),
                    "public_id", (String) uploadResult.get("public_id")
            );
        } catch (IOException e) {
            throw new ExternalServiceException("Falha ao fazer upload da imagem para o Cloudinary: " + e.getMessage(), e);
        }
    }

    /**
     * Remove arquivo do Cloudinary pelo publicId.
     *
     * <p>Falha é logada mas não propagada — o soft delete local continua mesmo se o Cloudinary falhar.</p>
     *
     * @param publicId identificador do arquivo no Cloudinary
     */
    public void destroy(String publicId) {
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (Exception e) {
            log.error("Falha ao deletar imagem do Cloudinary. publicId={}, erro={}", publicId, e.getMessage());
        }
    }

    private void validarArquivo(MultipartFile arquivo) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new IllegalArgumentException("Arquivo de imagem é obrigatório");
        }
        String contentType = arquivo.getContentType();
        if (contentType == null || !TIPOS_PERMITIDOS.contains(contentType)) {
            throw new IllegalArgumentException(
                    "Tipo de arquivo não permitido: " + contentType +
                    ". Tipos aceitos: JPEG, PNG, WebP, GIF"
            );
        }
        if (arquivo.getSize() > TAMANHO_MAXIMO_BYTES) {
            throw new IllegalArgumentException("Arquivo excede o tamanho máximo de 10 MB");
        }
    }
}
