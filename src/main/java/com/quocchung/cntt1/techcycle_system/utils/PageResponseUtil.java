package com.quocchung.cntt1.techcycle_system.utils;

import com.quocchung.cntt1.techcycle_system.utils.response.APIResponse;
import com.quocchung.cntt1.techcycle_system.utils.response.PageResponse;
import com.quocchung.cntt1.techcycle_system.utils.response.ResponseStatus;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class PageResponseUtil {
  public static <T> APIResponse<T> buildPageResponse(Page<T> page) {
    APIResponse<T> response = new APIResponse<>();
    response.setStatus(ResponseStatus.SUCCESS_STATUS);
    response.setData(page.getContent());
    response.setPage(extractPageMetadata(page));
    return response;
  }

  // Build với data đã được transform sang DTO
  public static <T, R> APIResponse<R> buildPageResponse(Page<T> page, List<R> transformedData) {
    APIResponse<R> response = new APIResponse<>();
    response.setStatus(ResponseStatus.SUCCESS_STATUS);
    response.setData(transformedData);
    response.setPage(extractPageMetadata(page));
    return response;
  }

  // Build với Function mapper (Entity -> DTO)
  public static <T, R> APIResponse<R> buildPageResponse(Page<T> page, Function<T, R> mapper) {
    List<R> transformedData = page.getContent()
        .stream()
        .map(mapper)
        .collect(Collectors.toList());
    return buildPageResponse(page, transformedData);
  }

  // Build với extra data (thêm thông tin bổ sung)
  public static <T, R> APIResponse<R> buildPageResponse(
      Page<T> page,
      Function<T, R> mapper,
      Map<String, Object> extraData) {

    APIResponse<R> response = buildPageResponse(page, mapper);
    response.setExtraData(extraData);
    return response;
  }

  public static Pageable buildPageable(int page, int size) {
    return PageRequest.of(
        Math.max(0, page - 1),
        size <= 0 ? 10 : Math.min(size, 100)
    );
  }

  public static Pageable buildPageable(int page, int size, String sortBy, String sortDir) {
    Sort sort = sortDir.equalsIgnoreCase("desc")
        ? Sort.by(sortBy).descending()
        : Sort.by(sortBy).ascending();

    return PageRequest.of(
        Math.max(0, page - 1),
        size <= 0 ? 10 : Math.min(size, 100),
        sort
    );
  }

  public static Pageable buildPageable(int page, int size, List<String> sortFields, List<String> sortDirs) {
    int adjustedPage = Math.max(0, page - 1);
    int adjustedSize = size <= 0 ? 10 : Math.min(size, 100);

    if (sortFields == null || sortFields.isEmpty()) {
      return PageRequest.of(adjustedPage, adjustedSize);
    }

    List<Sort.Order> orders = IntStream.range(0, sortFields.size())
        .mapToObj(i -> {
          String dir = (sortDirs != null && i < sortDirs.size()) ? sortDirs.get(i) : "asc";
          return dir.equalsIgnoreCase("desc")
              ? Sort.Order.desc(sortFields.get(i))
              : Sort.Order.asc(sortFields.get(i));
        })
        .collect(Collectors.toList());

    return PageRequest.of(adjustedPage, adjustedSize, Sort.by(orders));
  }

  // ===== HELPER =====

  public static PageResponse extractPageMetadata(Page<?> page) {
    PageResponse pageResponse = new PageResponse();
    pageResponse.setTotalPages(page.getTotalPages());
    pageResponse.setHasNext(page.hasNext());
    pageResponse.setHasPrevious(page.hasPrevious());
    pageResponse.setCurrentPage(page.getNumber() + 1);
    pageResponse.setTotalElements(page.getTotalElements());
    return pageResponse;
  }
}