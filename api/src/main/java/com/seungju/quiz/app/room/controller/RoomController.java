package com.seungju.quiz.app.room.controller;

import com.seungju.quiz.app.room.dto.RoomDto;
import com.seungju.quiz.app.room.service.RoomService;
import com.seungju.quiz.pagniation.dto.PageableDto;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RequestMapping(path = "/api/v1/room")
@RestController
public class RoomController {

    private final RoomService roomService;

    @PostMapping
    @Secured("ROLE_USER")
    public ResponseEntity<RoomDto.DetailResponse> create(@NonNull @RequestBody RoomDto.Create create) {
        return ResponseEntity.ok(roomService.create(create));
    }

    @GetMapping
    @Secured("ROLE_USER")
    public ResponseEntity<PageableDto.Cursor.Response<Long, RoomDto.Response>> getAll(@NonNull RoomDto.Request request) {
        return ResponseEntity.ok(roomService.getAll(request));
    }

    @GetMapping("/{id}")
    @Secured("ROLE_USER")
    public ResponseEntity<RoomDto.DetailResponse> get(@NonNull @PathVariable Long id) {
        return ResponseEntity.ok(roomService.get(id));
    }

    @PutMapping("/{id}")
    @Secured("ROLE_USER")
    public ResponseEntity<RoomDto.DetailResponse> update(@NonNull @PathVariable Long id, @NonNull @RequestBody RoomDto.Update update) {
        return ResponseEntity.ok(roomService.update(id, update));
    }

    @DeleteMapping("/{id}")
    @Secured("ROLE_USER")
    public void delete(@NonNull @PathVariable Long id) {
        roomService.delete(id);
    }

    @DeleteMapping
    @Secured("ROLE_USER")
    public void deleteAll(@NonNull RoomDto.DeleteAll deleteAll) {
        roomService.deleteAll(deleteAll);
    }

    @MessageMapping("/{id}/play")
    public void play(@NonNull @DestinationVariable Long id) {
        roomService.play(id);
    }

    @MessageMapping("/{id}/skip")
    public void skip(@NonNull @DestinationVariable Long id) {
        roomService.skip(id);
    }

    @MessageMapping("/{id}/chat")
    public void chat(@NonNull @DestinationVariable Long id, RoomDto.Chat chat) {
        roomService.chat(id, chat);

    }

}
