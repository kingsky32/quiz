package com.seungju.quiz.pagniation.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;

public class PageableDto {
    public enum Order {
        ASC(Sort.Direction.ASC), DESC(Sort.Direction.DESC);

        public final Sort.Direction dir;

        Order(Sort.Direction dir) {
            this.dir = dir;
        }
    }

    @Data
    private static class Request {
        private Integer limit = 20;
        private List<String> sorts = new ArrayList<>();

        private static PageRequest of(int page, int limit, List<String> sorts) {
            List<Sort.Order> orders = new ArrayList<>();

            for (String sort : sorts) {
                String[] splitSort = sort.split("\\|");
                String property = sort.split("\\|")[0];
                Sort.Direction direction = Order.valueOf(splitSort[1].toUpperCase()).dir;
                Sort.Order order = new Sort.Order(direction, property);
                orders.add(order);
            }

            return PageRequest.of(page - 1, limit, Sort.by(orders));
        }
    }

    @Getter
    @Setter
    private static class Response<T> {
        private Long totalResults;
        private Integer resultsPerPage;
        private Integer limit;
        private List<T> results;
    }

    public static class Page {
        @EqualsAndHashCode(callSuper = true)
        @Data
        public static class Request extends PageableDto.Request {
            private Integer page = 1;

            public static PageRequest of(Request request) {
                return PageableDto.Request.of(request.getPage(), request.getLimit(), request.getSorts());
            }

            private Pageable getPageable() {
                Request request = new Request();
                request.setPage(this.getPage());
                request.setLimit(this.getLimit());
                request.setSorts(this.getSorts());
                return of(request);
            }
        }

        @Getter
        @Setter
        public static class Response<T> extends PageableDto.Response<T> {
            private Integer page;
            private Integer lastPage;
            private Integer prevPage;
            private Integer nextPage;

            public static <T, P> Response<T> of(org.springframework.data.domain.Page<P> page, List<T> data) {
                Response<T> response = new Response<>();
                response.setTotalResults(page.getTotalElements());
                response.setResultsPerPage(page.getNumberOfElements());
                response.setLimit(page.getSize());
                response.setResults(data);
                response.setPage(page.getNumber() + 1);
                response.setLastPage(page.getTotalPages() + 1);
                if (page.getNumber() > 0) {
                    response.setPrevPage(response.getPage() - 1);
                }
                if (page.getNumber() < page.getTotalPages()) {
                    response.setNextPage(response.getPage() + 1);
                }
                return response;
            }
        }
    }

    public static class Cursor {
        @EqualsAndHashCode(callSuper = true)
        @Data
        public static class Request<T> extends PageableDto.Request {
            private T cursor;

            public static PageRequest of(Request request) {
                return PageableDto.Request.of(1, request.getLimit(), request.getSorts());
            }

            public Pageable getPageable() {
                Request request = new Request();
                request.setLimit(this.getLimit());
                request.setSorts(this.getSorts());
                return of(request);
            }
        }

        @Getter
        @Setter
        public static class Response<T, R> extends PageableDto.Response<R> {
            private T cursor;
            private T nextCursor;

            public static <T, D, P> Response<T, D> of(T cursor, T nextCursor,
                                                      org.springframework.data.domain.Page<P> page, List<D> data) {
                Response<T, D> response = new Response<>();
                response.setTotalResults(page.getTotalElements());
                response.setResultsPerPage(page.getNumberOfElements());
                response.setLimit(page.getSize());
                response.setResults(data);
                response.setCursor(cursor);
                response.setNextCursor(nextCursor);
                response.setResults(data);
                return response;
            }
        }
    }

}
