package br.ufpr.tads.manutencao.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.ufpr.tads.manutencao.model.Categoria;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
}
