package eci.edu.co.Parcial2ArswBack.controller;



import org.springframework.web.bind.annotation.*;

import eci.edu.co.Parcial2ArswBack.model.Room;
import eci.edu.co.Parcial2ArswBack.repository.RoomRepository;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {
    private final RoomRepository repo;

    public RoomController(RoomRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<Room> list() {
        return repo.findAll();
    }

    @GetMapping("/{name}")
    public Room get(@PathVariable String name) {
        return repo.findByName(name).orElse(null);
    }
}
