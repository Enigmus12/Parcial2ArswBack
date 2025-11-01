package eci.edu.co.Parcial2ArswBack.repository;



import org.springframework.data.mongodb.repository.MongoRepository;

import eci.edu.co.Parcial2ArswBack.model.Room;

import java.util.Optional;

public interface RoomRepository extends MongoRepository<Room, String> {
    Optional<Room> findByName(String name);
}
