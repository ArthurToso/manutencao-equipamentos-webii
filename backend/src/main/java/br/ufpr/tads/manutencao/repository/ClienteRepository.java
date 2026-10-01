package br.ufpr.tads.manutencao.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.ufpr.tads.manutencao.model.Cliente;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    Optional<Cliente> findByCpf(String cpf);

    Optional<Cliente> findByEmailIgnoreCase(String email);

    boolean existsByCpf(String cpf);

    boolean existsByEmailIgnoreCase(String email);
}
