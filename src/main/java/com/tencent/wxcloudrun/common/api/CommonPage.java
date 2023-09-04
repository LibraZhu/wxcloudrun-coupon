package com.tencent.wxcloudrun.common.api;

import java.util.List;

public class CommonPage<T> {
  /** 当前页码 */
  private Long page;
  /** 每页数量 */
  private Long pageSize;
  /** 总页数 */
  private Long totalPage;
  /** 总条数 */
  private Long total;
  /** 分页数据 */
  private List<T> list;
  /** PDD 用于翻页时锁定唯一的商品列表，请求商品分页数=1时非必填，请求商品分页数>1时必填（请求第1页商品时多多进宝会返回此参数），否则无法返回商品 */
  private String listId;

  public static <T> CommonPage<T> page(Long page, Long pageSize, Long total, List<T> list) {
    CommonPage<T> result = new CommonPage<T>();
    result.setPage(page);
    result.setPageSize(pageSize);
    result.setTotal(total);
    result.setList(list);
    return result;
  }

  public static <T> CommonPage<T> page(
      Long page, Long pageSize, Long total, List<T> list, String listId) {
    CommonPage<T> result = new CommonPage<T>();
    result.setPage(page);
    result.setPageSize(pageSize);
    result.setTotal(total);
    result.setList(list);
    result.setListId(listId);
    return result;
  }

  public Long getPage() {
    return page;
  }

  public void setPage(Long page) {
    this.page = page;
  }

  public Long getPageSize() {
    return pageSize;
  }

  public void setPageSize(Long pageSize) {
    this.pageSize = pageSize;
  }

  public Long getTotalPage() {
    return totalPage;
  }

  public void setTotalPage(Long totalPage) {
    this.totalPage = totalPage;
  }

  public List<T> getList() {
    return list;
  }

  public void setList(List<T> list) {
    this.list = list;
  }

  public Long getTotal() {
    return total;
  }

  public void setTotal(Long total) {
    this.total = total;
  }

  public String getListId() {
    return listId;
  }

  public void setListId(String listId) {
    this.listId = listId;
  }
}
