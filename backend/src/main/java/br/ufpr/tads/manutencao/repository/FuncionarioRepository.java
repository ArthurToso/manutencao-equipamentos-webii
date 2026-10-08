package br.ufpr.tads.manutencao.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.ufpr.tads.manutencao.model.Funcionario;

@Repository 
public interface FuncionarioRepository extends JpaRepository<Funcionario, Long> {  
    //JpaRepository faz com que herde save, findById, findAll

    List<Funcionario> findByUsuarioAtivoTrueOrderByUsuarioNomeAsc();

    Optional<Funcionario> findByIdAndUsuarioAtivoTrue(Long id);

    long countByUsuarioAtivoTrue();
}
