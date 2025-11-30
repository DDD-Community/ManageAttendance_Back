package com.ddd.manage_attendance.domain.sample.domain;

import com.ddd.manage_attendance.core.common.ListResponse;
import com.ddd.manage_attendance.core.exception.DataNotFoundException;
import com.ddd.manage_attendance.domain.sample.api.dto.SampleResponse;
import com.ddd.manage_attendance.domain.sample.api.dto.SampleSaveRequest;
import com.ddd.manage_attendance.domain.sample.api.dto.SampleSearchRequest;
import com.ddd.manage_attendance.domain.sample.api.dto.SampleUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SampleService {

    private final SampleRepository sampleRepository;

    @Transactional(readOnly = true)
    public SampleResponse getDetail(final Long id) {
        final Sample sample = findByIdOrThrow(id);

        return SampleResponse.from(sample);
    }

    @Transactional(readOnly = true)
    public ListResponse<SampleResponse> search(final SampleSearchRequest request) {
        final Page<Sample> samples =
                sampleRepository.findByConditions(
                        request.getTitle(), request.getContent(), request.toPageable());

        return ListResponse.from(samples, SampleResponse::from);
    }

    @Transactional
    public void register(final SampleSaveRequest request) {
        sampleRepository.save(request.toEntity());
    }

    @Transactional
    public void modify(final Long id, final SampleUpdateRequest request) {
        Sample sample = findByIdOrThrow(id);

        sample.modify(request.title(), request.content());
    }

    @Transactional
    public void remove(final Long id) {
        Sample sample = findByIdOrThrow(id);

        sample.remove();

        sampleRepository.delete(sample);
    }

    private Sample findByIdOrThrow(final Long id) {
        return sampleRepository.findById(id).orElseThrow(DataNotFoundException::new);
    }
}
