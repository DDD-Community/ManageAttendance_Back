package com.ddd.manage_attendance.domain.schedule.domain;

import com.ddd.manage_attendance.core.exception.DataNotFoundException;
import com.ddd.manage_attendance.domain.attendance.domain.AttendanceRepository;
import com.ddd.manage_attendance.domain.generation.domain.GenerationRepository;
import com.ddd.manage_attendance.domain.schedule.api.dto.InternalScheduleItem;
import com.ddd.manage_attendance.domain.schedule.api.dto.InternalScheduleSkip;
import com.ddd.manage_attendance.domain.schedule.api.dto.InternalScheduleSyncItem;
import com.ddd.manage_attendance.domain.schedule.api.dto.InternalScheduleSyncResult;
import com.ddd.manage_attendance.domain.schedule.exception.ScheduleDateConflictException;
import com.ddd.manage_attendance.domain.schedule.exception.ScheduleDuplicatedDateException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InternalScheduleSyncFacade {
    private final ScheduleRepository schedules;
    private final AttendanceRepository attendances;
    private final GenerationRepository generations;

    @Transactional(readOnly = true)
    public List<InternalScheduleItem> listSchedules(Long generationId) {
        requireGeneration(generationId);
        return schedules.findAllByGenerationIdOrderByDateAsc(generationId).stream()
                .map(
                        schedule ->
                                new InternalScheduleItem(
                                        schedule.getId(),
                                        schedule.getName(),
                                        schedule.getDescription(),
                                        schedule.getDate(),
                                        schedule.getScheduleTime(),
                                        attendances.countByScheduleId(schedule.getId())))
                .toList();
    }

    @Transactional
    public InternalScheduleSyncResult syncSchedules(
            Long generationId, List<InternalScheduleSyncItem> requested) {
        requireGeneration(generationId);
        validateUniqueDates(requested);
        final Map<LocalDate, Schedule> existing = existingSchedules(generationId);
        final SyncChanges changes = new SyncChanges();
        for (InternalScheduleSyncItem item : requested) {
            reconcileRequested(generationId, item, existing, changes);
        }
        reconcileRemoved(existing, changes);
        return changes.toResult();
    }

    private void reconcileRequested(
            Long generationId,
            InternalScheduleSyncItem item,
            Map<LocalDate, Schedule> existing,
            SyncChanges changes) {
        final Schedule schedule = existing.remove(item.date());
        if (schedule != null) {
            updateExisting(schedule, item, changes);
            return;
        }
        createIfAvailable(generationId, item, changes);
    }

    private void updateExisting(
            Schedule schedule, InternalScheduleSyncItem item, SyncChanges changes) {
        final String name = normalizedName(item);
        final String description = normalizedDescription(item, name);
        if (schedule.getName().equals(name)
                && schedule.getDescription().equals(description)
                && schedule.getScheduleTime().equals(item.scheduleTime())) {
            changes.unchanged.add(item.date());
            return;
        }
        schedule.update(name, description, item.scheduleTime());
        changes.updated.add(item.date());
    }

    private void createIfAvailable(
            Long generationId, InternalScheduleSyncItem item, SyncChanges changes) {
        if (schedules.existsByDate(item.date())) {
            changes.skipped.add(skip(item.date(), "DATE_TAKEN_BY_OTHER_GENERATION"));
            return;
        }
        final String name = normalizedName(item);
        final Schedule schedule =
                Schedule.createSchedule(
                        name,
                        normalizedDescription(item, name),
                        item.date(),
                        item.scheduleTime(),
                        generationId);
        try {
            schedules.saveAndFlush(schedule);
            changes.created.add(item.date());
        } catch (DataIntegrityViolationException exception) {
            throw new ScheduleDateConflictException();
        }
    }

    private void reconcileRemoved(Map<LocalDate, Schedule> existing, SyncChanges changes) {
        for (Schedule schedule : existing.values()) {
            if (attendances.existsByScheduleId(schedule.getId())) {
                changes.kept.add(skip(schedule.getDate(), "HAS_ATTENDANCE"));
                continue;
            }
            schedules.delete(schedule);
            changes.deleted.add(schedule.getDate());
        }
    }

    private Map<LocalDate, Schedule> existingSchedules(Long generationId) {
        final Map<LocalDate, Schedule> result = new HashMap<>();
        for (Schedule schedule : schedules.findAllByGenerationIdOrderByDateAsc(generationId)) {
            result.put(schedule.getDate(), schedule);
        }
        return result;
    }

    private void validateUniqueDates(List<InternalScheduleSyncItem> requested) {
        final Set<LocalDate> dates = new HashSet<>();
        for (InternalScheduleSyncItem item : requested) {
            if (!dates.add(item.date())) {
                throw new ScheduleDuplicatedDateException();
            }
        }
    }

    private String normalizedName(InternalScheduleSyncItem item) {
        return item.name();
    }

    private String normalizedDescription(InternalScheduleSyncItem item, String name) {
        if (item.description() == null || item.description().isBlank()) {
            return name;
        }
        return item.description();
    }

    private InternalScheduleSkip skip(LocalDate date, String reason) {
        return new InternalScheduleSkip(date, reason);
    }

    private void requireGeneration(Long generationId) {
        if (!generations.existsById(generationId)) {
            throw new DataNotFoundException();
        }
    }

    private static class SyncChanges {
        private final List<LocalDate> created = new ArrayList<>();
        private final List<LocalDate> updated = new ArrayList<>();
        private final List<LocalDate> unchanged = new ArrayList<>();
        private final List<LocalDate> deleted = new ArrayList<>();
        private final List<InternalScheduleSkip> kept = new ArrayList<>();
        private final List<InternalScheduleSkip> skipped = new ArrayList<>();

        private InternalScheduleSyncResult toResult() {
            return new InternalScheduleSyncResult(
                    created, updated, unchanged, deleted, kept, skipped);
        }
    }
}
