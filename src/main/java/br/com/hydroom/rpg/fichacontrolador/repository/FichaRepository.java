package br.com.hydroom.rpg.fichacontrolador.repository;

import br.com.hydroom.rpg.fichacontrolador.model.Ficha;
import br.com.hydroom.rpg.fichacontrolador.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FichaRepository extends JpaRepository<Ficha, Long> {

    @Query("SELECT f FROM Ficha f WHERE f.usuario = :usuario AND f.deletedAt IS NULL")
    List<Ficha> findByUsuarioAndAtivoTrue(@Param("usuario") Usuario usuario);

    @Query("SELECT f FROM Ficha f WHERE f.usuario.id = :usuarioId AND f.deletedAt IS NULL")
    List<Ficha> findByUsuarioIdAndAtivoTrue(@Param("usuarioId") Long usuarioId);
}
