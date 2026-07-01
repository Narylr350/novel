package com.wtl.novel.booksource.controller;

import com.wtl.novel.booksource.service.BookSourceDebugService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/book-sources/debug")
public class BookSourceDebugController {
    private final BookSourceDebugService debugService;

    public BookSourceDebugController(BookSourceDebugService debugService) {
        this.debugService = debugService;
    }

    @PostMapping("/run")
    public BookSourceDebugService.DebugRunResult run(@RequestBody BookSourceDebugService.DebugRunRequest request) {
        return debugService.run(request);
    }
}
