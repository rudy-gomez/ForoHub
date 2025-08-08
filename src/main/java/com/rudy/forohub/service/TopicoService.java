package com.rudy.forohub.service;

import com.rudy.forohub.domain.curso.CursoRepository;
import com.rudy.forohub.domain.topico.*;
import com.rudy.forohub.domain.usuario.UsuarioRepository;
import com.rudy.forohub.infra.errores.ValidacionDeIntegridad;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class TopicoService {

    @Autowired
    private TopicoRepository topicoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CursoRepository cursoRepository;

    public Topico registrarTopico(DatosRegistroTopico datos) {
        if (topicoRepository.existsByTituloAndMensaje(datos.titulo(), datos.mensaje())) {
            throw new ValidacionDeIntegridad("Ya existe un tópico con el mismo título y mensaje");
        }

        var autor = usuarioRepository.findById(datos.autorId())
                .orElseThrow(() -> new ValidacionDeIntegridad("El usuario especificado no existe"));

        var curso = cursoRepository.findById(datos.cursoId())
                .orElseThrow(() -> new ValidacionDeIntegridad("El curso especificado no existe"));

        var topico = new Topico(datos, autor, curso);
        return topicoRepository.save(topico);
    }

    public Page<DatosRespuestaTopico> listarTopicos(Pageable paginacion, String curso, Integer anio) {
        Page<Topico> topicos;

        if (curso != null && anio != null) {
            topicos = topicoRepository.findByCursoAndAnio(curso, anio, paginacion);
        } else {
            topicos = topicoRepository.findAll(paginacion);
        }

        return topicos.map(DatosRespuestaTopico::new);
    }

    public Topico obtenerTopicoPorId(Long id) {
        return topicoRepository.findById(id)
                .orElseThrow(() -> new ValidacionDeIntegridad("El tópico especificado no existe"));
    }

    public Topico actualizarTopico(Long id, DatosActualizarTopico datos) {
        var topico = obtenerTopicoPorId(id);

        String nuevoTitulo = datos.titulo() != null ? datos.titulo() : topico.getTitulo();
        String nuevoMensaje = datos.mensaje() != null ? datos.mensaje() : topico.getMensaje();

        if (!nuevoTitulo.equals(topico.getTitulo()) || !nuevoMensaje.equals(topico.getMensaje())) {
            if (topicoRepository.existsByTituloAndMensaje(nuevoTitulo, nuevoMensaje)) {
                throw new ValidacionDeIntegridad("Ya existe un tópico con el mismo título y mensaje");
            }
        }

        topico.actualizarDatos(datos);
        return topico;
    }

    public void eliminarTopico(Long id) {
        var topico = obtenerTopicoPorId(id);
        topicoRepository.delete(topico);
    }
}