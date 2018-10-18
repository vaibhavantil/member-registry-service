package com.hedvig.memberservice.util;

import org.springframework.data.domain.AbstractPageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;

public class PageableBuilder {
  boolean paged = false;
  int page = 0;
  int pageSize = 0;
  List<Sort.Order> orders = new ArrayList<>();

  public PageableBuilder paged(int page, int pageSize) {
    this.page = page;
    this.pageSize = pageSize;
    this.paged = true;

    return this;
  }

  public PageableBuilder orderBy(String prop, Sort.Direction direction, Sort.NullHandling nullHandlingHint) {
    orders.add(new Sort.Order(direction, prop, nullHandlingHint));
    return this;
  }

  public Pageable build() {
    return new PageableImpl(page, pageSize, Sort.by(orders), paged);
  }

  static class PageableImpl implements Pageable {
    final Sort sort;
    final boolean paged;
    final int page;
    final int pageSize;

    public PageableImpl(int page, int pageSize, Sort sort, boolean paged) {
      this.page = page;
      this.pageSize = pageSize;
      this.sort = sort;
      this.paged = paged;
    }

    @Override
    public boolean isPaged() {
      return paged;
    }

    @Override
    public int getPageNumber() {
      return page;
    }

    @Override
    public int getPageSize() {
      return pageSize;
    }

    @Override
    public long getOffset() {
      return page * pageSize;
    }

    @Override
    public Sort getSort() {
      return sort;
    }

    @Override
    public Pageable next() {
      if (!paged) {
        throw new UnsupportedOperationException();
      }

      return new PageableImpl(getPageNumber() + 1, getPageSize(), sort, paged);
    }

    @Override
    public Pageable previousOrFirst() {
      if (!paged) {
        throw new UnsupportedOperationException();
      }

      if (page > 0) {
        return new PageableImpl(page - 1, pageSize, sort, paged);
      } else {
        return this;
      }
    }


    @Override
    public Pageable first() {
      if (!paged) {
        throw new UnsupportedOperationException();
      }

      if (getPageNumber() == 0) {
        return this;
      }

      return new PageableImpl(0, getPageSize(), sort, paged);
    }

    @Override
    public boolean hasPrevious() {
      if (!paged) {
        throw new UnsupportedOperationException();
      }

      return page > 0;
    }
  }
}
