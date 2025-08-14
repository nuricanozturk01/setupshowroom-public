package com.setupshowroom.report;

import com.github.f4b6a3.ulid.UlidCreator;
import com.setupshowroom.report.converter.ReportConverter;
import com.setupshowroom.report.dto.ReportForm;
import com.setupshowroom.shared.exceptionhandler.exception.ItemNotFoundException;
import com.setupshowroom.user.User;
import com.setupshowroom.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {
  private final @NotNull ReportRepository reportRepository;
  private final @NotNull ReportConverter reportConverter;
  private final @NotNull UserRepository userRepository;

  public void create(final @NotNull String userId, final @NotNull ReportForm reportForm) {
    final User user =
        this.userRepository
            .findUserById(userId)
            .orElseThrow(() -> new ItemNotFoundException("userNotFound"));

    final Report report = this.reportConverter.toReport(reportForm);
    report.setId(UlidCreator.getUlid().toString());
    report.setUser(user);

    log.warn("Report created from: {}\nContent: {}", user.getUsername(), report);

    this.reportRepository.save(report);
  }
}
