package dev.kreaker.hile.bootstrap.api.tag;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record SetReportTagsRequest(@NotNull List<UUID> tagIds) {}
