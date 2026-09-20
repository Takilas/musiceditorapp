package com.perebziak.musiceditor.repository;

import com.perebziak.musiceditor.model.ConversionOrder;

import java.util.List;

public interface ConversionOrderRepository extends Repository<ConversionOrder, Long> {
  List<ConversionOrder> findByUserId(Long userId);
}