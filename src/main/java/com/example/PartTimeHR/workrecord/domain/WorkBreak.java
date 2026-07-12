package com.example.PartTimeHR.workrecord.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 휴게 1회의 기록.
 * WorkRecord에 시각 쌍/누적 분을 겹쳐 담던 것을 분리 —
 * 하루 여러 번의 휴게가 각각 이력으로 남고, 집계는 전부 여기서 파생된다.
 */
@Entity
@Table(name = "work_break")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WorkBreak {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_record_id", nullable = false)
    private WorkRecord workRecord;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    // null이면 진행 중인 휴게
    @Column(name = "end_time")
    private LocalDateTime endTime;

    static WorkBreak open(WorkRecord workRecord, LocalDateTime startTime) {
        WorkBreak b = new WorkBreak();
        b.workRecord = workRecord;
        b.startTime = startTime;
        return b;
    }

    static WorkBreak closed(WorkRecord workRecord, LocalDateTime startTime, LocalDateTime endTime) {
        WorkBreak b = open(workRecord, startTime);
        b.endTime = endTime;
        return b;
    }

    void close(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public boolean isOpen() {
        return endTime == null;
    }

    /** 종료된 휴게의 길이(분). 진행 중이면 0 */
    public long minutes() {
        if (isOpen()) {
            return 0L;
        }
        return Duration.between(startTime, endTime).toMinutes();
    }
}
