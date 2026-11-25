package org.bme.micro_futar.courier.controllers;

import lombok.RequiredArgsConstructor;
import org.bme.micro_futar.courier.dtos.MyAssignmentDTO;
import org.bme.micro_futar.courier.dtos.MyProfileDTO;
import org.bme.micro_futar.courier.services.MeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
public class MeController {

    private final MeService meService;

    @GetMapping
    public ResponseEntity<MyProfileDTO> findMyProfile(Authentication authentication) {
        return ResponseEntity.of(meService.findProfile(authentication));
    }

    @GetMapping("/assignments")
    public ResponseEntity<List<MyAssignmentDTO>> findMyAssignments(Authentication authentication) {
        return ResponseEntity.of(meService.findAssignments(authentication));
    }

    @GetMapping("/assignments/{assignmentId}")
    public ResponseEntity<MyAssignmentDTO> findMyAssignment(@PathVariable Long assignmentId,
                                                            Authentication authentication) {
        return ResponseEntity.of(meService.findAssignment(assignmentId, authentication));
    }
}
