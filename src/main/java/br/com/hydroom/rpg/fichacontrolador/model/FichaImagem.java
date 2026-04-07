package br.com.hydroom.rpg.fichacontrolador.model;

import br.com.hydroom.rpg.fichacontrolador.model.enums.TipoImagem;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "ficha_imagens", indexes = {
    @Index(name = "idx_ficha_imagem_ficha", columnList = "ficha_id"),
    @Index(name = "idx_ficha_imagem_tipo",  columnList = "tipo_imagem")
})
@SQLRestriction("deleted_at IS NULL")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FichaImagem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ficha_id", nullable = false)
    private Ficha ficha;

    @Column(name = "url_cloudinary", nullable = false, length = 2048)
    private String urlCloudinary;

    @Column(name = "public_id", nullable = false, length = 512)
    private String publicId;

    @Column(length = 200)
    private String titulo;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_imagem", nullable = false, length = 20)
    private TipoImagem tipoImagem;

    @Column(name = "ordem_exibicao", nullable = false)
    @Builder.Default
    private Integer ordemExibicao = 0;
}
