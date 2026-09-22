package br.ufpr.tads.manutencao.service;

import java.util.List;

import org.springframework.stereotype.Service;

import br.ufpr.tads.manutencao.dto.CategoriaRequest;
import br.ufpr.tads.manutencao.dto.CategoriaResponse;
import br.ufpr.tads.manutencao.model.Categoria;
import br.ufpr.tads.manutencao.repository.CategoriaRepository;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    public CategoriaService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    public List<CategoriaResponse> listar() {
        return categoriaRepository.findByAtivoTrueOrderByNome()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public CategoriaResponse criar(CategoriaRequest request) {
        Categoria categoria = new Categoria(request.getNome());
        return toResponse(categoriaRepository.save(categoria));
    }

    public CategoriaResponse atualizar(Long id, CategoriaRequest request) {
        Categoria categoria = buscarAtiva(id);
        categoria.setNome(request.getNome());
        return toResponse(categoriaRepository.save(categoria));
    }

    public void remover(Long id) {
        Categoria categoria = buscarAtiva(id);
        categoria.setAtivo(false);
        categoriaRepository.save(categoria);
    }

    private Categoria buscarAtiva(Long id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Categoria não encontrada"));

        if (!categoria.isAtivo()) {
            throw new IllegalArgumentException("Categoria não encontrada");
        }

        return categoria;
    }

    private CategoriaResponse toResponse(Categoria categoria) {
        CategoriaResponse response = new CategoriaResponse();
        response.setId(categoria.getId());
        response.setNome(categoria.getNome());
        response.setAtivo(categoria.isAtivo());
        return response;
    }
}
