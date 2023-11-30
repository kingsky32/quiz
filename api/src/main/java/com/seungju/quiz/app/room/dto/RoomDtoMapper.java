package com.seungju.quiz.app.room.dto;

import com.seungju.quiz.app.room.domain.Room;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.factory.Mappers;

@Mapper(nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface RoomDtoMapper {

    RoomDtoMapper INSTANCE = Mappers.getMapper(RoomDtoMapper.class);

    Room toEntity(RoomDto.Create save);

    void merge(RoomDto.Update update, @MappingTarget Room room);

    RoomDto.Response toResponse(Room room);

    RoomDto.DetailResponse toDetailResponse(Room room);

}
