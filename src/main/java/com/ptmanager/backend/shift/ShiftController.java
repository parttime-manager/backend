package com.ptmanager.backend.shift;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ptmanager.backend.domain.Shift;

@RestController
@RequestMapping("/api/shifts")
public class ShiftController {

    private final ShiftService shiftService;

    public ShiftController(ShiftService shiftService) {
        this.shiftService = shiftService;
    }

    @GetMapping
    public List<Shift> findShifts(@RequestParam long userId) {
        return shiftService.findShiftsByUser(userId);
    }

    @PostMapping("/{id}/check-in")
    public Shift checkIn(@PathVariable long id) {
        return shiftService.checkIn(id);
    }
}
