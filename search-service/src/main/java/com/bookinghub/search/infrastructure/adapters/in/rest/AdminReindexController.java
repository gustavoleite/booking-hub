package com.bookinghub.search.infrastructure.adapters.in.rest;

import com.bookinghub.search.core.usecases.ReindexUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminReindexController {

    private final ReindexUseCase reindexUseCase;

    @PostMapping("/reindex")
    public ResponseEntity<Map<String, Object>> reindex() {
        log.info("Manual reindex triggered");
        int count = reindexUseCase.execute();
        return ResponseEntity.accepted().body(Map.of(
                "status", "accepted",
                "indexed", count
        ));
    }
}
