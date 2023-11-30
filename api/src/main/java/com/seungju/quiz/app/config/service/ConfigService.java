package com.seungju.quiz.app.config.service;

import com.seungju.quiz.app.config.domain.Config;
import com.seungju.quiz.app.config.domain.ConfigRepository;
import com.seungju.quiz.app.config.dto.ConfigDto;
import com.seungju.quiz.app.config.dto.ConfigDtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ConfigService {

    private final ConfigRepository configRepository;

    @Transactional(readOnly = true)
    public ConfigDto.Response get() {
        Config config = this.configRepository.findTopByOrderByIdDesc().orElseGet(Config::new);
        return ConfigDtoMapper.INSTANCE.toResponse(config);
    }

    @Transactional
    public ConfigDto.Response save(ConfigDto.Save save) {
        Config config = ConfigDtoMapper.INSTANCE.toEntity(save);
        config = configRepository.save(config);
        return ConfigDtoMapper.INSTANCE.toResponse(config);
    }

}
