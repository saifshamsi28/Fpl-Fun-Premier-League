package com.zpl.handcricket.models;

import java.util.List;

public class PageResponse<T> {
    public List<T> items;
    public int page;
    public int size;
    public int totalPages;
    public long totalItems;
}
